package practice.english.n2l.util
import android.util.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import practice.english.n2l.util.Constants.Status
import java.util.concurrent.Executors

abstract class CoroutinesAsyncTask<Params, Progress, Result>(val taskName: String) {

    val TAG: String by lazy {
        CoroutinesAsyncTask::class.java.simpleName
    }

    companion object {
        private var threadPoolExecutor: CoroutineDispatcher? = null
    }

    var status: Status = Status.PENDING
    private var preJob: Job? = null
    private var bgJob: Deferred<Result>? = null
    abstract fun doInBackground(vararg params: Params?): Result
    open fun onProgressUpdate(vararg values: Progress?) {}
    open fun onPostExecute(result: Result?) {}
    open fun onPreExecute() {}
    open fun onCancelled(result: Result?) {}
    private var isCancelled = false

    /**
     * Executes background task parallel with other background tasks in the queue using
     * default thread pool
     */
    fun execute(vararg params: Params?) {
        execute(Dispatchers.Default, *params)
    }

    /**
     * Executes background tasks sequentially with other background tasks in the queue using
     * single thread executor @Executors.newSingleThreadExecutor().
     */
    fun executeOnExecutor(vararg params: Params?) {
        if (threadPoolExecutor == null) {
            threadPoolExecutor = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
        }
        execute(threadPoolExecutor!!, *params)
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun execute(dispatcher: CoroutineDispatcher, vararg params: Params?) {

        if (status != Status.PENDING) {
            when (status) {
                Status.RUNNING -> throw IllegalStateException("Cannot execute task:" + " the task is already running.")
                Status.FINISHED -> throw IllegalStateException("Cannot execute task:"
                        + " the task has already been executed "
                        + "(a task can be executed only once)")
                else -> {
                }
            }
        }

        status = Status.RUNNING

        // it can be used to setup UI - it should have access to Main Thread
        GlobalScope.launch(Dispatchers.Main) {
            preJob = launch(Dispatchers.Main) {
                printLog("$taskName onPreExecute started")
                onPreExecute()
                printLog("$taskName onPreExecute finished")
                bgJob = async(dispatcher) {
                    printLog("$taskName doInBackground started")
                    doInBackground(*params)
                }
            }
            preJob!!.join()
            if (!isCancelled) {
                withContext(Dispatchers.Main) {
                    onPostExecute(bgJob!!.await())
                    printLog("$taskName doInBackground finished")
                    status = Status.FINISHED
                }
            }
        }
    }

    fun cancel(mayInterruptIfRunning: Boolean) {
        if (preJob == null || bgJob == null) {
            printLog("$taskName has already been cancelled/finished/not yet started.")
            return
        }
        if (mayInterruptIfRunning || (!preJob!!.isActive && !bgJob!!.isActive)) {
            isCancelled = true
            status = Status.FINISHED
            if (bgJob!!.isCompleted) {
                GlobalScope.launch(Dispatchers.Main) {
                    onCancelled(bgJob!!.await())
                }
            }
            preJob?.cancel(CancellationException("PreExecute: Coroutine Task cancelled"))
            bgJob?.cancel(CancellationException("doInBackground: Coroutine Task cancelled"))
            printLog("$taskName has been cancelled.")
        }
    }

    fun publishProgress(vararg progress: Progress) {
        //need to update main thread
        GlobalScope.launch(Dispatchers.Main) {
            if (!isCancelled) {
                onProgressUpdate(*progress)
            }
        }
    }

    private fun printLog(message: String) {
        Log.d(TAG, message)
    }
}