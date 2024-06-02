package my.miltsm.resit.data.repository.paging_source

import androidx.paging.PagingSource
import androidx.paging.PagingState
import my.miltsm.resit.data.model.ReceiptWithImagePaths
import my.miltsm.resit.data.repository.ReceiptRepository

class ReceiptPagingSource(
    private val repository: ReceiptRepository
) : PagingSource<Int, ReceiptWithImagePaths>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ReceiptWithImagePaths> =
        try {
            val nextPageNumber = params.key ?: 1
            val receipts = repository.receipts(nextPageNumber)
            LoadResult.Page(
                data = receipts,
                nextKey = if (receipts.isEmpty()) null else nextPageNumber.plus(1),
                prevKey = null
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }

    override fun getRefreshKey(state: PagingState<Int, ReceiptWithImagePaths>): Int? =
        state.anchorPosition?.let { anchorPos ->
            val anchorPage = state.closestPageToPosition(anchorPos)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
}