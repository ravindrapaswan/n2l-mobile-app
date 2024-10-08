package practice.english.n2l.ui.profile

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.JsonObject
import practice.english.n2l.R
import practice.english.n2l.adapter.ViewFriendAdapter
import practice.english.n2l.api.ResponseHandle
import practice.english.n2l.api.RetrofitService
import practice.english.n2l.bridge.ConfirmationImp
import practice.english.n2l.bridge.CustomWarningImp
import practice.english.n2l.bridge.SuccessImp
import practice.english.n2l.bridge.ViewFriendImp
import practice.english.n2l.database.bao.FriendList
import practice.english.n2l.database.bao.UserDetail
import practice.english.n2l.databinding.ActivityViewFriendsBinding
import practice.english.n2l.dialog.ConfirmationDialog
import practice.english.n2l.dialog.CustomConfirmationDialog
import practice.english.n2l.dialog.CustomProgressDialog
import practice.english.n2l.dialog.CustomWarningDialog
import practice.english.n2l.dialog.ErrorDialog
import practice.english.n2l.dialog.NoInternetConnection
import practice.english.n2l.dialog.SuccessDialog
import practice.english.n2l.dialog.WarningDialog
import practice.english.n2l.repository.ProfileRepo
import practice.english.n2l.repository.ProfileRepoImp
import practice.english.n2l.util.Constants
import practice.english.n2l.viewmodel.ProfileViewModel
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class ViewFriendsActivity : AppCompatActivity(),ViewFriendImp {
    private val repository                           : ProfileRepo by lazy { ProfileRepoImp(RetrofitService.service,this) }
    private val viewModel                            : ProfileViewModel by lazy { ViewModelProvider(this, ProfileViewModel.Factory(repository))[ProfileViewModel::class.java] }
    private lateinit var binding                     : ActivityViewFriendsBinding
    private lateinit var userDetail                  : UserDetail
    private val progressDialog                       : CustomProgressDialog by lazy { CustomProgressDialog(this) }
    private val viewFriendAdapter                    : ViewFriendAdapter by lazy { ViewFriendAdapter(this) }
    private lateinit var friendList                  : FriendList

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_view_friends)
        binding.lifecycleOwner = this

        binding.apply {
            RecView.apply {
                adapter = viewFriendAdapter
                layoutManager = LinearLayoutManager(context)
                setHasFixedSize(true)
            }
            BtnBack.setOnClickListener {
                val intent = Intent(this@ViewFriendsActivity, UserProfileActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                startActivity(intent)
                finish()
            }
        }

        viewModel.friendList.observe(this) {
            when (it) {
                is ResponseHandle.Loading -> { progressDialog.startLoading() }
                is ResponseHandle.Success -> {
                    when (it.data!!.responseCode) {
                        800 -> {
                            progressDialog.dismissLoading()
                            val list = it.data.responseData.friendList
                            if(list.isNotEmpty()){
                                viewFriendAdapter.removeAllItems()
                                viewFriendAdapter.updateList(list)}
                            else {
                                viewFriendAdapter.removeAllItems()
                                dataNotFound()
                            }
                        }
                        300 -> {
                            progressDialog.dismissLoading()
                            viewFriendAdapter.removeAllItems()
                            CustomWarningDialog(this, Constants.Information,it.data.responseMessage,object : CustomWarningImp {
                                override fun onClickNo(context: Activity, dialog: Dialog?) {
                                    dialog?.dismiss()
                                    val intent = Intent(this@ViewFriendsActivity, UserProfileActivity::class.java)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                                    startActivity(intent)
                                    finish()
                                }
                            }).show(supportFragmentManager, CustomWarningDialog.TAG)
                        }

                        500 -> {
                            progressDialog.dismissLoading()
                            WarningDialog(this, Constants.Information, it.data.responseMessage).show(supportFragmentManager,
                                WarningDialog.TAG)
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

        viewModel.unfriend.observe(this) {
            when (it) {
                is ResponseHandle.Loading -> { progressDialog.startLoading() }
                is ResponseHandle.Success -> {
                    when (it.data!!.responseCode) {
                        800 -> {
                            progressDialog.dismissLoading()
                            val jsonParams = JsonObject()
                            jsonParams.addProperty("userid",userDetail.userId)
                            viewModel.getFriendList(jsonParams)
                        }
                        300 -> {
                            progressDialog.dismissLoading()
                            WarningDialog(this, Constants.Information, it.data.responseMessage).show(supportFragmentManager, WarningDialog.TAG)
                        }

                        500 -> {
                            progressDialog.dismissLoading()
                            WarningDialog(this, Constants.Information, it.data.responseMessage).show(supportFragmentManager,
                                WarningDialog.TAG)
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
            val jsonParams = JsonObject()
            jsonParams.addProperty("userid",userDetail.userId)
            viewModel.getFriendList(jsonParams)
            //EventBus.getDefault().removeStickyEvent(userDetailStickyEvent);
        }
    }
    private fun dataNotFound() {
        SuccessDialog(this@ViewFriendsActivity,
            Constants.Information, getString(R.string.record_not_available),object: SuccessImp {
                override fun onYes(context: Activity, dialog: Dialog?) {
                    dialog!!.dismiss()
                    startActivity(
                        Intent().setClassName(getString(R.string.project_package_name),
                            "practice.english.n2l.ui.profile.UserProfileActivity"))
                }
            }).show(supportFragmentManager, SuccessDialog.TAG)
    }
    override fun onUnFriend(friendList: FriendList) {
        ConfirmationDialog(this, Constants.Information,getString(R.string.unfriend_msg,friendList.friendName),object: ConfirmationImp {
            override fun onClickYes(context: Activity, dialog: Dialog?) {
                dialog!!.dismiss()
                val jsonParams = JsonObject()
                jsonParams.addProperty("userid",userDetail.userId)
                jsonParams.addProperty("recordid",friendList.recordId)
                jsonParams.addProperty("friendid",friendList.friendId)
                viewModel.getUnfriend(jsonParams)
            }

            override fun onClickNo(context: Activity, dialog: Dialog?) {
                dialog!!.dismiss()
            }
        }
        ).show(supportFragmentManager, CustomConfirmationDialog.TAG)

    }
}