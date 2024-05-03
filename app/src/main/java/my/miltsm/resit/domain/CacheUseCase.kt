package my.miltsm.resit.domain

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject

class CacheUseCase @Inject constructor(
    @ApplicationContext
    private val context: Context
) {
    companion object {
        const val ML_KIT_CACHE_PATH = "/mlkit_docscan_ui_client"
        const val RECEIPT_PATH = "/receipts/"
    }

    fun getCache() : Array<File> = try {
        File(context.cacheDir, ML_KIT_CACHE_PATH).listFiles() ?: throw Exception()
    } catch (e: Exception) {
        emptyArray()
    }

    fun saveCache(fileParentName: String) {
        getCache().forEach { cache ->
            cache.copyTo(
                File(
                    context.filesDir,
                    "${RECEIPT_PATH}${fileParentName.replace(" ", "-").lowercase()}/${cache.name}"
                )
            )
        }
        clearCache()
    }

    fun clearCache() {
        val caches = File(context.cacheDir, ML_KIT_CACHE_PATH).listFiles() ?: return

        try {
            caches.forEach { cache -> cache.delete() }
        } catch (e: Exception) {}
    }
}