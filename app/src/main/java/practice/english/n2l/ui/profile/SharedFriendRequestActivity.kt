package practice.english.n2l.ui.profile

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.gson.JsonObject
import practice.english.n2l.R
import practice.english.n2l.api.ResponseHandle
import practice.english.n2l.api.RetrofitService
import practice.english.n2l.bridge.ConfirmationImp
import practice.english.n2l.bridge.SuccessImp
import practice.english.n2l.databinding.ActivitySharedFriendRequestBinding
import practice.english.n2l.dialog.ConfirmationDialog
import practice.english.n2l.dialog.CustomProgressDialog
import practice.english.n2l.dialog.ErrorDialog
import practice.english.n2l.dialog.NoInternetConnection
import practice.english.n2l.dialog.SuccessDialog
import practice.english.n2l.dialog.WarningDialog
import practice.english.n2l.repository.ProfileRepo
import practice.english.n2l.repository.ProfileRepoImp
import practice.english.n2l.util.Constants
import practice.english.n2l.viewmodel.ProfileViewModel


class SharedFriendRequestActivity : AppCompatActivity() {
    private val repository                           : ProfileRepo by lazy { ProfileRepoImp(RetrofitService.service,this) }
    private val viewModel                            : ProfileViewModel by lazy { ViewModelProvider(this, ProfileViewModel.Factory(repository))[ProfileViewModel::class.java] }
    private lateinit var binding                     : ActivitySharedFriendRequestBinding
    private var recordId                             : String=""
    private var friendId                             : String=""
    private var myFriendMobileNumber                 : String=""
    private val progressDialog                       : CustomProgressDialog by lazy { CustomProgressDialog(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_shared_friend_request)
        binding.lifecycleOwner = this

        val intent = intent
        val data = intent.data
        if (data != null) {
            recordId = data.getQueryParameter("recordId").toString()
            friendId = data.getQueryParameter("friendId").toString()
            binding.TvFriendName.text = data.getQueryParameter("friendName")
            binding.TvFriendMobileNum.text = data.getQueryParameter("friendMobileNumber")
            binding.TvDate.text = data.getQueryParameter("friendRequestDate")
            myFriendMobileNumber= data.getQueryParameter("myFriendMob").toString()
        }

        binding.BtnAccept.setOnClickListener {
            ConfirmationDialog(this, getString(R.string.heading_information), getString(R.string.accept_msg), object : ConfirmationImp {
                override fun onClickYes(context: Activity, dialog: Dialog?) {
                    dialog!!.dismiss()
                    val jsonParams = JsonObject()
                    jsonParams.addProperty("userid",friendId)
                    jsonParams.addProperty("recordid",recordId)
                    jsonParams.addProperty("accept_reject","2")
                    viewModel.actionFriendRequest(jsonParams)
                }
                override fun onClickNo(context: Activity, dialog: Dialog?) {
                    dialog!!.dismiss()
                }
            }).show(supportFragmentManager, ConfirmationDialog.TAG)
        }
        binding.BtnReject.setOnClickListener {
            ConfirmationDialog(this, getString(R.string.heading_information), getString(R.string.reject_msg), object : ConfirmationImp {
                override fun onClickYes(context: Activity, dialog: Dialog?) {
                    dialog!!.dismiss()
                    val jsonParams = JsonObject()
                    jsonParams.addProperty("userid",friendId)
                    jsonParams.addProperty("recordid",recordId)
                    jsonParams.addProperty("accept_reject","3")
                    viewModel.actionFriendRequest(jsonParams)
                }
                override fun onClickNo(context: Activity, dialog: Dialog?) {
                    dialog!!.dismiss()
                }
            }).show(supportFragmentManager, ConfirmationDialog.TAG)
        }

        viewModel.actionFriendRequest.observe(this) {
            when (it) {
                is ResponseHandle.Loading -> { progressDialog.startLoading() }
                is ResponseHandle.Success -> {
                    when (it.data!!.responseCode) {
                        800 -> {
                            progressDialog.dismissLoading()
                            SuccessDialog(this@SharedFriendRequestActivity,Constants.Information,it.data.responseMessage,object:
                                SuccessImp {
                                override fun onYes(context: Activity, dialog: Dialog?) {
                                    dialog!!.dismiss()
                                    binding.BtnAccept.visibility= View.INVISIBLE
                                    binding.BtnReject.visibility= View.INVISIBLE
                                    this@SharedFriendRequestActivity.finishAndRemoveTask()
                                }
                            }).show(supportFragmentManager, SuccessDialog.TAG)
                        }
                        300 -> {
                            progressDialog.dismissLoading()
                            WarningDialog(this, Constants.Information, it.data.responseMessage).show(supportFragmentManager, WarningDialog.TAG)
                        }
                        301 -> {
                            progressDialog.dismissLoading()
                            WarningDialog(this, Constants.Information, it.data.responseMessage).show(supportFragmentManager, WarningDialog.TAG)
                        }
                        500 -> {
                            progressDialog.dismissLoading()
                            WarningDialog(this, Constants.Information, it.data.responseMessage).show(supportFragmentManager, WarningDialog.TAG)
                        }
                        else -> {
                            progressDialog.dismissLoading()
                            WarningDialog(this, Constants.Information, it.data.responseMessage).show(supportFragmentManager, WarningDialog.TAG)
                        }
                    }
                }
                is ResponseHandle.Error -> {
                    progressDialog.dismissLoading()
                    ErrorDialog(this, Constants.Error, Constants.ServerFailure).show(supportFragmentManager, ErrorDialog.TAG)
                }
                is ResponseHandle.Network -> {
                    NoInternetConnection(this, Constants.Information, Constants.NetworkMessage).show(supportFragmentManager, NoInternetConnection.TAG)
                }
            }
        }

        viewModel.friendRequest.observe(this) {
            when(it) {
                is ResponseHandle.Loading->{progressDialog.startLoading()}
                is ResponseHandle.Success-> {
                    when (it.data!!.responseCode) {
                        800 -> {
                            progressDialog.dismissLoading()
                            binding.BtnAccept.visibility=View.VISIBLE
                            binding.BtnReject.visibility=View.VISIBLE
                        }
                        300  -> {
                            progressDialog.dismissLoading()
                            WarningDialog(this, Constants.Information,it.data.responseMessage).show(supportFragmentManager,
                                WarningDialog.TAG)
                        }
                        301->{
                            progressDialog.dismissLoading()
                            binding.BtnAccept.visibility= View.INVISIBLE
                            binding.BtnReject.visibility= View.INVISIBLE
                            SuccessDialog(this@SharedFriendRequestActivity,Constants.Information,it.data.responseMessage,object:
                                SuccessImp {
                                override fun onYes(context: Activity, dialog: Dialog?) {
                                    dialog!!.dismiss()
                                    this@SharedFriendRequestActivity.finishAndRemoveTask()
                                }
                            }).show(supportFragmentManager, SuccessDialog.TAG)
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

        val jsonParams = JsonObject()
        jsonParams.addProperty("userid",friendId)
        jsonParams.addProperty("friendMobileNumber", myFriendMobileNumber)
        viewModel.sendFriendRequest(jsonParams)
    }
}