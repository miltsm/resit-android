package my.miltsm.resit.data

import androidx.room.Database
import androidx.room.RoomDatabase
import my.miltsm.resit.data.model.ImagePath
import my.miltsm.resit.data.model.ImagePathDao
import my.miltsm.resit.data.model.Receipt
import my.miltsm.resit.data.model.ReceiptDao

@Database(entities = [Receipt :: class, ImagePath :: class], version = 1)
abstract class DB : RoomDatabase() {
    companion object {
        const val NAME = "resit-db"
    }
    abstract fun receiptDao() : ReceiptDao
    abstract fun imagePathDao() : ImagePathDao
}