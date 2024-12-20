package com.example.androidcookbook.ui.nav.dest

import android.util.Log
import androidx.compose.runtime.collectAsState
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.example.androidcookbook.domain.model.user.GUEST_ID
import com.example.androidcookbook.ui.CookbookUiState
import com.example.androidcookbook.ui.CookbookViewModel
import com.example.androidcookbook.ui.common.containers.RefreshableScreen
import com.example.androidcookbook.ui.common.screens.FailureScreen
import com.example.androidcookbook.ui.common.screens.GuestLoginScreen
import com.example.androidcookbook.ui.common.screens.LoadingScreen
import com.example.androidcookbook.ui.common.state.ScreenUiState
import com.example.androidcookbook.ui.features.newsfeed.NewsfeedScreen
import com.example.androidcookbook.ui.features.newsfeed.NewsfeedViewModel
import com.example.androidcookbook.ui.nav.Routes
import com.example.androidcookbook.ui.nav.utils.guestNavToAuth
import com.example.androidcookbook.ui.nav.utils.sharedViewModel

fun NavGraphBuilder.newsfeed(
    cookbookViewModel: CookbookViewModel,
    navController: NavHostController,
) {
    composable<Routes.App.Newsfeed> {
        cookbookViewModel.updateTopBarState(CookbookUiState.TopBarState.Default)
        cookbookViewModel.updateBottomBarState(CookbookUiState.BottomBarState.Default)
        cookbookViewModel.updateCanNavigateBack(false)

        val currentUser = cookbookViewModel.user.collectAsState().value

        if (currentUser.id == GUEST_ID) {
            GuestLoginScreen {
                navController.guestNavToAuth()
            }
            return@composable
        }

        val newsfeedViewModel = sharedViewModel<NewsfeedViewModel>(it, navController, Routes.App)
        val posts = newsfeedViewModel.posts.collectAsState().value

        Log.d("Newsfeed", "newsfeedUiState: ${newsfeedViewModel.screenUiState.collectAsState().value}")

        when (val newsfeedUiState = newsfeedViewModel.screenUiState.collectAsState().value) {
            is ScreenUiState.Failure -> FailureScreen(message = newsfeedUiState.message) { newsfeedViewModel.refresh() }
            ScreenUiState.Guest -> GuestLoginScreen {
                navController.guestNavToAuth()
            }
            ScreenUiState.Loading -> LoadingScreen()
            is ScreenUiState.Success ->
                if (newsfeedUiState.data.isEmpty()) {
                    LoadingScreen()
                } else {
                    RefreshableScreen(
                        isRefreshing = newsfeedViewModel.isRefreshing.collectAsState().value,
                        onRefresh = { newsfeedViewModel.refresh() }
                    ) {
                        NewsfeedScreen(
                            posts = posts,
                            currentUser = cookbookViewModel.user.collectAsState().value,
                            onEditPost = { post ->
                                navController.navigate(Routes.UpdatePost(post))
                            },
                            onDeletePost = { post ->
                                newsfeedViewModel.deletePost(post)
                            },
                            onSeeDetailsClick = { post ->
                                navController.navigate(Routes.App.PostDetails(post.id))
                            },
                            onUserClick = { user ->
                                if (user.id == cookbookViewModel.user.value.id) {
                                    navController.navigate(Routes.App.UserProfile(user.id))
                                } else {
                                    navController.navigate(Routes.OtherProfile(user.id))
                                }
                            },
                            onLoadMore = {
                                newsfeedViewModel.loadMore()
                            },
                            isLoadingMore = newsfeedViewModel.isLoadingMore.collectAsState().value
                        )
                    }
                }

        }
    }
}