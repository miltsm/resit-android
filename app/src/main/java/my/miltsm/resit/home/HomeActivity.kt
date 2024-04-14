package my.miltsm.resit.home

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import dagger.hilt.android.AndroidEntryPoint
import my.miltsm.resit.save.SaveScreen
import my.miltsm.resit.save.SaveViewModel
import my.miltsm.resit.ui.theme.ResitTheme
import java.io.File

@AndroidEntryPoint
class HomeActivity : ComponentActivity() {

    private val options = GmsDocumentScannerOptions.Builder()
        .setGalleryImportAllowed(true)
        .setPageLimit(1)
        .setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_JPEG)
        .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL)
        .build()

    private val scanner = GmsDocumentScanning.getClient(options)

    private val scannerLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK)
            navigator.navigate(SAVE_DESTINATION)
    }

    private lateinit var navigator : NavHostController
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val navigateSave = {
            val cache = File(cacheDir, SaveViewModel.ML_KIT_CACHE_PATH)
            if (cache.list()?.isNotEmpty() == true)
                navigator.navigate(SAVE_DESTINATION)
            else
                throw Exception()
        }

        val startScanner = {
            scanner.getStartScanIntent(this)
                .addOnSuccessListener {
                    scannerLauncher.launch(
                        IntentSenderRequest.Builder(it).build()
                    )
                }
                .addOnFailureListener {

                }
        }

        setContent {
            navigator = rememberNavController()
            ResitApp(navController = navigator, navigateSave) {
                try {
                    navigateSave()
                } catch (_: Exception) {
                    startScanner()
                }
            }
        }
    }
}

@Composable
fun ResitApp(
    navController: NavHostController,
    navigateSave: () -> Unit,
    onScanActivityClick: () -> Unit,
) {
    ResitTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            ResitNavHost(
                navController = navController,
                onScanActivityClick
            )
        }
    }

    try {
        navigateSave()
    } catch (_: Exception) { }
}

@Composable
fun ResitNavHost(
    navController: NavHostController,
    onScanActivityClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = HOME_DESTINATION
    ) {
        composable(route = HOME_DESTINATION) {
            HomeScreen(modifier, onScanActivityClick)
        }
        composable(route = SAVE_DESTINATION) {
            SaveScreen(navController = navController)
        }
    }
}

const val HOME_DESTINATION = "home"
const val SAVE_DESTINATION = "save"