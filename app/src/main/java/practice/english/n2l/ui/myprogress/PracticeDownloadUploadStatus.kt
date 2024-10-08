package practice.english.n2l.ui.myprogress

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import practice.english.n2l.R
import practice.english.n2l.adapter.PracticeDownloadUploadStatusAdapter
import practice.english.n2l.api.ResponseHandle
import practice.english.n2l.api.RetrofitService
import practice.english.n2l.bridge.ConfirmationImp
import practice.english.n2l.bridge.PracticeDownloadUploadStatusImp
import practice.english.n2l.bridge.SuccessImp
import practice.english.n2l.database.bao.AttemptPractice
import practice.english.n2l.database.bao.UserDetail
import practice.english.n2l.databinding.ActivityPracticeDownloadUploadStatusBinding
import practice.english.n2l.dialog.ConfirmationDialog
import practice.english.n2l.dialog.CustomProgressDialog
import practice.english.n2l.dialog.ErrorDialog
import practice.english.n2l.dialog.NoInternetConnection
import practice.english.n2l.dialog.SuccessDialog
import practice.english.n2l.dialog.WarningDialog
import practice.english.n2l.repository.PracticeRepo
import practice.english.n2l.repository.PracticeRepoImp
import practice.english.n2l.ui.BaseActivity
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


class PracticeDownloadUploadStatus : BaseActivity(),PracticeDownloadUploadStatusImp {
    private val repository                           : PracticeRepo by lazy { PracticeRepoImp(RetrofitService.service, this) }
    private val viewModel                            : PracticeViewModel by lazy { ViewModelProvider(this, PracticeViewModel.Factory(repository))[PracticeViewModel::class.java] }
    private lateinit var binding                     : ActivityPracticeDownloadUploadStatusBinding
    private val practiceDownloadUploadStatusAdapter  : PracticeDownloadUploadStatusAdapter by lazy { PracticeDownloadUploadStatusAdapter(this) }
    private val progressDialog                       : CustomProgressDialog by lazy { CustomProgressDialog(this) }
    private lateinit var userDetail                  : UserDetail
    private lateinit var attemptPractice             : AttemptPractice
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_practice_download_upload_status)

        binding.lifecycleOwner = this
        binding.apply {
            RecView.apply {
                adapter = practiceDownloadUploadStatusAdapter
                layoutManager = LinearLayoutManager(context)
                setHasFixedSize(true)
            }
            BtnBack.setOnClickListener {
                startActivity(
                    Intent().setClassName(getString(R.string.project_package_name),
                        "practice.english.n2l.ui.myprogress.MyProgressHomeActivity"))
                        //.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
                //finishAndRemoveTask()
            }
        }

        viewModel.getAttemptPractice().observe(this){
            it?.let {
                if(it.isNotEmpty())
                    practiceDownloadUploadStatusAdapter.updateList(it)
                else {
                    practiceDownloadUploadStatusAdapter.removeAllItems()
                    dataNotFound()
                }
            }?: let  {
                practiceDownloadUploadStatusAdapter.removeAllItems()
                dataNotFound()
            }
        }

        viewModel.uploadPracticeVideo.observe(this){
            when(it) {
                is ResponseHandle.Loading -> { progressDialog.startLoading() }
                is ResponseHandle.Success -> {
                    when (it.data!!.responseCode) {
                        800 -> {
                            val practiceSno=it.data.responseData.uploadResult[0].practiceSno
                            progressDialog.dismissLoading()
                            viewModel.updateAttemptPracticeUploadStatus(attemptPractice.quizId)
                            SuccessDialog(this@PracticeDownloadUploadStatus,
                                Constants.Information, it.data.responseMessage,object: SuccessImp {
                                    override fun onYes(context: Activity, dialog: Dialog?) {
                                        dialog!!.dismiss()
                                    }
                                }).show(supportFragmentManager, SuccessDialog.TAG)
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
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    private fun dataNotFound() {
        SuccessDialog(this@PracticeDownloadUploadStatus,
            Constants.Information, getString(R.string.record_not_available),object: SuccessImp {
                override fun onYes(context: Activity, dialog: Dialog?) {
                    dialog!!.dismiss()
                    startActivity(
                        Intent().setClassName(getString(R.string.project_package_name),
                            "practice.english.n2l.ui.myprogress.MyProgressHomeActivity"))
                }
            }).show(supportFragmentManager, SuccessDialog.TAG)
    }

    override fun onPlay(attemptPractice: AttemptPractice) {
        EventBus.getDefault().postSticky(attemptPractice)
        startActivity(Intent().setClassName(getString(R.string.project_package_name),
            "practice.english.n2l.player.VideoPlayerActivity"))
    }

    override fun onDelete(attemptPractice: AttemptPractice) {
        ConfirmationDialog(this, getString(R.string.heading_information), getString(R.string.delete_msg), object : ConfirmationImp {
            override fun onClickYes(context: Activity, dialog: Dialog?) {
                dialog!!.dismiss()
                viewModel.deleteQuizIdAllTable(attemptPractice.quizId)
                practiceDownloadUploadStatusAdapter.removeItem(attemptPractice)
                val file=File(filesDir.absolutePath + File.separator + attemptPractice.practiceFilePath)
                if (file.exists()) file.delete()
                SuccessDialog(this@PracticeDownloadUploadStatus,
                    Constants.Information, getString(R.string.delete_success_msg),object: SuccessImp {
                    override fun onYes(context: Activity, dialog: Dialog?) {
                        dialog!!.dismiss()
                    }
                }).show(supportFragmentManager, SuccessDialog.TAG)
            }
            override fun onClickNo(context: Activity, dialog: Dialog?) {
                dialog!!.dismiss()
            }
        }).show(supportFragmentManager, ConfirmationDialog.TAG)
    }

    override fun onUpload(attemptPractice: AttemptPractice) {
        this.attemptPractice=attemptPractice
        ConfirmationDialog(this, getString(R.string.heading_information), getString(R.string.upload_msg), object : ConfirmationImp {
            override fun onClickYes(context: Activity, dialog: Dialog?) {
                dialog!!.dismiss()
                CoroutineScope(Dispatchers.IO).launch {
                    withContext(Dispatchers.IO) {
                        val file =File(attemptPractice.practiceFilePath)
                        val requestFile= file.absoluteFile.asRequestBody("video/mp4".toMediaTypeOrNull())
                        val body = MultipartBody.Part.createFormData("mp4", file.name, requestFile)
                        val singleMulti: RequestBody
                        val friendId: RequestBody
                        val studentId = userDetail.userId.toRequestBody("application/json".toMediaTypeOrNull())
                        val dateOfVideoCreation = TimestampConverter.dateToString(attemptPractice.practiceDate).toRequestBody("application/json".toMediaTypeOrNull())
                        val quizId = attemptPractice.quizId.toString().toRequestBody("application/json".toMediaTypeOrNull())
                        if(attemptPractice.singleMulti==1) {
                            singleMulti = attemptPractice.singleMulti.toString().toRequestBody("application/json".toMediaTypeOrNull())
                            friendId ="".toRequestBody("application/json".toMediaTypeOrNull())
                        }
                        else {
                            singleMulti= attemptPractice.singleMulti.toString().toRequestBody("application/json".toMediaTypeOrNull())
                            friendId= attemptPractice.friendId.toString().toRequestBody("application/json".toMediaTypeOrNull())
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
        }
    }
}