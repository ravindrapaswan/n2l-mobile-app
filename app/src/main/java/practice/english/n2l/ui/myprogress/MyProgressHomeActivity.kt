package practice.english.n2l.ui.myprogress

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.gson.JsonObject
import practice.english.n2l.R
import practice.english.n2l.api.ResponseHandle
import practice.english.n2l.api.RetrofitService
import practice.english.n2l.bridge.SuccessImp
import practice.english.n2l.database.bao.PointData
import practice.english.n2l.database.bao.PointDetails
import practice.english.n2l.database.bao.PointMessageEvent
import practice.english.n2l.database.bao.UserDetail
import practice.english.n2l.databinding.ActivityMyProgressHomeBinding
import practice.english.n2l.dialog.CustomProgressDialog
import practice.english.n2l.dialog.ErrorDialog
import practice.english.n2l.dialog.NoInternetConnection
import practice.english.n2l.dialog.SuccessDialog
import practice.english.n2l.dialog.WarningDialog
import practice.english.n2l.repository.PracticeRepo
import practice.english.n2l.repository.PracticeRepoImp
import practice.english.n2l.ui.BaseActivity
import practice.english.n2l.util.Constants
import practice.english.n2l.viewmodel.PracticeViewModel
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class MyProgressHomeActivity : BaseActivity() {
    private val repository                : PracticeRepo by lazy { PracticeRepoImp(RetrofitService.service, this) }
    private val viewModel                 : PracticeViewModel by lazy { ViewModelProvider(this, PracticeViewModel.Factory(repository))[PracticeViewModel::class.java] }
    private val progressDialog            : CustomProgressDialog by lazy { CustomProgressDialog(this) }

    private lateinit var binding          : ActivityMyProgressHomeBinding
    private lateinit var pointData        : PointData
    private lateinit var pointDetails     : List<PointDetails>
    private lateinit var userDetail       : UserDetail
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_my_progress_home)
        binding.lifecycleOwner = this

        binding.BtnMyPoints.setOnClickListener {
            if(::pointData.isInitialized) {
                startActivity(Intent().setClassName(getString(R.string.project_package_name), "practice.english.n2l.ui.myprogress.MyPointsActivity"))
            }
            else {
                val jsonParams = JsonObject()
                jsonParams.addProperty("studentid", userDetail.userId)
                viewModel.getPoints(jsonParams)
            }
        }
        viewModel.points.observe(this) {
            when(it) {
                is ResponseHandle.Loading -> { progressDialog.startLoading() }
                is ResponseHandle.Success -> {
                    when (it.data!!.responseCode) {
                        800 -> {
                            val pointData=it.data.responseData.pointData
                            val pointDetails=it.data.responseData.pointDetails
                            progressDialog.dismissLoading()
                            EventBus.getDefault().postSticky(PointMessageEvent(pointData,pointDetails))
                            startActivity(Intent().setClassName(getString(R.string.project_package_name), "practice.english.n2l.ui.myprogress.MyPointsActivity"))
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
        binding.BtnMyVideo.setOnClickListener {
            startActivity(Intent().setClassName(getString(R.string.project_package_name), "practice.english.n2l.ui.myprogress.MyVideoActivity"))
            /*viewModel.getAttemptPractice().observe(this){
                it?.let {
                        if(it.isNotEmpty()) {
                            val workRequest = OneTimeWorkRequestBuilder<PontDetailsApiWorker>().build()
                            WorkManager.getInstance(this).enqueue(workRequest)
                            startActivity(Intent().setClassName(getString(R.string.project_package_name), "practice.english.n2l.ui.myprogress.PracticeDownloadUploadStatus"))
                        }
                    else
                            dataNotFound()
                }?: dataNotFound()
            }*/
        }
       /* binding.BtnCompareWithMyFriends.setOnClickListener {
            WarningDialog(this, Constants.Information,getString(R.string.feature_not_implemented)).show(supportFragmentManager,
                WarningDialog.TAG)
        }
        binding.BtnMyActivities.setOnClickListener {
            WarningDialog(this, Constants.Information,getString(R.string.feature_not_implemented)).show(supportFragmentManager,
                WarningDialog.TAG)
        }*/
        binding.LinBackNav.setOnClickListener {
            startActivity(Intent().setClassName(getString(R.string.project_package_name), "practice.english.n2l.ui.HomeActivity"))
            //finishAndRemoveTask()
        }
        binding.TxtMyPoint.text=""
    }
    private fun dataNotFound() {
        SuccessDialog(this@MyProgressHomeActivity,
            Constants.Information, getString(R.string.record_not_available),object: SuccessImp {
                override fun onYes(context: Activity, dialog: Dialog?) {
                    dialog!!.dismiss()
                    startActivity(
                        Intent().setClassName(getString(R.string.project_package_name),
                            "practice.english.n2l.ui.myprogress.MyProgressHomeActivity"))
                }
            }).show(supportFragmentManager, SuccessDialog.TAG)
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
    fun onPointMessageEvent(pointMessageEvent : PointMessageEvent) {
        val pointMessageEventStickyEvent= EventBus.getDefault().getStickyEvent(PointMessageEvent::class.java)
        if (pointMessageEventStickyEvent != null) {
             this.pointData=pointMessageEventStickyEvent.pointData
             this.pointDetails=pointMessageEventStickyEvent.pointDetails
            runOnUiThread {
                binding.TxtMyPoint.text=pointData.totalPoint
            }
            //EventBus.getDefault().removeStickyEvent(pointMessageEvent)
        }
    }

    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    fun onUserDetailEvent(userDetail : UserDetail) {
        val userDetailStickyEvent= EventBus.getDefault().getStickyEvent(UserDetail::class.java)
        if (userDetailStickyEvent != null) {
            this.userDetail=userDetail
            //EventBus.getDefault().removeStickyEvent(userDetailStickyEvent);
        }
    }
}