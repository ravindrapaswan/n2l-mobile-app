package practice.english.n2l.util

import androidx.room.TypeConverter
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


object TimestampConverter {
    private var df: DateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
    private var df2: DateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
    private var df3: DateFormat = SimpleDateFormat("hh:mm.ss", Locale.ENGLISH)
    @TypeConverter
    fun fromTimestamp(value: String?): Date? {
        return if (value != null) {
            try {
                return df.parse(value)
            } catch (e: ParseException) {
                e.printStackTrace()
            }
            null
        } else {
            null
        }
    }

    @TypeConverter
    fun dateToTimestamp(value: Date?): String? {
        return if (value == null) null else df.format(value)
    }

    fun convertDateToddMMyy(value: Date?): String? {
        return  df2.format(value)
    }
    fun convertDateToTime(value: Date): String {
        return  df3.format(value)
    }
    fun dateToString(value: Date): String {
        return  df.format(value)
    }
    fun convertDateFormat(inputDate: String): String {
        val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = df2.parse(inputDate)
        return df.format(date)
    }
}