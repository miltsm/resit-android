package my.miltsm.resit.ui.home

import my.miltsm.resit.data.model.ReceiptWithImagePaths

sealed class ReceiptUIModel {
    class ReceiptModel(val data: ReceiptWithImagePaths) : ReceiptUIModel()
    class DateSeparatorModel(val id: Long, val date: String) : ReceiptUIModel()
    class ReceiptSeparatorModel(val id: Long) : ReceiptUIModel()
}