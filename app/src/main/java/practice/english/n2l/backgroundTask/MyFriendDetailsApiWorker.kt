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
import practice.english.n2l.database.bao.FriendList
import practice.english.n2l.database.bao.MyFriendMessageEvent
import org.greenrobot.eventbus.EventBus
//Muralidhar Mantripragada
class MyFriendDetailsApiWorker(private val context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    private val compositeDisposable = CompositeDisposable()
    override  fun doWork(): Result {
        return try {
            val appDatabase         =   AppDatabase.getInstance(applicationContext)
            val user =appDatabase.userDetailDao().getUserInfo()
            val jsonParams = JsonObject()
            jsonParams.addProperty("userid",  user.userId)
            val disposable = RetrofitService.service.getFriendListRx(jsonParams)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        when (it.responseCode) {
                            800 ->{
                                val mainUrl=it.responseData.mainUrl[0]
                                val friendList=it.responseData.friendList
                                friendList.stream().forEach { e->e.photoPath=mainUrl.url+e.photoPath}
                                sendResultToUI(friendList.toMutableList())
                            }
                            in 300.. 302 ->{
                                val friendList= mutableListOf<FriendList>()
                                sendResultToUI(friendList)
                            }
                            500 ->{ val friendList= mutableListOf<FriendList>()
                                sendResultToUI(friendList)}
                            else ->{ val friendList= mutableListOf<FriendList>()
                                sendResultToUI(friendList)}
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
            Result.success()
        }
        catch (e: Exception) {
            Log.e("NetworkRequestWorker", "Exception: ${e.message}")
            compositeDisposable.clear()
            compositeDisposable.dispose()
            Result.failure()
        }
    }
    private fun sendResultToUI(friendList: MutableList<FriendList>) {
        // Send the response data to the UI using a callback or any other mechanism
        // Example: use LiveData, EventBus, or callbacks
        // In this example, we'll use a simple callback
        //(applicationContext as? PointDetailsApiWorkerImp)?.onResultReceivedPointDetailsApiWorker(pointData,pointDetails)
        EventBus.getDefault().postSticky(MyFriendMessageEvent(friendList))
    }
    override fun onStopped() {
        compositeDisposable.clear()
        compositeDisposable.dispose()
        super.onStopped()
    }
}