package my.miltsm.resit.data.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import my.miltsm.resit.data.DB
import javax.inject.Inject

class HomeRepository @Inject constructor(
    @ApplicationContext
    val context: Context,
    private val dispatcher: CoroutineDispatcher,
    private val db: DB
) {
    companion object {
        const val PAGE_SIZE = 15
    }
    suspend fun receipts(page: Int) = withContext(dispatcher) {
        db.receiptDao().receipts(page.minus(1).times(PAGE_SIZE))
    }
}