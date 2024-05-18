package my.miltsm.resit.ui.home

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import my.miltsm.resit.R
import my.miltsm.resit.data.model.ImagePath
import my.miltsm.resit.data.model.Receipt
import my.miltsm.resit.data.model.ReceiptWithImagePaths
import my.miltsm.resit.data.model.Response
import my.miltsm.resit.ui.common.shimmerBrush
import my.miltsm.resit.ui.theme.ResitTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier,
    onScanActivityClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {

    val lazyPagingReceipts = viewModel.receipts.collectAsLazyPagingItems()

    Scaffold (
        topBar = {
            TopAppBar(title = {
                Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    Text(
                        text = stringResource(id = R.string.app_name),
                        style = MaterialTheme.typography.headlineLarge)
                }
            })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onScanActivityClick() }) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { innerPadding ->
        Column(modifier = modifier.padding(innerPadding)) {
            LazyColumn(modifier = modifier
                .padding(horizontal = 8.dp)
                .fillMaxSize()
            ) {
                items(
                    lazyPagingReceipts.itemCount,
                    key = lazyPagingReceipts.itemKey {
                        when(it) {
                            is ReceiptUIModel.ReceiptModel ->
                                it.data.receipt.id
                            is ReceiptUIModel.SeparatorModel ->
                                it.date
                        }
                    }
                ) {index ->
                    lazyPagingReceipts[index].let { item ->
                        when (item) {
                            null ->
                                ReceiptRowPlaceholder(modifier = modifier)
                            is ReceiptUIModel.ReceiptModel ->
                                ReceiptRow(item = item.data,
                                getImageBitmap = { path: String ->
                                    viewModel.getImageFile(
                                        path = path
                                    )
                                }, modifier = modifier)
                            is ReceiptUIModel.SeparatorModel ->
                                DateSeparator(date = item.date, modifier)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReceiptRow(
    item: ReceiptWithImagePaths,
    getImageBitmap: @Composable (String) -> State<Response<Bitmap>>,
    modifier: Modifier
) {
    val imageState = getImageBitmap(item.paths.first().path)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 80.dp)
            .padding(vertical = 4.dp)
    ) {
        Column(
            modifier = modifier.padding(8.dp)
        ) {
            Row {
                Column(
                    modifier = modifier.weight(1f)
                ) {
                    Text(
                        text = item.receipt.title,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = item.receipt.description ?: "",
                        maxLines = 5,
                        overflow = TextOverflow.Ellipsis,
                        color = Color.Gray)
                }
                Box(
                    modifier = modifier
                        .requiredSize(45.dp)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    imageState.value.let { response ->
                        when(response) {
                            is Response.Loading, is Response.Idle ->
                                ThumbnailPlaceholder(modifier = modifier)
                            is Response.Success ->
                                Image(bitmap = response.data?.asImageBitmap()!!, contentDescription = "")
                            else ->
                                Column(
                                    modifier = modifier
                                        .background(
                                            MaterialTheme.colorScheme.tertiary
                                        )
                                        .fillMaxSize(),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        tint = Color.White,
                                        painter = painterResource(id = R.drawable.baseline_receipt_long_24),
                                        contentDescription = "logo")
                                }
                        }
                    }
                }
            }
            HorizontalDivider(
                modifier = modifier.padding(top = 8.dp)
            )
        }
    }
}

@Preview
@Composable
fun PreviewReceiptCard() {
    ResitTheme {
        ReceiptRow(
            item =
            ReceiptWithImagePaths(
                receipt = Receipt(
                    "Sushi",
                    "- Mister cdchbdfjbvjdhcdshbcjs chbdsjcbdj\n".repeat(5),
                    createdAt = 112334
                ),
                paths = listOf(ImagePath(1, ""))
            ),
            getImageBitmap =  { string -> produceState(initialValue = Response.Fail()) { value = Response.Fail() } },
            modifier = Modifier
        )
    }
}

@Composable
fun ReceiptRowPlaceholder(
    modifier: Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 80.dp)
            .padding(vertical = 4.dp)
    ) {
        Column(
            modifier = modifier.padding(8.dp)
        ) {
            Row {
                Column {
                    Box(modifier = modifier
                        .size(width = 120.dp, height = 20.dp)
                        .background(shimmerBrush(), shape = RoundedCornerShape(12.dp)))
                    Spacer(modifier = modifier.height(6.dp))
                    Box(modifier = modifier
                        .size(width = 300.dp, height = 14.dp)
                        .background(shimmerBrush(), shape = RoundedCornerShape(8.dp)))
                    Spacer(modifier = modifier.height(5.dp))
                    Box(modifier = modifier
                        .size(width = 180.dp, height = 14.dp)
                        .background(shimmerBrush(), shape = RoundedCornerShape(8.dp)))
                }
                Spacer(modifier = modifier.weight(1f))
                ThumbnailPlaceholder(modifier = modifier)
            }
            HorizontalDivider(
                modifier = modifier.padding(top = 8.dp)
            )
        }
    }
}

@Preview
@Composable
fun PreviewReceiptRowPlaceholder() {
    ResitTheme {
        ReceiptRowPlaceholder(modifier = Modifier)
    }
}

@Composable
fun DateSeparator(
    date: String, modifier: Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp),
        //horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AssistChip(onClick = {},
            enabled = false,
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.event_upcoming_24dp),
                    contentDescription = "date")
            },
            label = {
                Text(text = date)
            }
        )
    }
}

@Preview
@Composable
fun PreviewDateSeparator() {
    ResitTheme {
        DateSeparator(date = "Today", Modifier)
    }
}

@Composable
fun ThumbnailPlaceholder(modifier: Modifier) {
    Box(modifier = modifier
        .size(45.dp)
        .background(shimmerBrush(), shape = RoundedCornerShape(8.dp))
    )
}

@Preview
@Composable
fun PreviewThumbnailPlaceholder() {
    ResitTheme {
        ThumbnailPlaceholder(modifier = Modifier)
    }
}