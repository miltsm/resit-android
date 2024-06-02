package my.miltsm.resit.domain

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import androidx.core.net.toUri
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import my.miltsm.resit.data.repository.ReceiptRepository
import java.io.File
import javax.inject.Inject

class ImageUseCase @Inject constructor(
    private val ioDispatcher: CoroutineDispatcher,
    private val receiptRepository: ReceiptRepository
) {
    suspend fun getResitBitmap(path: String) : Bitmap = withContext(ioDispatcher) {
        val imageFile = File(receiptRepository.context.filesDir, path)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
            ImageDecoder.decodeBitmap(
                ImageDecoder.createSource(imageFile)
            )
        else
            MediaStore.Images.Media.getBitmap(
                receiptRepository.context.contentResolver, imageFile.toUri()
            )
    }
}