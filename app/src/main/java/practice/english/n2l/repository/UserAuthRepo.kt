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
import practice.english.n2l.database.bao.UserDetail
import practice.english.n2l.util.Constants
import practice.english.n2l.util.NetworkUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserAuthRepoImp(private val apiService: ApiService, private val mContext: Context):UserAuthRepo {

    private var appDatabase: AppDatabase? = null
    private fun initializeDB(context: Context)   : AppDatabase { return AppDatabase.getInstance(context)}
    private var generateOTPLiveData              =  MutableLiveData<ResponseHandle<ResponseInfo>>()
    private var userRegistrationLiveData         =  MutableLiveData<ResponseHandle<ResponseInfo>>()

    val generateOTP : LiveData<ResponseHandle<ResponseInfo>>
    get() = generateOTPLiveData
    override suspend fun generateOTP(jsonParams: JsonObject) {
        if(NetworkUtils.isInternetAvailable(mContext))
        {
            generateOTPLiveData.postValue(ResponseHandle.Loading())
            try
            {
                Log.d("Request URL : ", Constants.URL + "GenerateOTPApp")
                Log.d("Request Data", "onRequestBody: $jsonParams")
                apiService.generateOTP(jsonParams).enqueue(object : Callback<ResponseInfo> {
                    override fun onResponse(call: Call<ResponseInfo>, response: Response<ResponseInfo>) {
                        generateOTPLiveData.postValue(ResponseHandle.Success(response.body()))
                        Log.d("Response", "onResponseBody: " + response.body())
                    }

                    override fun onFailure(call: Call<ResponseInfo>, t: Throwable) {
                        generateOTPLiveData.postValue(ResponseHandle.Error(t.message.toString()))
                        Log.d("ResponseFailure", "onFailureMessage: " + t.message!!)
                    }
                })
            }
            catch (ex:Exception)
            {
                generateOTPLiveData.postValue(ResponseHandle.Error(ex.message.toString()))
            }
        }
        else
            generateOTPLiveData.postValue(ResponseHandle.Network())
    }


    val userRegistration : LiveData<ResponseHandle<ResponseInfo>>
    get() = userRegistrationLiveData
    override suspend fun userRegistration(jsonParams: JsonObject) {
        if(NetworkUtils.isInternetAvailable(mContext))
        {
            userRegistrationLiveData.postValue(ResponseHandle.Loading())
            try
            {
                Log.d("Request URL : ", Constants.URL + "UserRegistrationApp")
                Log.d("Request Data", "onRequestBody: $jsonParams")
                apiService.userRegistration(jsonParams).enqueue(object : Callback<ResponseInfo> {
                    override fun onResponse(call: Call<ResponseInfo>, response: Response<ResponseInfo>) {
                        userRegistrationLiveData.postValue(ResponseHandle.Success(response.body()))
                        Log.d("Response", "onResponseBody: " + response.body())
                    }

                    override fun onFailure(call: Call<ResponseInfo>, t: Throwable) {
                        userRegistrationLiveData.postValue(ResponseHandle.Error(t.message.toString()))
                        Log.d("ResponseFailure", "onFailureMessage: " + t.message!!)
                    }
                })
            }
            catch (ex:Exception)
            {
                userRegistrationLiveData.postValue(ResponseHandle.Error(ex.message.toString()))
            }
        }
        else
            userRegistrationLiveData.postValue(ResponseHandle.Network())
    }

    override suspend fun insertUserDetails(userReg: UserDetail) {
        appDatabase = initializeDB(mContext)
        appDatabase!!.userDetailDao().deleteAll()
        appDatabase!!.userDetailDao().insertUserDetails(userReg)
    }

    override fun getUserDetail(): LiveData<UserDetail> {
        appDatabase = initializeDB(mContext)
        return appDatabase!!.userDetailDao().getUserDetail()
    }
    override fun updatePin(newPin:String) {
        appDatabase = initializeDB(mContext)
        return appDatabase!!.userDetailDao().updatePin(newPin)
    }

}
interface UserAuthRepo{
    suspend fun generateOTP(jsonParams: JsonObject)
    suspend fun userRegistration(jsonParams: JsonObject)
    suspend fun insertUserDetails(userReg: UserDetail)
    fun getUserDetail():LiveData<UserDetail>
    fun updatePin(newPin:String)
}