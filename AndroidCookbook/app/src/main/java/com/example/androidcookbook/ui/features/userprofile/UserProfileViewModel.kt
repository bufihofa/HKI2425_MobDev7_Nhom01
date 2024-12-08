package com.example.androidcookbook.ui.features.userprofile

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidcookbook.data.repositories.UserRepository
import com.example.androidcookbook.domain.model.post.Post
import com.example.androidcookbook.domain.usecase.DeletePostUseCase
import com.example.androidcookbook.domain.usecase.MakeToastUseCase
import com.skydoves.sandwich.onFailure
import com.skydoves.sandwich.onSuccess
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = UserProfileViewModel.UserProfileViewModelFactory::class)
class UserProfileViewModel @AssistedInject constructor(
    @Assisted private val userId: Int,
    private val userRepository: UserRepository,
    private val deletePostUseCase: DeletePostUseCase,
    private val makeToastUseCase: MakeToastUseCase,
) : ViewModel() {

    @AssistedFactory
    interface UserProfileViewModelFactory {
        fun create(userId: Int): UserProfileViewModel
    }

    init {
        getUser(userId)
        getUserPosts(userId)
    }

    var isRefreshing: Boolean by mutableStateOf(false)
        private set

    var uiState: UserProfileUiState by mutableStateOf(UserProfileUiState.Guest)
        private set

    var userPostState: UserPostState by mutableStateOf(UserPostState.Guest)
        private set

    private fun getUser(userId: Int) {
        viewModelScope.launch {
            userRepository.getUserProfile(userId = userId)
                .onSuccess {
                    uiState = UserProfileUiState.Success(user = data)
                }
                .onFailure {
                    uiState = UserProfileUiState.Failure
                }
        }
    }

    fun getUserPosts(userId: Int) {
        viewModelScope.launch {
            userRepository.getUserPosts(userId)
                .onSuccess { userPostState = UserPostState.Success(data) }
                .onFailure { userPostState = UserPostState.Failure }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            isRefreshing = true
            uiState = UserProfileUiState.Guest
            getUser(userId = userId)
            isRefreshing = false
        }
    }

    fun deletePost(post: Post) {
        viewModelScope.launch {
            deletePostUseCase(post).onSuccess {
                refresh()
            }.onFailure {
                viewModelScope.launch {
                    makeToastUseCase("Failed to delete post")
                }
            }
        }
    }
}