package my.miltsm.resit.ui.home

import android.content.Context
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import my.miltsm.resit.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier,
    onScanActivityClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
    context : Context = LocalContext.current
) {

    val receipts by viewModel.receipts.collectAsState()

    Scaffold (
        topBar = {
            TopAppBar(title = {
                Text(text = stringResource(id = R.string.app_name))
            })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onScanActivityClick() }) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { innerPadding ->
        Column(modifier = modifier.padding(innerPadding)) {
            LazyColumn {
                items(receipts) {receipt ->
                    Column {
                        receipt.listFiles()?.let {
                            it.forEach { image ->
                                val bm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
                                    ImageDecoder.decodeBitmap(
                                        ImageDecoder.createSource(image)
                                    )
                                else
                                    MediaStore.Images.Media.getBitmap(
                                        context.contentResolver, image.path.toUri()
                                    )

                                Image(bitmap = bm.asImageBitmap(), contentDescription = "")
                            }
                        }
                    }
                }
            }
        }
    }
}