package my.miltsm.resit.data.model

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Relation

@Entity
data class Receipt(
    val title: String,
    val description: String?,
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "receiptId")
    val id: Long = 0
)

@Dao
interface ReceiptDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg receipts: Receipt)

    @Delete
    fun delete(receipt: Receipt)

    @Query("delete from receipt")
    fun deleteAll()

    @Query("SELECT * FROM Receipt")
    fun receipt() : Receipt

    @Query("SELECT * FROM Receipt")
    fun receipts() : List<Receipt>
}

@Entity
data class ImagePath(
    val receiptId: Long,
    val path: String,
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "imageId")
    val id: Long = 0
)

@Dao
interface ImagePathDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(paths: List<ImagePath>)

    @Query("DELETE FROM imagepath")
    fun deleteAll()
}

data class ReceiptWithImagePaths(
    @Embedded val receipt: Receipt,
    @Relation(
        parentColumn = "receiptId",
        entityColumn = "imageId"
    )
    val paths: List<ImagePath>
)