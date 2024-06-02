package my.miltsm.resit.data.model

import androidx.paging.PagingSource
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
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Entity
data class Receipt(
    val title: String,
    val description: String?,
    val createdAt: Long,
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

    @Query("SELECT * FROM Receipt order by receiptId desc")
    fun receipt() : Receipt

    @Query("SELECT * FROM Receipt")
    fun flow() : Flow<List<Receipt>>

    @Transaction
    @Query("SELECT * FROM Receipt order by createdAt desc")
    fun pagingSource() : PagingSource<Int, ReceiptWithImagePaths>

    @Transaction
    @Query("SELECT * FROM Receipt order by createdAt desc LIMIT :pageSize OFFSET :pageOffset")
    fun receipts(pageSize: Int, pageOffset: Int) : List<ReceiptWithImagePaths>
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