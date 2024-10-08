package practice.english.n2l.player

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.MediaController
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import practice.english.n2l.R
import practice.english.n2l.database.bao.MultiPracticeObject
import practice.english.n2l.database.bao.SelfPracticeObject
import practice.english.n2l.databinding.ActivityInstuctionVideoPlayerBinding
import practice.english.n2l.ui.PracticeHomeActivity
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class InstructionVideoPlayerActivity : AppCompatActivity() {
    private lateinit var binding            : ActivityInstuctionVideoPlayerBinding
    private  var  selfPracticeObjectContext : Activity?=null
    private  var  multiPracticeObjectContext: Activity?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= DataBindingUtil.setContentView(this,R.layout.activity_instuction_video_player)

        binding.imgCancel.setOnClickListener {
            binding.videoView.pause()
            binding.videoView.stopPlayback()
            if(selfPracticeObjectContext!=null) {
                val intent = Intent(selfPracticeObjectContext, PracticeHomeActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                startActivity(intent)
                finish()
            }
            else if(multiPracticeObjectContext!=null) {
                val intent = Intent(multiPracticeObjectContext, PracticeHomeActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                startActivity(intent)
                finish()
            }
            else
                finish()
        }
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }
    private fun playVideoLocalPathSelfPractice(context: Activity, localPath: String) {
        runOnUiThread {
            val mediaController = MediaController(this@InstructionVideoPlayerActivity)
            mediaController.setAnchorView(binding.videoView)
            binding.videoView.apply {
                setMediaController(mediaController)
                setBackgroundColor(Color.TRANSPARENT)
                setVideoPath(localPath)
                setOnPreparedListener {
                    binding.videoView.apply{ requestFocus()
                        start()
                    }
                }
                setOnCompletionListener { mp ->
                    mp.release()
                    println("Video Finish")
                    finish()
                    val intent = Intent(context, PracticeHomeActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    startActivity(intent)
                    finish()
                }
            }
        }
    }
    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        EventBus.getDefault().unregister(this)
        super.onStop()
    }
    @Subscribe(sticky = true,threadMode = ThreadMode.BACKGROUND)
    fun onPlayVideoEvent(selfPracticeObject: SelfPracticeObject) {
        val selfPracticeObjectStickyEvent= EventBus.getDefault().getStickyEvent(SelfPracticeObject::class.java)
        if (selfPracticeObjectStickyEvent != null) {
            this.selfPracticeObjectContext=selfPracticeObjectStickyEvent.context
            playVideoLocalPathSelfPractice(selfPracticeObjectStickyEvent.context,selfPracticeObjectStickyEvent.practiceLocalFilePath)
            EventBus.getDefault().removeStickyEvent(selfPracticeObjectStickyEvent);
        }
    }
    @Subscribe(sticky = true,threadMode = ThreadMode.BACKGROUND)
    fun onPlayVideoEvent(multiPracticeObject: MultiPracticeObject) {
        val multiPracticeObjectStickyEvent= EventBus.getDefault().getStickyEvent(MultiPracticeObject::class.java)
        if (multiPracticeObjectStickyEvent != null) {
            this.multiPracticeObjectContext=multiPracticeObjectStickyEvent.context
            playVideoLocalPathSelfPractice(multiPracticeObjectStickyEvent.context,multiPracticeObjectStickyEvent.filePath)
            EventBus.getDefault().removeStickyEvent(multiPracticeObjectStickyEvent)
        }
    }

    private val onBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            backScreen()
        }
    }
    fun backScreen() {
        if(selfPracticeObjectContext!=null) {
            val intent = Intent(selfPracticeObjectContext, PracticeHomeActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
            finish()
        }
        else if(multiPracticeObjectContext!=null) {
            val intent = Intent(multiPracticeObjectContext, PracticeHomeActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
            finish()
        }
        else
            finish()
    }
}