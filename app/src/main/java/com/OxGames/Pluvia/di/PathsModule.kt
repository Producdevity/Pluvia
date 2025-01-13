package com.OxGames.Pluvia.di

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.nio.file.Paths
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.io.path.pathString

@Singleton
class PathsModule @Inject constructor(@ApplicationContext private val context: Context) {
    val serverListPath: String
        get() = Paths.get(context.cacheDir.path, "server_list.bin").pathString

    val depotManifestsPath: String
        get() = Paths.get(context.dataDir.path, "Steam", "depot_manifests.zip").pathString

    val defaultAppInstallPath: String
        get() = Paths.get(context.dataDir.path, "Steam", "steamapps", "common").pathString

    val defaultAppStagingPath: String
        get() = Paths.get(context.dataDir.path, "Steam", "steamapps", "staging").pathString
}
