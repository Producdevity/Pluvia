package com.OxGames.Pluvia.ui.model

import android.content.Context
import android.os.Process
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.OxGames.Pluvia.PluviaApp
import com.OxGames.Pluvia.PrefManager
import com.OxGames.Pluvia.SteamService
import com.OxGames.Pluvia.data.GameProcessInfo
import com.OxGames.Pluvia.enums.LoginResult
import com.OxGames.Pluvia.enums.PathType
import com.OxGames.Pluvia.events.AndroidEvent
import com.OxGames.Pluvia.events.SteamEvent
import com.OxGames.Pluvia.ui.data.MainState
import com.OxGames.Pluvia.ui.enums.PluviaScreen
import com.winlator.xserver.Window
import `in`.dragonbra.javasteam.steam.handlers.steamapps.AppProcessInfo
import java.nio.file.Paths
import kotlin.io.path.name
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

class MainViewModel : ViewModel() {

    sealed class MainUiEvent {
        data object OnBackPressed : MainUiEvent()
        data object OnLoggedOut : MainUiEvent()
        data object LaunchApp : MainUiEvent()
        data class OnLogonEnded(val result: LoginResult) : MainUiEvent()
    }

    private val _state = MutableStateFlow(MainState())
    val state: StateFlow<MainState> = _state.asStateFlow()

    private val _uiEvent = Channel<MainUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    private val onSteamConnected: (SteamEvent.Connected) -> Unit = {
        Timber.i("Received is connected")
        _state.update { it.copy(isSteamConnected = true) }
    }

    private val onSteamDisconnected: (SteamEvent.Disconnected) -> Unit = {
        Timber.i("Received disconnected from Steam")
        _state.update { it.copy(isSteamConnected = false) }
    }

    private val onLoggingIn: (SteamEvent.LogonStarted) -> Unit = {
        Timber.i("Received logon started")
    }

    private val onBackPressed: (AndroidEvent.BackPressed) -> Unit = {
        viewModelScope.launch {
            _uiEvent.send(MainUiEvent.OnBackPressed)
        }
    }

    private val onLogonEnded: (SteamEvent.LogonEnded) -> Unit = {
        Timber.i("Received logon ended")
        viewModelScope.launch {
            _uiEvent.send(MainUiEvent.OnLogonEnded(it.loginResult))
        }
    }

    private val onLoggedOut: (SteamEvent.LoggedOut) -> Unit = {
        Timber.i("Received logged out")
        viewModelScope.launch {
            _uiEvent.send(MainUiEvent.OnLoggedOut)
        }
    }

    init {
        PluviaApp.events.on<AndroidEvent.BackPressed, Unit>(onBackPressed)
        PluviaApp.events.on<SteamEvent.Connected, Unit>(onSteamConnected)
        PluviaApp.events.on<SteamEvent.Disconnected, Unit>(onSteamDisconnected)
        PluviaApp.events.on<SteamEvent.LogonStarted, Unit>(onLoggingIn)
        PluviaApp.events.on<SteamEvent.LogonEnded, Unit>(onLogonEnded)
        PluviaApp.events.on<SteamEvent.LoggedOut, Unit>(onLoggedOut)
    }

    override fun onCleared() {
        PluviaApp.events.off<AndroidEvent.BackPressed, Unit>(onBackPressed)
        PluviaApp.events.off<SteamEvent.Connected, Unit>(onSteamConnected)
        PluviaApp.events.off<SteamEvent.Disconnected, Unit>(onSteamDisconnected)
        PluviaApp.events.off<SteamEvent.LogonEnded, Unit>(onLogonEnded)
        PluviaApp.events.off<SteamEvent.LoggedOut, Unit>(onLoggedOut)
    }

    init {
        _state.update {
            it.copy(
                isSteamConnected = SteamService.isConnected,
                hasCrashedLastStart = PrefManager.recentlyCrashed,
                launchedAppId = SteamService.INVALID_APP_ID,
            )
        }
    }

    fun setAnnoyingDialogShown(value: Boolean) {
        _state.update { it.copy(annoyingDialogShown = value) }
    }

    fun setLoadingDialogVisible(value: Boolean) {
        _state.update { it.copy(loadingDialogVisible = value) }
    }

    fun setLoadingDialogProgress(value: Float) {
        _state.update { it.copy(loadingDialogProgress = value) }
    }

    fun setHasLaunched(value: Boolean) {
        _state.update { it.copy(hasLaunched = value) }
    }

    fun setCurrentScreen(currentScreen: String?) {
        PluviaScreen.valueOf(currentScreen ?: PluviaScreen.LoginUser.name).also(::setCurrentScreen)
    }

    fun setCurrentScreen(value: PluviaScreen) {
        _state.update { it.copy(currentScreen = value) }
    }

    fun setHasCrashedLastStart(value: Boolean) {
        if (value.not()) {
            PrefManager.recentlyCrashed = false
        }
        _state.update { it.copy(hasCrashedLastStart = value) }
    }

    fun setScreen() {
        _state.update { it.copy(resettedScreen = it.currentScreen) }
    }

    fun setLaunchedAppId(value: Int) {
        _state.update { it.copy(launchedAppId = value) }
    }

    fun setBootToContainer(value: Boolean) {
        _state.update { it.copy(bootToContainer = value) }
    }

    fun launchApp() {
        // SteamUtils.replaceSteamApi(context, appId)
        // TODO: fix XServerScreen change orientation issue rather than setting the orientation
        //  before entering XServerScreen
        viewModelScope.launch {
            PluviaApp.events.emit(AndroidEvent.SetAllowedOrientation(PrefManager.allowedOrientation))
            _uiEvent.send(MainUiEvent.LaunchApp)
        }
    }

    fun exitSteamApp(context: Context, appId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            SteamService.notifyRunningProcesses()
            SteamService.closeApp(appId) { prefix ->
                PathType.from(prefix).toAbsPath(context, appId)
            }.await()
        }
    }

    fun onWindowMapped(window: Window, appId: Int) {
        SteamService.getAppInfoOf(appId)?.let { appInfo ->
            // TODO: this should not be a search, the app should have been launched with a specific launch config that we then use to compare
            val launchConfig = SteamService.getWindowsLaunchInfos(appId).firstOrNull {
                val gameExe = Paths.get(it.executable.replace('\\', '/')).name.lowercase()
                val windowExe = window.className.lowercase()
                gameExe == windowExe
            }

            if (launchConfig != null) {
                val steamProcessId = Process.myPid()
                val processes = mutableListOf<AppProcessInfo>()
                var currentWindow: Window = window
                do {
                    var parentWindow: Window? = window.parent
                    val process = if (parentWindow != null && parentWindow.className.lowercase() != "explorer.exe") {
                        val processId = currentWindow.processId
                        val parentProcessId = parentWindow.processId
                        currentWindow = parentWindow

                        AppProcessInfo(processId, parentProcessId, false)
                    } else {
                        parentWindow = null

                        AppProcessInfo(currentWindow.processId, steamProcessId, true)
                    }
                    processes.add(process)
                } while (parentWindow != null)

                GameProcessInfo(appId = appId, processes = processes).let {
                    SteamService.notifyRunningProcesses(it)
                }
            }
        }
    }
}
