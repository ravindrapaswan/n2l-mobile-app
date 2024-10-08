package practice.english.n2l.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import practice.english.n2l.api.ResponseHandle
import practice.english.n2l.api.ResponseInfo
import practice.english.n2l.database.bao.StudentProfile
import practice.english.n2l.database.bao.UserDetail
import practice.english.n2l.repository.ProfileRepo
import practice.english.n2l.repository.ProfileRepoImp
import practice.english.n2l.validator.LiveDataValidator
import practice.english.n2l.validator.LiveDataValidatorResolver
import okhttp3.MultipartBody
import okhttp3.RequestBody

class ProfileViewModel(private val repository: ProfileRepo): ViewModel() {
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO

    val friendInfo = MutableLiveData<String>()

    val mobileNum = MutableLiveData<String>()
    val mobileNumValidator = LiveDataValidator(mobileNum).apply {
        addRule("Mobile Number is required") { it.isNullOrBlank() }
        addRule("Mobile Number must be 10 digit") { it?.length != 10 }
    }

    fun onSendFriendRequest(userDetail: UserDetail) {
        val validators = mutableListOf<LiveDataValidator>()
        validators.addAll(mutableListOf(mobileNumValidator))
        val validatorResolver = LiveDataValidatorResolver(validators)
        if(validatorResolver.isValid()) {
            friendInfo.value=mobileNum.value
            val jsonParams = JsonObject()
            jsonParams.addProperty("userid",userDetail.userId)
            jsonParams.addProperty("friendMobileNumber", mobileNum.value!!.trim())
            sendFriendRequest(jsonParams)
        }
    }

    val  friendRequest : LiveData<ResponseHandle<ResponseInfo>>
    get() = (repository as ProfileRepoImp).friendRequest
    fun sendFriendRequest(jsonParams: JsonObject) {
        CoroutineScope(ioDispatcher).launch{
            repository.sendFriendRequest(jsonParams)
        }
    }

    val  getFriendRequest : LiveData<ResponseHandle<ResponseInfo>>
    get() = (repository as ProfileRepoImp).getFriendRequests
    fun getFriendRequests(jsonParams: JsonObject) {
        CoroutineScope(ioDispatcher).launch{
            repository.getFriendRequests(jsonParams)
        }
    }

    val  actionFriendRequest : LiveData<ResponseHandle<ResponseInfo>>
    get() = (repository as ProfileRepoImp).actionFriendRequest
    fun actionFriendRequest(jsonParams: JsonObject) {
        CoroutineScope(ioDispatcher).launch{
            repository.getActionFriendRequest(jsonParams)
        }
    }

    val  friendList : LiveData<ResponseHandle<ResponseInfo>>
    get() = (repository as ProfileRepoImp).friendList
    fun getFriendList(jsonParams: JsonObject) {
        CoroutineScope(ioDispatcher).launch{
            repository.getFriendList(jsonParams)
        }
    }

    val  unfriend : LiveData<ResponseHandle<ResponseInfo>>
    get() = (repository as ProfileRepoImp).unfriend
    fun getUnfriend(jsonParams: JsonObject) {
        CoroutineScope(ioDispatcher).launch{
            repository.getUnfriend(jsonParams)
        }
    }

    fun getStudentProfileLiveData():LiveData<StudentProfile> = repository.getStudentProfileLiveData()

    val  updateProfile : LiveData<ResponseHandle<ResponseInfo>>
    get() = (repository as ProfileRepoImp).updateProfile
     fun updateProfile(userid:RequestBody, name:RequestBody, dob:RequestBody, email: RequestBody, profilePhoto: MultipartBody.Part) {
         CoroutineScope(ioDispatcher).launch {
             repository.updateProfile(userid,name,dob, email, profilePhoto)
         }
    }

   suspend fun updateStudentProfile(name: String?, emailId: String?, photoLocalPath: String, userId: Long) {
            repository.updateStudentProfile(name, emailId, photoLocalPath, userId)
    }
    suspend fun updateName(name:String) {
        repository.updateName(name)
    }

    class Factory(private val repository: ProfileRepo) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
                ProfileViewModel(repository) as T
            } else {
                throw IllegalArgumentException("ViewModel Not Found")
            }
        }
    }
}