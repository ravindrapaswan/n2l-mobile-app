package practice.english.n2l.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.JsonObject
import practice.english.n2l.api.ApiService
import practice.english.n2l.api.ResponseHandle
import practice.english.n2l.api.ResponseInfo
import practice.english.n2l.database.AppDatabase
import practice.english.n2l.database.bao.AnswerInfo
import practice.english.n2l.database.bao.Answers
import practice.english.n2l.database.bao.AttemptPractice
import practice.english.n2l.database.bao.Audio
import practice.english.n2l.database.bao.BlankTemplates
import practice.english.n2l.database.bao.LastPageTemplates
import practice.english.n2l.database.bao.MultiTemplates
import practice.english.n2l.database.bao.MyVideos
import practice.english.n2l.database.bao.QuestionInfo
import practice.english.n2l.database.bao.Questions
import practice.english.n2l.database.bao.SingleTemplates
import practice.english.n2l.database.bao.StudentProfile
import practice.english.n2l.util.Constants
import practice.english.n2l.util.NetworkUtils
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class PracticeRepoImp(private val apiService: ApiService, private val mContext: Context): PracticeRepo{
    private var appDatabase                         : AppDatabase? = null
    private fun initializeDB(context: Context)      : AppDatabase { return AppDatabase.getInstance(context)}
    private var contentTypeLiveData                 =  MutableLiveData<ResponseHandle<ResponseInfo>>()
    private var contentSubTypeLiveData              =  MutableLiveData<ResponseHandle<ResponseInfo>>()
    private var quizDetailsLiveData                 =  MutableLiveData<ResponseHandle<ResponseInfo>>()
    private var questionImageLiveData               =  MutableLiveData<ResponseHandle<ResponseInfo>>()
    private var answerImageLiveData                 =  MutableLiveData<ResponseHandle<ResponseInfo>>()
    private var masterResourcesLiveData             =  MutableLiveData<ResponseHandle<ResponseInfo>>()
    private var friendUserListLiveData              =  MutableLiveData<ResponseHandle<ResponseInfo>>()
    private var uploadPracticeVideoLiveData         =  MutableLiveData<ResponseHandle<ResponseInfo>>()
    private var pointsLiveData                      =  MutableLiveData<ResponseHandle<ResponseInfo>>()
    private var myVideoLiveData                     =  MutableLiveData<ResponseHandle<ResponseInfo>>()
    private var appExceptionLogLiveData             =  MutableLiveData<ResponseHandle<ResponseInfo>>()
    private var uploadAppPracticeAudioLiveData      =  MutableLiveData<ResponseHandle<ResponseInfo>>()


    val contentType : LiveData<ResponseHandle<ResponseInfo>>
    get() = contentTypeLiveData
    override suspend fun getContentType(jsonParams: JsonObject) {
        if(NetworkUtils.isInternetAvailable(mContext))
        {
            contentTypeLiveData.postValue(ResponseHandle.Loading())
            try
            {
                Log.d("Request URL : ", Constants.URL + "getContentType")
                Log.d("Request Data", "onRequestBody: $jsonParams")
                apiService.getContentType(jsonParams).enqueue(object : Callback<ResponseInfo> {
                    override fun onResponse(call: Call<ResponseInfo>, response: Response<ResponseInfo>) {
                        contentTypeLiveData.postValue(ResponseHandle.Success(response.body()))
                        Log.d("Response", "onResponseBody: " + response.body())
                    }
                    override fun onFailure(call: Call<ResponseInfo>, t: Throwable) {
                        contentTypeLiveData.postValue(ResponseHandle.Error(t.message.toString()))
                        Log.d("ResponseFailure", "onFailureMessage: " + t.message!!)
                    }
                })
            }
            catch (ex:Exception)
            {
                contentTypeLiveData.postValue(ResponseHandle.Error(ex.message.toString()))
            }
        }
        else
            contentTypeLiveData.postValue(ResponseHandle.Network())
    }

    val contentSubType : LiveData<ResponseHandle<ResponseInfo>>
    get() = contentSubTypeLiveData
    override suspend fun getContentSubType(jsonParams: JsonObject) {
        if(NetworkUtils.isInternetAvailable(mContext))
        {
            contentSubTypeLiveData.postValue(ResponseHandle.Loading())
            try
            {
                Log.d("Request URL : ", Constants.URL + "getContentSubType")
                Log.d("Request Data", "onRequestBody: $jsonParams")
                apiService.getContentSubType(jsonParams).enqueue(object : Callback<ResponseInfo> {
                    override fun onResponse(call: Call<ResponseInfo>, response: Response<ResponseInfo>) {
                        contentSubTypeLiveData.postValue(ResponseHandle.Success(response.body()))
                        Log.d("Response", "onResponseBody: " + response.body())
                    }
                    override fun onFailure(call: Call<ResponseInfo>, t: Throwable) {
                        contentSubTypeLiveData.postValue(ResponseHandle.Error(t.message.toString()))
                        Log.d("ResponseFailure", "onFailureMessage: " + t.message!!)
                    }
                })
            }
            catch (ex:Exception)
            {
                contentSubTypeLiveData.postValue(ResponseHandle.Error(ex.message.toString()))
            }
        }
        else
            contentSubTypeLiveData.postValue(ResponseHandle.Network())
    }

    val quizDetails : LiveData<ResponseHandle<ResponseInfo>>
    get() = quizDetailsLiveData
    override suspend fun getQuizDetails(jsonParams: JsonObject) {
        if(NetworkUtils.isInternetAvailable(mContext))
        {
            quizDetailsLiveData.postValue(ResponseHandle.Loading())
            try
            {
                Log.d("Request URL : ", Constants.URL + "GetQuizDetailsApp")
                Log.d("Request Data", "onRequestBody: $jsonParams")
                apiService.getQuizDetails(jsonParams).enqueue(object : Callback<ResponseInfo> {
                    override fun onResponse(call: Call<ResponseInfo>, response: Response<ResponseInfo>) {
                        quizDetailsLiveData.postValue(ResponseHandle.Success(response.body()))
                        Log.d("Response", "onResponseBody: " + response.body())
                    }
                    override fun onFailure(call: Call<ResponseInfo>, t: Throwable) {
                        quizDetailsLiveData.postValue(ResponseHandle.Error(t.message.toString()))
                        Log.d("ResponseFailure", "onFailureMessage: " + t.message!!)
                    }
                })
            }
            catch (ex:Exception)
            {
                quizDetailsLiveData.postValue(ResponseHandle.Error(ex.message.toString()))
            }
        }
        else
            quizDetailsLiveData.postValue(ResponseHandle.Network())
    }

    val questionImage : LiveData<ResponseHandle<ResponseInfo>>
    get() = questionImageLiveData
    override suspend fun getQuestionImageApp(jsonParams: JsonObject) {
        if(NetworkUtils.isInternetAvailable(mContext))
        {
            questionImageLiveData.postValue(ResponseHandle.Loading())
            try
            {
                Log.d("Request URL : ", Constants.URL + "GetQuestionImageApp")
                Log.d("Request Data", "onRequestBody: $jsonParams")
                apiService.getQuestionImageApp(jsonParams).enqueue(object : Callback<ResponseInfo> {
                    override fun onResponse(call: Call<ResponseInfo>, response: Response<ResponseInfo>) {
                        questionImageLiveData.postValue(ResponseHandle.Success(response.body()))
                        Log.d("Response", "onResponseBody: " + response.body())
                    }
                    override fun onFailure(call: Call<ResponseInfo>, t: Throwable) {
                        questionImageLiveData.postValue(ResponseHandle.Error(t.message.toString()))
                        Log.d("ResponseFailure", "onFailureMessage: " + t.message!!)
                    }
                })
            }
            catch (ex:Exception)
            {
                questionImageLiveData.postValue(ResponseHandle.Error(ex.message.toString()))
            }
        }
        else
            questionImageLiveData.postValue(ResponseHandle.Network())
    }

    val answerImage : LiveData<ResponseHandle<ResponseInfo>>
    get() = answerImageLiveData
    override suspend fun getAnswerImageApp(jsonParams: JsonObject) {
        if(NetworkUtils.isInternetAvailable(mContext))
        {
            answerImageLiveData.postValue(ResponseHandle.Loading())
            try
            {
                Log.d("Request URL : ", Constants.URL + "GetAnswerImageApp")
                Log.d("Request Data", "onRequestBody: $jsonParams")
                apiService.getAnswerImageApp(jsonParams).enqueue(object : Callback<ResponseInfo> {
                    override fun onResponse(call: Call<ResponseInfo>, response: Response<ResponseInfo>) {
                        answerImageLiveData.postValue(ResponseHandle.Success(response.body()))
                        Log.d("Response", "onResponseBody: " + response.body())
                    }
                    override fun onFailure(call: Call<ResponseInfo>, t: Throwable) {
                        answerImageLiveData.postValue(ResponseHandle.Error(t.message.toString()))
                        Log.d("ResponseFailure", "onFailureMessage: " + t.message!!)
                    }
                })
            }
            catch (ex:Exception)
            {
                answerImageLiveData.postValue(ResponseHandle.Error(ex.message.toString()))
            }
        }
        else
            answerImageLiveData.postValue(ResponseHandle.Network())
    }

    val masterResources : LiveData<ResponseHandle<ResponseInfo>>
    get() = masterResourcesLiveData
    override suspend fun getMasterResources(jsonParams: JsonObject) {
        if(NetworkUtils.isInternetAvailable(mContext))
        {
            masterResourcesLiveData.postValue(ResponseHandle.Loading())
            try
            {
                Log.d("Request URL : ", Constants.URL + "GetMasterResources")
                Log.d("Request Data", "onRequestBody: $jsonParams")
                apiService.getMasterResources(jsonParams).enqueue(object : Callback<ResponseInfo> {
                    override fun onResponse(call: Call<ResponseInfo>, response: Response<ResponseInfo>) {
                        masterResourcesLiveData.postValue(ResponseHandle.Success(response.body()))
                        Log.d("Response", "onResponseBody: " + response.body())
                    }
                    override fun onFailure(call: Call<ResponseInfo>, t: Throwable) {
                        masterResourcesLiveData.postValue(ResponseHandle.Error(t.message.toString()))
                        Log.d("ResponseFailure", "onFailureMessage: " + t.message!!)
                    }
                })
            }
            catch (ex:Exception)
            {
                masterResourcesLiveData.postValue(ResponseHandle.Error(ex.message.toString()))
            }
        }
        else
            masterResourcesLiveData.postValue(ResponseHandle.Network())
    }
    val points : LiveData<ResponseHandle<ResponseInfo>>
    get() = pointsLiveData
    override suspend fun getPoints(jsonParams: JsonObject) {
        if(NetworkUtils.isInternetAvailable(mContext))
        {
            pointsLiveData.postValue(ResponseHandle.Loading())
            try {
                Log.d("Request URL : ", Constants.URL + "getPoints")
                Log.d("Request Data", "onRequestBody: $jsonParams")
                apiService.getPoints(jsonParams).enqueue(object : Callback<ResponseInfo> {
                    override fun onResponse(call: Call<ResponseInfo>, response: Response<ResponseInfo>) {
                        pointsLiveData.postValue(ResponseHandle.Success(response.body()))
                        Log.d("Response", "onResponseBody: " + response.body())
                    }
                    override fun onFailure(call: Call<ResponseInfo>, t: Throwable) {
                        pointsLiveData.postValue(ResponseHandle.Error(t.message.toString()))
                        Log.d("ResponseFailure", "onFailureMessage: " + t.message!!)
                    }
                })
            }
            catch (ex:Exception) {
                pointsLiveData.postValue(ResponseHandle.Error(ex.message.toString()))
            }
        }
        else
            pointsLiveData.postValue(ResponseHandle.Network())
    }

    val uploadPracticeVideo : LiveData<ResponseHandle<ResponseInfo>>
    get() = uploadPracticeVideoLiveData
    override suspend fun uploadPracticeVideo(studentId: RequestBody, dateOfVideoCreation: RequestBody, quizId: RequestBody, singleMulti: RequestBody, friendId: RequestBody, mp4: MultipartBody.Part) {
        if(NetworkUtils.isInternetAvailable(mContext))
        {
            uploadPracticeVideoLiveData.postValue(ResponseHandle.Loading())
            try {
                Log.d("Request URL : ", Constants.URL + "UploadPracticeVideo")
                apiService.uploadPracticeVideo(studentId,dateOfVideoCreation,quizId,singleMulti,friendId,mp4).enqueue(object : Callback<ResponseInfo> {
                    override fun onResponse(call: Call<ResponseInfo>, response: Response<ResponseInfo>) {
                        uploadPracticeVideoLiveData.postValue(ResponseHandle.Success(response.body()))
                        Log.d("Response", "onResponseBody: " + response.body())
                    }
                    override fun onFailure(call: Call<ResponseInfo>, t: Throwable) {
                        uploadPracticeVideoLiveData.postValue(ResponseHandle.Error(t.message.toString()))
                        Log.d("ResponseFailure", "onFailureMessage: " + t.message!!)
                    }
                })
            }
            catch (ex:Exception)
            {
                uploadPracticeVideoLiveData.postValue(ResponseHandle.Error(ex.message.toString()))
            }
        }
        else
            uploadPracticeVideoLiveData.postValue(ResponseHandle.Network())
    }

    val  myVideo: LiveData<ResponseHandle<ResponseInfo>>
    get() = myVideoLiveData
    override suspend fun getMyVideo(jsonParams: JsonObject) {
        if(NetworkUtils.isInternetAvailable(mContext))
        {
            myVideoLiveData.postValue(ResponseHandle.Loading())
            try
            {
                Log.d("Request URL : ", Constants.URL + "getMyVideo")
                Log.d("Request Data", "onRequestBody: $jsonParams")
                apiService.getMyVideo(jsonParams).enqueue(object : Callback<ResponseInfo> {
                    override fun onResponse(call: Call<ResponseInfo>, response: Response<ResponseInfo>) {
                        myVideoLiveData.postValue(ResponseHandle.Success(response.body()))
                        Log.d("Response", "onResponseBody: " + response.body())
                    }

                    override fun onFailure(call: Call<ResponseInfo>, t: Throwable) {
                        myVideoLiveData.postValue(ResponseHandle.Error(t.message.toString()))
                        Log.d("ResponseFailure", "onFailureMessage: " + t.message!!)
                    }
                })
            }
            catch (ex:Exception)
            {
                myVideoLiveData.postValue(ResponseHandle.Error(ex.message.toString()))
            }
        }
        else
            myVideoLiveData.postValue(ResponseHandle.Network())
    }

    val friendUserList : LiveData<ResponseHandle<ResponseInfo>>
    get() = friendUserListLiveData
    override suspend fun getFriendUserList(jsonParams: JsonObject) {
        if(NetworkUtils.isInternetAvailable(mContext))
        {
            friendUserListLiveData.postValue(ResponseHandle.Loading())
            try
            {
                Log.d("Request URL : ", Constants.URL + "getFriendUserList")
                Log.d("Request Data", "onRequestBody: $jsonParams")
                apiService.getFriendUserList(jsonParams).enqueue(object : Callback<ResponseInfo> {
                    override fun onResponse(call: Call<ResponseInfo>, response: Response<ResponseInfo>) {
                        friendUserListLiveData.postValue(ResponseHandle.Success(response.body()))
                        Log.d("Response", "onResponseBody: " + response.body())
                    }

                    override fun onFailure(call: Call<ResponseInfo>, t: Throwable) {
                        friendUserListLiveData.postValue(ResponseHandle.Error(t.message.toString()))
                        Log.d("ResponseFailure", "onFailureMessage: " + t.message!!)
                    }
                })
            }
            catch (ex:Exception)
            {
                friendUserListLiveData.postValue(ResponseHandle.Error(ex.message.toString()))
            }
        }
        else
            friendUserListLiveData.postValue(ResponseHandle.Network())
    }

    val appExceptionLog : LiveData<ResponseHandle<ResponseInfo>>
    get() = appExceptionLogLiveData
    override suspend fun saveAppExceptionLog(jsonParams: JsonObject) {
        if(NetworkUtils.isInternetAvailable(mContext))
        {
            appExceptionLogLiveData.postValue(ResponseHandle.Loading())
            try
            {
                Log.d("Request URL : ", Constants.URL + "appApi/AppExceptionLog")
                Log.d("Request Data", "onRequestBody: $jsonParams")
                apiService.saveAppExceptionLog(jsonParams).enqueue(object : Callback<ResponseInfo> {
                    override fun onResponse(call: Call<ResponseInfo>, response: Response<ResponseInfo>) {
                        appExceptionLogLiveData.postValue(ResponseHandle.Success(response.body()))
                        Log.d("Response", "onResponseBody: " + response.body())
                    }

                    override fun onFailure(call: Call<ResponseInfo>, t: Throwable) {
                        appExceptionLogLiveData.postValue(ResponseHandle.Error(t.message.toString()))
                        Log.d("ResponseFailure", "onFailureMessage: " + t.message!!)
                    }
                })
            }
            catch (ex:Exception)
            {
                appExceptionLogLiveData.postValue(ResponseHandle.Error(ex.message.toString()))
            }
        }
        else
            appExceptionLogLiveData.postValue(ResponseHandle.Network())
    }

    val uploadAppPracticeAudio : LiveData<ResponseHandle<ResponseInfo>>
    get() = uploadAppPracticeAudioLiveData
    override suspend fun uploadAppPracticeAudio(studentId: RequestBody, dateOfAudioCreation: RequestBody, quizId: RequestBody, singleMulti: RequestBody, friendId: RequestBody, audio: MultipartBody.Part) {
        if(NetworkUtils.isInternetAvailable(mContext))
        {
            uploadAppPracticeAudioLiveData.postValue(ResponseHandle.Loading())
            try {
                Log.d("Request URL : ", Constants.URL + "appApi/UploadAppPracticeAudio")
                apiService.uploadAppPracticeAudio(studentId,dateOfAudioCreation,quizId,singleMulti,friendId,audio).enqueue(object : Callback<ResponseInfo> {
                    override fun onResponse(call: Call<ResponseInfo>, response: Response<ResponseInfo>) {
                        uploadAppPracticeAudioLiveData.postValue(ResponseHandle.Success(response.body()))
                        Log.d("Response", "onResponseBody: " + response.body())
                    }
                    override fun onFailure(call: Call<ResponseInfo>, t: Throwable) {
                        uploadAppPracticeAudioLiveData.postValue(ResponseHandle.Error(t.message.toString()))
                        Log.d("ResponseFailure", "onFailureMessage: " + t.message!!)
                    }
                })
            }
            catch (ex:Exception)
            {
                uploadAppPracticeAudioLiveData.postValue(ResponseHandle.Error(ex.message.toString()))
            }
        }
        else
            uploadAppPracticeAudioLiveData.postValue(ResponseHandle.Network())

    }


    override suspend fun insertQuestionInfo(questionInfo: QuestionInfo) {
        appDatabase = initializeDB(mContext)
        appDatabase!!.questionInfoDao().insertQuestionInfo(questionInfo)
    }

    override suspend fun insertAllQuestions(quizId: Long,questions: List<Questions>) {
        appDatabase = initializeDB(mContext)
        appDatabase!!.questionsDao().insertAllQuestions(quizId,questions)
    }

    override fun ifQuestionInfoExist(quizId: Long): List<QuestionInfo>{
        appDatabase = initializeDB(mContext)
        return appDatabase!!.questionInfoDao().ifQuestionInfoExist(quizId)
    }

    override fun startQuestionsPracticeSession(quizId: Long): List<Questions> {
        appDatabase = initializeDB(mContext)
        return appDatabase!!.questionsDao().startQuestionsPracticeSession(quizId)
    }

    override suspend fun  insertAnswerInfo(answerInfo: AnswerInfo) {
        appDatabase = initializeDB(mContext)
        appDatabase!!.answerInfoDao().insertAnswerInfo(answerInfo)
    }

    override suspend fun insertAllAnswers(quizId: Long,answers: List<Answers>) {
        appDatabase = initializeDB(mContext)
        appDatabase!!.answersDao().insertAllAnswers(quizId,answers)
    }
    override fun getAnswerInfoInTable(quizId: Long): AnswerInfo {
        appDatabase = initializeDB(mContext)
        return appDatabase!!.answerInfoDao().getAnswerInfoInTable(quizId)
    }
    override fun getAnswersPracticeSession(quizId: Long): List<Answers> {
        appDatabase = initializeDB(mContext)
        return appDatabase!!.answersDao().getAnswersPracticeSession(quizId)
    }
    override suspend fun updateAnswerBase64(answerImageBase64:String?,answersId:Long,quizId: Long){
        appDatabase = initializeDB(mContext)
        return appDatabase!!.answersDao().updateAnswerBase64(answerImageBase64,answersId,quizId)
    }
    override fun getAttemptPractice():LiveData<List<AttemptPractice>> {
        appDatabase = initializeDB(mContext)
        return appDatabase!!.attemptPracticeDao().getAttemptPractice()
    }

    override suspend fun deleteQuizIdAllTable(quizId:Long) {
        appDatabase = initializeDB(mContext)
        return appDatabase!!.attemptPracticeDao().deleteQuizIdAllTable(quizId)
    }
    override suspend fun insertAttemptPractice(attemptPractice: AttemptPractice) {
        appDatabase = initializeDB(mContext)
        return appDatabase!!.attemptPracticeDao().insertAttemptPractice(attemptPractice)
    }
    override suspend fun insertAllAudio(audio: List<Audio>) {
        appDatabase = initializeDB(mContext)
        return appDatabase!!.audioDao().insertAllAudio(audio)
    }
    override suspend fun insertStudentProfile(studentProfile: StudentProfile){
        appDatabase = initializeDB(mContext)
        return appDatabase!!.studentProfileDao().insertStudentProfile(studentProfile)
    }
    override suspend fun insertAllSingleTemplates(templates: List<SingleTemplates>) {
        appDatabase = initializeDB(mContext)
        return appDatabase!!.singleTemplatesDao().insertAllSingleTemplates(templates)
    }
    override suspend fun insertAllMultiTemplates(templates: List<MultiTemplates>) {
        appDatabase = initializeDB(mContext)
        return appDatabase!!.multiTemplatesDao().insertAllMultiTemplates(templates)
    }
    override suspend fun insertAllBlankTemplates(templates: List<BlankTemplates>) {
        appDatabase = initializeDB(mContext)
        return appDatabase!!.blankTemplatesDao().insertAllBlankTemplates(templates)
    }
    override suspend fun insertAllLastPageTemplates(templates: List<LastPageTemplates>) {
        appDatabase = initializeDB(mContext)
        return appDatabase!!.lastPageTemplatesDao().insertAllLastPageTemplates(templates)
    }
    override fun getSingleTemplates(): SingleTemplates {
        appDatabase = initializeDB(mContext)
        return appDatabase!!.singleTemplatesDao().getSingleTemplates()
    }
    override fun getMultiTemplates(): MultiTemplates {
        appDatabase = initializeDB(mContext)
        return appDatabase!!.multiTemplatesDao().getMultiTemplates()
    }
    override fun getBlankTemplates(): BlankTemplates {
        appDatabase = initializeDB(mContext)
        return appDatabase!!.blankTemplatesDao().getBlankTemplates()
    }
    override fun getLastPageTemplates(): LastPageTemplates {
        appDatabase = initializeDB(mContext)
        return appDatabase!!.lastPageTemplatesDao().getLastPageTemplates()
    }

    override fun getAudio(videoOneSlideTime: Int): Audio {
        appDatabase = initializeDB(mContext)
        return appDatabase!!.audioDao().getAudio(videoOneSlideTime)
    }
    override fun getStudentProfile(): StudentProfile {
        appDatabase = initializeDB(mContext)
        return appDatabase!!.studentProfileDao().getStudentProfile()
    }

    override fun getAttemptPracticeNotUpload():List<AttemptPractice>{
        appDatabase = initializeDB(mContext)
        return appDatabase!!.attemptPracticeDao().getAttemptPracticeNotUpload()
    }

    override fun updateAttemptPracticeUploadStatus(quizId:Long):Int{
        appDatabase = initializeDB(mContext)
        return appDatabase!!.attemptPracticeDao().updateAttemptPracticeUploadStatus(quizId)
    }

    override suspend fun insertMyVideos(myVideos: MyVideos){
        appDatabase = initializeDB(mContext)
        return appDatabase!!.myVideosDao().insert(myVideos)
    }
    override suspend fun deleteMyVideosByPracticesNo(practicesNo:Long,quizId:Long){
        appDatabase = initializeDB(mContext)
        return appDatabase!!.myVideosDao().deleteMyVideosByPracticesNo(practicesNo,quizId)
    }

    override fun getMyVideos(practicesNo:Long):MyVideos{
        appDatabase = initializeDB(mContext)
        return appDatabase!!.myVideosDao().getMyVideos(practicesNo)
    }

    override fun getAllMyVideosLiveData():LiveData<List<MyVideos>>{
        appDatabase = initializeDB(mContext)
        return appDatabase!!.myVideosDao().getAllMyVideosLiveData()
    }

    override suspend fun insertAllVideos(myVideos: List<MyVideos>) {
        appDatabase = initializeDB(mContext)
        return appDatabase!!.myVideosDao().insertAllVideos(myVideos)
    }
    override suspend fun updateMyVideos(practiceLocalFilePath:String?,practicesNo:Long) {
        appDatabase = initializeDB(mContext)
        return appDatabase!!.myVideosDao().updateMyVideos(practiceLocalFilePath,practicesNo)
    }
    override suspend fun updateMyVideoUploadStatus(practicesNo:Long,practiceOnlineFilePath:String,practiceLocalFilePath:String,quizId:Long):Int {
        appDatabase = initializeDB(mContext)
        return appDatabase!!.myVideosDao().updateMyVideoUploadStatus(practicesNo,practiceOnlineFilePath,practiceLocalFilePath,quizId)
    }
    override suspend fun updateMyVideoPracticeLocalFilePath(practiceLocalFilePath:String,practicesNo:Long,quizId:Long):Int {
        appDatabase = initializeDB(mContext)
        return appDatabase!!.myVideosDao().updateMyVideoPracticeLocalFilePath(practiceLocalFilePath,practicesNo,quizId)
    }
    override  fun getMyVideos(): List<MyVideos> {
        appDatabase = initializeDB(mContext)
        return appDatabase!!.myVideosDao().getMyVideos()
    }
}
interface PracticeRepo{
    suspend fun getContentType(jsonParams: JsonObject)
    suspend fun getContentSubType(jsonParams: JsonObject)
    suspend fun getQuizDetails(jsonParams: JsonObject)
    suspend fun getQuestionImageApp(jsonParams: JsonObject)
    suspend fun getAnswerImageApp(jsonParams: JsonObject)
    suspend fun insertQuestionInfo(questionInfo: QuestionInfo)
    suspend fun insertAllQuestions(quizId: Long,questions: List<Questions>)
    fun ifQuestionInfoExist(quizId: Long): List<QuestionInfo>
    fun startQuestionsPracticeSession(quizId: Long): List<Questions>
    fun getAnswerInfoInTable(quizId: Long): AnswerInfo
    suspend fun updateAnswerBase64(answerImageBase64:String?,answersId:Long,quizId: Long)
    fun getAnswersPracticeSession(quizId: Long): List<Answers>
    suspend fun insertAnswerInfo(answerInfo: AnswerInfo)
    suspend fun insertAllAnswers(quizId: Long,answers: List<Answers>)
    suspend fun getFriendUserList(jsonParams: JsonObject)
    suspend fun getMasterResources(jsonParams: JsonObject)
    suspend fun deleteQuizIdAllTable(quizId:Long)
    fun getAttemptPractice():LiveData<List<AttemptPractice>>
    suspend fun insertAttemptPractice(attemptPractice: AttemptPractice)
    suspend fun insertAllAudio(audio: List<Audio>)
    suspend fun insertStudentProfile(studentProfile: StudentProfile)
    suspend fun insertAllSingleTemplates(templates: List<SingleTemplates>)
    suspend fun insertAllMultiTemplates(templates: List<MultiTemplates>)
    suspend fun insertAllBlankTemplates(templates: List<BlankTemplates>)
    suspend fun insertAllLastPageTemplates(templates: List<LastPageTemplates>)
    fun getSingleTemplates():SingleTemplates
    fun getMultiTemplates():MultiTemplates
    fun getBlankTemplates():BlankTemplates
    fun getLastPageTemplates(): LastPageTemplates
    fun getAudio(videoOneSlideTime:Int):Audio
    fun getStudentProfile():StudentProfile
    fun getAttemptPracticeNotUpload():List<AttemptPractice>
    fun updateAttemptPracticeUploadStatus(quizId:Long):Int
    suspend fun uploadPracticeVideo(studentId:RequestBody,dateOfVideoCreation:RequestBody,quizId:RequestBody,singleMulti:RequestBody,friendId:RequestBody,mp4: MultipartBody.Part)
    suspend fun getPoints(jsonParams: JsonObject)
    suspend fun getMyVideo(jsonParams: JsonObject)
    suspend fun insertMyVideos(myVideos: MyVideos)
    suspend fun deleteMyVideosByPracticesNo(practicesNo:Long,quizId:Long)
    fun getMyVideos(practicesNo:Long): MyVideos
    fun getAllMyVideosLiveData(): LiveData<List<MyVideos>>
    suspend fun insertAllVideos(myVideos: List<MyVideos>)
    suspend fun updateMyVideos(practiceLocalFilePath:String?,practicesNo:Long)
    suspend fun updateMyVideoUploadStatus(practicesNo:Long,practiceOnlineFilePath:String,practiceLocalFilePath:String,quizId:Long):Int
    suspend fun updateMyVideoPracticeLocalFilePath(practiceLocalFilePath:String,practicesNo:Long,quizId:Long):Int
    fun getMyVideos(): List<MyVideos>
    suspend fun saveAppExceptionLog(jsonParams: JsonObject)
    suspend fun uploadAppPracticeAudio(studentId:RequestBody,dateOfAudioCreation:RequestBody,quizId:RequestBody,singleMulti:RequestBody,friendId:RequestBody,audio: MultipartBody.Part)
}