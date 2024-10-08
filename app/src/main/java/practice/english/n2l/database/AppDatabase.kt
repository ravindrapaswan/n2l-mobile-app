package practice.english.n2l.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import practice.english.n2l.database.bao.AnswerInfo
import practice.english.n2l.database.bao.Answers
import practice.english.n2l.database.bao.AppExceptionLog
import practice.english.n2l.database.bao.AttemptPractice
import practice.english.n2l.database.bao.Audio
import practice.english.n2l.database.bao.BlankTemplates
import practice.english.n2l.database.bao.LastPageTemplates
import practice.english.n2l.database.bao.MultiTemplates
import practice.english.n2l.database.bao.MyVideos
import practice.english.n2l.database.bao.QuestionInfo
import practice.english.n2l.database.bao.Questions
import practice.english.n2l.database.bao.SingleTemplates
import practice.english.n2l.database.bao.StudentProfile
import practice.english.n2l.database.bao.UserDetail
import practice.english.n2l.database.dao.AnswerInfoDao
import practice.english.n2l.database.dao.AnswersDao
import practice.english.n2l.database.dao.AppExceptionLogDao
import practice.english.n2l.database.dao.AttemptPracticeDao
import practice.english.n2l.database.dao.AudioDao
import practice.english.n2l.database.dao.BlankTemplatesDao
import practice.english.n2l.database.dao.LastPageTemplatesDao
import practice.english.n2l.database.dao.MultiTemplatesDao
import practice.english.n2l.database.dao.MyVideosDao
import practice.english.n2l.database.dao.QuestionInfoDao
import practice.english.n2l.database.dao.QuestionsDao
import practice.english.n2l.database.dao.SingleTemplatesDao
import practice.english.n2l.database.dao.StudentProfileDao
import practice.english.n2l.database.dao.UserDetailDao
import practice.english.n2l.util.TimestampConverter

@Database(entities = [AppExceptionLog::class,UserDetail::class,QuestionInfo::class, Questions::class,
                     AnswerInfo::class,Answers::class,AttemptPractice::class,Audio::class,
                     SingleTemplates::class,MultiTemplates::class, BlankTemplates::class,
                     StudentProfile::class,MyVideos::class,LastPageTemplates::class], version = 1, exportSchema = false)
@TypeConverters(TimestampConverter::class)
abstract class AppDatabase : RoomDatabase() {
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        fun getInstance(ctx: Context): AppDatabase {
            if (INSTANCE != null) return INSTANCE!!
            synchronized(this) {
                INSTANCE = Room.databaseBuilder(ctx.applicationContext, AppDatabase::class.java, "EasyLearning").build()
                return INSTANCE!!
            }
        }
    }
    abstract fun appExceptionLogDao()       : AppExceptionLogDao
    abstract fun userDetailDao()            : UserDetailDao
    abstract fun questionInfoDao()          : QuestionInfoDao
    abstract fun questionsDao()             : QuestionsDao
    abstract fun answerInfoDao()            : AnswerInfoDao
    abstract fun answersDao()               : AnswersDao
    abstract fun attemptPracticeDao()       : AttemptPracticeDao
    abstract fun audioDao()                 : AudioDao
    abstract fun singleTemplatesDao()       : SingleTemplatesDao
    abstract fun multiTemplatesDao()        : MultiTemplatesDao
    abstract fun blankTemplatesDao()        : BlankTemplatesDao
    abstract fun studentProfileDao()        : StudentProfileDao
    abstract fun myVideosDao()              : MyVideosDao
    abstract fun lastPageTemplatesDao()     : LastPageTemplatesDao
}