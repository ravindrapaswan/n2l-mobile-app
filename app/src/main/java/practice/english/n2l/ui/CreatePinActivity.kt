package practice.english.n2l.ui

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.util.Log
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import practice.english.n2l.R
import practice.english.n2l.api.ResponseHandle
import practice.english.n2l.api.RetrofitService
import practice.english.n2l.bridge.SuccessImp
import practice.english.n2l.database.bao.OTPInfo
import practice.english.n2l.databinding.ActivityCreatePinBinding
import practice.english.n2l.dialog.CustomProgressDialog
import practice.english.n2l.dialog.ErrorDialog
import practice.english.n2l.dialog.NoInternetConnection
import practice.english.n2l.dialog.SuccessDialog
import practice.english.n2l.dialog.WarningDialog
import practice.english.n2l.repository.PracticeRepo
import practice.english.n2l.repository.PracticeRepoImp
import practice.english.n2l.repository.UserAuthRepo
import practice.english.n2l.repository.UserAuthRepoImp
import practice.english.n2l.util.Constants
import practice.english.n2l.util.Utilities
import practice.english.n2l.viewmodel.PracticeViewModel
import practice.english.n2l.viewmodel.UserAuthViewModel
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.net.URL


class CreatePinActivity : BaseActivity() {
    private lateinit var oTPInfo    : OTPInfo
    private val repository          : UserAuthRepo by lazy { UserAuthRepoImp(RetrofitService.service,this) }
    private val repository1         : PracticeRepo by lazy { PracticeRepoImp(RetrofitService.service,this) }
    private val viewModel           : UserAuthViewModel by lazy { ViewModelProvider(this, UserAuthViewModel.Factory(repository))[UserAuthViewModel::class.java] }
    private val practiceViewModel   : PracticeViewModel by lazy { ViewModelProvider(this, PracticeViewModel.Factory(repository1))[PracticeViewModel::class.java] }
    private lateinit var binding    : ActivityCreatePinBinding
    private val progressDialog      : CustomProgressDialog by lazy { CustomProgressDialog(this) }
    private var activityContext     : String?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_create_pin)
        binding.userAuthViewModel = viewModel
        binding.lifecycleOwner = this

        val gson = Gson()
        val type = object : TypeToken<OTPInfo?>() {}.type

        oTPInfo = gson.fromJson(intent.getStringExtra("OTPInfoDesc"),type)
        activityContext=intent.getStringExtra("SourceActivity")
        viewModel.passingDataToViewModel(oTPInfo)

        viewModel.userRegistration.observe(this) {
            when(it) {
                is ResponseHandle.Loading->{progressDialog.startLoading()}
                is ResponseHandle.Success-> {
                    when (it.data!!.responseCode) {
                        800 -> {
                            val list=it.data.responseData.userDetail
                            list.userName=oTPInfo.userName
                            list.userNickName=oTPInfo.userNickName
                            list.userMobileNumber=oTPInfo.userMobileNumber
                            list.dateTimeStamp=Utilities.getCurrentDateTime()
                            list.pinNo=binding.EtPIN.text.toString().trim()

                            /*Start Save Resource Data*/
                            val mainUrl=it.data.responseData.resourceData.MainUrl[0]
                            val audioResourceDataList=it.data.responseData.resourceData.audio
                            val singleTemplatesResourceDataList=it.data.responseData.resourceData.singleTemplates
                            val multiTemplatesResourceDataList=it.data.responseData.resourceData.multiTemplates
                            val blankTemplatesResourceDataList=it.data.responseData.resourceData.blankTemplates
                            val lastPageTemplatesResourceDataList=it.data.responseData.resourceData.lastPageTemplates
                            val studentProfile=it.data.responseData.studentProfile

                            CoroutineScope(Dispatchers.IO).launch {
                                deleteAllFolderData()
                                viewModel.insertUserDetails(list)

                                withContext(Dispatchers.IO) {
                                    audioResourceDataList.stream().forEach{e -> e.fileData = downloadResourceData(mainUrl.url+e.url)}
                                    singleTemplatesResourceDataList.stream().forEach{e -> e.fileData    = downloadResourceData(mainUrl.url+e.url)}
                                    multiTemplatesResourceDataList.stream().forEach{e -> e.fileData     = downloadResourceData(mainUrl.url+e.url)}
                                    blankTemplatesResourceDataList.stream().forEach{e -> e.fileData     = downloadResourceData(mainUrl.url+e.url)}
                                    lastPageTemplatesResourceDataList.stream().forEach{e -> e.fileData  = downloadResourceData(mainUrl.url+e.url)}
                                    studentProfile.photoData = downloadResourceData(studentProfile.photoPath)
                                }
                                withContext(Dispatchers.IO) {
                                    audioResourceDataList.stream().forEach{e -> e.fileLocalPath             = writeMasterResourceAudioDataInInternalMemory(e.name,e.fileType,e.fileData)}
                                    singleTemplatesResourceDataList.stream().forEach{e -> e.fileLocalPath   = writeMasterResourceImageDataInInternalMemory("single",e.name,e.fileType,e.fileData)}
                                    multiTemplatesResourceDataList.stream().forEach{e -> e.fileLocalPath    = writeMasterResourceImageDataInInternalMemory("multi",e.name,e.fileType,e.fileData)}
                                    blankTemplatesResourceDataList.stream().forEach{e -> e.fileLocalPath    = writeMasterResourceImageDataInInternalMemory("blank",e.name,e.fileType,e.fileData)}
                                    lastPageTemplatesResourceDataList.stream().forEach{e -> e.fileLocalPath = writeMasterResourceImageDataInInternalMemory("last",e.name,e.fileType,e.fileData)}
                                    studentProfile.photoLocalPath = writeMasterResourceImageDataInInternalMemory(studentProfile.userId.toString(),"jpg",studentProfile.photoData)

                                    /*audioResourceDataList.stream().forEach{e -> e.fileLocalPath             = writeMasterResourceAudioDataInInternalMemory(e.name,e.fileType,e.fileData)}
                                    singleTemplatesResourceDataList.stream().forEach{e -> e.fileLocalPath   = writeMasterResourceImageDataInInternalMemory(e.name,e.fileType,e.fileData)}
                                    multiTemplatesResourceDataList.stream().forEach{e -> e.fileLocalPath    = writeMasterResourceImageDataInInternalMemory(e.name,e.fileType,e.fileData)}
                                    blankTemplatesResourceDataList.stream().forEach{e -> e.fileLocalPath    = writeMasterResourceImageDataInInternalMemory(e.name,e.fileType,e.fileData)}
                                    lastPageTemplatesResourceDataList.stream().forEach{e -> e.fileLocalPath = writeMasterResourceImageDataInInternalMemory(e.name,e.fileType,e.fileData)}
                                    studentProfile.photoLocalPath = writeMasterResourceImageDataInInternalMemory(studentProfile.userId.toString(),"jpg",studentProfile.photoData)*/
                                    //studentProfile.stream().forEach{e ->e.photoLocalPath = writeMasterResourceImageDataInInternalMemory(e.userId.toString(),"jpg",e.photoData)}
                                }
                                practiceViewModel.insertAllAudio(audioResourceDataList)
                                practiceViewModel.insertAllSingleTemplates(singleTemplatesResourceDataList)
                                practiceViewModel.insertAllMultiTemplates(multiTemplatesResourceDataList)
                                practiceViewModel.insertAllBlankTemplates(blankTemplatesResourceDataList)
                                practiceViewModel.insertAllLastPageTemplates(lastPageTemplatesResourceDataList)
                                practiceViewModel.insertAllStudentProfile(studentProfile)


                                progressDialog.dismissLoading()
                                SuccessDialog(this@CreatePinActivity,Constants.Information,it.data.responseMessage,object:SuccessImp{
                                    override fun onYes(context: Activity, dialog: Dialog?) {
                                        dialog!!.dismiss()
                                        startActivity(
                                            Intent().setClassName(getString(R.string.project_package_name), "practice.english.n2l.ui.LoginActivity")
                                                .apply {
                                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                                }
                                        )
                                        finish()
                                    }
                                }).show(supportFragmentManager,SuccessDialog.TAG)
                            }

                            /*End  Save Resource Data*/

                        }
                        801 -> {
                            val list=it.data.responseData.userDetail
                            list.userName=oTPInfo.userName
                            list.userNickName=oTPInfo.userNickName
                            list.userMobileNumber=oTPInfo.userMobileNumber
                            list.dateTimeStamp=Utilities.getCurrentDateTime()
                            list.pinNo=binding.EtPIN.text.toString().trim()

                            /*Start Save Resource Data*/
                            val mainUrl=it.data.responseData.resourceData.MainUrl[0]
                            val audioResourceDataList=it.data.responseData.resourceData.audio
                            val singleTemplatesResourceDataList=it.data.responseData.resourceData.singleTemplates
                            val multiTemplatesResourceDataList=it.data.responseData.resourceData.multiTemplates
                            val blankTemplatesResourceDataList=it.data.responseData.resourceData.blankTemplates
                            val lastPageTemplatesResourceDataList=it.data.responseData.resourceData.lastPageTemplates
                            val studentProfile=it.data.responseData.studentProfile

                            CoroutineScope(Dispatchers.IO).launch {
                                deleteAllFolderData()
                                viewModel.insertUserDetails(list)
                                withContext(Dispatchers.IO) {
                                    audioResourceDataList.stream().forEach{e -> e.fileData = downloadResourceData(mainUrl.url+e.url)}
                                    singleTemplatesResourceDataList.stream().forEach{e -> e.fileData    = downloadResourceData(mainUrl.url+e.url)}
                                    multiTemplatesResourceDataList.stream().forEach{e -> e.fileData     = downloadResourceData(mainUrl.url+e.url)}
                                    blankTemplatesResourceDataList.stream().forEach{e -> e.fileData     = downloadResourceData(mainUrl.url+e.url)}
                                    lastPageTemplatesResourceDataList.stream().forEach{e -> e.fileData  = downloadResourceData(mainUrl.url+e.url)}
                                    studentProfile.photoData = downloadResourceData(studentProfile.photoPath)
                                }
                                withContext(Dispatchers.IO) {
                                    audioResourceDataList.stream().forEach{e -> e.fileLocalPath             = writeMasterResourceAudioDataInInternalMemory(e.name,e.fileType,e.fileData)}
                                    singleTemplatesResourceDataList.stream().forEach{e -> e.fileLocalPath   = writeMasterResourceImageDataInInternalMemory("single",e.name,e.fileType,e.fileData)}
                                    multiTemplatesResourceDataList.stream().forEach{e -> e.fileLocalPath    = writeMasterResourceImageDataInInternalMemory("multi",e.name,e.fileType,e.fileData)}
                                    blankTemplatesResourceDataList.stream().forEach{e -> e.fileLocalPath    = writeMasterResourceImageDataInInternalMemory("blank",e.name,e.fileType,e.fileData)}
                                    lastPageTemplatesResourceDataList.stream().forEach{e -> e.fileLocalPath = writeMasterResourceImageDataInInternalMemory("last",e.name,e.fileType,e.fileData)}
                                    studentProfile.photoLocalPath = writeMasterResourceImageDataInInternalMemory(studentProfile.userId.toString(),"jpg",studentProfile.photoData)
                                }
                                practiceViewModel.insertAllAudio(audioResourceDataList)
                                practiceViewModel.insertAllSingleTemplates(singleTemplatesResourceDataList)
                                practiceViewModel.insertAllMultiTemplates(multiTemplatesResourceDataList)
                                practiceViewModel.insertAllBlankTemplates(blankTemplatesResourceDataList)
                                practiceViewModel.insertAllLastPageTemplates(lastPageTemplatesResourceDataList)
                                practiceViewModel.insertAllStudentProfile(studentProfile)

                                progressDialog.dismissLoading()
                                SuccessDialog(this@CreatePinActivity,Constants.Information,it.data.responseMessage,object:SuccessImp{
                                    override fun onYes(context: Activity, dialog: Dialog?) {
                                        dialog!!.dismiss()
                                        startActivity(
                                            Intent().setClassName(getString(R.string.project_package_name), "practice.english.n2l.ui.LoginActivity")
                                                .apply {
                                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                                }
                                        )
                                        finish()
                                    }
                                }).show(supportFragmentManager,SuccessDialog.TAG)
                            }

                            /*End  Save Resource Data*/
                        }
                        300 -> {
                            progressDialog.dismissLoading()
                            WarningDialog(this, Constants.Information,it.data.responseMessage).show(supportFragmentManager,
                                WarningDialog.TAG)
                        }
                        500 -> {
                            progressDialog.dismissLoading()
                            WarningDialog(this, Constants.Information,it.data.responseMessage).show(supportFragmentManager,
                                WarningDialog.TAG)
                        }
                        else->{
                            progressDialog.dismissLoading()
                            WarningDialog(this, Constants.Information,it.data.responseMessage).show(supportFragmentManager,
                                WarningDialog.TAG)
                        }
                    }
                }
                is ResponseHandle.Error->{
                    progressDialog.dismissLoading()
                    ErrorDialog(this, Constants.Error, Constants.ServerFailure).show(supportFragmentManager,
                        ErrorDialog.TAG)
                }
                is ResponseHandle.Network->{
                    NoInternetConnection(this, Constants.Information, Constants.NetworkMessage).show(supportFragmentManager,
                        NoInternetConnection.TAG)
                }
            }
        }
        binding.BtnBack.setOnClickListener {
            if(activityContext!=null)
            {
                when(activityContext) {
                    "RegistrationActivity"->{
                        val intent = Intent(this@CreatePinActivity, RegistrationActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        startActivity(intent)
                        finish()
                    }
                    "LoginActivity"->{
                        val intent = Intent(this@CreatePinActivity, LoginActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        startActivity(intent)
                        finish()
                    }
                }
            }
        }

        binding.EtPIN.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(p0?.length==4) Utilities.hideKeyboard(binding.EtPIN,this@CreatePinActivity)
            }
            override fun afterTextChanged(p0: Editable?) {

            }
        })
        binding.EtRePIN.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(p0?.length==4) Utilities.hideKeyboard(binding.EtRePIN,this@CreatePinActivity)
            }
            override fun afterTextChanged(p0: Editable?) {

            }
        })
    }
    private fun deleteAllFolderData() {
        val assetsDir = File(filesDir, "assets")
        if (assetsDir.deleteRecursively()) assetsDir.mkdir()
    }
    private fun downloadResourceData(url: String?): String {
        try {
            val imageUrl = URL(url)
            val uCon = imageUrl.openConnection()
            val `is` = uCon.getInputStream()
            val byteArrayOutputStream = ByteArrayOutputStream()
            val buffer = ByteArray(1024)
            var read: Int
            while (`is`.read(buffer, 0, buffer.size).also { read = it } != -1) {
                byteArrayOutputStream.write(buffer, 0, read)
            }
            byteArrayOutputStream.flush()
            return Base64.encodeToString(byteArrayOutputStream.toByteArray(),0)

            //return   org.apache.commons.codec.binary.Base64.encodeBase64String(byteArrayOutputStream.toByteArray())
        } catch (e: Exception) {
            Log.d("Error", e.toString())
        }
        return ""
    }
    private fun writeMasterResourceAudioDataInInternalMemory(filename:String,fileType:String,filDataBase64:String?):String {
        try {
            val assetsDir = File(filesDir, "assets")
            if (!assetsDir.exists()) assetsDir.mkdirs()
            val assetsDirPath = assetsDir.absolutePath
            val assetsDirFile =File("$assetsDirPath/$filename.$fileType")
            val bytes: ByteArray = Base64.decode(filDataBase64, Base64.DEFAULT)
            val fOut = FileOutputStream(assetsDirFile)
            val bufferSize =1024
            var offset = 0
            while (offset < bytes.size) {
                val bytesToWrite = kotlin.math.min(bufferSize, bytes.size - offset)
                fOut.write(bytes, offset, bytesToWrite)
                offset += bytesToWrite
            }
            fOut.flush()
            fOut.close()
            return assetsDirFile.canonicalPath
        }
        catch (e: Exception) {
            Log.d("Error", e.toString())
        }
        return ""
    }
    private fun writeMasterResourceImageDataInInternalMemory(subDir:String,filename:String,fileType:String,filDataBase64:String?):String {
        try {
            val assetsDir = File(filesDir, "assets/$subDir")
            if (!assetsDir.exists()) assetsDir.mkdirs()
            val assetsDirPath = assetsDir.absolutePath
            val assetsDirFile =File("$assetsDirPath/$filename.$fileType")
            val bytes: ByteArray = Base64.decode(filDataBase64, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.count())
            val fOut = FileOutputStream(assetsDirFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut)
            fOut.flush()
            fOut.close()
            return assetsDirFile.absolutePath
        }
        catch (e: Exception) {
            Log.d("Error", e.toString())
        }
        return ""
    }
    private fun writeMasterResourceImageDataInInternalMemory(filename:String,fileType:String,filDataBase64:String?):String {
        try {
            val assetsDir = File(filesDir, "assets")
            if (!assetsDir.exists()) assetsDir.mkdirs()
            val assetsDirPath = assetsDir.absolutePath
            val assetsDirFile =File("$assetsDirPath/$filename.$fileType")
            val bytes: ByteArray = Base64.decode(filDataBase64, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.count())
            val fOut = FileOutputStream(assetsDirFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut)
            fOut.flush()
            fOut.close()
            return assetsDirFile.absolutePath
        }
        catch (e: Exception) {
            Log.d("Error", e.toString())
        }
        return ""
    }
}