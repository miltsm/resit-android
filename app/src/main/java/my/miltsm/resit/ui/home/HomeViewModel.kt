package my.miltsm.resit.ui.home

import android.graphics.Bitmap
import android.os.Build
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
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
import my.miltsm.resit.data.repository.ReceiptRepository
import my.miltsm.resit.data.repository.paging_source.ReceiptPagingSource
import my.miltsm.resit.domain.FormatUseCase
import my.miltsm.resit.domain.ImageUseCase
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val receiptRepository: ReceiptRepository,
    private val imageUseCase: ImageUseCase,
    private val formatUseCase: FormatUseCase
) : ViewModel() {

    companion object {
        const val TAG = "HOME_VM"
    }

    val receiptFlow = receiptRepository.receiptFlow()

    val receipts = Pager(
        PagingConfig(ReceiptRepository.PAGE_SIZE)
    ) {
        ReceiptPagingSource(receiptRepository)
    }
        .flow
        .map { pagingData ->
            pagingData.map { receipt ->
                ReceiptUIModel.ReceiptModel(receipt)
            }
                .insertSeparators { before, after ->
                    try {
                        when {
                            before == null && after != null -> {
                                val timestamp = after.data.receipt.createdAt
                                ReceiptUIModel.DateSeparatorModel(
                                    timestamp,
                                    formatUseCase.formatDateIfToday(timestamp)
                                )
                            }

                            !isSameDate(
                                before?.data?.receipt?.createdAt!!,
                                after?.data?.receipt?.createdAt!!) -> {
                                val timestamp = after.data.receipt.createdAt
                                ReceiptUIModel.DateSeparatorModel(
                                    timestamp,
                                    formatUseCase.formatDateIfToday(timestamp)
                                )
                            }

                            else ->
                                ReceiptUIModel.ReceiptSeparatorModel(
                                    before.data.receipt.createdAt.plus(
                                        after.data.receipt.createdAt))
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "$e")
                        null
                    }
                }
        }
        .cachedIn(viewModelScope)

    @Composable
    fun getThumbnail(path: String) : State<Response<Bitmap>> =
        produceState<Response<Bitmap>>(initialValue = Response.Idle()) {
            value = try {
                Response.Success(imageUseCase.getResitBitmap(path))
            } catch (e: Exception) {
                Response.Fail()
            }
        }

    private fun isSameDate(beforeTimestamp: Long, afterTimestamp: Long) : Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val beforeInstant = Instant.ofEpochMilli(beforeTimestamp)
                .truncatedTo(ChronoUnit.DAYS)
            val afterInstant = Instant.ofEpochMilli(afterTimestamp)
                .truncatedTo(ChronoUnit.DAYS)
            beforeInstant == afterInstant
        } else {
            val beforeDateFormatted = formatUseCase.formatDate(beforeTimestamp)
            val afterDateFormatted = formatUseCase.formatDate(afterTimestamp)
            beforeDateFormatted == afterDateFormatted
        }
}