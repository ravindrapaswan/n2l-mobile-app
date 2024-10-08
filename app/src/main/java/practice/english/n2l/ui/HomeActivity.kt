package practice.english.n2l.ui

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import practice.english.n2l.R
import practice.english.n2l.api.ResponseHandle
import practice.english.n2l.api.RetrofitService
import practice.english.n2l.backgroundTask.PontDetailsApiWorker
import practice.english.n2l.backgroundTask.UploadApiWorker
import practice.english.n2l.bridge.ConfirmationImp
import practice.english.n2l.bridge.SuccessImp
import practice.english.n2l.database.bao.UserDetail
import practice.english.n2l.databinding.ActivityHomeBinding
import practice.english.n2l.dialog.ConfirmationDialog
import practice.english.n2l.dialog.CustomProgressDialog
import practice.english.n2l.dialog.ErrorDialog
import practice.english.n2l.dialog.NoInternetConnection
import practice.english.n2l.dialog.SuccessDialog
import practice.english.n2l.dialog.WarningDialog
import practice.english.n2l.repository.PracticeRepo
import practice.english.n2l.repository.PracticeRepoImp
import practice.english.n2l.util.Constants
import practice.english.n2l.viewmodel.PracticeViewModel
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.util.concurrent.TimeUnit


class HomeActivity : BaseActivity() {
    private val repository              : PracticeRepo by lazy { PracticeRepoImp(RetrofitService.service,this) }
    private val viewModel               : PracticeViewModel by lazy { ViewModelProvider(this, PracticeViewModel.Factory(repository))[PracticeViewModel::class.java] }
    private val progressDialog          : CustomProgressDialog by lazy { CustomProgressDialog(this) }
    private lateinit var binding        : ActivityHomeBinding
    private lateinit var userDetail     : UserDetail

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.my_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            R.id.registration_settings -> startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://youtu.be/dW7t893gU-U")))
            R.id.profile_settings -> startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://youtu.be/plJRnimK9tk")))
            R.id.my_progress_settings ->startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://youtu.be/ODDLaUe7OcQ")))
            R.id.single_practice_settings ->startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://youtu.be/pWspPkJGq60")))
            R.id.multi_practice_settings -> startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://youtu.be/kGhnRD8xJ-M")))
            R.id.delete_account  -> Toast.makeText(this@HomeActivity, "Your account deletion request has been successfully sent.  We are processing your request.", Toast.LENGTH_LONG).show()
        }
        return super.onOptionsItemSelected(item)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_home)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.title = ""
        binding.BtnStartPractice.setOnClickListener {
        startActivity(Intent().setClassName(getString(R.string.project_package_name),
                "practice.english.n2l.ui.PracticeHomeActivity"))
        }
        binding.BtnProfile.setOnClickListener {
            startActivity(Intent().setClassName(getString(R.string.project_package_name),
                "practice.english.n2l.ui.profile.UserProfileActivity"))
        }
        binding.BtnMyProgress.setOnClickListener {
            val workRequest = OneTimeWorkRequestBuilder<PontDetailsApiWorker>().build()
            WorkManager.getInstance(this).enqueue(workRequest)

            /*WorkManager.getInstance(this).getWorkInfoByIdLiveData(workRequest.id)
                .observe(this, Observer { workInfo ->
                    if (workInfo != null && workInfo.state == WorkInfo.State.SUCCEEDED) {
                        // Work has completed successfully
                        // UI has already been updated through the callback
                    }
                })
           */
            startActivity(Intent().setClassName(getString(R.string.project_package_name),
                "practice.english.n2l.ui.myprogress.MyProgressHomeActivity"))
        }
        binding.BtnSync.setOnClickListener {
            ConfirmationDialog(this, getString(R.string.heading_information), getString(R.string.msg_download_sync), object : ConfirmationImp {
                override fun onClickYes(context: Activity, dialog: Dialog?) {
                    dialog!!.dismiss()
                    val jsonParam = JsonObject()
                    jsonParam.addProperty("userid", userDetail.userId)
                    viewModel.getMasterResources(jsonParam)
                }
                override fun onClickNo(context: Activity, dialog: Dialog?) {
                    dialog!!.dismiss()
                }
            }).show(this.supportFragmentManager, ConfirmationDialog.TAG)
        }

        binding.ImgLogout.setOnClickListener {
            logoutScreen()
        }
        viewModel.masterResources.observe(this) {
            when(it) {
                is ResponseHandle.Loading->{progressDialog.startLoading()}
                is ResponseHandle.Success-> {
                    when (it.data!!.responseCode) {
                        800 -> {
                            val mainUrl=it.data.responseData.resourceData.MainUrl[0]
                            val audioResourceDataList=it.data.responseData.resourceData.audio
                            val singleTemplatesResourceDataList=it.data.responseData.resourceData.singleTemplates
                            val multiTemplatesResourceDataList=it.data.responseData.resourceData.multiTemplates
                            val blankTemplatesResourceDataList=it.data.responseData.resourceData.blankTemplates
                            val lastPageTemplatesResourceDataList=it.data.responseData.resourceData.lastPageTemplates
                            val studentProfile=it.data.responseData.studentProfile

                            CoroutineScope(Dispatchers.IO).launch {
                                deleteAllFolderData()
                                withContext(Dispatchers.IO) {
                                    audioResourceDataList.stream().forEach{e -> e.fileData = downloadResourceData(mainUrl.url+e.url)}
                                    singleTemplatesResourceDataList.stream().forEach{e -> e.fileData    = downloadResourceData(mainUrl.url+e.url)}
                                    multiTemplatesResourceDataList.stream().forEach{e -> e.fileData     = downloadResourceData(mainUrl.url+e.url)}
                                    blankTemplatesResourceDataList.stream().forEach{e -> e.fileData     = downloadResourceData(mainUrl.url+e.url)}
                                    lastPageTemplatesResourceDataList.stream().forEach{e -> e.fileData  = downloadResourceData(mainUrl.url+e.url)}
                                    studentProfile.photoData = downloadResourceData(studentProfile.photoPath)
                                }
                                withContext(Dispatchers.IO) {
                                    audioResourceDataList.stream().forEach{e -> e.fileLocalPath   = writeMasterResourceAudioDataInInternalMemory(e.name,e.fileType,e.fileData)}
                                    singleTemplatesResourceDataList.stream().forEach{e -> e.fileLocalPath   = writeMasterResourceImageDataInInternalMemory("single",e.name,e.fileType,e.fileData)}
                                    multiTemplatesResourceDataList.stream().forEach{e -> e.fileLocalPath    = writeMasterResourceImageDataInInternalMemory("multi",e.name,e.fileType,e.fileData)}
                                    blankTemplatesResourceDataList.stream().forEach{e -> e.fileLocalPath    = writeMasterResourceImageDataInInternalMemory("blank",e.name,e.fileType,e.fileData)}
                                    lastPageTemplatesResourceDataList.stream().forEach{e -> e.fileLocalPath = writeMasterResourceImageDataInInternalMemory("last",e.name,e.fileType,e.fileData)}
                                    studentProfile.photoLocalPath = writeMasterResourceImageDataInInternalMemory(studentProfile.userId.toString(),"jpg",studentProfile.photoData)
                                }
                                viewModel.insertAllAudio(audioResourceDataList)
                                viewModel.insertAllSingleTemplates(singleTemplatesResourceDataList)
                                viewModel.insertAllMultiTemplates(multiTemplatesResourceDataList)
                                viewModel.insertAllBlankTemplates(blankTemplatesResourceDataList)
                                viewModel.insertAllLastPageTemplates(lastPageTemplatesResourceDataList)
                                viewModel.insertAllStudentProfile(studentProfile)
                                progressDialog.dismissLoading()
                                runOnUiThread {
                                    SuccessDialog(this@HomeActivity, Constants.Information, getString(R.string.msg_success_sync), object : SuccessImp {
                                        override fun onYes(context: Activity, dialog: Dialog?) {
                                            dialog!!.dismiss()
                                        }
                                    }).show(supportFragmentManager, SuccessDialog.TAG)
                                }
                            }
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
                    progressDialog.dismissLoading()
                    NoInternetConnection(this, Constants.Information, Constants.NetworkMessage).show(supportFragmentManager,
                        NoInternetConnection.TAG)
                }
            }
        }
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val workerRequest: WorkRequest = OneTimeWorkRequest.Builder(
            UploadApiWorker::class.java)
            .setInitialDelay(5, TimeUnit.SECONDS)
            .setConstraints(constraints)
            .build()
        Log.i("WorkManager",workerRequest.toString())
        WorkManager.getInstance(this@HomeActivity).enqueue(workerRequest)
    }

    private val onBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            logoutScreen()
        }
    }
    private fun logoutScreen() {
        ConfirmationDialog(this@HomeActivity, getString(R.string.heading_information), getString(R.string.msg_logout), object : ConfirmationImp {
            override fun onClickYes(context: Activity, dialog: Dialog?) {
                dialog!!.dismiss()
                this@HomeActivity.finishAndRemoveTask()
            }
            override fun onClickNo(context: Activity, dialog: Dialog?) {
                dialog!!.dismiss()
            }
        }).show(this@HomeActivity.supportFragmentManager, ConfirmationDialog.TAG)
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
            return   org.apache.commons.codec.binary.Base64.encodeBase64String(byteArrayOutputStream.toByteArray())
        } catch (e: Exception) {
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

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }
    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    fun onUserDetailEvent(userDetail : UserDetail) {
        val userDetailStickyEvent= EventBus.getDefault().getStickyEvent(UserDetail::class.java)
        if (userDetailStickyEvent != null) {
            this.userDetail=userDetail
            binding.txtUserName.text=userDetail.userName
            //EventBus.getDefault().removeStickyEvent(userDetailStickyEvent);
        }
    }

    /*override fun onResultReceivedPointDetailsApiWorker(
        pointData: PointData,
        pointDetails: List<PointDetails>
    ) {
        EventBus.getDefault().postSticky(PointMessageEvent(pointData,pointDetails))
    }*/
}




/*CoroutineScope(Dispatchers.IO).launch {
           signalRServiceExecute() // back on UI thread
       }*/

/*private suspend fun signalRServiceExecute() {
       return withContext(Dispatchers.IO) {
           val signalRClient= SignalRClient.getInstance("Amit",object: SignalRProcessImp {
               override fun receiveMessageToAllUsers(users: List<Users>) {
                   Log.e("getUserList", users[0].username)
               }
           })
           signalRClient.start()
           signalRClient.receiveMessageToAllUsers()
       }
   }*/