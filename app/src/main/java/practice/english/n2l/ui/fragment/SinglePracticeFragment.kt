package practice.english.n2l.ui.fragment

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.lifecycle.ViewModelProvider
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import practice.english.n2l.R
import practice.english.n2l.api.ResponseHandle
import practice.english.n2l.api.RetrofitService
import practice.english.n2l.bridge.ConfirmationImp
import practice.english.n2l.bridge.ShowInstructionConfirmationImp
import practice.english.n2l.database.bao.BlankTemplates
import practice.english.n2l.database.bao.ContentType
import practice.english.n2l.database.bao.QuestionInfo
import practice.english.n2l.database.bao.Questions
import practice.english.n2l.database.bao.QuizDetails
import practice.english.n2l.database.bao.SelfPracticeObject
import practice.english.n2l.database.bao.SingleUserMessageEvent
import practice.english.n2l.database.bao.SubContentType
import practice.english.n2l.database.bao.UserDetail
import practice.english.n2l.databinding.FragmentSinglePracticeBinding
import practice.english.n2l.dialog.ConfirmationDialog
import practice.english.n2l.dialog.CustomProgressDialog
import practice.english.n2l.dialog.ErrorDialog
import practice.english.n2l.dialog.NoInternetConnection
import practice.english.n2l.dialog.ShowInstructionConfirmationDialog
import practice.english.n2l.dialog.WarningDialog
import practice.english.n2l.repository.PracticeRepo
import practice.english.n2l.repository.PracticeRepoImp
import practice.english.n2l.util.Constants
import practice.english.n2l.viewmodel.PracticeViewModel
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Response
import org.apache.commons.codec.binary.Base64
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL

class SinglePracticeFragment : BindingFragment<FragmentSinglePracticeBinding>() {

    private val repository                          : PracticeRepo by lazy { PracticeRepoImp(RetrofitService.service,requireActivity()) }
    private val viewModel                           : PracticeViewModel by lazy { ViewModelProvider(this, PracticeViewModel.Factory(repository))[PracticeViewModel::class.java] }
    private val progressDialog                      : CustomProgressDialog by lazy { CustomProgressDialog(requireActivity())}
    private lateinit var contentTypeAdapter         : ArrayAdapter<ContentType>
    private lateinit var subContentTypeAdapter      : ArrayAdapter<SubContentType>
    private lateinit var quizDetailsAdapter         : ArrayAdapter<QuizDetails>
    private lateinit var pauseTimerAdapter          : ArrayAdapter<Int>
    private lateinit var userDetail                 : UserDetail
    private var spContentTypePos                    =0
    private var spSubContentTypePos                 =0
    private var spQuizDetailsPos                    =0
    private var spPauseTimerPos                     =0
    private var pauseStatus                         = emptyArray<Int>()
    private lateinit var blankTemplates             : BlankTemplates
    override fun getViewBinding() = FragmentSinglePracticeBinding.inflate(layoutInflater)


    override  fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.practiceViewModel = viewModel
        binding.lifecycleOwner = this

        contentTypeAdapter= ArrayAdapter(requireActivity(), R.layout.dropdown)
        binding.SpContentType.threshold = 0
        binding.SpContentType.setAdapter(contentTypeAdapter)

        pauseStatus = arrayOf(1,2,3,4,5,6,7,8,9,10)
        pauseTimerAdapter= ArrayAdapter(requireActivity(), R.layout.dropdown,pauseStatus.toList())
        binding.SpPauseTimer.threshold = 0
        binding.SpPauseTimer.setAdapter(pauseTimerAdapter)

        binding.SpPauseTimer.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                spPauseTimerPos=position
            }

        val jsonParams = JsonObject()
        jsonParams.addProperty("CourseId",  "")
        viewModel.contentType(jsonParams)

        viewModel.contentType.observe(viewLifecycleOwner) {
            when(it) {
                is ResponseHandle.Loading->{progressDialog.startLoading()}
                is ResponseHandle.Success-> {
                    when (it.data!!.responseCode) {
                        800 -> {
                            progressDialog.dismissLoading()

                            if(it.data.responseData.contentType.isNotEmpty()) {

                                contentTypeAdapter.apply {
                                    addAll(it.data.responseData.contentType)
                                    notifyDataSetChanged()
                                }
                                binding.SpContentType.onItemClickListener =
                                    AdapterView.OnItemClickListener { parent, _, position, _ ->
                                        val contentType = parent!!.getItemAtPosition(position) as ContentType
                                        spContentTypePos=position
                                        if (contentType.contentId != 0) {
                                            val jsonParam = JsonObject()
                                            jsonParam.addProperty("ContentId", contentType.contentId)
                                            viewModel.contentSubType(jsonParam)
                                        }
                                    }
                            }
                            else
                            {
                                contentTypeAdapter= ArrayAdapter(requireActivity(), R.layout.dropdown)
                                binding.SpContentType.threshold = 0
                                binding.SpContentType.setAdapter(contentTypeAdapter)
                                if(!contentTypeAdapter.isEmpty) {
                                    contentTypeAdapter.clear()
                                }

                                subContentTypeAdapter= ArrayAdapter(requireActivity(), R.layout.dropdown)
                                binding.SpContentSubType.threshold = 0
                                binding.SpContentSubType.setAdapter(subContentTypeAdapter)
                                if(!subContentTypeAdapter.isEmpty) {
                                    subContentTypeAdapter.clear()
                                }

                                quizDetailsAdapter= ArrayAdapter(requireActivity(), R.layout.dropdown)
                                binding.SpQuizDetails.threshold = 0
                                binding.SpQuizDetails.setAdapter(quizDetailsAdapter)
                                if(!quizDetailsAdapter.isEmpty) {
                                    quizDetailsAdapter.clear()
                                }
                            }
                        }
                        300 -> {
                            progressDialog.dismissLoading()
                            if(!contentTypeAdapter.isEmpty) {
                                contentTypeAdapter.clear()
                            }

                            binding.SpContentType.setText("")
                           // binding.SpContentType.hint=getString(R.string.label_select_content)
                            WarningDialog(requireActivity(), Constants.Information,it.data.responseMessage).show(requireActivity().supportFragmentManager,
                                WarningDialog.TAG)
                        }
                        500 -> {
                            progressDialog.dismissLoading()
                            WarningDialog(requireActivity(), Constants.Information,it.data.responseMessage).show(requireActivity().supportFragmentManager,
                                WarningDialog.TAG)
                        }
                        else->{
                            progressDialog.dismissLoading()
                            WarningDialog(requireActivity(), Constants.Information,it.data.responseMessage).show(requireActivity().supportFragmentManager,
                                WarningDialog.TAG)
                        }
                    }
                }
                is ResponseHandle.Error->{
                    progressDialog.dismissLoading()
                    ErrorDialog(requireActivity(), Constants.Error, Constants.ServerFailure).show(requireActivity().supportFragmentManager,
                        ErrorDialog.TAG)
                }
                is ResponseHandle.Network->{
                    NoInternetConnection(requireActivity(), Constants.Information, Constants.NetworkMessage).show(requireActivity().supportFragmentManager,
                        NoInternetConnection.TAG)
                }
            }
        }
        viewModel.contentSubType.observe(viewLifecycleOwner) {
            when(it) {
                is ResponseHandle.Loading->{progressDialog.startLoading()}
                is ResponseHandle.Success-> {
                    when (it.data!!.responseCode) {
                        800 -> {
                            progressDialog.dismissLoading()

                            binding.SpContentSubType.setText("",false)
                            binding.SpQuizDetails.setText("",false)

                            //binding.SpContentSubTypeValidator.hint=getString(R.string.label_select_topic)
                            //binding.SpQuizDetailsValidator.hint=getString(R.string.label_select_dialogue)

                            subContentTypeAdapter= ArrayAdapter(requireActivity(), R.layout.dropdown)
                            binding.SpContentSubType.threshold = 0
                            binding.SpContentSubType.setAdapter(subContentTypeAdapter)

                            if(it.data.responseData.subContentType.isNotEmpty()) {

                                subContentTypeAdapter.apply {
                                    clear()
                                    addAll(it.data.responseData.subContentType)
                                    notifyDataSetChanged()
                                }

                                binding.SpContentSubType.onItemClickListener =
                                    AdapterView.OnItemClickListener { parent, _, position, _ ->
                                        val subContentType = parent!!.getItemAtPosition(position) as SubContentType
                                        spSubContentTypePos=position
                                        if (subContentType.subContentId != 0L) {
                                            val jsonParam = JsonObject()
                                            jsonParam.addProperty("Single_Multi", 1)
                                            jsonParam.addProperty("ContentId", subContentType.contentId)
                                            jsonParam.addProperty("SubContentId", subContentType.subContentId)
                                            viewModel.quizDetails(jsonParam)
                                        }
                                    }
                            }
                            else
                            {
                                subContentTypeAdapter= ArrayAdapter(requireActivity(), R.layout.dropdown)
                                binding.SpContentSubType.threshold = 0
                                binding.SpContentSubType.setAdapter(subContentTypeAdapter)
                                if(!subContentTypeAdapter.isEmpty) {
                                    subContentTypeAdapter.clear()
                                }

                                quizDetailsAdapter= ArrayAdapter(requireActivity(), R.layout.dropdown)
                                binding.SpQuizDetails.threshold = 0
                                binding.SpQuizDetails.setAdapter(quizDetailsAdapter)
                                if(!quizDetailsAdapter.isEmpty) {
                                    quizDetailsAdapter.clear()
                                }
                            }
                        }
                        300 -> {
                            progressDialog.dismissLoading()
                            subContentTypeAdapter= ArrayAdapter(requireActivity(), R.layout.dropdown)
                            binding.SpContentSubType.threshold = 0
                            binding.SpContentSubType.setAdapter(subContentTypeAdapter)

                            if(!subContentTypeAdapter.isEmpty) {
                                subContentTypeAdapter.clear()
                            }
                            binding.SpContentSubType.setText("")
                            //binding.SpContentSubType.hint=getString(R.string.label_select_topic)
                            WarningDialog(requireActivity(), Constants.Information,it.data.responseMessage).show(requireActivity().supportFragmentManager,
                                WarningDialog.TAG)
                        }
                        500 -> {
                            progressDialog.dismissLoading()
                            WarningDialog(requireActivity(), Constants.Information,it.data.responseMessage).show(requireActivity().supportFragmentManager,
                                WarningDialog.TAG)
                        }
                        else->{
                            progressDialog.dismissLoading()
                            WarningDialog(requireActivity(), Constants.Information,it.data.responseMessage).show(requireActivity().supportFragmentManager,
                                WarningDialog.TAG)
                        }
                    }
                }
                is ResponseHandle.Error->{
                    progressDialog.dismissLoading()
                    ErrorDialog(requireActivity(), Constants.Error, Constants.ServerFailure).show(requireActivity().supportFragmentManager,
                        ErrorDialog.TAG)
                }
                is ResponseHandle.Network->{
                    NoInternetConnection(requireActivity(), Constants.Information, Constants.NetworkMessage).show(requireActivity().supportFragmentManager,
                        NoInternetConnection.TAG)
                }
            }
        }
        viewModel.quizDetails.observe(viewLifecycleOwner) {
            when(it) {
                is ResponseHandle.Loading->{progressDialog.startLoading()}
                is ResponseHandle.Success-> {
                    when (it.data!!.responseCode) {
                        800 -> {
                            progressDialog.dismissLoading()
                            quizDetailsAdapter= ArrayAdapter(requireActivity(), R.layout.dropdown)
                            binding.SpQuizDetails.threshold = 0
                            binding.SpQuizDetails.setAdapter(quizDetailsAdapter)
                            binding.SpQuizDetails.setText("",false)
                            if(it.data.responseData.quizDetails.isNotEmpty()) {

                                quizDetailsAdapter.apply {
                                    clear()
                                    addAll(it.data.responseData.quizDetails)
                                    notifyDataSetChanged()
                                }
                                binding.SpQuizDetails.onItemClickListener =
                                    AdapterView.OnItemClickListener { _, _, position, _ ->
                                        //val quizDetails = parent!!.getItemAtPosition(position) as QuizDetails
                                        spQuizDetailsPos=position
                                        onSelectPause(it.data.responseData.quizDetails[position].videoPauseTime)
                                    }
                            }
                            else
                            {
                                quizDetailsAdapter.clear()
                            }
                        }
                        300 -> {
                            progressDialog.dismissLoading()
                            quizDetailsAdapter= ArrayAdapter(requireActivity(), R.layout.dropdown)
                            binding.SpQuizDetails.threshold = 0
                            binding.SpQuizDetails.setAdapter(quizDetailsAdapter)
                            if(!quizDetailsAdapter.isEmpty) {
                                quizDetailsAdapter.clear()
                            }

                            binding.SpQuizDetails.setText("")
                            //binding.SpQuizDetails.hint=getString(R.string.label_select_dialogue)
                            WarningDialog(requireActivity(), Constants.Information,it.data.responseMessage).show(requireActivity().supportFragmentManager,
                                WarningDialog.TAG)
                        }
                        500 -> {
                            progressDialog.dismissLoading()
                            WarningDialog(requireActivity(), Constants.Information,it.data.responseMessage).show(requireActivity().supportFragmentManager,
                                WarningDialog.TAG)
                        }
                        else->{
                            progressDialog.dismissLoading()
                            WarningDialog(requireActivity(), Constants.Information,it.data.responseMessage).show(requireActivity().supportFragmentManager,
                                WarningDialog.TAG)
                        }
                    }
                }
                is ResponseHandle.Error->{
                    progressDialog.dismissLoading()
                    ErrorDialog(requireActivity(), Constants.Error, Constants.ServerFailure).show(requireActivity().supportFragmentManager,
                        ErrorDialog.TAG)
                }
                is ResponseHandle.Network->{
                    NoInternetConnection(requireActivity(), Constants.Information, Constants.NetworkMessage).show(requireActivity().supportFragmentManager,
                        NoInternetConnection.TAG)
                }
            }
        }
        viewModel.questionImage.observe(viewLifecycleOwner) {
            when(it) {
                is ResponseHandle.Loading->{progressDialog.startLoading()}
                is ResponseHandle.Success-> {
                    when (it.data!!.responseCode) {
                        800 -> {
                            val questionInfo = it.data.responseData.questionInfo
                            val questions = it.data.responseData.questions

                            val answerInfo = it.data.responseData.answerInfo
                            val answers = it.data.responseData.answers

                            CoroutineScope(Dispatchers.IO).launch {
                                withContext(Dispatchers.IO) {
                                    if(!questionInfo.instructionLink.isNullOrEmpty()) {
                                        questionInfo.instructionLink=questionInfo.uRL+questionInfo.instructionLink
                                        questionInfo.instructionLinkLocalPath = downloadInstructionLinkMP4(questionInfo.instructionLink!!,questionInfo.quizId.toString()+".mp4")
                                    }
                                }
                                viewModel.insertQuestionInfo(questionInfo)

                                withContext(Dispatchers.IO) {
                                    questions.stream()
                                        .peek{e-> e.singleMultiQuestionType=1}
                                        .peek{e -> e.quizId=questionInfo.quizId}
                                        //.forEach{e -> e.questionImageBase64 = downloadEachImage(questionInfo.uRL, e.questionImage) }
                                        .forEach { e ->
                                            try {
                                                var base64Image = downloadEachImage(questionInfo.uRL, e.questionImage)
                                                if(base64Image.isBlank()) {
                                                    base64Image = blankTemplates.fileData.toString()
                                                    e.questionImageAsBlank=1
                                                }
                                                e.questionImageBase64 = base64Image
                                            } catch (ex: Exception) {
                                                println("Error downloading image for question: ${e.questionId}, ${ex.message}")
                                            }
                                        }
                                }
                                questionInfo.singleMultiQuestionType=1
                                viewModel.insertAllQuestions(questionInfo.quizId, questions)

                                withContext(Dispatchers.IO) {
                                    answers.stream()
                                        .peek{e -> e.quizId=questionInfo.quizId}
                                        //.forEach{e -> e.answerImageBase64 = downloadEachImage(questionInfo.uRL, e.answerImagePath) }
                                        .forEach { e ->
                                            try {
                                                var base64Image = downloadEachImage(questionInfo.uRL, e.answerImagePath)
                                                if(base64Image.isBlank()){
                                                    base64Image=blankTemplates.fileData.toString()
                                                    e.answerImageAsBlank=1
                                                }
                                                e.answerImageBase64 = base64Image
                                            } catch (ex: Exception) {
                                                println("Error downloading image for question: ${e.questionId}, ${ex.message}")
                                            }
                                        }
                                }
                                viewModel.insertAnswerInfo(answerInfo)
                                viewModel.insertAllAnswers(answerInfo.quizId, answers)

                                progressDialog.dismissLoading()

                                EventBus.getDefault().postSticky(SingleUserMessageEvent(questionInfo,binding.SpPauseTimer.adapter.getItem(spPauseTimerPos) as Int))

                                requireActivity().runOnUiThread {
                                    val isShowInstruction = questionInfo.instructionLink.isNullOrEmpty()
                                    ShowInstructionConfirmationDialog(requireActivity(), getString(R.string.heading_information), getString(R.string.msg_start_session),
                                        getString(R.string.Ok),getString(R.string.label_show_instructions),isShowInstruction,
                                        object : ShowInstructionConfirmationImp {
                                            override fun onClickYes(context: Activity, dialog: Dialog?) {
                                                dialog!!.dismiss()
                                                startActivity(Intent().setClassName(getString(R.string.project_package_name),
                                                    "practice.english.n2l.ui.practice.SelfPracticeActivity"))
                                            }
                                            override fun onClickShowInstruction(context: Activity, dialog: Dialog?) {
                                                dialog!!.dismiss()
                                                showInstructionQuizVideo(questionInfo)
                                            }
                                        }).show(requireActivity().supportFragmentManager, ShowInstructionConfirmationDialog.TAG)
                                    /*SuccessDialog(requireActivity(), Constants.Information, getString(R.string.msg_start_session), object : SuccessImp {
                                        override fun onYes(context: Activity, dialog: Dialog?) {
                                            dialog!!.dismiss()
                                            startActivity(Intent().setClassName(getString(R.string.project_package_name),
                                                "practice.english.n2l.ui.practice.SelfPracticeActivity"))
                                        }
                                    }).show(requireActivity().supportFragmentManager, SuccessDialog.TAG)*/
                                }
                            }
                                                         
                        }
                        300 -> {
                            progressDialog.dismissLoading()
                            WarningDialog(requireActivity(), Constants.Information,it.data.responseMessage).show(requireActivity().supportFragmentManager,
                                WarningDialog.TAG)
                        }
                        500 -> {
                            progressDialog.dismissLoading()
                            WarningDialog(requireActivity(), Constants.Information,it.data.responseMessage).show(requireActivity().supportFragmentManager,
                                WarningDialog.TAG)
                        }
                        else->{
                            progressDialog.dismissLoading()
                            WarningDialog(requireActivity(), Constants.Information,it.data.responseMessage).show(requireActivity().supportFragmentManager,
                                WarningDialog.TAG)
                        }
                    }
                }
                is ResponseHandle.Error->{
                    progressDialog.dismissLoading()
                    ErrorDialog(requireActivity(), Constants.Error, Constants.ServerFailure).show(requireActivity().supportFragmentManager,
                        ErrorDialog.TAG)
                }
                is ResponseHandle.Network->{
                    NoInternetConnection(requireActivity(), Constants.Information, Constants.NetworkMessage).show(requireActivity().supportFragmentManager,
                        NoInternetConnection.TAG)
                }
            }
        }

        binding.BtnContinue.setOnClickListener {
            binding.SpContentTypeValidator.isErrorEnabled = false
            binding.SpContentSubTypeValidator.isErrorEnabled = false
            binding.SpQuizDetailsValidator.isErrorEnabled = false

            if (binding.SpContentType.text.toString().isBlank()) {
                binding.SpContentTypeValidator.isErrorEnabled = true
                binding.SpContentTypeValidator.error = "Please Select First Content"
            } else if (binding.SpContentSubType.text.toString().isBlank()) {
                binding.SpContentSubTypeValidator.isErrorEnabled = true
                binding.SpContentSubTypeValidator.error = "Please Select First Topic"
            } else if (binding.SpQuizDetails.text.toString().isBlank()) {
                binding.SpQuizDetailsValidator.isErrorEnabled = true
                binding.SpQuizDetailsValidator.error = "Please Select First Dialogue"
            } else {
                val quizDetails = binding.SpQuizDetails.adapter.getItem(spQuizDetailsPos) as QuizDetails
                CoroutineScope(Dispatchers.IO).launch {
                    withContext(Dispatchers.IO) {
                        val questionInfoList= viewModel.ifQuestionInfoExist(quizDetails.quizid)
                        when(questionInfoList.size) {
                            0->{
                                ConfirmationDialog(requireActivity(), getString(R.string.heading_information), getString(R.string.msg_download_practice,binding.SpQuizDetails.text), object : ConfirmationImp {
                                    override fun onClickYes(context: Activity, dialog: Dialog?) {
                                        dialog!!.dismiss()
                                        val jsonParam = JsonObject()
                                        jsonParam.addProperty("QuizId", quizDetails.quizid)
                                        viewModel.questionImage(jsonParam)
                                    }
                                    override fun onClickNo(context: Activity, dialog: Dialog?) {
                                        dialog!!.dismiss()
                                    }
                                }).show(requireActivity().supportFragmentManager, ConfirmationDialog.TAG)
                            }
                            in 1..Integer.MAX_VALUE->{
                             val isShowInstruction = questionInfoList[0].instructionLink.isNullOrEmpty()
                                ShowInstructionConfirmationDialog(requireActivity(), getString(R.string.heading_information), getString(R.string.msg_start_session),
                                 getString(R.string.Ok),getString(R.string.label_show_instructions),isShowInstruction,
                                 object : ShowInstructionConfirmationImp {
                                 override fun onClickYes(context: Activity, dialog: Dialog?) {
                                     dialog!!.dismiss()
                                     EventBus.getDefault().postSticky(SingleUserMessageEvent(questionInfoList[0],binding.SpPauseTimer.adapter.getItem(spPauseTimerPos) as Int))
                                     startActivity(Intent().setClassName(getString(R.string.project_package_name),
                                         "practice.english.n2l.ui.practice.SelfPracticeActivity"))
                                 }
                                 override fun onClickShowInstruction(context: Activity, dialog: Dialog?) {
                                     dialog!!.dismiss()
                                     showInstructionQuizVideo(questionInfoList[0])
                                 }
                             }).show(requireActivity().supportFragmentManager, ShowInstructionConfirmationDialog.TAG)
                            }
                        }
                    }
                }
            }
        }

        CoroutineScope(Dispatchers.IO).launch{
                withContext(Dispatchers.IO) {
                    blankTemplates=viewModel.getBlankTemplates()
                }
        }
    }
    private fun downloadEachImage(url: String?,urlImage: String): String {
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
            val base64 =Base64.encodeBase64String(baos.toByteArray())
            baos.flush()
            baos.close()
            return  base64
        } catch (e: Exception) {
            Log.d("Error", e.toString())
        }
        return ""
    }

    private fun downloadEachImage2(baseUrl: String?, questions: Questions) {
       val okHttpClient = OkHttpClient()
       val request = okhttp3.Request.Builder()
           .url("$baseUrl ${questions.questionImage}")
           .build()
       okHttpClient.newCall(request).enqueue(object : Callback {
           override fun onFailure(call: Call, e: IOException) {
               Log.d("Imgerror",e.toString())
           }

           override fun onResponse(call: Call, response: Response) {
               questions.questionImageBase64 =Base64.encodeBase64String(response.body?.byteStream()?.readBytes())
               Log.d("ImgSuccess","Success")
           }
       })
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
            //EventBus.getDefault().removeStickyEvent(userDetailStickyEvent);
        }
    }
    /*
        val pauseStatus = arrayOf(1,2,3,4,5,6,7,8,9,10)
        pauseTimerAdapter= ArrayAdapter(requireActivity(), R.layout.dropdown,pauseStatus.toList())
        binding.SpPauseTimer.threshold = 0
        binding.SpPauseTimer.setAdapter(pauseTimerAdapter)
        binding.SpPauseTimer.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                spPauseTimerPos=position
            }
    */

    private fun  onSelectPause(valueToMatch:Int){
        var selectedIndex = -1
        for (i in pauseStatus.indices) {
            if (pauseStatus[i] == valueToMatch) {
                selectedIndex = i
                break
            }
        }
        if (selectedIndex != -1) {
            binding.SpPauseTimer.threshold = selectedIndex
            binding.SpPauseTimer.setText(pauseTimerAdapter.getItem(selectedIndex).toString(), false)
        }
    }
    private fun downloadInstructionLinkMP4(url: String, fileName: String):String {
        try {
            val destination = File(requireActivity().filesDir.absolutePath + File.separator + "assets/$fileName")
            val connection = URL(url).openConnection()
            connection.connect()
            val inputStream = BufferedInputStream(connection.getInputStream())
            val outputStream = FileOutputStream(destination)
            val buffer = ByteArray(1024)
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }
            outputStream.flush()
            outputStream.close()
            inputStream.close()
            return destination.absolutePath
        }
        catch (e: Exception) {
            Log.d("Error", e.toString())
        }
        return ""
    }
    private fun showInstructionQuizVideo(questionInfo: QuestionInfo) {
        EventBus.getDefault().postSticky(SelfPracticeObject(requireActivity(),questionInfo.instructionLinkLocalPath!!))
        startActivity(Intent().setClassName(getString(R.string.project_package_name), "practice.english.n2l.player.InstructionVideoPlayerActivity"))
    }
}

