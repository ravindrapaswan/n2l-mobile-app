package practice.english.n2l.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.JsonObject
import practice.english.n2l.api.ApiService
import practice.english.n2l.api.ResponseHandle
import practice.english.n2l.api.ResponseInfo
import practice.english.n2l.database.AppDatabase
import practice.english.n2l.database.bao.StudentProfile
import practice.english.n2l.util.Constants
import practice.english.n2l.util.NetworkUtils
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileRepoImp(private val apiService: ApiService, private val mContext: Context): ProfileRepo {
    private var appDatabase                         : AppDatabase? = null
    private fun initializeDB(context: Context)      : AppDatabase { return AppDatabase.getInstance(context)}
    private var sendFriendRequestLiveData           =  MutableLiveData<ResponseHandle<ResponseInfo>>()
    private var getFriendRequestsLiveData           =  MutableLiveData<ResponseHandle<ResponseInfo>>()
    private var actionFriendRequestLiveData         =  MutableLiveData<ResponseHandle<ResponseInfo>>()
    private var friendListLiveData                  =  MutableLiveData<ResponseHandle<ResponseInfo>>()
    private var updateProfileLiveData               =  MutableLiveData<ResponseHandle<ResponseInfo>>()
    private var unfriendLiveData                    =  MutableLiveData<ResponseHandle<ResponseInfo>>()

    val  friendRequest : LiveData<ResponseHandle<ResponseInfo>>
    get() = sendFriendRequestLiveData
    override suspend fun sendFriendRequest(jsonParams: JsonObject){
        if(NetworkUtils.isInternetAvailable(mContext))
        {
            sendFriendRequestLiveData.postValue(ResponseHandle.Loading())
            try
            {
                Log.d("Request URL : ", Constants.URL + "sendFriendRequest")
                Log.d("Request Data", "onRequestBody: $jsonParams")
                apiService.sendFriendRequest(jsonParams).enqueue(object : Callback<ResponseInfo> {
                    override fun onResponse(call: Call<ResponseInfo>, response: Response<ResponseInfo>) {
                        sendFriendRequestLiveData.postValue(ResponseHandle.Success(response.body()))
                        Log.d("Response", "onResponseBody: " + response.body())
                    }
                    override fun onFailure(call: Call<ResponseInfo>, t: Throwable) {
                        sendFriendRequestLiveData.postValue(ResponseHandle.Error(t.message.toString()))
                        Log.d("ResponseFailure", "onFailureMessage: " + t.message!!)
                    }
                })
            }
            catch (ex:Exception)
            {
                sendFriendRequestLiveData.postValue(ResponseHandle.Error(ex.message.toString()))
            }
        }
        else
            sendFriendRequestLiveData.postValue(ResponseHandle.Network())
    }

    val  getFriendRequests : LiveData<ResponseHandle<ResponseInfo>>
    get() = getFriendRequestsLiveData
    override suspend fun getFriendRequests(jsonParams: JsonObject){
        if(NetworkUtils.isInternetAvailable(mContext))
        {
            getFriendRequestsLiveData.postValue(ResponseHandle.Loading())
            try
            {
                Log.d("Request URL : ", Constants.URL + "getFriendRequests")
                Log.d("Request Data", "onRequestBody: $jsonParams")
                apiService.getFriendRequests(jsonParams).enqueue(object : Callback<ResponseInfo> {
                    override fun onResponse(call: Call<ResponseInfo>, response: Response<ResponseInfo>) {
                        getFriendRequestsLiveData.postValue(ResponseHandle.Success(response.body()))
                        Log.d("Response", "onResponseBody: " + response.body())
                    }
                    override fun onFailure(call: Call<ResponseInfo>, t: Throwable) {
                        getFriendRequestsLiveData.postValue(ResponseHandle.Error(t.message.toString()))
                        Log.d("ResponseFailure", "onFailureMessage: " + t.message!!)
                    }
                })
            }
            catch (ex:Exception)
            {
                getFriendRequestsLiveData.postValue(ResponseHandle.Error(ex.message.toString()))
            }
        }
        else
            getFriendRequestsLiveData.postValue(ResponseHandle.Network())
    }

    val  actionFriendRequest : LiveData<ResponseHandle<ResponseInfo>>
    get() = actionFriendRequestLiveData
    override suspend fun getActionFriendRequest(jsonParams: JsonObject){
        if(NetworkUtils.isInternetAvailable(mContext))
        {
            actionFriendRequestLiveData.postValue(ResponseHandle.Loading())
            try
            {
                Log.d("Request URL : ", Constants.URL + "actionFriendRequest")
                Log.d("Request Data", "onRequestBody: $jsonParams")
                apiService.actionFriendRequest(jsonParams).enqueue(object : Callback<ResponseInfo> {
                    override fun onResponse(call: Call<ResponseInfo>, response: Response<ResponseInfo>) {
                        actionFriendRequestLiveData.postValue(ResponseHandle.Success(response.body()))
                        Log.d("Response", "onResponseBody: " + response.body())
                    }
                    override fun onFailure(call: Call<ResponseInfo>, t: Throwable) {
                        actionFriendRequestLiveData.postValue(ResponseHandle.Error(t.message.toString()))
                        Log.d("ResponseFailure", "onFailureMessage: " + t.message!!)
                    }
                })
            }
            catch (ex:Exception)
            {
                actionFriendRequestLiveData.postValue(ResponseHandle.Error(ex.message.toString()))
            }
        }
        else
            actionFriendRequestLiveData.postValue(ResponseHandle.Network())
    }

    val  friendList : LiveData<ResponseHandle<ResponseInfo>>
    get() = friendListLiveData
    override suspend fun getFriendList(jsonParams: JsonObject){
        if(NetworkUtils.isInternetAvailable(mContext))
        {
            friendListLiveData.postValue(ResponseHandle.Loading())
            try
            {
                Log.d("Request URL : ", Constants.URL + "getfriendList")
                Log.d("Request Data", "onRequestBody: $jsonParams")
                apiService.getFriendList(jsonParams).enqueue(object : Callback<ResponseInfo> {
                    override fun onResponse(call: Call<ResponseInfo>, response: Response<ResponseInfo>) {
                        friendListLiveData.postValue(ResponseHandle.Success(response.body()))
                        Log.d("Response", "onResponseBody: " + response.body())
                    }
                    override fun onFailure(call: Call<ResponseInfo>, t: Throwable) {
                        friendListLiveData.postValue(ResponseHandle.Error(t.message.toString()))
                        Log.d("ResponseFailure", "onFailureMessage: " + t.message!!)
                    }
                })
            }
            catch (ex:Exception)
            {
                friendListLiveData.postValue(ResponseHandle.Error(ex.message.toString()))
            }
        }
        else
            friendListLiveData.postValue(ResponseHandle.Network())
    }

    val updateProfile : LiveData<ResponseHandle<ResponseInfo>>
    get() = updateProfileLiveData
    override suspend fun updateProfile(userid:RequestBody, name:RequestBody, dob:RequestBody, email: RequestBody, profilePhoto: MultipartBody.Part){
        if(NetworkUtils.isInternetAvailable(mContext))
        {
            updateProfileLiveData.postValue(ResponseHandle.Loading())
            try {
                Log.d("Request URL : ", Constants.URL + "updateProfile")
                apiService.updateProfile(userid,name,dob,email,profilePhoto).enqueue(object : Callback<ResponseInfo> {
                    override fun onResponse(call: Call<ResponseInfo>, response: Response<ResponseInfo>) {
                        updateProfileLiveData.postValue(ResponseHandle.Success(response.body()))
                        Log.d("Response", "onResponseBody: " + response.body())
                    }
                    override fun onFailure(call: Call<ResponseInfo>, t: Throwable) {
                        updateProfileLiveData.postValue(ResponseHandle.Error(t.message.toString()))
                        Log.d("ResponseFailure", "onFailureMessage: " + t.message!!)
                    }
                })
            }
            catch (ex:Exception)
            {
                updateProfileLiveData.postValue(ResponseHandle.Error(ex.message.toString()))
            }
        }
        else
            updateProfileLiveData.postValue(ResponseHandle.Network())
    }
    val  unfriend : LiveData<ResponseHandle<ResponseInfo>>
    get() = unfriendLiveData
    override suspend fun getUnfriend( jsonParams: JsonObject){
        if(NetworkUtils.isInternetAvailable(mContext))
        {
            unfriendLiveData.postValue(ResponseHandle.Loading())
            try
            {
                Log.d("Request URL : ", Constants.URL + "unfriend")
                Log.d("Request Data", "onRequestBody: $jsonParams")
                apiService.getUnfriend(jsonParams).enqueue(object : Callback<ResponseInfo> {
                    override fun onResponse(call: Call<ResponseInfo>, response: Response<ResponseInfo>) {
                        unfriendLiveData.postValue(ResponseHandle.Success(response.body()))
                        Log.d("Response", "onResponseBody: " + response.body())
                    }
                    override fun onFailure(call: Call<ResponseInfo>, t: Throwable) {
                        unfriendLiveData.postValue(ResponseHandle.Error(t.message.toString()))
                        Log.d("ResponseFailure", "onFailureMessage: " + t.message!!)
                    }
                })
            }
            catch (ex:Exception)
            {
                unfriendLiveData.postValue(ResponseHandle.Error(ex.message.toString()))
            }
        }
        else
            unfriendLiveData.postValue(ResponseHandle.Network())
    }

    override fun getStudentProfileLiveData():LiveData<StudentProfile> {
        appDatabase = initializeDB(mContext)
        return appDatabase!!.studentProfileDao().getStudentProfileLiveData()
    }
    override suspend fun updateStudentProfile(name: String?, emailId: String?, photoLocalPath: String, userId: Long): Int {
        appDatabase = initializeDB(mContext)
        return appDatabase!!.studentProfileDao().updateStudentProfile(name, emailId, photoLocalPath, userId)
    }
    override suspend fun updateName(name:String){
        appDatabase = initializeDB(mContext)
        return appDatabase!!.userDetailDao().updateName(name)
    }
}
interface  ProfileRepo{
    suspend fun sendFriendRequest(jsonParams: JsonObject)
    suspend fun getFriendRequests(jsonParams: JsonObject)
    suspend fun getActionFriendRequest(jsonParams: JsonObject)
    suspend fun getFriendList(jsonParams: JsonObject)
    fun getStudentProfileLiveData():LiveData<StudentProfile>
    suspend fun updateProfile(userid:RequestBody, name:RequestBody, dob:RequestBody, email: RequestBody, profilePhoto: MultipartBody.Part)
    suspend fun updateStudentProfile(name:String?, emailId:String?,photoLocalPath:String,userId: Long):Int
    suspend fun getUnfriend( jsonParams: JsonObject)
    suspend fun updateName(name:String)
}