package com.OxGames.Pluvia.ui.screen.library

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.InstallMobile
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.BackNavigationBehavior
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.OxGames.Pluvia.SteamService
import com.OxGames.Pluvia.data.AppInfo
import com.OxGames.Pluvia.ui.component.fabmenu.FloatingActionMenu
import com.OxGames.Pluvia.ui.component.fabmenu.FloatingActionMenuItem
import com.OxGames.Pluvia.ui.component.fabmenu.state.FloatingActionMenuState
import com.OxGames.Pluvia.ui.component.fabmenu.state.FloatingActionMenuValue
import com.OxGames.Pluvia.ui.component.fabmenu.state.rememberFloatingActionMenuState
import com.OxGames.Pluvia.ui.component.topbar.AccountButton
import com.OxGames.Pluvia.ui.data.LibraryState
import com.OxGames.Pluvia.ui.enums.FabFilter
import com.OxGames.Pluvia.ui.internal.fakeAppInfo
import com.OxGames.Pluvia.ui.model.LibraryViewModel
import com.OxGames.Pluvia.ui.theme.PluviaTheme
import timber.log.Timber

@Composable
fun HomeLibraryScreen(
    viewModel: LibraryViewModel = viewModel(),
    onClickPlay: (Int, Boolean) -> Unit,
    onSettings: () -> Unit,
    onLogout: () -> Unit,
) {
    val vmState by viewModel.state.collectAsStateWithLifecycle()
    val fabState = rememberFloatingActionMenuState()

    LibraryScreenContent(
        state = vmState,
        fabState = fabState,
        onFabFilter = viewModel::onFabFilter,
        onIsSearching = viewModel::onIsSearching,
        onSearchQuery = viewModel::onSearchQuery,
        onClickPlay = onClickPlay,
        onSettings = onSettings,
        onLogout = onLogout,
    )
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun LibraryScreenContent(
    state: LibraryState,
    fabState: FloatingActionMenuState,
    onIsSearching: (Boolean) -> Unit,
    onSearchQuery: (String) -> Unit,
    onFabFilter: (FabFilter) -> Unit,
    onClickPlay: (Int, Boolean) -> Unit,
    onSettings: () -> Unit,
    onLogout: () -> Unit,
) {
    val snackbarHost = remember { SnackbarHostState() }
    val navigator = rememberListDetailPaneScaffoldNavigator<Int>()
    val listState = rememberLazyListState()

    // Pretty much the same as 'NavigableListDetailPaneScaffold'
    BackHandler(navigator.canNavigateBack(BackNavigationBehavior.PopUntilContentChange)) {
        navigator.navigateBack(BackNavigationBehavior.PopUntilContentChange)
    }

    ListDetailPaneScaffold(
        modifier = Modifier.displayCutoutPadding(),
        directive = navigator.scaffoldDirective,
        value = navigator.scaffoldValue,
        listPane = {
            AnimatedPane {
                Scaffold(
                    snackbarHost = { SnackbarHost(snackbarHost) },
                    topBar = {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            SearchBar(
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .semantics { traversalIndex = 0f },
                                inputField = {
                                    SearchBarDefaults.InputField(
                                        query = state.searchQuery,
                                        onSearch = {
                                            Timber.i("SearchBar onSearch()")
                                        },
                                        expanded = state.isSearching,
                                        onExpandedChange = onIsSearching,
                                        placeholder = { Text("Search for games") },
                                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                                        trailingIcon = {
                                            Crossfade(state.isSearching) { cfState ->
                                                if (cfState) {
                                                    IconButton(
                                                        onClick = {
                                                            if (state.searchQuery.isEmpty()) {
                                                                onIsSearching(false)
                                                            } else {
                                                                onSearchQuery("")
                                                            }
                                                        },
                                                        content = {
                                                            Icon(Icons.Default.Clear, "Clear search query")
                                                        },
                                                    )
                                                } else {
                                                    AccountButton(
                                                        onSettings = onSettings,
                                                        onLogout = onLogout,
                                                    )
                                                }
                                            }
                                        },
                                        onQueryChange = onSearchQuery,
                                    )
                                },
                                expanded = state.isSearching,
                                onExpandedChange = onIsSearching,
                                content = {
                                    if (state.isSearching) {
                                        LibraryListPane(
                                            paddingValues = PaddingValues(),
                                            listState = listState,
                                            list = state.appInfoList,
                                            onItemClick = { item ->
                                                navigator.navigateTo(
                                                    pane = ListDetailPaneScaffoldRole.Detail,
                                                    content = item,
                                                )
                                            },
                                        )
                                    }
                                },
                            )
                        }
                    },
                    floatingActionButton = {
                        FloatingActionMenu(
                            state = fabState,
                            imageVector = Icons.Filled.FilterList,
                            closeImageVector = Icons.Filled.Close,
                        ) {
                            FloatingActionMenuItem(
                                labelText = "Alphabetic",
                                onClick = { onFabFilter(FabFilter.ALPHABETIC) },
                                content = { Icon(Icons.Filled.SortByAlpha, "Alphabetic") },
                            )
                            FloatingActionMenuItem(
                                labelText = "Installed",
                                onClick = { onFabFilter(FabFilter.INSTALLED) },
                                content = { Icon(Icons.Filled.InstallMobile, "Installed") },
                            )
                        }
                    },
                ) { paddingValues ->
                    LibraryListPane(
                        paddingValues = paddingValues,
                        listState = listState,
                        list = state.appInfoList,
                        onItemClick = { item ->
                            navigator.navigateTo(
                                pane = ListDetailPaneScaffoldRole.Detail,
                                content = item,
                            )
                        },
                    )
                }
            }
        },
        detailPane = {
            val appId = (navigator.currentDestination?.content ?: SteamService.INVALID_APP_ID)
            AnimatedPane {
                LibraryDetailPane(
                    appId = appId,
                    onBack = {
                        // We're still in Adaptive navigation.
                        navigator.navigateBack()
                    },
                    onClickPlay = { onClickPlay(appId, it) },
                )
            }
        },
    )
}

@Composable
private fun LibraryListPane(
    paddingValues: PaddingValues,
    listState: LazyListState,
    list: List<AppInfo>,
    onItemClick: (Int) -> Unit,
) {
    if (list.isEmpty()) {
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Surface(
                modifier = Modifier.padding(horizontal = 24.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shadowElevation = 8.dp,
            ) {
                Text(
                    modifier = Modifier.padding(24.dp),
                    text = "No items listed with selection",
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            state = listState,
            contentPadding = PaddingValues(bottom = 72.dp), // Extra space for fab
        ) {
            items(list, key = { it.appId }) { item ->
                AppItem(
                    modifier = Modifier.animateItem(),
                    appInfo = item,
                    onClick = { onItemClick(item.appId) },
                )
            }
        }
    }
}

@Composable
private fun LibraryDetailPane(
    appId: Int,
    onClickPlay: (Boolean) -> Unit,
    onBack: () -> Unit,
) {
    Surface {
        if (appId == SteamService.INVALID_APP_ID) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shadowElevation = 8.dp,
                ) {
                    Text(
                        modifier = Modifier.padding(24.dp),
                        text = "Select an item in the list to view game info",
                    )
                }
            }
        } else {
            AppScreen(
                appId = appId,
                onClickPlay = onClickPlay,
                onBack = onBack,
            )
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL,
    device = "spec:width=1080px,height=1920px,dpi=440,orientation=landscape",
)
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL,
    device = "id:pixel_tablet",
)
@Composable
private fun Preview_LibraryScreenContent() {
    PluviaTheme {
        LibraryScreenContent(
            state = LibraryState(
                appInfoList = List(15) { fakeAppInfo(it).copy(appId = it) },
            ),
            fabState = rememberFloatingActionMenuState(FloatingActionMenuValue.Open),
            onIsSearching = {},
            onSearchQuery = {},
            onFabFilter = {},
            onClickPlay = { _, _ -> },
            onSettings = {},
            onLogout = {},
        )
    }
}
