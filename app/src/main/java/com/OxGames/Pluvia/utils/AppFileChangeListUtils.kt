package com.OxGames.Pluvia.utils

import com.OxGames.Pluvia.data.AppInfo
import `in`.dragonbra.javasteam.steam.handlers.steamcloud.AppFileChangeList
import timber.log.Timber

/**
 * Extension functions relating to [AppFileChangeList] as the receiver type.
 */

fun AppFileChangeList.printFileChangeList(appInfo: AppInfo) {
    with(this) {
        Timber.i(
            "GetAppFileListChange(${appInfo.appId}):" +
                "\n\tTotal Files: ${files.size}" +
                "\n\tCurrent Change Number: $currentChangeNumber" +
                "\n\tIs Only Delta: $isOnlyDelta" +
                "\n\tApp BuildID Hwm: $appBuildIDHwm" +
                "\n\tPath Prefixes: \n\t\t${pathPrefixes.joinToString("\n\t\t")}" +
                "\n\tMachine Names: \n\t\t${machineNames.joinToString("\n\t\t")}" +
                files.joinToString {
                    "\n\t${it.filename}:" +
                        "\n\t\tshaFile: ${it.shaFile}" +
                        "\n\t\ttimestamp: ${it.timestamp}" +
                        "\n\t\trawFileSize: ${it.rawFileSize}" +
                        "\n\t\tpersistState: ${it.persistState}" +
                        "\n\t\tplatformsToSync: ${it.platformsToSync}" +
                        "\n\t\tpathPrefixIndex: ${it.pathPrefixIndex}" +
                        "\n\t\tmachineNameIndex: ${it.machineNameIndex}"
                },
        )
    }
}
