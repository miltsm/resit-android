package my.miltsm.resit.data.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import my.miltsm.resit.data.DB
import my.miltsm.resit.data.model.ImagePath
import my.miltsm.resit.data.model.Receipt
import javax.inject.Inject

class SaveRepository @Inject constructor(
    @ApplicationContext
    val context: Context,
    private val db: DB
) {
    fun saveReceipt(receipt: Receipt) : Receipt {
        db.receiptDao().insert(receipt)
        return db.receiptDao().receipt()
    }

    fun saveImagePaths(paths: List<ImagePath>) {
        db.imagePathDao().insertAll(paths)
    }
}