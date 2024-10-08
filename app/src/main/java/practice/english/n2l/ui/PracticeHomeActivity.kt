package practice.english.n2l.ui


import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import practice.english.n2l.R
import practice.english.n2l.database.bao.UserDetail
import practice.english.n2l.databinding.ActivityPracticeBinding
import practice.english.n2l.ui.fragment.MultiPracticeFragment
import practice.english.n2l.ui.fragment.SinglePracticeFragment
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class PracticeHomeActivity : BaseActivity() {
    private lateinit var binding    : ActivityPracticeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_practice)

        binding.ImgSingleStatus.visibility=View.GONE
        binding.ImgMultiStatus.visibility=View.GONE

        binding.BtnSinglePractice.setOnClickListener {
            navigation(1)
            binding.ImgSingleStatus.visibility=View.VISIBLE
            binding.ImgMultiStatus.visibility=View.GONE
        }
        binding.BtnMultiPractice.setOnClickListener {
            navigation(2)
            binding.ImgMultiStatus.visibility=View.VISIBLE
            binding.ImgSingleStatus.visibility=View.GONE
        }
        binding.LinBackNav.setOnClickListener {
            startActivity(Intent().setClassName(getString(R.string.project_package_name), "practice.english.n2l.ui.HomeActivity"))
            //finishAndRemoveTask()
        }
    }
    private fun navigation(n:Int) {
        val transactionHomeFragments = supportFragmentManager.beginTransaction()
        if(n==1) {
            transactionHomeFragments.replace(R.id.frame_container, SinglePracticeFragment())
        }
        else {
            transactionHomeFragments.replace(R.id.frame_container, MultiPracticeFragment())
        }
        transactionHomeFragments.addToBackStack(null)
        transactionHomeFragments.commit()
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
        /*if (userDetailStickyEvent != null) {
            //Toast.makeText(this, userDetail.userId, Toast.LENGTH_LONG).show()
            //EventBus.getDefault().removeStickyEvent(userDetailStickyEvent);
        }*/
    }
}