package my.miltsm.resit.ui.home

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import android.text.format.DateFormat
import android.text.format.DateUtils
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import androidx.paging.insertSeparators
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import my.miltsm.resit.data.model.Response
import my.miltsm.resit.data.repository.HomeRepository
import my.miltsm.resit.data.repository.paging_source.ReceiptPagingSource
import java.io.File
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val homeRepository: HomeRepository
) : ViewModel() {

    companion object {
        const val TAG = "HOME_VM"
    }

    val receipts = Pager(
        PagingConfig(HomeRepository.PAGE_SIZE)
    ) {
        ReceiptPagingSource(homeRepository)
    }
        .flow
        .map { pagingData ->
            pagingData.map { receipt ->
                ReceiptUIModel.ReceiptModel(receipt)
            }
                .insertSeparators { before, after ->
                    try {
                        when {
                            before == null && after != null ->
                                ReceiptUIModel.SeparatorModel(
                                    formatDate(after.data.receipt.createdAt)
                                )

                            !isSameDate(
                                before?.data?.receipt?.createdAt!!,
                                after?.data?.receipt?.createdAt!!) ->
                                ReceiptUIModel.SeparatorModel(
                                    formatDate(before.data.receipt.createdAt)
                                )

                            else -> throw Exception()
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "$e")
                        null
                    }
                }
                //page footer
        }
        .cachedIn(viewModelScope)

    @Composable
    fun getImageFile(path: String) : State<Response<Bitmap>> {
        return produceState<Response<Bitmap>>(initialValue = Response.Idle()) {
            value = try {
                val imageFile = File(homeRepository.context.filesDir, path)
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
                    ImageDecoder.decodeBitmap(
                        ImageDecoder.createSource(imageFile)
                    )
                else
                    MediaStore.Images.Media.getBitmap(
                        homeRepository.context.contentResolver, imageFile.toUri()
                    )
                Response.Success(bitmap)
            } catch (e: Exception) {
                Response.Fail()
            }
        }
    }

    private fun isSameDate(beforeTimestamp: Long, afterTimestamp: Long) : Boolean {
        val beforeDate = Date(beforeTimestamp)
        val afterDate = Date(afterTimestamp)
        return !(beforeDate.before(afterDate) && beforeDate.after(afterDate))
    }

    private fun formatDate(timestamp: Long) : String =
        if (DateUtils.isToday(timestamp))
            "Today"
        else
            DateFormat.format(
                "dd MM yyyy",
                timestamp
            ).toString()
}