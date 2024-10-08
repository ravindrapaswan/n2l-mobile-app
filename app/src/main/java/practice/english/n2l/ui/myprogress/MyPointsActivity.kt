package practice.english.n2l.ui.myprogress

import android.content.Intent
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import practice.english.n2l.R
import practice.english.n2l.adapter.PracticePointDetailsAdapter
import practice.english.n2l.database.bao.PointData
import practice.english.n2l.database.bao.PointDetails
import practice.english.n2l.database.bao.PointMessageEvent
import practice.english.n2l.databinding.ActivityMyPointsBinding
import practice.english.n2l.ui.BaseActivity
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class MyPointsActivity : BaseActivity() {
    //private val repository                           : PracticeRepo by lazy { PracticeRepoImp(RetrofitService.service, this) }
    //private val viewModel                            : PracticeViewModel by lazy { ViewModelProvider(this, PracticeViewModel.Factory(repository))[PracticeViewModel::class.java] }
    private lateinit var binding                     : ActivityMyPointsBinding
    private lateinit var practicePointDetailsAdapter : PracticePointDetailsAdapter
    private lateinit var pointData                   : PointData
    private lateinit var pointDetails                : List<PointDetails>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_my_points)

        practicePointDetailsAdapter = PracticePointDetailsAdapter { clickedItem ->
            // Handle item click, for example, show an image
            showPracticeVideo(clickedItem.filePath)
        }
        binding.apply {
            RecView.apply {
                adapter = practicePointDetailsAdapter
                layoutManager = LinearLayoutManager(context)
                setHasFixedSize(true)
            }

            BtnBack.setOnClickListener {
                startActivity(
                    Intent().setClassName(getString(R.string.project_package_name),
                        "practice.english.n2l.ui.myprogress.MyProgressHomeActivity")
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
                //finishAndRemoveTask()
            }
        }
    }
    private fun showPracticeVideo(url:String) {
        EventBus.getDefault().postSticky(pointData.url+url)
        startActivity(Intent().setClassName(getString(R.string.project_package_name),
            "practice.english.n2l.player.VideoPlayerActivity"))
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
            binding.txtEarnPoint.text=getString(R.string.total_earn_points, this.pointData.totalPoint)
            practicePointDetailsAdapter.updateItems(pointDetails)
            //EventBus.getDefault().removeStickyEvent(pointMessageEvent)
        }
    }
}