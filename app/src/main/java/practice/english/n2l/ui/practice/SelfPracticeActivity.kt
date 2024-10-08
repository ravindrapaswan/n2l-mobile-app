package practice.english.n2l.ui.practice

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.Base64
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import androidx.activity.OnBackPressedCallback
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.Config.RETURN_CODE_CANCEL
import com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS
import com.arthenica.mobileffmpeg.FFmpeg
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import practice.english.n2l.R
import practice.english.n2l.api.ResponseHandle
import practice.english.n2l.api.RetrofitService
import practice.english.n2l.bridge.ConfirmationImp
import practice.english.n2l.bridge.CustomWarningImp
import practice.english.n2l.bridge.SuccessImp
import practice.english.n2l.bridge.VideoGeneratedSuccessfullyImp
import practice.english.n2l.database.AppDatabase
import practice.english.n2l.database.bao.AppExceptionLog
import practice.english.n2l.database.bao.Audio
import practice.english.n2l.database.bao.BlankTemplates
import practice.english.n2l.database.bao.LastPageTemplates
import practice.english.n2l.database.bao.MyVideos
import practice.english.n2l.database.bao.PracticeAudioExceptionLog
import practice.english.n2l.database.bao.QuestionInfo
import practice.english.n2l.database.bao.Questions
import practice.english.n2l.database.bao.SelfPracticeBack
import practice.english.n2l.database.bao.SelfPracticeObject
import practice.english.n2l.database.bao.SingleTemplates
import practice.english.n2l.database.bao.SingleUserMessageEvent
import practice.english.n2l.database.bao.StudentProfile
import practice.english.n2l.database.bao.UserDetail
import practice.english.n2l.databinding.ActivitySelfPracticeBinding
import practice.english.n2l.dialog.ConfirmationDialog
import practice.english.n2l.dialog.CustomPercentageProgressbarDialog
import practice.english.n2l.dialog.CustomProgressDialog
import practice.english.n2l.dialog.CustomWarningDialog
import practice.english.n2l.dialog.ErrorDialog
import practice.english.n2l.dialog.NoInternetConnection
import practice.english.n2l.dialog.SuccessDialog
import practice.english.n2l.dialog.VideoConfirmationDialog
import practice.english.n2l.dialog.VideoGeneratedSuccessfullyDialog
import practice.english.n2l.dialog.WarningDialog
import practice.english.n2l.repository.PracticeRepo
import practice.english.n2l.repository.PracticeRepoImp
import practice.english.n2l.ui.BaseActivity
import practice.english.n2l.ui.PracticeHomeActivity
import practice.english.n2l.util.Constants
import practice.english.n2l.util.TimestampConverter
import practice.english.n2l.util.Utilities
import practice.english.n2l.viewmodel.PracticeViewModel
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.net.URL
import java.text.DecimalFormat
import java.util.Date
import java.util.Locale

class SelfPracticeActivity : BaseActivity() {
    private val appDatabase                         : AppDatabase by lazy { AppDatabase.getInstance(this) }
    private val repository                          : PracticeRepo by lazy { PracticeRepoImp(RetrofitService.service, this) }
    private val viewModel                           : PracticeViewModel by lazy { ViewModelProvider(this, PracticeViewModel.Factory(repository))[PracticeViewModel::class.java] }
    private val progressDialog                      : CustomProgressDialog by lazy { CustomProgressDialog(this) }
    private val customPercentageProgressbarDialog   : CustomPercentageProgressbarDialog by lazy { CustomPercentageProgressbarDialog(this) }
    private lateinit var binding                    : ActivitySelfPracticeBinding

    private var mRecorder                           : MediaRecorder? = null
    private var countdownTimer                      : CountDownTimer?=null
    private var milliSeconds                        = 6000L
    private var pauseMilliSeconds                   = 1000L
    private var mTimeLeftInMillis                   : Long = milliSeconds
    private lateinit var userDetail                 : UserDetail

    private lateinit var questionInfoEvent          : QuestionInfo
    private var questionsList                       : List<Questions>? = null
    private var iteratorList                        : ListIterator<Questions>? = null
    private lateinit var questions                  : Questions
    private var isPaused                            = false
    private var isRecording                         = false
    private var mTimerRunning                       = false
    private lateinit var filePath                   : String
    private lateinit var audio                      : Audio
    private lateinit var templates                  : SingleTemplates
    private lateinit var studentProfile             : StudentProfile
    private lateinit var blankTemplates             : BlankTemplates
    private lateinit var lastPageTemplates          : LastPageTemplates
    private lateinit var myVideos                   : MyVideos
    private lateinit var practiceAudioExceptionLog  : PracticeAudioExceptionLog
    //private var scaleAnimation                  = ScaleAnimation(1f, 2f, 1f, 2f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
    private val RECORD_PERMISSION_REQUEST_CODE = 101
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_self_practice)
        binding.practiceViewModel = viewModel
        binding.lifecycleOwner = this

        binding.BtnEnd.visibility = View.INVISIBLE

        binding.BtnEnd.setOnClickListener {
            VideoConfirmationDialog(this, getString(R.string.heading_information), getString(R.string.msg_end_practice), object : ConfirmationImp {
                override fun onClickYes(context: Activity, dialog: Dialog?) {
                    dialog!!.dismiss()
                    stopRecording(1)
                }
                override fun onClickNo(context: Activity, dialog: Dialog?) {
                    dialog!!.dismiss()
                    stopRecording(2)
                }
            }).show(supportFragmentManager, VideoConfirmationDialog.TAG)
        }

        binding.BtnPause.setOnClickListener {
            when{
                isPaused -> resumeTimer()
                isRecording -> pauseTimer()
            }
        }

        viewModel.uploadPracticeVideo.observe(this){
            when(it) {
                is ResponseHandle.Loading->{
                    progressDialog.setMessage(getString(R.string.loading))
                    progressDialog.startLoading()
                }
                is ResponseHandle.Success ->{
                    when (it.data!!.responseCode) {
                        800 -> {
                            progressDialog.dismissLoading()
                            val uploadResult = it.data.responseData.uploadResult[0]
                            CoroutineScope(Dispatchers.IO).launch {
                                withContext(Dispatchers.IO) {
                                    val originalFileMp4=File(myVideos.practiceLocalFilePath!!)
                                    val practiceVideoDir = File(filesDir, "practiceVideo")
                                    if (!practiceVideoDir.exists()) practiceVideoDir.mkdirs()
                                    val newFileMp4=File(practiceVideoDir.absolutePath + File.separator + "${uploadResult.practiceSno}.mp4")
                                    originalFileMp4.copyTo(newFileMp4, true)
                                    if(newFileMp4.exists())originalFileMp4.delete()
                                    appDatabase.myVideosDao().updateMyVideoUploadStatus(uploadResult.practiceSno,uploadResult.filePath,newFileMp4.absolutePath,uploadResult.quizId)
                                    val myVideos=appDatabase.myVideosDao().getMyVideosByPracticeNoAndQuizID(uploadResult.practiceSno,uploadResult.quizId)
                                    appDatabase.questionsDao().deleteAllQuestionsExistQuestionImageAsBlank(questionInfoEvent.quizId)
                                    appDatabase.answersDao().deleteAllAnswersExistQuestionImageAsBlank(questionInfoEvent.quizId)
                                    deleteQuizFolder()
                                    runOnUiThread {
                                        VideoGeneratedSuccessfullyDialog(this@SelfPracticeActivity, Constants.Information, getString(R.string.video_status_complete_msg),object: VideoGeneratedSuccessfullyImp{
                                            override fun onViewVideo(context: Activity, dialog: Dialog?) {
                                                dialog!!.dismiss()
                                                EventBus.getDefault().postSticky(SelfPracticeObject(context,myVideos.practiceLocalFilePath!!))
                                                startActivity(Intent().setClassName(getString(R.string.project_package_name), "practice.english.n2l.player.VideoPlayerActivity"))
                                            }
                                            override fun onShareVideo(context: Activity, dialog: Dialog?) {
                                                dialog!!.dismiss()
                                                EventBus.getDefault().postSticky(SelfPracticeBack("back"))
                                                try {
                                                    val path = FileProvider.getUriForFile(this@SelfPracticeActivity, applicationContext.packageName + ".provider", File(myVideos.practiceLocalFilePath!!).absoluteFile)
                                                    val pdfIntent = Intent(Intent.ACTION_SEND)
                                                    pdfIntent.setDataAndType(path, "video/*")
                                                    pdfIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                                    pdfIntent.putExtra(Intent.EXTRA_STREAM, path)
                                                    pdfIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                    startActivity(Intent.createChooser(pdfIntent, "Share file via"))
                                                } catch (e: java.lang.Exception) {
                                                    e.printStackTrace()
                                                    Log.d("pepe", e.toString())
                                                }
                                            }
                                            override fun onBack(context: Activity, dialog: Dialog?) {
                                                dialog!!.dismiss()
                                                startActivity(Intent().setClassName(getString(R.string.project_package_name), "practice.english.n2l.ui.PracticeHomeActivity"))
                                            }
                                        }).show(supportFragmentManager, VideoGeneratedSuccessfullyDialog.TAG)
                                    }
                                }
                            }
                        }
                        in 300..302 -> {
                            progressDialog.dismissLoading()
                            WarningDialog(this, Constants.Information,it.data.responseMessage).show(this.supportFragmentManager,
                                WarningDialog.TAG)}
                        500 -> {
                            progressDialog.dismissLoading()
                            WarningDialog(this, Constants.Information,it.data.responseMessage).show(this.supportFragmentManager,
                                WarningDialog.TAG)
                        }
                        else -> {
                            progressDialog.dismissLoading()
                            WarningDialog(this, Constants.Information,it.data.responseMessage).show(this.supportFragmentManager,
                                WarningDialog.TAG)
                        }
                    }
                }
                is ResponseHandle.Error->{
                    progressDialog.dismissLoading()
                    ErrorDialog(this, Constants.Error, Constants.ServerFailure).show(this.supportFragmentManager,
                        ErrorDialog.TAG)
                }
                is ResponseHandle.Network->{
                    NoInternetConnection(this, Constants.Information, Constants.NetworkMessage).show(this.supportFragmentManager,
                        NoInternetConnection.TAG)
                }
            }
        }
        viewModel.uploadAppPracticeAudio.observe(this){
            when(it) {
                is ResponseHandle.Loading->{
                    progressDialog.setMessage(getString(R.string.loading))
                    progressDialog.startLoading()
                }
                is ResponseHandle.Success ->{
                    when (it.data!!.responseCode) {
                        800 -> {
                            progressDialog.dismissLoading()
                            CoroutineScope(Dispatchers.IO).launch {
                                withContext(Dispatchers.IO) {
                                    deleteQuizFolder()
                                }
                            }
                            val uploadResult = it.data.responseData.uploadResult[0]
                            val jsonParams = JsonObject()
                            jsonParams.addProperty("UserId", practiceAudioExceptionLog.userId)
                            jsonParams.addProperty("QuizId", practiceAudioExceptionLog.quizId)
                            jsonParams.addProperty("Exception", practiceAudioExceptionLog.exception)
                            jsonParams.addProperty("ExceptionDetails", practiceAudioExceptionLog.stackException)
                            jsonParams.addProperty("AppPage", practiceAudioExceptionLog.appPage)
                            jsonParams.addProperty("CreateDate",TimestampConverter.dateToString(practiceAudioExceptionLog.createDate))
                            viewModel.saveAppExceptionLog(jsonParams)
                        }
                        in 300..302 -> {
                            progressDialog.dismissLoading()
                            WarningDialog(this, Constants.Information,it.data.responseMessage).show(this.supportFragmentManager,
                                WarningDialog.TAG)
                        }
                        500 -> {
                            progressDialog.dismissLoading()
                            WarningDialog(this, Constants.Information,it.data.responseMessage).show(this.supportFragmentManager,
                                WarningDialog.TAG)
                        }
                        else -> {
                            progressDialog.dismissLoading()
                            WarningDialog(this, Constants.Information,it.data.responseMessage).show(this.supportFragmentManager,
                                WarningDialog.TAG)
                        }
                    }
                }
                is ResponseHandle.Error->{
                    progressDialog.dismissLoading()
                    ErrorDialog(this, Constants.Error, Constants.ServerFailure).show(this.supportFragmentManager,
                        ErrorDialog.TAG)
                }
                is ResponseHandle.Network->{
                    NoInternetConnection(this, Constants.Information, Constants.NetworkMessage).show(this.supportFragmentManager,
                        NoInternetConnection.TAG)
                }
            }
        }
        viewModel.appExceptionLog.observe(this){
            when(it) {
                is ResponseHandle.Loading->{
                    progressDialog.setMessage(getString(R.string.loading))
                    progressDialog.startLoading()
                }
                is ResponseHandle.Success ->{
                    when (it.data!!.responseCode) {
                        800 -> {
                            progressDialog.dismissLoading()
                            CustomWarningDialog(this@SelfPracticeActivity, Constants.Information,getString(R.string.dialog_practice_exception_message),object : CustomWarningImp {
                                override fun onClickNo(context: Activity, dialog: Dialog?) {
                                    dialog?.dismiss()
                                    val intent = Intent(this@SelfPracticeActivity, PracticeHomeActivity::class.java)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                                    startActivity(intent)
                                    finish()
                                }
                            }).show(supportFragmentManager, CustomWarningDialog.TAG)
                        }
                        in 300..302 -> {
                            progressDialog.dismissLoading()
                            WarningDialog(this, Constants.Information,it.data.responseMessage).show(this.supportFragmentManager,
                                WarningDialog.TAG)}
                        500 -> {
                            progressDialog.dismissLoading()
                            WarningDialog(this, Constants.Information,it.data.responseMessage).show(this.supportFragmentManager,
                                WarningDialog.TAG)
                        }
                        else -> {
                            progressDialog.dismissLoading()
                            WarningDialog(this, Constants.Information,it.data.responseMessage).show(this.supportFragmentManager,
                                WarningDialog.TAG)
                        }
                    }
                }
                is ResponseHandle.Error->{
                    progressDialog.dismissLoading()
                    ErrorDialog(this, Constants.Error, Constants.ServerFailure).show(this.supportFragmentManager,
                        ErrorDialog.TAG)
                }
                is ResponseHandle.Network->{
                    NoInternetConnection(this, Constants.Information, Constants.NetworkMessage).show(this.supportFragmentManager,
                        NoInternetConnection.TAG)
                }
            }
        }

        binding.BtnMidEnd.setOnClickListener {
            ConfirmationDialog(this, getString(R.string.heading_information), getString(R.string.msg_end__mid_practice), object : ConfirmationImp {
                override fun onClickYes(context: Activity, dialog: Dialog?) {
                    dialog!!.dismiss()
                    //stopTimer()
                    stopRecording(2)
                }
                override fun onClickNo(context: Activity, dialog: Dialog?) {
                    dialog!!.dismiss()
                }
            }).show(supportFragmentManager, ConfirmationDialog.TAG)
        }

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    private fun pauseTimer() {
        if (mRecorder != null) mRecorder!!.pause()
        binding.BtnPause.setBackgroundResource(R.drawable.play_button)
        binding.imgReverseTimer.setBackgroundResource(R.drawable.voice_yellow)
        //stopSpeakLabel()
        binding.txtSpeak.visibility=View.INVISIBLE
        isPaused=true
        //pauseTimer()
        if(countdownTimer!=null)
            countdownTimer!!.cancel()
        mTimerRunning = false
    }

    private fun resumeTimer() {
        if(mRecorder!=null) mRecorder!!.resume()
        binding.BtnPause.setBackgroundResource(R.drawable.pause_button)
        if(mTimeLeftInMillis==0L)
            binding.imgReverseTimer.setBackgroundResource(R.drawable.voice_red)
        else
            binding.imgReverseTimer.setBackgroundResource(R.drawable.voice_green)
        //stopSpeakLabel()
        binding.txtSpeak.visibility=View.INVISIBLE
        isPaused=false
        startReverseCounter(mTimeLeftInMillis)
    }

    private fun startHandler() {
        runOnUiThread {
            mRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(this)
            } else {
                @Suppress("deprecation")
                MediaRecorder()
            }
            mRecorder!!.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)//Call this only before setOutputFormat().
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                filePath=getRecordingFilePath()
                setOutputFile(filePath)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                prepare()
                start()
                isPaused=false
                isRecording=true
            }
        }
    }

    private fun stopTimer() {
        if(countdownTimer!=null) countdownTimer!!.cancel()
        milliSeconds=0L
    }

    private fun updateTextUI(millisUntilFinished: Long) {
        val f = DecimalFormat("00")
        val min = (millisUntilFinished / 60000) % 60
        val seconds = (millisUntilFinished / 1000) % 60
        binding.ReverseTimer.text = getString(R.string.msg_timer, f.format(min), f.format(seconds))
        if(binding.ReverseTimer.text=="00:00"){
            binding.imgReverseTimer.setBackgroundResource(R.drawable.voice_red)
            binding.txtSpeak.visibility=View.INVISIBLE}
    }

    private fun getRecordingFilePath(): String {
        val audioDir = File(filesDir, questionInfoEvent.quizId.toString())
        if (!audioDir.exists()) audioDir.mkdirs()
        else
            if (audioDir.deleteRecursively()) audioDir.mkdir()
        val audioDirPath = audioDir.absolutePath
        val fileName = "${questionInfoEvent.quizId}.mp3"
        val recordingFile = File("$audioDirPath/$fileName")
        return recordingFile.path
    }
    private fun startReverseCounter(mTime:Long) {
        countdownTimer = object : CountDownTimer(mTime, 1000) {
            override fun onFinish() {
                binding.imgReverseTimer.setBackgroundResource(R.drawable.voice_red)
                binding.ReverseTimer.text = getString(R.string.msg_timer, "00", "00")
                mTimerRunning = false
                binding.txtSpeak.visibility=View.INVISIBLE
                nextQuiz()
            }
            override fun onTick(millisUntilFinished: Long) {
                mTimeLeftInMillis  = millisUntilFinished
                updateTextUI(millisUntilFinished)
            }
        }
        countdownTimer!!.start()
        mTimerRunning = true
    }

    private fun nextQuiz() {
        runOnUiThread {
            if (iteratorList!!.hasNext()) {
                if (mRecorder != null)  mRecorder!!.pause()
                questions = iteratorList!!.next()
                binding.TxtNoOfQuestion.text=getString(R.string.total_num_question, questions.questionSNO,questionInfoEvent.noOfQuestions)
                showPracticeQuestion(questions.questionImageBase64)
                binding.imgReverseTimer.setBackgroundResource(R.drawable.voice_red)
                //stopSpeakLabel()
                binding.txtSpeak.visibility=View.INVISIBLE
                Handler(Looper.getMainLooper()).postDelayed({
                    if(mTimeLeftInMillis==0L)
                        binding.imgReverseTimer.setBackgroundResource(R.drawable.voice_red)
                    else
                        binding.imgReverseTimer.setBackgroundResource(R.drawable.voice_green)
                    startSpeakLabel()
                    binding.txtSpeak.visibility=View.VISIBLE
                    //milliSeconds=6000L
                    if (mRecorder != null)  mRecorder!!.resume()
                    startReverseCounter(milliSeconds)
                },pauseMilliSeconds)
            } else {
               // stopRecording()
                stopTimer()
                binding.BtnPause.visibility = View.INVISIBLE
                binding.TxtNoOfQuestion.visibility=View.GONE
                binding.BtnMidEnd.visibility=View.INVISIBLE
                binding.imgReverseTimer.visibility=View.INVISIBLE
                binding.ReverseTimer.visibility=View.INVISIBLE
                binding.BtnEnd.visibility = View.VISIBLE
            }
        }
    }

    private fun stopRecording(flag:Int) {
        try {
            if (mRecorder != null) {
                mRecorder!!.stop()
                mRecorder!!.release()
                mRecorder = null
                stopTimer()
                //stopSpeakLabel()

                if(flag==1) {
                    //progressDialog.setMessage(getString(R.string.video_status_msg))
                    //progressDialog.startLoading()
                    //customPercentageProgressbarDialog.show()
                    customPercentageProgressbarDialog.startLoading()
                    CoroutineScope(Dispatchers.IO).launch {
                        async {  val answerInfo = viewModel.getAnswerInfoInTable(questionInfoEvent.quizId)
                            val answersList = viewModel.getAnswersPracticeSession(questionInfoEvent.quizId)
                            for (answer in answersList) {
                                if (answer.answerImageBase64 == "") {
                                    answer.answerImageBase64 = downloadAnswerImage(answerInfo.uRL, answer.answerImagePath)
                                    if(answer.answerImageBase64=="")
                                        answer.answerImageBase64=blankTemplates.fileData
                                    viewModel.updateAnswerBase64(answer.answerImageBase64, answer.answersId, answer.quizId)
                                }
                            } }.await()
                        async{
                            runOnUiThread {
                                customPercentageProgressbarDialog.setProgress(20)
                            }
                            saveImageAndCreateMp4(progressDialog) }.await()
                    }
                }
                else {
                    val intent = Intent(this@SelfPracticeActivity, PracticeHomeActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    startActivity(intent)
                    finish()
                }
            }
        }
        catch (ex:Exception) {
            Log.d("Error2",ex.toString())
            val intent = Intent(this@SelfPracticeActivity, PracticeHomeActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
            finish()
        }
    }
    private fun saveImageAndCreateMp4(progressDialog : CustomProgressDialog) = runBlocking{
        CoroutineScope(Dispatchers.IO).launch{
            try {
                withContext(Dispatchers.IO) {
                    getMasterResource(questionInfoEvent)
                    delay(100)
                    runOnUiThread {
                        customPercentageProgressbarDialog.setProgress(10)
                    }
                }

                withContext(Dispatchers.IO) {
                    convertAnswerImageToJPG(questionInfoEvent)
                    delay(100)
                    runOnUiThread {
                        customPercentageProgressbarDialog.setProgress(10)
                    }
                }

                withContext(Dispatchers.IO) {
                    firstImageCreation(questionInfoEvent, userDetail)
                    delay(100)
                    runOnUiThread {
                        customPercentageProgressbarDialog.setProgress(10)
                    }
                }

                withContext(Dispatchers.IO) {
                    preFirstLastTempImageMp4Creation(questionInfoEvent)
                    delay(100)
                    runOnUiThread {
                        customPercentageProgressbarDialog.setProgress(20)
                    }
                }

                withContext(Dispatchers.IO) {
                    finalMp4CreationProcess(questionInfoEvent)
                    delay(100)
                    runOnUiThread {
                        customPercentageProgressbarDialog.setProgress(50)
                    }
                }

                //progressDialog.dismissLoading()
                customPercentageProgressbarDialog.dismissLoading()

               withContext(Dispatchers.IO) {
                   // val attemptPractice=appDatabase.attemptPracticeDao().getAttemptPracticeNotUpload(questionInfoEvent.quizId)
                    val myVideos=appDatabase.myVideosDao().getMyVideosNotUpload(questionInfoEvent.quizId)
                    val file =File(myVideos.practiceLocalFilePath!!)
                    val requestFile= file.absoluteFile.asRequestBody("video/mp4".toMediaTypeOrNull())
                    val body = MultipartBody.Part.createFormData("mp4", file.name, requestFile)
                    val studentId = userDetail.userId.toRequestBody("application/json".toMediaTypeOrNull())
                    val dateOfVideoCreation = TimestampConverter.convertDateFormat(myVideos.practiceDate).toRequestBody("application/json".toMediaTypeOrNull())
                    val quizId = myVideos.quizId.toString().toRequestBody("application/json".toMediaTypeOrNull())
                    val singleMulti =myVideos.singleMulti.toString().toRequestBody("application/json".toMediaTypeOrNull())
                    val friendId ="".toRequestBody("application/json".toMediaTypeOrNull())
                    viewModel.uploadPracticeVideo(studentId, dateOfVideoCreation, quizId,singleMulti,friendId,body)
                }

                /*runOnUiThread {
                    SuccessDialog(this@SelfPracticeActivity, Constants.Information, getString(R.string.video_status_complete_msg), object : SuccessImp {
                        override fun onYes(context: Activity, dialog: Dialog?) {
                            dialog!!.dismiss()
                            val constraints = Constraints.Builder()
                                .setRequiredNetworkType(NetworkType.CONNECTED)
                                .build()
                            val workerRequest: WorkRequest = OneTimeWorkRequest.Builder(
                                UploadApiWorker::class.java)
                                .setInitialDelay(5, TimeUnit.SECONDS)
                                .setConstraints(constraints)
                                .build()
                            Log.i("WorkManager",workerRequest.toString())
                            WorkManager.getInstance(this@SelfPracticeActivity).enqueue(workerRequest)

                            startActivity(Intent().setClassName(getString(R.string.project_package_name), "practice.english.n2l.ui.HomeActivity"))
                            //this@SelfPracticeActivity.finishAndRemoveTask()
                        }
                    }).show(supportFragmentManager, SuccessDialog.TAG)
                }*/
            }
            catch (ex:Exception) {
                appDatabase.appExceptionLogDao().insert(AppExceptionLog(0,questionInfoEvent.quizId,ex.toString(),ex.stackTraceToString()))
                practiceAudioExceptionLog=PracticeAudioExceptionLog(userDetail.userId,questionInfoEvent.quizId,ex.toString(),ex.stackTraceToString(),"SelfPractice")
                runOnUiThread {
                    val completeVoiceRecodingMp3= "${questionInfoEvent.quizId}/${questionInfoEvent.quizId}.mp3"
                    val completeVoiceRecodingMp3Path = File(filesDir.absolutePath + File.separator + completeVoiceRecodingMp3)
                    val requestFile= completeVoiceRecodingMp3Path.absoluteFile.asRequestBody("audio/mp3".toMediaTypeOrNull())
                    val body = MultipartBody.Part.createFormData("audio", completeVoiceRecodingMp3Path.name, requestFile)
                    val studentId = userDetail.userId.toRequestBody("application/json".toMediaTypeOrNull())
                    val dateOfAudioCreation = TimestampConverter.dateToString(Date()).toRequestBody("application/json".toMediaTypeOrNull())
                    val quizId =questionInfoEvent.quizId.toString().toRequestBody("application/json".toMediaTypeOrNull())
                    val singleMulti ="1".toRequestBody("application/json".toMediaTypeOrNull())
                    val friendId ="".toRequestBody("application/json".toMediaTypeOrNull())
                    viewModel.uploadAppPracticeAudio(studentId, dateOfAudioCreation, quizId,singleMulti,friendId,body)
                }
            }
        }
    }

    private fun getMasterResource(questionInfoEvent : QuestionInfo) {
        audio=viewModel.getAudio(6)
        //audio=viewModel.getAudio(questionInfoEvent.videoOneSlideTime)
        templates=viewModel.getSingleTemplates()
        studentProfile=viewModel.getStudentProfile()
        blankTemplates=viewModel.getBlankTemplates()
        lastPageTemplates=viewModel.getLastPageTemplates()
    }
    private fun convertAnswerImageToJPG(questionInfoEvent : QuestionInfo) {
        val answersList   = viewModel.getAnswersPracticeSession(questionInfoEvent.quizId)
        val audioDir = File(filesDir, questionInfoEvent.quizId.toString())
        if (!audioDir.exists()) audioDir.mkdirs()
        for(answers in answersList) {
            val audioDirPath = audioDir.absolutePath
            val recordingFile = if(answers.questionSNO>9)
                File("$audioDirPath/aimg${answers.questionSNO}.jpg")
            else
                File("$audioDirPath/aimg0${answers.questionSNO}.jpg")
            val bytes: ByteArray = Base64.decode(answers.answerImageBase64, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.count())
            val fOut = FileOutputStream(recordingFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut)
            fOut.flush()
            fOut.close()
        }
    }
    private fun firstImageCreation(questionInfoEvent : QuestionInfo,userDetail : UserDetail){
        //val studentImage=File(filesDir.absolutePath + File.separator + "assets/${questionInfoEvent.quizId}.jpg")
        //val originalFile=File(filesDir.absolutePath + File.separator + "assets/firstpage1.jpg")
        val studentImage=File(studentProfile.photoLocalPath)
        val originalFile=File(templates.fileLocalPath)
        val newFile=File(filesDir.absolutePath + File.separator + "${questionInfoEvent.quizId}/firstImage.jpg")
        originalFile.copyTo(newFile, true)
        firstImageCreation(studentImage,originalFile,newFile,questionInfoEvent.topic+"\n("+questionInfoEvent.quizId+")",userDetail.userName)
    }
    private fun preFirstLastTempImageMp4Creation(questionInfoEvent : QuestionInfo) {
        val firstImageJpg= "${questionInfoEvent.quizId}/firstImage.jpg"
        val firstImageJpg4 = File(filesDir.absolutePath + File.separator + firstImageJpg)
        val lastImageJpg4 = File(lastPageTemplates.fileLocalPath)

        val destinationFirstFilePath4 = File(filesDir.absolutePath + File.separator + "${questionInfoEvent.quizId}/firstPage.mp4")
        val destinationLastFilePath4 = File(filesDir.absolutePath + File.separator + "${questionInfoEvent.quizId}/lastPage.mp4")
        val destinationTempRemainFilePath4 = File(filesDir.absolutePath + File.separator + "${questionInfoEvent.quizId}/tempRemainPage.mp4")

        val sourceFile4= "${questionInfoEvent.quizId}/aimg%02d.jpg"
        val sourceFilePath4 = File(filesDir.absolutePath + File.separator + sourceFile4)

        var slideTime = 1/ questionInfoEvent.videoOneSlideTime.toDouble()
        slideTime=String.format(Locale.US,"%.3f", slideTime).toDouble()

        val convertFirstJpgToMp4 ="-y -loop 1 -i $firstImageJpg4 -c:v libx264 -t 6 -r 30 -pix_fmt yuv420p -vf scale=720:480 $destinationFirstFilePath4"
        val convertLastJpgToMp4 ="-y -loop 1 -i $lastImageJpg4 -c:v libx264 -t 6 -r 30 -pix_fmt yuv420p -vf scale=720:480 $destinationLastFilePath4"
        val convertRemainJpgToMp4 ="-y -r $slideTime -i $sourceFilePath4 -c:v libx264 -r 30 -pix_fmt yuv420p -vf scale=720:480 $destinationTempRemainFilePath4"

        val rc11 = FFmpeg.execute(convertFirstJpgToMp4)
        val rc12 = FFmpeg.execute(convertLastJpgToMp4)
        val rc13 = FFmpeg.execute(convertRemainJpgToMp4)

    }
    private suspend fun finalMp4CreationProcess(questionInfoEvent : QuestionInfo) {
        //File(filesDir.absolutePath + File.separator + "assets/firstpage1.jpg").copyTo(File(filesDir.absolutePath + File.separator + "${questionInfoEvent.quizId}/aimg00.jpg"), true)
        //File(filesDir.absolutePath + File.separator + "assets/blank6.mp3").copyTo(File(filesDir.absolutePath + File.separator + "${questionInfoEvent.quizId}/blank6.mp3"), true)
        //File(filesDir.absolutePath + File.separator + audio.fileLocalPath).copyTo(File(filesDir.absolutePath + File.separator + "${questionInfoEvent.quizId}/standardBlank.mp3"), true)

        val destinationFirstFilePath4 = File(filesDir.absolutePath + File.separator + "${questionInfoEvent.quizId}/firstPage.mp4")
        val destinationLastFilePath4 = File(filesDir.absolutePath + File.separator + "${questionInfoEvent.quizId}/lastPage.mp4")
        val destinationTempRemainFilePath4 = File(filesDir.absolutePath + File.separator + "${questionInfoEvent.quizId}/tempRemainPage.mp4")

        File(audio.fileLocalPath).copyTo(File(filesDir.absolutePath + File.separator + "${questionInfoEvent.quizId}/standardBlank.mp3"), true)
        val destinationFile4= "${questionInfoEvent.quizId}/withoutVoice.mp4"
        val destinationFilePath4 = File(filesDir.absolutePath + File.separator + destinationFile4)

        val margeAllMp4="-y -i $destinationFirstFilePath4 -i $destinationTempRemainFilePath4 -i $destinationLastFilePath4 -filter_complex [0:v][1:v][2:v]concat=n=3:v=1[vv] -map [vv]  $destinationFilePath4"

        val blankMp3= "${questionInfoEvent.quizId}/standardBlank.mp3"
        val blankMp3Path = File(filesDir.absolutePath + File.separator + blankMp3)
        val completeVoiceRecodingMp3= "${questionInfoEvent.quizId}/${questionInfoEvent.quizId}.mp3"
        val completeVoiceRecodingMp3Path = File(filesDir.absolutePath + File.separator + completeVoiceRecodingMp3)

        val destinationMergeBlankWithCompleteVoiceFile= "${questionInfoEvent.quizId}/voiceRecording${questionInfoEvent.quizId}.mp3"
        val destinationMergeBlankWithCompleteVoiceFilePath = File(filesDir.absolutePath + File.separator + destinationMergeBlankWithCompleteVoiceFile)
        //val mergeBlankWithAudio = "-y -i $blankMp3Path -i $completeVoiceRecodingMp3Path -filter_complex concat=n=2:v=0:a=1 -vn -y $destinationMergeBlankWithCompleteVoiceFilePath"
        val mergeBlankWithAudio = "-y -i $blankMp3Path -i $completeVoiceRecodingMp3Path -filter_complex [0:0][1:0]concat=n=2:v=0:a=1[out] -map [out] $destinationMergeBlankWithCompleteVoiceFilePath"

        val sourceFileMp4= "${questionInfoEvent.quizId}/withoutVoice.mp4"
        val sourceFilePathMp4 = File(filesDir.absolutePath + File.separator + sourceFileMp4)
        val sourceFileMp3= "${questionInfoEvent.quizId}/voiceRecording${questionInfoEvent.quizId}.mp3"
        val sourceFilePathMp3 = File(filesDir.absolutePath + File.separator + sourceFileMp3)

        //val destinationFile5= "Download/${questionInfoEvent.quizId}.mp4"
        //val destinationFilePath5 = File(Environment.getExternalStorageDirectory().absolutePath + File.separator + destinationFile5)
        val finalMp4Dir = File(filesDir, "FinalMp4")
        if (!finalMp4Dir.exists()) finalMp4Dir.mkdirs()
        //val destinationFile5= "${finalMp4Dir.absoluteFile}/${questionInfoEvent.quizId}.mp4"
        val destinationFile5= "${finalMp4Dir.absoluteFile}/${Utilities.generateRandomFileName("mp4")}"
        val destinationFilePath5 = File(destinationFile5)
        val mergeMp4AndMp3="-y -i $sourceFilePathMp4 -i $sourceFilePathMp3 -c:v copy -c:a aac $destinationFilePath5"

        when(val rc1 = FFmpeg.execute(margeAllMp4)){
            RETURN_CODE_SUCCESS -> {
                Log.i(Config.TAG, "First Command execution completed successfully.")
                when(val rc2 = FFmpeg.execute(mergeBlankWithAudio)){
                    RETURN_CODE_SUCCESS -> {
                        Log.i(Config.TAG, "Second Command execution completed successfully.")
                        when(val rc3 = FFmpeg.execute(mergeMp4AndMp3)){
                            RETURN_CODE_SUCCESS -> {
                                Log.i(Config.TAG, "Third Command execution completed successfully.")
                            }
                            RETURN_CODE_CANCEL ->   Log.i(Config.TAG, "Third Command execution cancelled by user.")
                            else->{
                                Log.i(Config.TAG, String.format("Third Command execution failed with rc=%d and the output below.", rc3))
                                Config.printLastCommandOutput(Log.INFO)
                            }
                        }
                    }
                    RETURN_CODE_CANCEL -> Log.i(Config.TAG, "Second Command execution cancelled by user.")
                    else->{
                        Log.i(Config.TAG, String.format("Second Command execution failed with rc=%d and the output below.", rc2))
                        Config.printLastCommandOutput(Log.INFO)
                    }
                }
            }
            RETURN_CODE_CANCEL ->   Log.i(Config.TAG, " First Command execution cancelled by user.")
            else->{
                Log.i(Config.TAG, String.format("First Command execution failed with rc=%d and the output below.", rc1))
                Config.printLastCommandOutput(Log.INFO)
            }
        }

        myVideos=MyVideos(0,questionInfoEvent.quizId, questionInfoEvent.topic,
            null,destinationFilePath5.absolutePath,
            TimestampConverter.convertDateToddMMyy(Date())!!,
            TimestampConverter.convertDateToTime(Date()),1,null,null,null)
        appDatabase.myVideosDao().deleteMyVideosByQuizId(questionInfoEvent.quizId)
        appDatabase.myVideosDao().insertMyVideos(myVideos)

        /*val attemptPractice = AttemptPractice(
            0, questionInfoEvent.quizId, questionInfoEvent.topic,
            destinationFilePath5.absolutePath, Date(),1,0)
        appDatabase.attemptPracticeDao().deleteAttemptPracticeById(attemptPractice.quizId)
        appDatabase.attemptPracticeDao().insertAttemptPractice(attemptPractice)
        //viewModel.deleteAttemptPracticeById(attemptPractice.quizId)
        //viewModel.insertAttemptPractice(attemptPractice)
        */
    }
    private fun firstImageCreation(studentImage:File,originalFileName:File,newFileName:File,title:String,studentName:String) {
        try {

            if (newFileName.exists()) newFileName.delete()

            val outputStream: OutputStream = FileOutputStream(newFileName)

            val notMutableBitmap = BitmapFactory.decodeFile(originalFileName.absolutePath)
            val originalBitmap: Bitmap = notMutableBitmap.copy(Bitmap.Config.ARGB_8888, true)

            val tf = Typeface.create("Helvetica", Typeface.BOLD)
            val paint = Paint()
            paint.style = Paint.Style.FILL
            paint.color = Color.BLACK
            paint.typeface = tf
            paint.textAlign = Paint.Align.LEFT
            paint.textSize =  40.0f
            paint.setXfermode(PorterDuffXfermode(PorterDuff.Mode.SRC_OVER))
            var textRect = Rect()
            paint.getTextBounds(studentName, 0, studentName.length, textRect)

            val canvas = Canvas(originalBitmap)
            if (textRect.width() >= canvas.width - 4)
                paint.textSize = 40.0f
            val xPos = canvas.width.toFloat()/2 + -2
            val yPos = (canvas.height / 2 - (paint.descent() + paint.ascent()) / 2) + 0
            canvas.drawText(studentName.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }, xPos, yPos, paint)

            /*textRect = Rect()
            paint.textSize =  30.0f
            paint.getTextBounds(title, 0, title.length, textRect)
            val topicWidth = paint.measureText(title).toInt()
            val topicX = (canvas.width.toFloat() - topicWidth) / 2
            val topicY = canvas.height.toFloat() / 10
            canvas.drawText(title, topicX, topicY, paint)*/

            textRect = Rect()
            paint.textSize =  40f
            paint.getTextBounds(title, 0, title.length, textRect)
            val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                textSize = 40f
                color = 0xFF000000.toInt() // black color
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val topMargin = 10
            canvas.apply {
                save()
                val staticLayout = StaticLayout.Builder
                    .obtain(title, 0, title.length, textPaint, width)
                    .setAlignment(Layout.Alignment.ALIGN_CENTER)
                    .setLineSpacing(0f, 1f)
                    .setIncludePad(true)
                    .build()
                     //staticLayout.draw(canvas)
                val x = (width - staticLayout.width) / 2 // Calculate the x-coordinate for center alignment
                val y = topMargin.toFloat() // Top margin
                translate(x.toFloat(), y) // Translate the canvas to align the layout
                staticLayout.draw(this) // Draw the layout onto the canvas
                restore()
            }

            val studentImg: Bitmap  = BitmapFactory.decodeFile(studentImage.absolutePath)
            var width = studentImg.width
            var height = studentImg.height

            val wf = notMutableBitmap.width
            val hf = notMutableBitmap.height

            val stdWidth = (154f / 1038) * wf
            val stdHeight = (244f / 585) * hf
            val x = (175f / 1038 * wf).toInt()
            val y = (160f / 585 * hf).toInt()
            val scale = stdWidth/width

            width = (width * scale).toInt()
            height = (height * scale).toInt()

            var scaledImgph = Bitmap.createScaledBitmap(studentImg, width, height, true)

            val left = 1
            val top: Int
            val bottom: Int
            val right: Int

            if (height > stdHeight) {
                top = ((height - stdHeight) / 4).toInt()
                right = width
                bottom = top + stdHeight.toInt()
                scaledImgph = Bitmap.createBitmap(scaledImgph, left, top, right - left, bottom - top)
            } else {
                top = 1
            }
            val y1 = (y + (stdHeight - scaledImgph.height) / 2).toInt()
            canvas.drawBitmap(scaledImgph, x.toFloat(), y1.toFloat(), null)

            originalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            outputStream.flush()
            outputStream.close()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }
    private fun showPracticeQuestion(practiceImagePathBase64: String?) {
        val decodedString = Base64.decode(practiceImagePathBase64, Base64.DEFAULT)
        val decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
        Glide.with(applicationContext)
            .asBitmap()
            .load(decodedString)
            .into(object : CustomTarget<Bitmap?>() {
                override fun onResourceReady(
                    resource: Bitmap,
                    transition: Transition<in Bitmap?>?
                ) {
                    binding.imgPracticeQuestion.setImageBitmap(decodedByte)
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                }
            })
    }
    private fun startSpeakLabel() {
        //zoomIn()
        //binding.txtSpeak.postDelayed({
          //  zoomOut()
        //}, milliSeconds)
    }
    /*private fun stopSpeakLabel() {
        scaleAnimation.cancel()
    }*/

    private fun zoomIn() {
        val zoomInAnimation  = ScaleAnimation(
            1f, 2f, 1f, 2f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f)
        zoomInAnimation.duration = 1000
        //zoomInAnimation.fillAfter = true
        zoomInAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {
            }
            override fun onAnimationEnd(animation: Animation) {
                binding.txtSpeak.visibility = View.INVISIBLE
            }
            override fun onAnimationRepeat(animation: Animation) {
            }
        })
        binding.txtSpeak.startAnimation(zoomInAnimation)
    }
    private fun zoomOut() {
        val zoomOutAnimation = ScaleAnimation(
            2f, 1f, 2f, 1f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f)
       zoomOutAnimation.duration = 1000
      // zoomOutAnimation.fillAfter = true
       zoomOutAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {
            }
            override fun onAnimationEnd(animation: Animation) {
                binding.txtSpeak.visibility = View.VISIBLE
            }
            override fun onAnimationRepeat(animation: Animation) {
            }
        })
        binding.txtSpeak.startAnimation(zoomOutAnimation)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Check if the permission request is granted
        if (requestCode == RECORD_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted, start the next process
                startHandler()
                nextQuiz()
            } else {
                SuccessDialog(this@SelfPracticeActivity,
                    Constants.Information, getString(R.string.record_success_msg),object: SuccessImp {
                        override fun onYes(context: Activity, dialog: Dialog?) {
                            dialog!!.dismiss()
                            val intent = Intent(this@SelfPracticeActivity, PracticeHomeActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                            startActivity(intent)
                            finish()
                        }
                    }).show(supportFragmentManager, SuccessDialog.TAG)
            }
        }
    }

    /*private val storageActivityResultLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                //Android is 11 (R) or above
                if (isMicroPhonePresent()) {
                    Log.d("TAG", "onActivityResult: Manage External Storage Permissions Granted")
                    startHandler()
                    nextQuiz()
                } else
                    Toast.makeText(this@SelfPracticeActivity, "Storage Permissions Denied", Toast.LENGTH_SHORT).show()
            } else {
                if (isMicroPhonePresent()) {
                    Log.d("TAG", "onActivityResult: Manage External Storage Permissions Granted")
                    startHandler()
                    nextQuiz()
                } else
                    Toast.makeText(this@SelfPracticeActivity, "Storage Permissions Denied", Toast.LENGTH_SHORT).show()
            }
        }*/
    /*private val storageActivityResultLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                //Android is 11 (R) or above
                if (Environment.isExternalStorageManager() && isMicroPhonePresent()) {
                    //Manage External Storage Permissions Granted
                    Log.d("TAG", "onActivityResult: Manage External Storage Permissions Granted")
                    startHandler()
                    nextQuiz()
                } else
                    Toast.makeText(this@SelfPracticeActivity, "Storage Permissions Denied", Toast.LENGTH_SHORT).show()
            } else {
                //Below android 11
            }
        }*/

  /*  override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101) {
            val write = grantResults[0] == PackageManager.PERMISSION_GRANTED
            val read = grantResults[1] == PackageManager.PERMISSION_GRANTED
            val recordAudio = grantResults[2] == PackageManager.PERMISSION_GRANTED
            if (write && read && recordAudio) {
                startHandler()
                nextQuiz()
                Toast.makeText(this@SelfPracticeActivity, "The permission is grant", Toast.LENGTH_SHORT).show()
            } else
                Toast.makeText(this@SelfPracticeActivity, "The permission is denied", Toast.LENGTH_SHORT).show()
        }
    }*/

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }
    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }
    override fun onDestroy() {
        super.onDestroy()
        if (mRecorder != null) {
            mRecorder!!.stop()
            mRecorder!!.release()
            mRecorder=null
        }
        stopTimer()
    }

    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    fun onBackEvent(selfPracticeBack: SelfPracticeBack) {
        val selfPracticeBackStickyEvent= EventBus.getDefault().getStickyEvent(selfPracticeBack::class.java)
        if (selfPracticeBackStickyEvent != null) {
            EventBus.getDefault().removeStickyEvent(selfPracticeBackStickyEvent)
            val intent = Intent(this@SelfPracticeActivity, PracticeHomeActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
            finish()
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

    @Subscribe(sticky = true,threadMode = ThreadMode.BACKGROUND)
    fun onPracticeDetailEvent(messageEvent : SingleUserMessageEvent) {
        val messageEventStickyEvent= EventBus.getDefault().getStickyEvent(SingleUserMessageEvent::class.java)
        if (messageEventStickyEvent != null) {
            this.questionInfoEvent=messageEvent.questionInfo
            this.pauseMilliSeconds=1000L*messageEvent.pauseTimer
            val questionsList = viewModel.startQuestionsPracticeSession(questionInfoEvent.quizId)
            if (questionsList.isNotEmpty()) {
                this.milliSeconds = questionInfoEvent.videoOneSlideTime*1000L
                this.questionsList = questionsList
                iteratorList = questionsList.listIterator()

                if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    // Permission is not granted, request it
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), RECORD_PERMISSION_REQUEST_CODE)
                } else {
                    // Permission is already granted, start the next process
                    startHandler()
                    nextQuiz()
                }
            }
            EventBus.getDefault().removeStickyEvent(messageEventStickyEvent)
        }
    }
    private val onBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            backScreen()
        }
    }
    fun backScreen() {
        pauseTimer()
        ConfirmationDialog(this@SelfPracticeActivity, getString(R.string.heading_information), getString(R.string.msg_recoding_logout), object : ConfirmationImp {
            override fun onClickYes(context: Activity, dialog: Dialog?) {
                dialog!!.dismiss()
                stopRecording(2)
            }
            override fun onClickNo(context: Activity, dialog: Dialog?) {
                dialog!!.dismiss()
                resumeTimer()
            }
        }).show(this@SelfPracticeActivity.supportFragmentManager, ConfirmationDialog.TAG)
    }

    private fun downloadAnswerImage(url: String?,urlImage: String): String {
        try {
            val imageUrl = URL("$url$urlImage")
            val uCon = imageUrl.openConnection()
            val `is` = uCon.getInputStream()
            val baos = ByteArrayOutputStream()
            val buffer = ByteArray(1024)
            var read = 0
            while (`is`.read(buffer, 0, buffer.size).also { read = it } != -1) {
                baos.write(buffer, 0, read)
            }
            val base64 = org.apache.commons.codec.binary.Base64.encodeBase64String(baos.toByteArray())
            baos.flush()
            baos.close()
            return  base64
        } catch (e: Exception) {
            Log.d("Error", e.toString())
        }
        return ""
    }
    private fun deleteQuizFolder() {
        val quizDir = File(filesDir, questionInfoEvent.quizId.toString())
        if (quizDir.deleteRecursively()) quizDir.mkdir()
    }
}


