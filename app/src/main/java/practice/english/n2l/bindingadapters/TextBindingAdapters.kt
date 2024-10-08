package practice.english.n2l.bindingadapters

import android.widget.TextView
import androidx.databinding.BindingAdapter
import practice.english.n2l.util.TimestampConverter
import java.util.Date

class TextBindingAdapters {
    companion object {
        @BindingAdapter("app:convertDateToTime")
        @JvmStatic
        fun convertDateToTime(textView: TextView, date: Date?) {
            date?.let {
                val timeString = TimestampConverter.convertDateToddMMyy(it)
                textView.text = timeString
            }
        }
        @BindingAdapter("app:convertNumberToString")
        @JvmStatic
        fun convertNumberToString(textView: TextView, number: Long?) {
            number?.let {
                textView.text = number.toString()
            }
        }

    }
}