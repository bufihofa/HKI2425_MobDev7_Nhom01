package com.example.androidcookbook.ui.nav.dest.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.example.androidcookbook.domain.model.user.User
import com.example.androidcookbook.ui.CookbookUiState
import com.example.androidcookbook.ui.CookbookViewModel
import com.example.androidcookbook.ui.common.containers.RefreshableScreen
import com.example.androidcookbook.ui.common.screens.FailureScreen
import com.example.androidcookbook.ui.common.screens.LoadingScreen
import com.example.androidcookbook.ui.features.follow.FollowListScreenType
import com.example.androidcookbook.ui.features.follow.FollowViewModel
import com.example.androidcookbook.ui.features.userprofile.GuestProfile
import com.example.androidcookbook.ui.features.userprofile.UserPostState
import com.example.androidcookbook.ui.features.userprofile.UserProfileScreen
import com.example.androidcookbook.ui.features.userprofile.UserProfileUiState
import com.example.androidcookbook.ui.features.userprofile.UserProfileViewModel
import com.example.androidcookbook.ui.features.userprofile.EditProfileButton
import com.example.androidcookbook.ui.features.userprofile.userPostPortion
import com.example.androidcookbook.ui.nav.CustomNavTypes
import com.example.androidcookbook.ui.nav.Routes
import kotlin.reflect.typeOf

fun NavGraphBuilder.userProfile(
    cookbookViewModel: CookbookViewModel,
    navController: NavHostController,
) {
    composable<Routes.App.UserProfile>(
        typeMap = mapOf(
            typeOf<User>() to CustomNavTypes.UserType
        )
    ) {
        cookbookViewModel.updateTopBarState(CookbookUiState.TopBarState.Default)
        cookbookViewModel.updateBottomBarState(CookbookUiState.BottomBarState.Default)
        cookbookViewModel.updateCanNavigateBack(false)

        val currentUser = cookbookViewModel.user.collectAsState().value

        val userId = it.toRoute<Routes.App.UserProfile>().userId

        val userProfileViewModel =
            hiltViewModel<UserProfileViewModel, UserProfileViewModel.UserProfileViewModelFactory>(
//                it, navController, Routes.App
            ) { factory ->
                factory.create(userId)
            }

        val userProfileUiState = userProfileViewModel.uiState.collectAsState().value
        val followViewModel = hiltViewModel<FollowViewModel, FollowViewModel.FollowViewModelFactory>(
//            it, navController, Routes.App.UserProfile(user)
        ) { factory ->
            factory.create(currentUser, userId)
        }

        LaunchedEffect(Unit) {
            userProfileViewModel.refreshNoIndicator()
            followViewModel.refresh()
        }

        RefreshableScreen(
            isRefreshing = userProfileViewModel.isRefreshing.collectAsState().value,
            onRefresh = {
                userProfileViewModel.refresh()
                followViewModel.refresh()
            }
        ) {
            when (userProfileUiState) {
                is UserProfileUiState.Loading -> {
                    LoadingScreen()
                }

                is UserProfileUiState.Success -> {
                    val userPostState = userProfileViewModel.userPostState.collectAsState().value

                    UserProfileScreen(
                        user = userProfileUiState.user,
                        headerButton = {
                            EditProfileButton(
                                onEditProfileClick = {
                                    navController.navigate(Routes.EditProfile(userProfileUiState.user))
                                }
                            )
                        },
                        navigateToEditProfile = {
                            navController.navigate(Routes.EditProfile(userProfileUiState.user))
                        },
                        followersCount = followViewModel.followers.collectAsState().value.size,
                        followingCount = followViewModel.following.collectAsState().value.size,
                        onFollowersClick = {
                            navController.navigate(
                                Routes.Follow(userProfileUiState.user, FollowListScreenType.Followers)
                            )
                        },
                        onFollowingClick = {
                            navController.navigate(
                                Routes.Follow(userProfileUiState.user, FollowListScreenType.Following)
                            )
                        },
                    ) {
                        when (userPostState) {
                            is UserPostState.Loading -> item {
                                LoadingScreen()
                            }

                            is UserPostState.Success -> {
                                userPostPortion(
                                    userPosts = userPostState.userPosts,
                                    onEditPost = { post ->
                                        navController.navigate(Routes.UpdatePost(post))
                                    },
                                    onDeletePost = { post ->
                                        userProfileViewModel.deletePost(post)
                                    },
                                    onPostSeeDetailsClick = { post ->
                                        navController.navigate(Routes.App.PostDetails(post.id))
                                    },
                                    onUserClick = { },
                                    currentUser = userProfileUiState.user,
                                )
                            }

                            is UserPostState.Failure -> item {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Text("Failed to fetch user posts.")
                                }
                            }

                            is UserPostState.Guest -> item {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Text("Login to see your posts.")
                                }
                            }
                        }
                    }
                }

                is UserProfileUiState.Failure -> {
                    FailureScreen(
                        message = userProfileUiState.message,
                        onRetryClick = {
                            userProfileViewModel.refresh()
                            followViewModel.refresh()
                        }
                    )
                }

                is UserProfileUiState.Guest -> {
                    GuestProfile("Login to see your posts.")
                }
            }
        }
    }
}