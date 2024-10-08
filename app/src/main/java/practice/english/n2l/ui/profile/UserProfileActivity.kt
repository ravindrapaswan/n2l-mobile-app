package practice.english.n2l.ui.profile

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import practice.english.n2l.R
import practice.english.n2l.databinding.ActivityUserProfileBinding

class UserProfileActivity : AppCompatActivity() {
    private lateinit var binding          : ActivityUserProfileBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_user_profile)

        binding.BtnChangePin.setOnClickListener {
           PinChangeDialog(this).show(supportFragmentManager, PinChangeDialog.TAG)
        }

        binding.BtnAddFriend.setOnClickListener {
            startActivity(Intent().setClassName(getString(R.string.project_package_name), "practice.english.n2l.ui.profile.AddFriendActivity"))
            //finishAndRemoveTask()
        }

        binding.BtnFriendList.setOnClickListener {
            startActivity(Intent().setClassName(getString(R.string.project_package_name), "practice.english.n2l.ui.profile.ViewFriendsActivity"))
            //finishAndRemoveTask()
        }

        binding.BtnUpdateProfile.setOnClickListener {
            startActivity(Intent().setClassName(getString(R.string.project_package_name), "practice.english.n2l.ui.profile.UpdateProfileActivity"))
        }

        binding.BtnFriendViewAcceptReject.setOnClickListener {
            startActivity(Intent().setClassName(getString(R.string.project_package_name), "practice.english.n2l.ui.profile.FriendRequestActivity"))
        }

        binding.LinBackNav.setOnClickListener {
            startActivity(Intent().setClassName(getString(R.string.project_package_name), "practice.english.n2l.ui.HomeActivity"))
            //finishAndRemoveTask()
        }
    }
}

/*
 ConfirmationDialog(this, getString(R.string.heading_information), getString(R.string.msg_end_practice), object : ConfirmationImp
            {
                override fun onClickYes(context: Activity, dialog: Dialog?) {
                    dialog!!.dismiss()
                    stopRecording(1)
                }
                override fun onClickNo(context: Activity, dialog: Dialog?) {
                    dialog!!.dismiss()
                    stopRecording(2)
                }
            }).show(supportFragmentManager, ConfirmationDialog.TAG)
* */