package practice.english.n2l.database

import androidx.room.TypeConverter
import java.util.Date


internal object DateConverter {
    @TypeConverter
    fun toDate(dateLong: Long): Date {
        return Date(dateLong)
    }

    @TypeConverter
    fun fromDate(date: Date): Long {
        return date.time
    }
}