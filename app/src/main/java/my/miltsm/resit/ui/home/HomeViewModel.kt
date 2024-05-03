package my.miltsm.resit.ui.home

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import my.miltsm.resit.data.repository.HomeRepository
import my.miltsm.resit.domain.CacheUseCase
import java.io.File
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val homeRepository: HomeRepository
) : ViewModel() {

    private val _receipts = MutableStateFlow<Array<File>>(emptyArray())
    val receipts : StateFlow<Array<File>> get() = _receipts

    init {
        _receipts.value = try {
            File(
                homeRepository.context.filesDir, CacheUseCase.RECEIPT_PATH
            ).let { savedFile ->
                savedFile.listFiles() ?: throw Exception()
            }
        } catch (e: Exception) {
            emptyArray()
        }
    }
}