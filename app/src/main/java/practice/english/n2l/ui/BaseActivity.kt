package practice.english.n2l.ui

import android.os.Bundle
import android.os.PowerManager
import android.util.Log
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import practice.english.n2l.backgroundTask.UploadApiWorker
import practice.english.n2l.database.AppDatabase
import practice.english.n2l.database.bao.AppExceptionLog
import java.util.concurrent.TimeUnit


open class BaseActivity : AppCompatActivity() {
    private val appDatabase         : AppDatabase by lazy { AppDatabase.getInstance(this) }
    private var wakeLock: PowerManager.WakeLock? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Thread.setDefaultUncaughtExceptionHandler { _, ex ->
            ex.printStackTrace();
            Log.e("Alert", ex.toString())
            CoroutineScope(Dispatchers.IO).launch{
                appDatabase.appExceptionLogDao().insert(AppExceptionLog(0,0,ex.toString(),ex.stackTraceToString()));
            }
        }
         /*wakeLock = (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyApp::MyWakelockTag").apply {
                    acquire(20*60*1000L /*10 minutes*/)
                }
            }*/
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        //setupWorkManager()
    }

    override fun onDestroy() {
        super.onDestroy()
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
    /*override fun onDestroy() {
        super.onDestroy()
        wakeLock?.let {
            if (it.isHeld) {
                it.release()
                wakeLock = null
            }
        }
    }*/
    private fun setupWorkManager() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresStorageNotLow(true)
            .setRequiresDeviceIdle(true)
            .build()
        val workerRequest=PeriodicWorkRequest.Builder(UploadApiWorker::class.java,15,TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()
        WorkManager.getInstance(this).enqueue(workerRequest)
    }
}