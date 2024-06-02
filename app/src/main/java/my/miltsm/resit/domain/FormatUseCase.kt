package my.miltsm.resit.domain

import android.text.format.DateUtils
import java.text.DateFormat
import java.util.Date
import javax.inject.Inject

class FormatUseCase @Inject constructor() {
    private val dateFmt = DateFormat.getDateInstance()

    fun formatDate(timestamp: Long): String =
        Date(timestamp).let { dt -> dateFmt.format(dt) }

    fun formatDateIfToday(timestamp: Long): String =
        if (DateUtils.isToday(timestamp))
            "Today"
        else
            formatDate(timestamp = timestamp)
}