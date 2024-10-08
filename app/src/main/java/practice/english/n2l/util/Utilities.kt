package practice.english.n2l.util

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID


class Utilities {
    companion object {
        fun getCurrentDateTime(): String {
            val sdf = SimpleDateFormat("dd:MM:yyyy_HH:mm:ss", Locale.getDefault())
            return sdf.format(Date())
        }
        fun generateRandomFileName(extension: String): String {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val randomString = UUID.randomUUID().toString().substring(0, 8)
            return "file_$timeStamp$randomString.$extension"
        }
        fun hideKeyboard(view: View?, context: Context) {
            if (view != null) {
                val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(view.windowToken, InputMethodManager.RESULT_UNCHANGED_SHOWN)
            }
        }
    }
}