package io.eshort.user.presentation.screens.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.eshort.user.domain.repository.VideoRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CreateState(
    val isPublishing: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class CreateViewModel @Inject constructor(
    private val videoRepo: VideoRepo
) : ViewModel() {

    private val _createState = MutableStateFlow(CreateState())
    val createState: StateFlow<CreateState> = _createState.asStateFlow()

    fun publish(url: String, caption: String, tags: List<String>) {
        viewModelScope.launch {
            _createState.value = CreateState(isPublishing = true)
            videoRepo.publishVideo(url, caption, tags).fold(
                onSuccess = {
                    _createState.value = CreateState(isSuccess = true)
                },
                onFailure = { e ->
                    _createState.value = CreateState(error = e.message)
                }
            )
        }
    }
}
