package practice.english.n2l.backgroundTask

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.gson.JsonObject
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import practice.english.n2l.api.RetrofitService
import practice.english.n2l.database.AppDatabase
import practice.english.n2l.database.bao.PointData
import practice.english.n2l.database.bao.PointDetails
import practice.english.n2l.database.bao.PointMessageEvent
import org.greenrobot.eventbus.EventBus

class PontDetailsApiWorker(private val context: Context, workerParams: WorkerParameters) : Worker(context, workerParams)
{
    private val compositeDisposable = CompositeDisposable()
    override  fun doWork(): Result {
        return try {
            val appDatabase         =   AppDatabase.getInstance(applicationContext)
            val user =appDatabase.userDetailDao().getUserInfo()
            val jsonParams = JsonObject()
            jsonParams.addProperty("studentid",  user.userId)

            val disposable = RetrofitService.service.getPointsRx(jsonParams)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        when (it.responseCode) {
                            800 ->{
                                val pointData=it.responseData.pointData
                                val pointDetails=it.responseData.pointDetails
                                sendResultToUI(pointData,pointDetails)
                            }
                            in 300.. 302 ->{}
                            500 ->{}
                            else ->{}
                        }
                        compositeDisposable.clear()
                        compositeDisposable.dispose()
                        Log.d("Response", "onResponseBody: $it")
                    },
                    { error ->
                        compositeDisposable.clear()
                        compositeDisposable.dispose()
                        error.printStackTrace()
                        Log.d("ResponseFailure", "onFailureMessage: " + error.message!!)
                    }
                )
            compositeDisposable.add(disposable)

            /*RetrofitService.service.getPoints(jsonParams).enqueue(object :
                Callback<ResponseInfo> {
                override fun onResponse(call: Call<ResponseInfo>, response: Response<ResponseInfo>) {
                    val it = ResponseHandle.Success(response.body())
                    when (it.data!!.responseCode) {
                        800 ->{
                            val pointData=it.data.responseData.pointData
                            val pointDetails=it.data.responseData.pointDetails
                            sendResultToUI(pointData,pointDetails)
                        }
                        in 300.. 302 ->{}
                        500 ->{}
                        else ->{}
                    }
                    Log.d("Response", "onResponseBody: " + response.body())
                }
                override fun onFailure(call: Call<ResponseInfo>, t: Throwable) {
                    Log.d("ResponseFailure", "onFailureMessage: " + t.message!!)
                }
            })*/
            Result.success()
        } catch (e: Exception) {
            Log.e("NetworkRequestWorker", "Exception: ${e.message}")
            compositeDisposable.clear()
            compositeDisposable.dispose()
            Result.failure()
        }
    }

    private fun sendResultToUI(pointData: PointData, pointDetails: List<PointDetails>) {
        // Send the response data to the UI using a callback or any other mechanism
        // Example: use LiveData, EventBus, or callbacks
        // In this example, we'll use a simple callback
        //(applicationContext as? PointDetailsApiWorkerImp)?.onResultReceivedPointDetailsApiWorker(pointData,pointDetails)
        EventBus.getDefault().postSticky(PointMessageEvent(pointData,pointDetails))
    }

    override fun onStopped() {
        compositeDisposable.clear()
        compositeDisposable.dispose()
        super.onStopped()
    }
}
