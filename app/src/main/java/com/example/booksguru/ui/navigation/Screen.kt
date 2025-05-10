package com.example.booksguru.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.booksguru.ui.screens.CharactersScreen
import com.example.booksguru.ui.screens.NativeVisualizationScreen
import com.example.booksguru.ui.screens.SearchScreen
import com.example.booksguru.ui.screens.VisualizationScreen

sealed class Screen(val route: String) {
    object Search : Screen("search")
    object Characters : Screen("characters/{bookId}") {
        fun createRoute(bookId: Int) = "characters/$bookId"
    }
    object NativeVisualization : Screen("native-visualization/{bookId}") {
        fun createRoute(bookId: Int) = "native-visualization/$bookId"
    }
}

@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Search.route
    ) {
        composable(
            route = Screen.Search.route,
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                )
            }
        ) {
            SearchScreen(
                onSearch = { bookId, visualize ->
                    if (visualize) {
                        navController.navigate(Screen.NativeVisualization.createRoute(bookId))
                    } else {
                        navController.navigate(Screen.Characters.createRoute(bookId))
                    }
                }
            )
        }

        composable(
            route = Screen.Characters.route,
            arguments = listOf(navArgument("bookId") { type = NavType.IntType }),
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                )
            }
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getInt("bookId") ?: 0
            CharactersScreen(
                bookId = bookId,
//                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.NativeVisualization.route,
            arguments = listOf(navArgument("bookId") { type = NavType.IntType }),
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                )
            }
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getInt("bookId") ?: 0
            NativeVisualizationScreen(
                bookId = bookId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
