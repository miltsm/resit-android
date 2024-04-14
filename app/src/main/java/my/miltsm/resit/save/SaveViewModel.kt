package my.miltsm.resit.save

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
import my.miltsm.resit.domain.RecogniseTextUseCase
import java.io.File
import javax.inject.Inject

@HiltViewModel
class SaveViewModel @Inject constructor(
    private val recogniseTxtUC: RecogniseTextUseCase,
) : ViewModel(), OnSuccessListener<Text>, OnFailureListener {

    companion object {
        const val TAG = "SAVE_VM"
        const val ML_KIT_CACHE_PATH = "/mlkit_docscan_ui_client"
    }

    private val _processState = MutableStateFlow<State<String>>(State.IdleState())
    val processState : StateFlow<State<String>> get() = _processState

    private val _saveState = MutableStateFlow<State<Unit>>(State.IdleState())
    val saveState : StateFlow<State<Unit>> get() = _saveState

    private val _caches = MutableStateFlow<Array<File>>(emptyArray())
    val caches : StateFlow<Array<File>> get() = _caches

    init {
        _caches.value = try {
            File(
                recogniseTxtUC.context.cacheDir, ML_KIT_CACHE_PATH
            ).let { cacheFile ->
                cacheFile.listFiles() ?: throw Exception()
            }
        } catch (e: Exception) {
            emptyArray()
        }
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

    fun saveResits(
        pages: Array<File> //List<GmsDocumentScanningResult.Page>
    ) = viewModelScope.launch {

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