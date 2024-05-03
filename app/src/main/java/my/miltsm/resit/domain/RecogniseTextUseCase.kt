package my.miltsm.resit.domain

import android.content.Context
import android.net.Uri
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognizer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RecogniseTextUseCase @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val recogniser : TextRecognizer
) {
    suspend fun processTextsFromImage(
        imageUri: Uri,
        onSuccess: OnSuccessListener<Text>,
        onFailureListener: OnFailureListener
    ) {
        withContext(Dispatchers.IO) {
            val path = InputImage.fromFilePath(context, imageUri)
            recogniser.process(path)
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailureListener)
        }
    }
}