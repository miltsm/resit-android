package my.miltsm.resit.domain

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import my.miltsm.resit.data.model.ImagePath
import my.miltsm.resit.data.model.Receipt
import my.miltsm.resit.data.repository.SaveRepository
import java.io.File
import javax.inject.Inject

class CacheUseCase @Inject constructor(
    private val saveRepository: SaveRepository,
    private val ioDispatcher: CoroutineDispatcher
) {
    companion object {
        const val ML_KIT_CACHE_PATH = "/mlkit_docscan_ui_client"
        const val RECEIPT_PATH = "/receipts/"
    }

    private val context = saveRepository.context

    fun getCache() : Array<File> = try {
        File(context.cacheDir, ML_KIT_CACHE_PATH).listFiles() ?: throw Exception()
    } catch (e: Exception) {
        emptyArray()
    }

    suspend fun saveCache(fileParentName: String, description: String? = "") = withContext(ioDispatcher) {
        getCache().let { caches ->
            val receipt = saveRepository.saveReceipt(
                Receipt(
                    title = fileParentName,
                    description = description
                )
            )

            val newPaths = mutableListOf<ImagePath>()

            caches.forEach { cache ->
                val newPath = "${RECEIPT_PATH}${fileParentName.replace(" ", "-").lowercase()}/${cache.name}"
                cache.copyTo(
                    File(
                        context.filesDir,
                        newPath
                    )
                )
                newPaths.add(ImagePath(receiptId = receipt.id, path= newPath))
            }

            saveRepository.saveImagePaths(newPaths)
        }

        clearCache()
    }

    suspend fun clearCache() = withContext(ioDispatcher) {
        val caches = File(context.cacheDir, ML_KIT_CACHE_PATH).listFiles()

        try {
            caches?.forEach { cache -> cache.delete() }
        } catch (_: Exception) {}
    }
}