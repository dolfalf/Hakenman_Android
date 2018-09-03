package archiveasia.jp.co.hakenman.Extension

import java.text.SimpleDateFormat
import java.util.*

fun Date.year(): String = SimpleDateFormat("yyyy").format(this)

fun Date.month(): String = SimpleDateFormat("MM").format(this)
fun Date.day(): String = SimpleDateFormat("dd").format(this)

fun Date.yearMonth(): String = SimpleDateFormat("yyyyMM").format(this)

fun Date.week(): String = SimpleDateFormat("E").format(this)

fun Date.hourMinute(): String = SimpleDateFormat("HH:mm").format(this)

fun Date.isHoliday(): Boolean {
    var calendar = Calendar.getInstance()
    calendar.time = this
    val weekOfYear = calendar.get(Calendar.DAY_OF_WEEK)

    when (weekOfYear) {
        1, 7 -> {
            return true
        }
        else -> {
            return false
        }
    }

}

fun Date.hourMinuteToDouble(): Double {
    var calendar = Calendar.getInstance()
    calendar.time = this
    var hour = calendar.get(Calendar.HOUR_OF_DAY)
    var minute = calendar.get(Calendar.MINUTE)

    return hour.toDouble() + (minute.toDouble() / 60)
}