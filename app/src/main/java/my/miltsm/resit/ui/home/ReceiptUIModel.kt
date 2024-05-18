package my.miltsm.resit.ui.home

import my.miltsm.resit.data.model.ReceiptWithImagePaths

sealed class ReceiptUIModel {
    class ReceiptModel(val data: ReceiptWithImagePaths) : ReceiptUIModel()
    class SeparatorModel(val date: String) : ReceiptUIModel()
}