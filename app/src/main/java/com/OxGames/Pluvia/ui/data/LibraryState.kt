package com.OxGames.Pluvia.ui.data

import com.OxGames.Pluvia.data.AppInfo
import com.OxGames.Pluvia.ui.enums.FabFilter

data class LibraryState(
    val searchText: String = "",
    val appInfoSortType: FabFilter = FabFilter.ALPHABETIC,
    val appInfoList: List<AppInfo> = listOf(),
)
