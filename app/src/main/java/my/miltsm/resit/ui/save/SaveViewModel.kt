package my.miltsm.resit.ui.save

import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.mlkit.vision.text.Text
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import my.miltsm.resit.data.model.State
import my.miltsm.resit.domain.CacheUseCase
import my.miltsm.resit.domain.RecogniseTextUseCase
import java.io.File
import javax.inject.Inject

@HiltViewModel
class SaveViewModel @Inject constructor(
    private val recogniseTxtUC: RecogniseTextUseCase,
    private val cacheUseCase: CacheUseCase
) : ViewModel(), OnSuccessListener<Text>, OnFailureListener {

    companion object {
        const val TAG = "SAVE_VM"
    }

    private val _processState = MutableStateFlow<State<String>>(State.IdleState())
    val processState : StateFlow<State<String>> get() = _processState

    private val _saveState = MutableStateFlow<State<Unit>>(State.IdleState())
    val saveState : StateFlow<State<Unit>> get() = _saveState

    private val _caches = MutableStateFlow<Array<File>>(emptyArray())
    val caches : StateFlow<Array<File>> get() = _caches

    init {
        _caches.value = cacheUseCase.getCache()
    }

    fun readResit(
        firstPage: File
    ) = viewModelScope.launch {
        _processState
            .takeIf { it.value !is State.LoadingState }
            ?.apply {
                value = State.LoadingState()
                try {
                    recogniseTxtUC.processTextsFromImage(
                        firstPage.toUri(),
                        this@SaveViewModel,
                        this@SaveViewModel
                    )
                } catch (e: Exception) {
                    Log.e(TAG, e.toString())
                    value = State.FailedState()
                }
            }
    }

    fun saveResits(title: String) = viewModelScope.launch {
        _saveState.apply {
            value = State.LoadingState()
            value = try {
                _caches.value = emptyArray()
                cacheUseCase.saveCache(title)
                State.SuccessState()
            } catch (e: Exception) {
                State.FailedState()
            }
        }
    }

    fun discardResits() = viewModelScope.launch {
        _saveState.apply {
            value = State.LoadingState()
            value = try {
                _caches.value = emptyArray()
                cacheUseCase.clearCache()
                State.SuccessState()
            } catch (e: Exception) {
                State.FailedState()
            }
        }
    }

    override fun onSuccess(p0: Text?) {
        p0?.textBlocks?.firstOrNull()?.also {
            _processState.value = State.SuccessState(data = it.lines.firstOrNull()?.text ?: "")
        } ?: {
            _processState.value = State.FailedState()
        }
    }

    override fun onFailure(p0: java.lang.Exception) {
        Log.e(TAG, p0.toString())
        _processState.value = State.FailedState()
    }
}