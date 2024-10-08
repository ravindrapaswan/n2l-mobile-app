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
import practice.english.n2l.database.bao.OTPInfo
import practice.english.n2l.database.bao.UserDetail
import practice.english.n2l.repository.UserAuthRepo
import practice.english.n2l.repository.UserAuthRepoImp
import practice.english.n2l.validator.LiveDataValidator
import practice.english.n2l.validator.LiveDataValidatorResolver


class UserAuthViewModel(private val repository: UserAuthRepo): ViewModel()  {
    //https://oozou.com/blog/modern-android-form-validations-with-data-binding-147 #valdation url
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO

    private var oTPInfoLiveData   =  MutableLiveData<OTPInfo>()
    private var loginStatusLiveData   =  MutableLiveData<Boolean>()
    val fullName = MutableLiveData<String>()
    val nickName = MutableLiveData<String>()
    val mobileNum = MutableLiveData<String>()
    val referralId = MutableLiveData<String>()

    val userId = MutableLiveData<String>()
    val otp = MutableLiveData<String>()
    val pin = MutableLiveData<String>()
    val rePin = MutableLiveData<String>()

    val loginUserId = MutableLiveData<String>()
    val loginPin = MutableLiveData<String>()

    /*Start Register*/
    val fullNameValidator = LiveDataValidator(fullName).apply {
        addRule("Full Name is required") { it.isNullOrBlank() }
    }
    val nickNameValidator = LiveDataValidator(nickName).apply {
        addRule("Nick Name is required") { it.isNullOrBlank() }
    }
    val mobileNumValidator = LiveDataValidator(mobileNum).apply {
        addRule("Mobile Number is required") { it.isNullOrBlank() }
        addRule("Mobile Number must be 10 digit") { it?.length != 10 }
    }
    val referralIdValidator = LiveDataValidator(referralId).apply {
        addRule("Mobile Number length must be 10 digit") { it?.length != 10 }
    }
    /*Stop Register*/

    /*Start Create PIN*/
    val otpValidator = LiveDataValidator(otp).apply {
        addRule("OTP is required") { it.isNullOrBlank() }
    }
    val pinValidator = LiveDataValidator(pin).apply {
        addRule("PIN is required") { it.isNullOrBlank() }
        addRule("PIN Number must be 4 Digit") { it?.length != 4}
    }
    val rePinValidator = LiveDataValidator(rePin).apply {
        addRule("Re-Pin is required") { it.isNullOrBlank() }
        addRule("Re-PIN Number must be 4 Digit") { it?.length != 4}
        addRule("PIN and Re-PIN must be same") { !it.equals(pin.value)}
    }
    /*Stop Create PIN*/

    /*Start Login*/
    val loginPinValidator = LiveDataValidator(loginPin).apply {
        addRule("PIN is required") { it.isNullOrBlank() }
    }
    /*Stop Login*/

    fun onGenerateOTP() {
        val validators = mutableListOf<LiveDataValidator>()
        val mutableList1 = mutableListOf(fullNameValidator, nickNameValidator, mobileNumValidator)
        val mutableList2 = mutableListOf(fullNameValidator, nickNameValidator,mobileNumValidator,referralIdValidator)
        //val mutableList3 = mutableListOf(fullNameValidator, mobileNumValidator)
        val mutableList3 = mutableListOf(fullNameValidator, mobileNumValidator,referralIdValidator)
        val mutableList4 = mutableListOf(fullNameValidator, mobileNumValidator)

        if(!nickName.value.isNullOrBlank()) {
            if(!referralId.value.isNullOrBlank()) {
                validators.addAll(mutableList2)
            }
            else {
                validators.addAll(mutableList1)
                referralIdValidator.error.value=""
            }
        }
        else {
            nickNameValidator.error.value=""
            if(!referralId.value.isNullOrBlank()) {
                validators.addAll(mutableList3)
            }
            else {
                validators.addAll(mutableList4)
                referralIdValidator.error.value=""
            }
        }


        /*if(nickName.value.isNullOrBlank())
        {
            validators.addAll(mutableList3)
            nickNameValidator.error.value=""
        }
        else
            validators.addAll(mutableList1)

        if(referralId.value.isNullOrBlank())
        {
            validators.addAll(mutableList1)
            referralIdValidator.error.value=""
        }
        else
            validators.addAll(mutableList2)*/

        val validatorResolver = LiveDataValidatorResolver(validators)

        if(validatorResolver.isValid()) {
            val jsonParams = JsonObject()
            jsonParams.addProperty("MobileNo", mobileNum.value!!.trim())
            jsonParams.addProperty("FullName", fullName.value!!.trim())
            if(nickName.value.isNullOrBlank())
                jsonParams.addProperty("NickName", fullName.value!!.trim())
            else
                jsonParams.addProperty("NickName", nickName.value!!.trim())
            jsonParams.addProperty("MobileNo", mobileNum.value!!.trim())
            if(referralId.value.isNullOrBlank())
                jsonParams.addProperty("ReferrerId", "")
            else
                jsonParams.addProperty("ReferrerId", referralId.value!!.trim())
            generateOTP(jsonParams)
        }
    }

    val  generateOTP : LiveData<ResponseHandle<ResponseInfo>>
    get() = (repository as UserAuthRepoImp).generateOTP
    private fun generateOTP(jsonParams: JsonObject) {
        CoroutineScope(ioDispatcher).launch{
            repository.generateOTP(jsonParams)
        }
    }

    fun onCreatePin() {
        val validators = mutableListOf<LiveDataValidator>()
        validators.addAll(mutableListOf(otpValidator, pinValidator,rePinValidator))
        val validatorResolver = LiveDataValidatorResolver(validators)
        if(validatorResolver.isValid()) {
            val jsonParams = JsonObject()
            //jsonParams.addProperty("UserId", userId.value!!.trim())
            jsonParams.addProperty("UserName", oTPInfoLiveData.value!!.userName.trim())
            jsonParams.addProperty("NickName", oTPInfoLiveData.value!!.userNickName.trim())
            jsonParams.addProperty("MobileNo",  oTPInfoLiveData.value!!.userMobileNumber.trim().toLong())
            jsonParams.addProperty("PinNo", pin.value!!.trim())
            jsonParams.addProperty("OTP", otp.value!!.trim())
            jsonParams.addProperty("OtpRecordId",  oTPInfoLiveData.value!!.otpRecordId)
            //jsonParams.addProperty("ReferrerId",  oTPInfoLiveData.value!!.referralId)
            jsonParams.addProperty("ReferrerId",  0)
            jsonParams.addProperty("ApkVersion",  "1.0")
            userRegistration(jsonParams)
        }
        else
        {
            pin.value=""
            rePin.value=""
        }
    }

    val  userRegistration : LiveData<ResponseHandle<ResponseInfo>>
    get() = (repository as UserAuthRepoImp).userRegistration
    private fun userRegistration(jsonParams: JsonObject) {
        CoroutineScope(ioDispatcher).launch{
            repository.userRegistration(jsonParams)
        }
    }

    fun insertUserDetails(userReg: UserDetail) {
        CoroutineScope(Dispatchers.IO).launch  {
                repository.insertUserDetails(userReg)
        }
    }

    fun passingDataToViewModel(oTPInfo:OTPInfo) {
        oTPInfoLiveData.value=oTPInfo
        userId.value=oTPInfoLiveData.value!!.userName
    }

    fun onLogin() {
        val validators = mutableListOf<LiveDataValidator>()
        validators.addAll(mutableListOf(loginPinValidator))
        val validatorResolver = LiveDataValidatorResolver(validators)
        if(validatorResolver.isValid()) {
            //val userDetailLivedata= repository.getUserUserDetail()
            val userDetailLivedata= getUserUserDetail
            loginStatusLiveData.value = userDetailLivedata.value!!.pinNo!! == loginPin.value
        }
    }

    fun onForgotPinGenerateOTP() {
        val jsonParams = JsonObject()
        jsonParams.addProperty("MobileNo", getUserUserDetail.value!!.userMobileNumber)
        jsonParams.addProperty("ApkVersion",  "1.0")
        userRegistration(jsonParams)
        generateOTP(jsonParams)
    }

    fun updatePin(newPin:String) {
        CoroutineScope(ioDispatcher).launch{
            repository.updatePin(newPin)
        }
    }

    val getLoginStatus:MutableLiveData<Boolean> =loginStatusLiveData
    val getUserUserDetail: LiveData<UserDetail> =repository.getUserDetail()

    class Factory(private val repository: UserAuthRepo) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return if (modelClass.isAssignableFrom(UserAuthViewModel::class.java)) {
                UserAuthViewModel(repository) as T
            } else {
                throw IllegalArgumentException("ViewModel Not Found")
            }
        }
    }
}