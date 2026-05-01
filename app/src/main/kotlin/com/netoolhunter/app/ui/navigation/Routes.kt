package com.netoolhunter.app.ui.navigation

sealed class Route(val path: String) {
    data object Home      : Route("home")
    data object Tools     : Route("tools?cat={cat}") {
        const val ARG_CAT = "cat"
        fun build(categoryId: String? = null): String =
            if (categoryId != null) "tools?cat=$categoryId" else "tools"
    }
    data object Repos     : Route("repos")
    data object Terminal  : Route("terminal")
    data object Installed : Route("installed")
    data object Prereqs   : Route("prereqs")
}
