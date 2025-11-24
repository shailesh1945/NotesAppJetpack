package com.shailesh.notesappjetpack.Navigation

sealed class NotesNavigationItem(val route: String) {

    object SplashScreen : NotesNavigationItem(route = "splash")
    object HomeScreen : NotesNavigationItem(route = "home")
    object AddNotesScreen : NotesNavigationItem(route = "add")
}