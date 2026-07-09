package com.siae.biometricsiae.feature.camera

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.siae.biometricsiae.data.remote.firebase.StorageManager
import com.siae.biometricsiae.util.SecureLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class CameraViewModel @Inject constructor(
    application: Application,
    private val storageManager: StorageManager
) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(CameraState())
    val state: StateFlow<CameraState> = _state.asStateFlow()

    fun onPhotoCaptured(photoFile: File) {
        _state.value = _state.value.copy(
            capturedPhoto = photoFile,
            isProcessing = true
        )
    }

    fun onFaceDetected(
        faceDetected: Boolean,
        faceCentered: Boolean,
        eyesVisible: Boolean,
        livenessScore: Float?
    ) {
        _state.value = _state.value.copy(
            faceDetected = faceDetected,
            faceCentered = faceCentered,
            eyesVisible = eyesVisible,
            livenessScore = livenessScore
        )
    }

    fun uploadEvidence(orgId: String, employeeId: String) {
        val photoFile = _state.value.capturedPhoto ?: return
        
        viewModelScope.launch {
            _state.value = _state.value.copy(isUploading = true)
            
            val result = storageManager.uploadEvidence(
                orgId = orgId,
                employeeId = employeeId,
                imageFile = photoFile
            )
            
            result.fold(
                onSuccess = { url ->
                    _state.value = _state.value.copy(
                        uploadedUrl = url,
                        isUploading = false,
                        isComplete = true
                    )
                },
                onFailure = { error ->
                    SecureLogger.e("CameraViewModel", "Upload failed", error)
                    _state.value = _state.value.copy(
                        error = error.message,
                        isUploading = false
                    )
                }
            )
        }
    }

    fun reset() {
        _state.value = CameraState()
    }
}

data class CameraState(
    val capturedPhoto: File? = null,
    val faceDetected: Boolean = false,
    val faceCentered: Boolean = false,
    val eyesVisible: Boolean = false,
    val livenessScore: Float? = null,
    val isProcessing: Boolean = false,
    val isUploading: Boolean = false,
    val uploadedUrl: String? = null,
    val isComplete: Boolean = false,
    val error: String? = null
)
