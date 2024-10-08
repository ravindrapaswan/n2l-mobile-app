package practice.english.n2l.ui.profile

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import practice.english.n2l.R
import practice.english.n2l.api.ResponseHandle
import practice.english.n2l.api.RetrofitService
import practice.english.n2l.bridge.ConfirmationImp
import practice.english.n2l.database.bao.UserDetail
import practice.english.n2l.databinding.ActivityAddFriendBinding
import practice.english.n2l.dialog.CustomConfirmationDialog
import practice.english.n2l.dialog.CustomProgressDialog
import practice.english.n2l.dialog.ErrorDialog
import practice.english.n2l.dialog.NoInternetConnection
import practice.english.n2l.dialog.WarningDialog
import practice.english.n2l.repository.ProfileRepo
import practice.english.n2l.repository.ProfileRepoImp
import practice.english.n2l.ui.BaseActivity
import practice.english.n2l.util.Constants
import practice.english.n2l.util.TimestampConverter
import practice.english.n2l.viewmodel.ProfileViewModel
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.Date

class AddFriendActivity : BaseActivity() {

    private val repository                           : ProfileRepo by lazy { ProfileRepoImp(RetrofitService.service,this) }
    private val viewModel                            : ProfileViewModel by lazy { ViewModelProvider(this, ProfileViewModel.Factory(repository))[ProfileViewModel::class.java] }
    private lateinit var binding                     : ActivityAddFriendBinding
    private lateinit var userDetail                  : UserDetail
    private val progressDialog                       : CustomProgressDialog by lazy { CustomProgressDialog(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_friend)
        binding.profileViewModel = viewModel
        binding.lifecycleOwner = this

        viewModel.friendRequest.observe(this) {
            when(it) {
                is ResponseHandle.Loading->{progressDialog.startLoading()}
                is ResponseHandle.Success-> {
                    when (it.data!!.responseCode) {
                        800 -> {
                            progressDialog.dismissLoading()
                            CustomConfirmationDialog(this, Constants.Information,getString(R.string.dialog_success_friend_request_share,viewModel.friendInfo.value),
                                getString(R.string.label_Share),getString(R.string.cancel),object: ConfirmationImp{
                                    override fun onClickYes(context: Activity, dialog: Dialog?) {
                                        dialog?.dismiss()
                                        val intent = Intent(Intent.ACTION_SEND)//9907414544
                                        intent.type = "text/plain"
                                        var url="https://n2lacademy.in/ShareToOtherApp?recordId="+it.data.responseData.sendFriendRequest.recordId.toString() +
                                                "&friendId="+userDetail.userId+"&friendName="+userDetail.userName+
                                                "&friendMobileNumber="+userDetail.userMobileNumber+"&friendRequestDate="+TimestampConverter.convertDateToTime(Date())+
                                                "&myFriendMob="+viewModel.mobileNum.value
                                        url=url.replace(" ","%20")
                                        intent.putExtra(Intent.EXTRA_TEXT, url)
                                        startActivity(Intent.createChooser(intent, "choose one"));
                                    }
                                    override fun onClickNo(context: Activity, dialog: Dialog?) {
                                        dialog?.dismiss()
                                    }
                                }
                            ).show(supportFragmentManager, CustomConfirmationDialog.TAG)

                        }
                        in 300.. 306  -> {
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

        binding.ImgContactList.setOnClickListener {
          if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
              pickContact()
            } else {
                contactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
            }
        }
        binding.LinBackNav.setOnClickListener {
            val intent = Intent(this@AddFriendActivity, UserProfileActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
            finish()
            //startActivity(Intent().setClassName(getString(R.string.project_package_name), "practice.english.n2l.ui.HomeActivity"))
            //finishAndRemoveTask()
        }
    }
    private val contactsPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                pickContact()
            } else {
                // Permission denied, handle accordingly
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    private fun pickContact() {
        val intent = Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
        pickContactLauncher.launch(intent)
    }
    private val pickContactLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data: Intent? = result.data
                data?.data?.let { contactUri ->
                    // Handle the picked contact URI here
                    val projection = arrayOf(
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                        ContactsContract.CommonDataKinds.Phone.NUMBER
                    )

                    contentResolver.query(contactUri, projection, null, null, null)?.use { cursor ->
                        val nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                        val numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

                        if (nameIndex == -1 || numberIndex == -1) {
                            // The required columns were not found in the cursor
                            Toast.makeText(this@AddFriendActivity, "Contacts data not available", Toast.LENGTH_SHORT).show()
                            return@use
                        }

                        if (cursor.moveToFirst()) {
                            val name = cursor.getString(nameIndex)
                            val number = cursor.getString(numberIndex)
                            // Use name and number as needed
                            val mobileNumber = extractMobileNumber(number)
                            viewModel.mobileNum.value=mobileNumber
                            if(name.isNullOrEmpty())
                                viewModel.friendInfo.value=mobileNumber
                            else
                                viewModel.friendInfo.value=name

                        }
                    }
                }
            } else {
                // The user canceled the operation or an error occurred
                Toast.makeText(this@AddFriendActivity, "Failed to pick a contact", Toast.LENGTH_SHORT).show()
            }
        }
    private fun extractMobileNumber(phoneNumber: String): String {
        // Remove any non-numeric characters from the phone number
        val numericPhoneNumber = phoneNumber.replace("\\D".toRegex(), "")
        // Check if the phone number starts with the country code
        if (numericPhoneNumber.startsWith("91")) {
            // Remove the country code prefix
            return numericPhoneNumber.substring(2)
        }
        else if (numericPhoneNumber.startsWith("0")) {
            // Remove the country code prefix
            return numericPhoneNumber.substring(1)
        }
        return numericPhoneNumber
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
            binding.userDetailObject =userDetail
            //EventBus.getDefault().removeStickyEvent(userDetailStickyEvent);
        }
    }

}