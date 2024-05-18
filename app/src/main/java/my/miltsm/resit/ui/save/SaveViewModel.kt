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
import my.miltsm.resit.data.model.Response
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

    private val _processState = MutableStateFlow<Response<String>>(Response.Idle())
    val processState : StateFlow<Response<String>> get() = _processState

    private val _saveState = MutableStateFlow<Response<Unit>>(Response.Idle())
    val saveState : StateFlow<Response<Unit>> get() = _saveState

    private val _caches = MutableStateFlow<Array<File>>(emptyArray())
    val caches : StateFlow<Array<File>> get() = _caches

    init {
        _caches.value = cacheUseCase.getCache()
    }

    fun readResit(
        firstPage: File
    ) = viewModelScope.launch {
        _processState
            .takeIf { it.value !is Response.Loading }
            ?.apply {
                value = Response.Loading()
                try {
                    recogniseTxtUC.processTextsFromImage(
                        firstPage.toUri(),
                        this@SaveViewModel,
                        this@SaveViewModel
                    )
                } catch (e: Exception) {
                    Log.e(TAG, e.toString())
                    value = Response.Fail()
                }
            }
    }

    fun saveResits(label: String, note: String?) = viewModelScope.launch {
        _saveState.apply {
            value = Response.Loading()
            value = try {
                _caches.value = emptyArray()
                cacheUseCase.saveCache(label, note)
                Response.Success()
            } catch (e: Exception) {
                Response.Fail()
            }
        }
    }

    fun discardResits() = viewModelScope.launch {
        _saveState.apply {
            value = Response.Loading()
            value = try {
                _caches.value = emptyArray()
                cacheUseCase.clearCache()
                Response.Success()
            } catch (e: Exception) {
                Response.Fail()
            }
        }
    }

    override fun onSuccess(p0: Text?) {
        p0?.textBlocks?.firstOrNull()?.also {
            _processState.value = Response.Success(data = it.lines.firstOrNull()?.text ?: "")
        } ?: {
            _processState.value = Response.Fail()
        }
    }

    override fun onFailure(p0: java.lang.Exception) {
        Log.e(TAG, p0.toString())
        _processState.value = Response.Fail()
    }
}