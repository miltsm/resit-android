package my.miltsm.resit.data.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import my.miltsm.resit.data.DB
import javax.inject.Inject

class ReceiptRepository @Inject constructor(
    @ApplicationContext
    val context: Context,
    private val ioDispatcher: CoroutineDispatcher,
    private val db: DB
) {
    companion object {
        const val PAGE_SIZE = 10
        const val PAGE_KEY = "receipt"
    }
    fun receiptFlow() = db.receiptDao().flow()

    suspend fun receipts(page: Int) = withContext(ioDispatcher) {
        db.receiptDao().receipts(
            PAGE_SIZE,
            page.minus(1).times(PAGE_SIZE)
        )
    }
}