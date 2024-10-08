package practice.english.n2l.ui

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextWatcher
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.arthenica.mobileffmpeg.FFmpeg
import com.google.gson.Gson
import practice.english.n2l.R
import practice.english.n2l.api.ResponseHandle
import practice.english.n2l.api.RetrofitService
import practice.english.n2l.bridge.SuccessImp
import practice.english.n2l.database.bao.QuestionInfo
import practice.english.n2l.database.bao.UserDetail
import practice.english.n2l.databinding.ActivityLoginBinding
import practice.english.n2l.dialog.CustomProgressDialog
import practice.english.n2l.dialog.ErrorDialog
import practice.english.n2l.dialog.NoInternetConnection
import practice.english.n2l.dialog.SuccessDialog
import practice.english.n2l.dialog.WarningDialog
import practice.english.n2l.repository.PracticeRepo
import practice.english.n2l.repository.PracticeRepoImp
import practice.english.n2l.repository.UserAuthRepo
import practice.english.n2l.repository.UserAuthRepoImp
import practice.english.n2l.util.Constants
import practice.english.n2l.util.Utilities
import practice.english.n2l.viewmodel.PracticeViewModel
import practice.english.n2l.viewmodel.UserAuthViewModel
import org.greenrobot.eventbus.EventBus
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.Locale


class LoginActivity : BaseActivity() {
    //https://agrawalsuneet.github.io/blogs/lazy-property-in-kotlin/
    private val repository          : UserAuthRepo by lazy { UserAuthRepoImp(RetrofitService.service,this) }
    private val viewModel           : UserAuthViewModel by lazy { ViewModelProvider(this, UserAuthViewModel.Factory(repository))[UserAuthViewModel::class.java] }
    private val repository2         : PracticeRepo by lazy { PracticeRepoImp(RetrofitService.service,this) }
    private val viewModel2          : PracticeViewModel by lazy { ViewModelProvider(this, PracticeViewModel.Factory(repository2))[PracticeViewModel::class.java] }
    private lateinit var binding    : ActivityLoginBinding
    private lateinit var userDetail : UserDetail
    private val progressDialog      : CustomProgressDialog by lazy { CustomProgressDialog(this) }
    private val repository3         : PracticeRepo by lazy { PracticeRepoImp(RetrofitService.service, this) }
    private val viewModel3          : PracticeViewModel by lazy { ViewModelProvider(this, PracticeViewModel.Factory(repository3))[PracticeViewModel::class.java] }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        //binding.LoginPin.transformationMethod= AsteriskPasswordTransformationMethod()
        binding.userAuthViewModel = viewModel
        binding.lifecycleOwner = this

        binding.mPin.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(p0?.length==4) {
                    progressDialog.startLoading()
                    Utilities.hideKeyboard(binding.mPin, this@LoginActivity)
                    if(binding.mPin.text.toString()==userDetail.pinNo)
                    {
                        EventBus.getDefault().postSticky(userDetail)
                        progressDialog.dismissLoading()
                        startActivity(
                            Intent().setClassName(getString(R.string.project_package_name), "practice.english.n2l.ui.HomeActivity")
                                .setFlags(FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_CLEAR_TASK)
                        )
                        finish()
                    }
                    else
                    {
                        progressDialog.dismissLoading()
                        binding.mPin.setText("")
                        WarningDialog(this@LoginActivity, Constants.Information, getString(R.string.login_failure_msg)).show(supportFragmentManager,
                            WarningDialog.TAG)
                    }

                }
            }
            override fun afterTextChanged(p0: Editable?) {}
        })
        viewModel.getUserUserDetail.observe(this) {
            it ?.let{
                userDetail=it
                binding.EtUserId.setText(it.userName)
            }
        }
        viewModel.getLoginStatus.observe(this){
            when(it) {
                true-> {
                    EventBus.getDefault().postSticky(userDetail)
                    startActivity(
                        Intent().setClassName(getString(R.string.project_package_name), "practice.english.n2l.ui.HomeActivity")
                            .setFlags(FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_CLEAR_TASK)
                    )
                    finish()
                }
                false->
                {
                    viewModel.loginPin.value=""
                    WarningDialog(this, Constants.Information, getString(R.string.login_failure_msg)).show(supportFragmentManager,
                        WarningDialog.TAG)
                }
            }
        }
        viewModel.generateOTP.observe(this) {
            when(it) {
                is ResponseHandle.Loading->{progressDialog.startLoading()}
                is ResponseHandle.Success-> {
                    when (it.data!!.responseCode) {
                        800 -> {
                            val list=it.data.responseData.oTPInfo
                            list.userName=userDetail.userName
                            list.userNickName=userDetail.userNickName
                            list.referralId=""
                            list.userMobileNumber=userDetail.userMobileNumber
                            progressDialog.dismissLoading()
                            SuccessDialog(this,Constants.Information,it.data.responseMessage,object:
                                SuccessImp {
                                override fun onYes(context: Activity, dialog: Dialog?) {
                                    dialog!!.dismiss()
                                    startActivity(Intent().setClassName(getString(R.string.project_package_name),
                                        "practice.english.n2l.ui.CreatePinActivity")
                                        .putExtra("OTPInfoDesc", Gson().toJson(list))
                                        .putExtra("SourceActivity","LoginActivity")
                                    )
                                    finish()
                                }
                            }).show(supportFragmentManager, SuccessDialog.TAG)
                        }
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
                    ErrorDialog(this,Constants.Error,Constants.ServerFailure).show(supportFragmentManager,
                        ErrorDialog.TAG)
                }
                is ResponseHandle.Network->{
                    NoInternetConnection(this,Constants.Information,Constants.NetworkMessage).show(supportFragmentManager,
                        NoInternetConnection.TAG)
                }
            }
        }

       /*CoroutineScope(Dispatchers.IO).launch{
            withContext(Dispatchers.IO){
                val singleTemplates=viewModel2.getSingleTemplates()
                val multiTemplates=viewModel2.getMultiTemplates()
                val studentProfile=viewModel2.getStudentProfile()
                val studentImage=File(studentProfile.photoLocalPath)
                val originalFile=File(multiTemplates.fileLocalPath)

                val newFile=File(filesDir.absolutePath + File.separator + "11/aimg00.jpg")
                originalFile.copyTo(newFile, true)
                firstImageCreationSelfMulti(studentImage,originalFile,newFile,"hello welcome\n(11111)","ffgggfgg",studentImage)
            }
        }*/

        /*CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                val questionInfoEvent=QuestionInfo(0,1752,10,"",10,"","","",2,1)
                finalMp4CreationProcess(questionInfoEvent)
            }
        }*/
    }
    private  fun firstImageCreationSelfMulti(studentImage: File, originalFileName: File, newFileName: File, title:String, studentName:String, friendImage: File) {
        try {
            if (newFileName.exists()) newFileName.delete()
            val outputStream: OutputStream = FileOutputStream(newFileName)
            val notMutableBitmap = BitmapFactory.decodeFile(originalFileName.absolutePath)
            val originalBitmap: Bitmap = notMutableBitmap.copy(Bitmap.Config.ARGB_8888, true)
            val tf = Typeface.create("Helvetica", Typeface.BOLD)
            val canvas = Canvas(originalBitmap)


            val paint = Paint()
            paint.style = Paint.Style.FILL
            paint.color = Color.BLACK
            paint.typeface = tf
            paint.textAlign = Paint.Align.LEFT
            paint.setXfermode(PorterDuffXfermode(PorterDuff.Mode.SRC_OVER))
            paint.textSize =  25.0f
            val canvasWidth = canvas.width.toFloat()
            //val canvasHeight = canvas.height.toFloat()

            //val lineY1 = canvasHeight / 4f
            //val lineY2 = lineY1 * 2
           // val lineY3 = lineY1 * 3

            // Draw horizontal lines
            //canvas.drawLine(0f, lineY1, canvasWidth.toFloat(), lineY1, paint)
            //canvas.drawLine(0f, lineY2, canvasWidth.toFloat(), lineY2, paint)
            //canvas.drawLine(0f, lineY3, canvasWidth.toFloat(), lineY3, paint)



            //var text1 = "Section 1"
           // var text2 = "Section 2"
            //var text3 = "Section 3"
           //var text4 = "Section 4"

            //var textHeight = paint.descent() - paint.ascent()

            //canvas.drawText(text1, 10f, lineY1 - textHeight / 2, paint)
            //canvas.drawText(text2, 10f, lineY2 - textHeight / 2, paint)
            //canvas.drawText(text3, 10f, lineY3 - textHeight / 2, paint)
            //canvas.drawText(text4, 10f, canvasHeight - textHeight / 2, paint)


            val lineX1 = canvasWidth / 4f
            val lineX2 = lineX1 * 2
            val lineX3 = lineX1 * 3

            // Draw vertical lines
            //canvas.drawLine(lineX1, 0f, lineX1, canvasHeight.toFloat(), paint)
            //canvas.drawLine(lineX2, 0f, lineX2, canvasHeight.toFloat(), paint)
           // canvas.drawLine(lineX3, 0f, lineX3, canvasHeight.toFloat(), paint)

            // Draw text within each section
             //text1 = "yogesh peter graham"
             //text3 = "Murli"


            val textWidth1 = paint.measureText(studentName)
            val textWidth3 = paint.measureText(studentName)
            val textHeight = paint.descent() - paint.ascent()
            canvas.drawText(studentName, lineX1 - textWidth1 / 2, canvas.height/4f *3, paint)
            canvas.drawText(studentName, lineX3 - textWidth3 / 2, canvas.height/4f*3, paint)


            paint.textAlign = Paint.Align.LEFT
            val textRect = Rect()
            paint.textSize =  35.0f
            paint.getTextBounds(title, 0, title.length, textRect)
            val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                textSize = 40f
                color = 0xFF000000.toInt() // black color
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }

            canvas.apply {
                val staticLayout = StaticLayout.Builder
                    .obtain(title, 0, title.length, textPaint, width)
                    .setAlignment(Layout.Alignment.ALIGN_CENTER)
                    .setLineSpacing(0f, 1f)
                    .setIncludePad(true)
                    .build()
                staticLayout.draw(canvas)}


            var studentImg: Bitmap = BitmapFactory.decodeFile(studentImage.absolutePath)
            var width = studentImg.width
            var height = studentImg.height
            var wf = notMutableBitmap.width
            var hf = notMutableBitmap.height
            var stdWidth = (154f / 1038) * wf
            var stdHeight = (244f / 585) * hf
            var x = (179f / 1038 * wf).toInt()
            var y = (160f / 585 * hf).toInt()
            var scale = stdWidth/width
            width = (width * scale).toInt()
            height = (height * scale).toInt()
            var scaledImg = Bitmap.createScaledBitmap(studentImg, width, height, true)
            var left = 1
            var top: Int
            var bottom: Int
            var right: Int
            if (height > stdHeight) {
                top = ((height - stdHeight) / 4).toInt()
                right = width
                bottom = top + stdHeight.toInt()
                scaledImg = Bitmap.createBitmap(scaledImg, left, top, right - left, bottom - top)
            } else {
                top = 1
            }
            var y1 = (y + (stdHeight - scaledImg.height) / 2).toInt()
            canvas.drawBitmap(scaledImg, x.toFloat(), y1.toFloat(), null)


            studentImg = BitmapFactory.decodeFile(studentImage.absolutePath)
            width = studentImg.width
            height = studentImg.height
            wf = notMutableBitmap.width
            hf = notMutableBitmap.height
            stdWidth = (154f / 1038) * wf
            stdHeight = (244f / 585) * hf
            x = (702f / 1038 * wf).toInt()
            y = (160f / 585 * hf).toInt()
            scale = stdWidth/width
            width = (width * scale).toInt()
            height = (height * scale).toInt()
            scaledImg = Bitmap.createScaledBitmap(studentImg, width, height, true)
            left = 1
            top=0
            bottom=0
            right=0
            if (height > stdHeight) {
                top = ((height - stdHeight) / 4).toInt()
                right = width
                bottom = top + stdHeight.toInt()
                scaledImg = Bitmap.createBitmap(scaledImg, left, top, right - left, bottom - top)
            } else {
                top = 1
            }
             y1 = (y + (stdHeight - scaledImg.height) / 2).toInt()

            canvas.drawBitmap(scaledImg, x.toFloat(), y1.toFloat(), null)
            originalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            outputStream.flush()
            outputStream.close()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }
    private  fun finalMp4CreationProcess(questionInfoEvent : QuestionInfo) {


        val audio=viewModel3.getAudio(6)
        File(audio.fileLocalPath).copyTo(File(filesDir.absolutePath + File.separator + "${questionInfoEvent.quizId}/standardBlank.mp3"), true)

        val sourceFile4= "${questionInfoEvent.quizId}/aimg%02d.jpg"
        val destinationFile4= "${questionInfoEvent.quizId}/withoutVoice.mp4"
        val sourceFilePath4 = File(filesDir.absolutePath + File.separator + sourceFile4)
        val destinationFilePath4 = File(filesDir.absolutePath + File.separator + destinationFile4)

        var slideTime6 = (1/6.toDouble())
        slideTime6=String.format(Locale.US,"%.3f", slideTime6).toDouble()

        var slideTime = 1/ questionInfoEvent.videoOneSlideTime.toDouble()
        slideTime=String.format(Locale.US,"%.3f", slideTime).toDouble()

        val firstImageJpg= "${questionInfoEvent.quizId}/firstImage.jpg"
        val firstImageJpg4 = File(filesDir.absolutePath + File.separator + firstImageJpg)
        val lastImageJpg= "${questionInfoEvent.quizId}/lastImage.jpg"
        val lastImageJpg4 = File(filesDir.absolutePath + File.separator + lastImageJpg)

        val destinationFirstFilePath4 = File(filesDir.absolutePath + File.separator + "${questionInfoEvent.quizId}/firstPage.mp4")
        val destinationLastFilePath4 = File(filesDir.absolutePath + File.separator + "${questionInfoEvent.quizId}/lastPage.mp4")
        val destinationTempRemainFilePath4 = File(filesDir.absolutePath + File.separator + "${questionInfoEvent.quizId}/tempRemainPage.mp4")

        val convertFirstJpgToMp4 ="-y -loop 1 -i $firstImageJpg4 -c:v libx264 -t 6 -r 30 -pix_fmt yuv420p -vf scale=720:480 $destinationFirstFilePath4"
        val convertLastJpgToMp4 ="-y -loop 1 -i $lastImageJpg4 -c:v libx264 -t 6 -r 30 -pix_fmt yuv420p -vf scale=720:480 $destinationLastFilePath4"
        val convertRemainJpgToMp4 ="-y -r $slideTime -i $sourceFilePath4 -c:v libx264 -r 30 -pix_fmt yuv420p -vf scale=720:480 $destinationTempRemainFilePath4"

        val margeAllMp4="-y -i $destinationFirstFilePath4 -i $destinationTempRemainFilePath4 -i $destinationLastFilePath4 -filter_complex [0:v][1:v][2:v]concat=n=3:v=1[vv] -map [vv]  $destinationFilePath4"

        val rc1 = FFmpeg.execute(convertFirstJpgToMp4)
        val rc2 = FFmpeg.execute(convertLastJpgToMp4)
        val rc3 = FFmpeg.execute(convertRemainJpgToMp4)
        val rc4 = FFmpeg.execute(margeAllMp4)


        /*val blankMp3= "${questionInfoEvent.quizId}/standardBlank.mp3"
        val blankMp3Path = File(filesDir.absolutePath + File.separator + blankMp3)
        val completeVoiceRecodingMp3= "${questionInfoEvent.quizId}/${questionInfoEvent.quizId}.mp3"
        val completeVoiceRecodingMp3Path = File(filesDir.absolutePath + File.separator + completeVoiceRecodingMp3)
        val destinationMergeBlankWithCompleteVoiceFile= "${questionInfoEvent.quizId}/voiceRecording${questionInfoEvent.quizId}.mp3"
        val destinationMergeBlankWithCompleteVoiceFilePath = File(filesDir.absolutePath + File.separator + destinationMergeBlankWithCompleteVoiceFile)
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
        val mergeMp4AndMp3="-y -i $sourceFilePathMp4 -i $sourceFilePathMp3 -c:v copy -c:a aac $destinationFilePath5"*/


        /*when(val rc1 = FFmpeg.execute(convertJpgToMp4)){
            Config.RETURN_CODE_SUCCESS -> {
                Log.i(Config.TAG, "First Command execution completed successfully.")
                when(val rc2 = FFmpeg.execute(mergeBlankWithAudio)){
                    Config.RETURN_CODE_SUCCESS -> {
                        Log.i(Config.TAG, "Second Command execution completed successfully.")
                        when(val rc3 = FFmpeg.execute(mergeMp4AndMp3)){
                            Config.RETURN_CODE_SUCCESS -> {
                                Log.i(Config.TAG, "Third Command execution completed successfully.")
                            }
                            Config.RETURN_CODE_CANCEL ->   Log.i(Config.TAG, "Third Command execution cancelled by user.")
                            else->{
                                Log.i(Config.TAG, String.format("Third Command execution failed with rc=%d and the output below.", rc3))
                                Config.printLastCommandOutput(Log.INFO)
                            }
                        }
                    }
                    Config.RETURN_CODE_CANCEL ->   Log.i(Config.TAG, "Second Command execution cancelled by user.")
                    else->{
                        Log.i(Config.TAG, String.format("Second Command execution failed with rc=%d and the output below.", rc2))
                        Config.printLastCommandOutput(Log.INFO)
                    }
                }
            }
            Config.RETURN_CODE_CANCEL ->   Log.i(Config.TAG, " First Command execution cancelled by user.")
            else->{
                Log.i(Config.TAG, String.format("First Command execution failed with rc=%d and the output below.", rc1))
                Config.printLastCommandOutput(Log.INFO)
            }
        }*/
    }
}