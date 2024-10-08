package practice.english.n2l.ui.myprogress

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import practice.english.n2l.R
import practice.english.n2l.adapter.MyVideoAdapter
import practice.english.n2l.api.ResponseHandle
import practice.english.n2l.api.RetrofitService
import practice.english.n2l.bridge.ConfirmationImp
import practice.english.n2l.bridge.CustomWarningImp
import practice.english.n2l.bridge.MyVideosImp
import practice.english.n2l.bridge.SuccessImp
import practice.english.n2l.database.bao.MyVideos
import practice.english.n2l.database.bao.UserDetail
import practice.english.n2l.databinding.ActivityMyVideoBinding
import practice.english.n2l.dialog.ConfirmationDialog
import practice.english.n2l.dialog.CustomProgressDialog
import practice.english.n2l.dialog.CustomWarningDialog
import practice.english.n2l.dialog.ErrorDialog
import practice.english.n2l.dialog.NoInternetConnection
import practice.english.n2l.dialog.SuccessDialog
import practice.english.n2l.dialog.WarningDialog
import practice.english.n2l.repository.PracticeRepo
import practice.english.n2l.repository.PracticeRepoImp
import practice.english.n2l.util.Constants
import practice.english.n2l.util.TimestampConverter
import practice.english.n2l.viewmodel.PracticeViewModel
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File
import java.io.FileOutputStream
import java.net.URL

class MyVideoActivity : AppCompatActivity(),MyVideosImp {
    private val repository                   : PracticeRepo by lazy { PracticeRepoImp(RetrofitService.service, this) }
    private val viewModel                    : PracticeViewModel by lazy { ViewModelProvider(this, PracticeViewModel.Factory(repository))[PracticeViewModel::class.java] }
    private lateinit var binding             : ActivityMyVideoBinding
    private val progressDialog               : CustomProgressDialog by lazy { CustomProgressDialog(this) }
    private val myVideoAdapter               : MyVideoAdapter by lazy { MyVideoAdapter(this,this) }
    private lateinit var userDetail          : UserDetail
    private  lateinit var myVideos           : MyVideos
    private  var myVideosArrayList           : MutableList<MyVideos>?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_my_video)
        binding.lifecycleOwner = this

        binding.apply {
            RecView.apply {
                adapter = myVideoAdapter
                layoutManager = LinearLayoutManager(context)
                setHasFixedSize(true)
            }
            BtnBack.setOnClickListener {
                startActivity(Intent().setClassName(getString(R.string.project_package_name),
                    "practice.english.n2l.ui.myprogress.MyProgressHomeActivity"))
            }
        }

        viewModel.getAllMyVideosLiveData().observe(this){
            it?.let {
                if(it.isNotEmpty()){
                    myVideosArrayList=it.toMutableList()
                    myVideoAdapter.updateList(it)}
                else {
                    myVideoAdapter.removeAllItems()
                }
            }?: let  {
                myVideoAdapter.removeAllItems()
            }
        }
        viewModel.myVideo.observe(this){
            when(it) {
                is ResponseHandle.Loading -> { progressDialog.startLoading() }
                is ResponseHandle.Success -> {
                    when (it.data!!.responseCode) {
                        800 -> {
                            val mainUrl=it.data.responseData.mainUrl[0]
                            val myVideos=it.data.responseData.myVideos
                            myVideosArrayList=myVideos.toMutableList()
                            CoroutineScope(Dispatchers.IO).launch {
                                withContext(Dispatchers.IO) {
                                    myVideos.stream()
                                        .peek { e->e.uploadStatus="Y" }
                                        .forEach{e->e.practiceOnlineFilePath=mainUrl.url+e.practiceOnlineFilePath}
                                            //.peek { e->e.practiceOnlineFilePath=mainUrl.url+e.practiceOnlineFilePath }
                                        //.peek { e->e.uploadStatus="Y" }
                                        //.forEach{e->e.practiceLocalFilePath=writePracticeVideoDataInInternalMemory(e.practiceOnlineFilePath!!,e.practicesNo.toString(),"mp4")}
                                    viewModel.insertAllVideos(myVideos)
                                    progressDialog.dismissLoading()
                                }
                            }
                        }
                        in 300.. 302 -> {
                            progressDialog.dismissLoading()
                            CustomWarningDialog(this, Constants.Information,it.data.responseMessage,object : CustomWarningImp {
                                override fun onClickNo(context: Activity, dialog: Dialog?) {
                                    dialog?.dismiss()
                                    val intent = Intent(this@MyVideoActivity, MyProgressHomeActivity::class.java)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                                    startActivity(intent)
                                    finish()
                                }
                            }).show(supportFragmentManager, CustomWarningDialog.TAG)
                        }
                        500 -> {
                            progressDialog.dismissLoading()
                            WarningDialog(this, Constants.Information,it.data.responseMessage).show(supportFragmentManager, WarningDialog.TAG)
                        }
                        else->{
                            progressDialog.dismissLoading()
                            WarningDialog(this, Constants.Information,it.data.responseMessage).show(supportFragmentManager, WarningDialog.TAG)
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
                        NoInternetConnection.TAG)}
            }
        }
        viewModel.uploadPracticeVideo.observe(this){
            when(it) {
                is ResponseHandle.Loading -> { progressDialog.startLoading() }
                is ResponseHandle.Success -> {
                    when (it.data!!.responseCode) {
                        800 -> {
                            progressDialog.dismissLoading()
                            CoroutineScope(Dispatchers.IO).launch {
                                withContext(Dispatchers.IO) {
                                    val uploadResult=it.data.responseData.uploadResult[0]
                                    val originalFileMp4=File(myVideos.practiceLocalFilePath!!)
                                    val practiceVideoDir = File(filesDir, "practiceVideo")
                                    if (!practiceVideoDir.exists()) practiceVideoDir.mkdirs()
                                    val newFileMp4=File(practiceVideoDir.absolutePath + File.separator + "${uploadResult.practiceSno}.jpg")
                                    originalFileMp4.copyTo(newFileMp4, true)
                                    if(newFileMp4.exists())originalFileMp4.delete()
                                    viewModel.updateMyVideoUploadStatus(uploadResult.practiceSno,uploadResult.filePath,newFileMp4.absolutePath,uploadResult.quizId)
                                    runOnUiThread {
                                        SuccessDialog(this@MyVideoActivity,
                                            Constants.Information, it.data.responseMessage,object: SuccessImp {
                                                override fun onYes(context: Activity, dialog: Dialog?) {
                                                    dialog!!.dismiss()
                                                }
                                            }).show(supportFragmentManager, SuccessDialog.TAG)
                                    }
                                }
                            }
                        }
                        in 300.. 302 -> {
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
                        NoInternetConnection.TAG)}
            }
        }

        binding.fabReDownload.setOnClickListener {
            val jsonParams = JsonObject()
            jsonParams.addProperty("userid",userDetail.userId)
            viewModel.getMyVideo(jsonParams)
        }
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

    }
    private val onBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            finishAndRemoveTask()
        }
    }
    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }
    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }
    @Subscribe(sticky = true,threadMode = ThreadMode.BACKGROUND)
    fun onUserDetailEvent(userDetail : UserDetail) {
        val userDetailStickyEvent= EventBus.getDefault().getStickyEvent(UserDetail::class.java)
        if (userDetailStickyEvent != null) {
            this.userDetail=userDetail
            if(viewModel.getMyVideos().isEmpty())
            {
                val jsonParams = JsonObject()
                jsonParams.addProperty("userid",userDetail.userId)
                viewModel.getMyVideo(jsonParams)
            }
        }
    }

    override fun onPlay(myVideos: MyVideos) {
        EventBus.getDefault().postSticky(myVideos)
        startActivity(Intent().setClassName(getString(R.string.project_package_name),
                "practice.english.n2l.player.VideoPlayerActivity"))
    }
    override fun onDelete(myVideos: MyVideos) {
        ConfirmationDialog(this, getString(R.string.heading_information), getString(R.string.delete_video_msg), object : ConfirmationImp {
            override fun onClickYes(context: Activity, dialog: Dialog?) {
                dialog!!.dismiss()
                SuccessDialog(this@MyVideoActivity,
                    Constants.Information, getString(R.string.delete_success_msg),object: SuccessImp {
                        override fun onYes(context: Activity, dialog: Dialog?) {
                            dialog!!.dismiss()
                            val file=File(filesDir.absolutePath + File.separator +"practiceVideo"+ File.separator  +myVideos.practicesNo)
                            if (file.exists()) file.delete()
                            viewModel.updateMyVideos(null,myVideos.practicesNo!!)
                        }
                    }).show(supportFragmentManager, SuccessDialog.TAG)
            }
            override fun onClickNo(context: Activity, dialog: Dialog?) {
                dialog!!.dismiss()
            }
        }).show(supportFragmentManager, ConfirmationDialog.TAG)
    }
    override fun onUpload(myVideos: MyVideos) {
        this.myVideos=myVideos
        ConfirmationDialog(this, getString(R.string.heading_information), getString(R.string.upload_msg), object : ConfirmationImp {
            override fun onClickYes(context: Activity, dialog: Dialog?) {
                dialog!!.dismiss()
                CoroutineScope(Dispatchers.IO).launch {
                    withContext(Dispatchers.IO) {
                        val file =File(myVideos.practiceLocalFilePath!!)
                        val requestFile= file.absoluteFile.asRequestBody("video/mp4".toMediaTypeOrNull())
                        val body = MultipartBody.Part.createFormData("mp4", file.name, requestFile)
                        val singleMulti: RequestBody
                        val friendId: RequestBody
                        val studentId = userDetail.userId.toRequestBody("application/json".toMediaTypeOrNull())
                        val dateOfVideoCreation = TimestampConverter.convertDateFormat(myVideos.practiceDate).toRequestBody("application/json".toMediaTypeOrNull())
                        val quizId = myVideos.quizId.toString().toRequestBody("application/json".toMediaTypeOrNull())
                        if(myVideos.singleMulti==1) {
                            singleMulti = myVideos.singleMulti.toString().toRequestBody("application/json".toMediaTypeOrNull())
                            friendId ="".toRequestBody("application/json".toMediaTypeOrNull())
                        }
                        else {
                            singleMulti= myVideos.singleMulti.toString().toRequestBody("application/json".toMediaTypeOrNull())
                            friendId= myVideos.friendId.toString().toRequestBody("application/json".toMediaTypeOrNull())
                        }
                        viewModel.uploadPracticeVideo(studentId, dateOfVideoCreation, quizId,singleMulti,friendId,body)
                    }
                }
            }
            override fun onClickNo(context: Activity, dialog: Dialog?) {
                dialog!!.dismiss()
            }
        }).show(supportFragmentManager, ConfirmationDialog.TAG)
    }
    override fun onDownload(myVideos: MyVideos) {
        this.myVideos=myVideos
        ConfirmationDialog(this, getString(R.string.heading_information), getString(R.string.download_msg), object : ConfirmationImp {
            override fun onClickYes(context: Activity, dialog: Dialog?) {
                dialog!!.dismiss()
                progressDialog.startLoading()
                CoroutineScope(Dispatchers.IO).launch {
                    withContext(Dispatchers.IO) {
                        myVideos.practiceLocalFilePath=writePracticeVideoDataInInternalMemory(myVideos.practiceOnlineFilePath!!,myVideos.practicesNo.toString(),"mp4")
                        viewModel.updateMyVideoPracticeLocalFilePath(myVideos.practiceLocalFilePath!!,myVideos.practicesNo!!,myVideos.quizId)
                        val index = myVideosArrayList!!.indexOfFirst { it.practicesNo == myVideos.practicesNo }
                        if (index != -1) {
                            myVideosArrayList!![index] = myVideos
                            runOnUiThread{
                                myVideoAdapter.removeAllItems()
                                myVideoAdapter.updateList(myVideosArrayList!!)
                            }
                        }
                        progressDialog.dismissLoading()
                        //runOnUiThread{
                        //myVideoAdapter.notifyDataSetChanged()
                        //}
                    }
                }
            }
            override fun onClickNo(context: Activity, dialog: Dialog?) {
                dialog!!.dismiss()
            }
        }).show(supportFragmentManager, ConfirmationDialog.TAG)
    }
    override fun onShare(myVideos: MyVideos) {
        try {
            val path = FileProvider.getUriForFile(this, applicationContext.packageName + ".provider", File(myVideos.practiceLocalFilePath!!).absoluteFile)
            val pdfIntent = Intent(Intent.ACTION_SEND)
            pdfIntent.setDataAndType(path, "video/*")
            pdfIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            pdfIntent.putExtra(Intent.EXTRA_STREAM, path)
            pdfIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
           startActivity(Intent.createChooser(pdfIntent, "Share file via"))
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            Log.d("pepe", e.toString())
        }
       /* val fileUri = FileProvider.getUriForFile(applicationContext, applicationContext.packageName+".provider", File(myVideos.practiceLocalFilePath!!))
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "video/*" // Set MIME type according to your file type
        shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri)
        shareIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivity(Intent.createChooser(shareIntent, "Share with"))*/
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.IO) {
                myVideos.practiceLocalFilePath=writePracticeVideoDataInInternalMemory(myVideos.practiceOnlineFilePath!!,myVideos.practicesNo.toString(),"mp4")
                viewModel.updateMyVideos(myVideos.practiceLocalFilePath!!,myVideos.practicesNo!!)
                runOnUiThread {
                    progressDialog.dismissLoading()
                    myVideoAdapter.notifyDataSetChanged()
                }
            }
        }*/
    }
    private  fun writePracticeVideoDataInInternalMemory(url:String,filename:String,fileType:String):String {
        try {
            val practiceVideoDir = File(filesDir, "practiceVideo")
            if (!practiceVideoDir.exists()) practiceVideoDir.mkdirs()
            val practiceVideoDirPath = practiceVideoDir.absolutePath
            val practiceVideoDirFile = File("$practiceVideoDirPath/$filename.$fileType")
            val fOut = FileOutputStream(practiceVideoDirFile)
            val imageUrl = URL(url)
            val uCon = imageUrl.openConnection()
            val input = uCon.getInputStream()
            val data = ByteArray(1024)
            var count: Int
            while (input.read(data).also { count = it } != -1) {
                fOut.write(data, 0, count)
            }
            fOut.flush()
            fOut.close()
            return practiceVideoDirFile.canonicalPath
        }
        catch (e: Exception) {
            Log.d("Error", e.toString())
        }
        return ""
    }
}