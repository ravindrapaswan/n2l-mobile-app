package practice.english.n2l.ui

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import practice.english.n2l.R
import practice.english.n2l.api.ResponseHandle
import practice.english.n2l.api.RetrofitService
import practice.english.n2l.bridge.SuccessImp
import practice.english.n2l.databinding.ActivityRegistrationBinding
import practice.english.n2l.dialog.CustomProgressDialog
import practice.english.n2l.dialog.ErrorDialog
import practice.english.n2l.dialog.NoInternetConnection
import practice.english.n2l.dialog.SuccessDialog
import practice.english.n2l.dialog.WarningDialog
import practice.english.n2l.repository.UserAuthRepo
import practice.english.n2l.repository.UserAuthRepoImp
import practice.english.n2l.util.Constants
import practice.english.n2l.viewmodel.UserAuthViewModel


class RegistrationActivity : BaseActivity() {

    private val repository          : UserAuthRepo by lazy { UserAuthRepoImp(RetrofitService.service,this) }
    private val viewModel           : UserAuthViewModel by lazy { ViewModelProvider(this, UserAuthViewModel.Factory(repository))[UserAuthViewModel::class.java] }
    private lateinit var binding    : ActivityRegistrationBinding
    private val progressDialog      : CustomProgressDialog by lazy { CustomProgressDialog(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_registration)
        binding.userAuthViewModel = viewModel
        binding.lifecycleOwner = this

        viewModel.generateOTP.observe(this) {
            when(it) {
                is ResponseHandle.Loading->{progressDialog.startLoading()}
                is ResponseHandle.Success-> {
                    when (it.data!!.responseCode) {
                        800 -> {
                            val list=it.data.responseData.oTPInfo
                            list.userName=binding.EtFullName.text.toString().trim()
                            if(binding.EtNickName.text.toString().trim().isEmpty())
                                list.userNickName=binding.EtFullName.text.toString().trim()
                            else
                                list.userNickName=binding.EtNickName.text.toString().trim()
                            list.referralId=binding.EtReferralId.text.toString().trim()
                            list.userMobileNumber=binding.EtMobileNum.text.toString().trim()
                            progressDialog.dismissLoading()
                            SuccessDialog(this,Constants.Information,it.data.responseMessage,object:SuccessImp{
                                override fun onYes(context: Activity, dialog: Dialog?) {
                                    dialog!!.dismiss()
                                    startActivity(Intent().setClassName(getString(R.string.project_package_name),
                                        "practice.english.n2l.ui.CreatePinActivity").putExtra("OTPInfoDesc", Gson().toJson(list))
                                        .putExtra("SourceActivity","RegistrationActivity"))
                                    finish()
                                }
                            }).show(supportFragmentManager,SuccessDialog.TAG)
                        }
                        /*801 -> {
                            val list=it.data.responseData.oTPInfo
                            progressDialog.dismissLoading()
                            SuccessDialog(this,Constants.Information,it.data.responseMessage,object:SuccessImp{
                                override fun onYes(context: Activity, dialog: Dialog?) {
                                    dialog!!.dismiss()
                                    startActivity(Intent().setAction("CreatePin").putExtra("OTPInfoDesc", Gson().toJson(list)))
                                }
                            }).show(supportFragmentManager,SuccessDialog.TAG)
                        }*/
                        300 -> {
                            progressDialog.dismissLoading()
                            WarningDialog(this, Constants.Information,it.data.responseMessage).show(supportFragmentManager,WarningDialog.TAG)
                        }
                        500 -> {
                            progressDialog.dismissLoading()
                            WarningDialog(this, Constants.Information,it.data.responseMessage).show(supportFragmentManager,WarningDialog.TAG)
                        }
                        else->{
                            progressDialog.dismissLoading()
                            WarningDialog(this,Constants.Information,it.data.responseMessage).show(supportFragmentManager,WarningDialog.TAG)
                        }
                    }
                }
                is ResponseHandle.Error->{
                    progressDialog.dismissLoading()
                    ErrorDialog(this,Constants.Error,Constants.ServerFailure).show(supportFragmentManager,ErrorDialog.TAG)
                }
                is ResponseHandle.Network->{
                    NoInternetConnection(this,Constants.Information,Constants.NetworkMessage).show(supportFragmentManager,NoInternetConnection.TAG)
                }
            }
        }
    }
}