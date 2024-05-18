package my.miltsm.resit.ui.save

import android.content.Context
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.Job
import my.miltsm.resit.R
import my.miltsm.resit.data.model.Response
import my.miltsm.resit.ui.theme.ResitTheme
import java.io.File

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SaveScreen(
    navController: NavController,
    viewModel: SaveViewModel = hiltViewModel()
) {
    val caches by viewModel.caches.collectAsState()
    val pagerState = rememberPagerState (
        pageCount = {
            caches.size
        }
    )

    val readResit by rememberUpdatedState(newValue = { viewModel.readResit(caches.first()) })

    val processState by viewModel.processState.collectAsState()
    val saveState by viewModel.saveState.collectAsState()

    val openConfirmDialog = remember {
        mutableStateOf(false)
    }

    //snack bar appears behind scrin
//    val sbHost = remember {
//        SnackbarHostState()
//    }

    if (saveState is Response.Success)
        navController.navigateUp()

    SaveContent(
        Modifier, navController, pagerState,
        caches, readResit, openConfirmDialog,
        processState,
        onConfirm = { label, note ->
            openConfirmDialog.value = false
            viewModel.saveResits(label, note)
        },
        onDiscard = {
            viewModel.discardResits()
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SaveContent(
    modifier: Modifier,
    navController: NavController,
    pagerState: PagerState,
    caches: Array<File>,
    readResit: () -> Job,
    confirmDialogState: MutableState<Boolean>,
    processState: Response<String>,
    onConfirm: (String, String?) -> Unit,
    onDiscard: () -> Unit,
    context: Context = LocalContext.current,
) {
    Scaffold(
        topBar = {
            SaveAppBar(navController = navController)
        },
        bottomBar = {
            BottomActionBar(
                modifier = modifier,
                discardCache = onDiscard
            ) { confirmDialogState.value = true }
        }
    ) { innerPadding ->
        Column(
            modifier = modifier.padding(innerPadding)
        ) {
            SavePager(
                context = context,
                pagerState = pagerState,
                caches = caches,
            )
        }

        var label by remember {
            mutableStateOf("")
        }

        var note : String? by remember {
            mutableStateOf(null)
        }

        //auto-fill label feature
        var shouldFillLabel by remember {
            mutableStateOf(false)
        }

        if (shouldFillLabel && processState is Response.Success) {
            label = processState.data ?: ""
            shouldFillLabel = false
        }

        var errorLabel by remember {
            mutableStateOf("")
        }

        if (processState is Response.Fail)
            LaunchedEffect(key1 = processState) {
                errorLabel = context.getString(R.string.unable_to_read_text)
            }

        //to reset error text
        LaunchedEffect(key1 = label) {
            if (errorLabel.isNotEmpty())
                errorLabel = ""
        }

        if (confirmDialogState.value)
            ConfirmDialog(
                modifier = modifier,
                processState = processState,
                label = label,
                onLabelChanged = { label = it },
                note = note,
                onNoteChanged = { note = it },
                errorLabel = errorLabel,
                onErrorLabelChanged = { errorLabel = it },
                onDismiss = {
                    confirmDialogState.value = false
                },
                readResit = {
                    shouldFillLabel = true
                    readResit()
                },
                onConfirm = onConfirm,
                context
            )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaveAppBar(
    navController: NavController
) {
    TopAppBar(
        title = { Text(text = stringResource(id = R.string.save_header)) },
        navigationIcon = {
            IconButton(onClick = { navController.navigateUp() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "back-icon"
                )
            }
        }
    )
}

@Preview
@Composable
fun PreviewSaveAppBar() {
    ResitTheme {
        SaveAppBar(navController = rememberNavController())
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SavePager(
    context: Context,
    pagerState: PagerState,
    caches: Array<File>
) {
    if (caches.isNotEmpty())
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 32.dp)
        ) { page ->
            val bm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
                ImageDecoder.decodeBitmap(
                    ImageDecoder.createSource(caches[page])
                )
            else
                MediaStore.Images.Media.getBitmap(
                    context.contentResolver, caches[page].path.toUri()
                )

            Image(bitmap = bm.asImageBitmap(), contentDescription = "")
        }
    else
        Column {
            //placeholder screen
            Text(text = "Not found")
        }
}

@Composable
fun BottomActionBar(
    modifier: Modifier,
    discardCache: () -> Unit,
    promptConfirmDialog: () -> Unit
) {
    Surface(
        shadowElevation = 3.dp
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = discardCache) {
                Text(text = stringResource(id = R.string.discard_action))
            }
            Spacer(modifier = modifier.size(16.dp))
            Button(onClick = promptConfirmDialog) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    val saveStr = stringResource(id = R.string.save_action)
                    Icon(
                        imageVector = Icons.Default.Done,
                        contentDescription = saveStr
                    )
                    Text(text = saveStr)
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewBottomActionBar() {
    ResitTheme {
        BottomActionBar(
            modifier = Modifier,
            discardCache = {}
        ) {}
    }
}

@Composable
fun ConfirmDialog(
    modifier: Modifier,
    processState: Response<String>,
    label: String,
    onLabelChanged: (String) -> Unit,
    note: String?,
    onNoteChanged: (String) -> Unit,
    errorLabel: String,
    onErrorLabelChanged: (String) -> Unit,
    onDismiss: () -> Unit,
    readResit: () -> Unit,
    onConfirm: (String, String?) -> Unit,
    context: Context
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(CornerSize(28.dp))
        ) {
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_save_24),
                    contentDescription = "save-icon",
                    tint = MaterialTheme.colorScheme.secondary
                )
                Text(
                    modifier = modifier.padding(vertical = 16.dp),
                    text = stringResource(id = R.string.confirm_header),
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = stringResource(id = R.string.confirm_desc),
                    style = MaterialTheme.typography.bodyMedium
                )
                HorizontalDivider(modifier = modifier.padding(vertical = 8.dp))

                OutlinedTextField(
                    value = label,
                    onValueChange = onLabelChanged,
                    label = {
                        Text(text = stringResource(id = R.string.save_label))
                    },
                    enabled = processState !is Response.Loading,
                    supportingText = {
                        if (errorLabel.isNotEmpty())
                            Text(text = errorLabel)
                    },
                    isError = errorLabel.isNotEmpty(),
                    singleLine = true,
                    trailingIcon = {
                        when {
                            label.isNotEmpty() -> {
                                IconButton(onClick = { onLabelChanged("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "clear-icon",
                                        //tint = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                }
                            }
                            processState is Response.Loading -> {
                                CircularProgressIndicator(modifier = modifier.size(24.dp))
                            }
                            else -> {
                                IconButton(
                                    onClick = {
                                        when(processState) {
                                            is Response.Success ->
                                                onLabelChanged(processState.data ?: "")
                                            else -> readResit()
                                        }
                                    },
                                    enabled = processState !is Response.Loading
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.baseline_document_scanner_24),
                                        contentDescription = "scan-text-icon",
                                        //tint = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                }
                            }
                        }
                    }
                )
                OutlinedTextField(
                    value = note ?: "",
                    onValueChange = onNoteChanged,
                    label = {
                        Text(text = "Note (optional)")
                    },
                    maxLines = 5,
                )
                HorizontalDivider(modifier = modifier.padding(top = 16.dp))
                Row(
                    modifier = modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(text = stringResource(id = R.string.cancel_action))
                    }
                    Spacer(modifier = modifier.size(16.dp))
                    TextButton(onClick = {
                        when {
                            label.isEmpty() ->
                                onErrorLabelChanged(
                                    context.getString(R.string.cant_be_empty)
                                )
                            else -> onConfirm(label, note)
                        }
                    }) {
                        Text(text = stringResource(id = R.string.confirm_action))
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewConfirmDialog() {
    ResitTheme {
        ConfirmDialog(
            modifier = Modifier,
            processState = //State.LoadingState()
            Response.Success("Test")
            ,label = "",
            onLabelChanged = {},
            note = "",
            onNoteChanged = {},
            errorLabel = "",
            onErrorLabelChanged = {},
            onDismiss = {},
            readResit = {},
            onConfirm = { label, note -> },
            context = LocalContext.current
        )
    }
}