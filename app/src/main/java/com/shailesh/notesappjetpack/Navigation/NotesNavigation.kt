package com.shailesh.notesappjetpack.Navigation

import androidx.compose.runtime.Composable

import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.shailesh.notesappjetpack.Screens.AddNoteScreen
import com.shailesh.notesappjetpack.Screens.NotesScreen
import com.shailesh.notesappjetpack.Screens.SplashScreen


@Composable
fun NotesNavigation(navHostController: NavHostController) {
    NavHost(navController = navHostController, startDestination = "splash")
    {
        composable(NotesNavigationItem.SplashScreen.route)
        {
            SplashScreen(navHostController)
        }

        composable(NotesNavigationItem.HomeScreen.route) {
            NotesScreen(navHostController)
        }

        composable(NotesNavigationItem.AddNotesScreen.route + "/{id}")
        { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id")
            AddNoteScreen(navHostController, id)
        }
    }
}