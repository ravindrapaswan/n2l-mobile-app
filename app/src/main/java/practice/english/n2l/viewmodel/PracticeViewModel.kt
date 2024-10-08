package practice.english.n2l.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import practice.english.n2l.api.ResponseHandle
import practice.english.n2l.api.ResponseInfo
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
import practice.english.n2l.repository.PracticeRepo
import practice.english.n2l.repository.PracticeRepoImp
import okhttp3.MultipartBody
import okhttp3.RequestBody


class PracticeViewModel(private val repository: PracticeRepo): ViewModel() {
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO

    fun contentType(jsonParams:JsonObject) {
        getContentType(jsonParams)
    }

    val  contentType : LiveData<ResponseHandle<ResponseInfo>>
    get() = (repository as PracticeRepoImp).contentType
    private fun getContentType(jsonParams: JsonObject) {
        CoroutineScope(ioDispatcher).launch{
            repository.getContentType(jsonParams)
        }
    }

    fun contentSubType(jsonParams:JsonObject) {
        getContentSubType(jsonParams)
    }
    val  contentSubType : LiveData<ResponseHandle<ResponseInfo>>
    get() = (repository as PracticeRepoImp).contentSubType
    private fun getContentSubType(jsonParams: JsonObject) {
        CoroutineScope(ioDispatcher).launch{
            repository.getContentSubType(jsonParams)
        }
    }

    fun quizDetails(jsonParams:JsonObject) {
        getQuizDetails(jsonParams)
    }
    val  quizDetails : LiveData<ResponseHandle<ResponseInfo>>
    get() = (repository as PracticeRepoImp).quizDetails
    private fun getQuizDetails(jsonParams: JsonObject) {
        CoroutineScope(ioDispatcher).launch{
            repository.getQuizDetails(jsonParams)
        }
    }

    fun questionImage(jsonParams:JsonObject) {
        getQuestionImage(jsonParams)
    }
    val  questionImage : LiveData<ResponseHandle<ResponseInfo>>
    get() = (repository as PracticeRepoImp).questionImage
    private fun getQuestionImage(jsonParams: JsonObject) {
        CoroutineScope(ioDispatcher).launch{
            repository.getQuestionImageApp(jsonParams)
        }
    }

    fun answerImage(jsonParams:JsonObject) {
        getAnswerImageApp(jsonParams)
    }
    val  answerImage : LiveData<ResponseHandle<ResponseInfo>>
    get() = (repository as PracticeRepoImp).answerImage
    private fun getAnswerImageApp(jsonParams: JsonObject) {
        CoroutineScope(ioDispatcher).launch{
            repository.getAnswerImageApp(jsonParams)
        }
    }

    val  masterResources : LiveData<ResponseHandle<ResponseInfo>>
    get() = (repository as PracticeRepoImp).masterResources
    fun getMasterResources(jsonParams: JsonObject) {
        CoroutineScope(ioDispatcher).launch{
            repository.getMasterResources(jsonParams)
        }
    }

    fun startQuestionsPracticeSession(quizId: Long): List<Questions>  = repository.startQuestionsPracticeSession(quizId)
    fun getAnswersPracticeSession(quizId: Long): List<Answers> = repository.getAnswersPracticeSession(quizId)
    fun getAnswerInfoInTable(quizId: Long): AnswerInfo  = repository.getAnswerInfoInTable(quizId)
    fun updateAnswerBase64(answerImageBase64:String?,answersId:Long,quizId: Long) {
        CoroutineScope(Dispatchers.IO).launch  {
            repository.updateAnswerBase64(answerImageBase64,answersId,quizId)
        }
    }

    val  points : LiveData<ResponseHandle<ResponseInfo>>
    get() = (repository as PracticeRepoImp).points
    fun getPoints(jsonParams: JsonObject) {
        CoroutineScope(ioDispatcher).launch{
            repository.getPoints(jsonParams)
        }
    }

    val  friendUserList : LiveData<ResponseHandle<ResponseInfo>>
    get() = (repository as PracticeRepoImp).friendUserList
    private fun getFriendUserList(jsonParams: JsonObject) {
        CoroutineScope(ioDispatcher).launch{
            repository.getFriendUserList(jsonParams)
        }
    }

    val  appExceptionLog : LiveData<ResponseHandle<ResponseInfo>>
    get() = (repository as PracticeRepoImp).appExceptionLog
    fun saveAppExceptionLog(jsonParams: JsonObject) {
        CoroutineScope(ioDispatcher).launch{
            repository.saveAppExceptionLog(jsonParams)
        }
    }

    val  uploadAppPracticeAudio : LiveData<ResponseHandle<ResponseInfo>>
    get() = (repository as PracticeRepoImp).uploadAppPracticeAudio
    fun uploadAppPracticeAudio(studentId: RequestBody, dateOfAudioCreation: RequestBody, quizId: RequestBody, singleMulti: RequestBody, friendId: RequestBody, audio: MultipartBody.Part) {
        CoroutineScope(ioDispatcher).launch{
            repository.uploadAppPracticeAudio(studentId,dateOfAudioCreation,quizId,singleMulti,friendId,audio)
        }
    }

    suspend fun insertQuestionInfo(questionInfo: QuestionInfo) {
        //CoroutineScope(Dispatchers.IO).launch  {
            repository.insertQuestionInfo(questionInfo)
        //}
    }
    suspend fun insertAllQuestions(quizId: Long, questions: List<Questions>) {
       // CoroutineScope(Dispatchers.IO).launch  {
            repository.insertAllQuestions(quizId,questions)
        //}
    }
    fun ifQuestionInfoExist(quizId: Long): List<QuestionInfo> = repository.ifQuestionInfoExist(quizId)


    suspend fun insertAnswerInfo(answerInfo: AnswerInfo){
            repository.insertAnswerInfo(answerInfo)
    }
    suspend fun insertAllAnswers(quizId: Long,answers: List<Answers>) {
            repository.insertAllAnswers(quizId,answers)
    }
    fun getAttemptPractice():LiveData<List<AttemptPractice>> = repository.getAttemptPractice()
    fun deleteQuizIdAllTable(quizId: Long) {
        CoroutineScope(Dispatchers.IO).launch  {
            repository.deleteQuizIdAllTable(quizId)
        }
    }

    fun insertAttemptPractice(attemptPractice: AttemptPractice)  {
        CoroutineScope(Dispatchers.IO).launch  {
            repository.insertAttemptPractice(attemptPractice)
        }
    }
    suspend fun insertAllAudio(audio: List<Audio>) {
            repository.insertAllAudio(audio)
    }
    suspend fun  insertAllSingleTemplates(templates: List<SingleTemplates>) {
        repository.insertAllSingleTemplates(templates)
    }
    fun getSingleTemplates():SingleTemplates = repository.getSingleTemplates()

    suspend fun  insertAllMultiTemplates(templates: List<MultiTemplates>) {
        repository.insertAllMultiTemplates(templates)
    }
    fun getMultiTemplates():MultiTemplates = repository.getMultiTemplates()

    suspend fun insertAllBlankTemplates(templates: List<BlankTemplates>) {
        repository.insertAllBlankTemplates(templates)
    }

    fun getBlankTemplates():BlankTemplates = repository.getBlankTemplates()

    suspend fun insertAllStudentProfile(studentProfile:StudentProfile) {
            repository.insertStudentProfile(studentProfile)
    }
    suspend fun insertAllLastPageTemplates(templates: List<LastPageTemplates>) {
        repository.insertAllLastPageTemplates(templates)
    }
    fun getLastPageTemplates():LastPageTemplates = repository.getLastPageTemplates()

    fun getAudio(videoOneSlideTime:Int):Audio = repository.getAudio(videoOneSlideTime)
    fun getStudentProfile():StudentProfile = repository.getStudentProfile()

    val  uploadPracticeVideo : LiveData<ResponseHandle<ResponseInfo>>
    get() = (repository as PracticeRepoImp).uploadPracticeVideo
    suspend fun uploadPracticeVideo(studentId: RequestBody, dateOfVideoCreation: RequestBody, quizId: RequestBody, singleMulti: RequestBody, friendId: RequestBody, mp4: MultipartBody.Part) {
            repository.uploadPracticeVideo(studentId,dateOfVideoCreation,quizId,singleMulti,friendId,mp4)
    }

    val  myVideo : LiveData<ResponseHandle<ResponseInfo>>
    get() = (repository as PracticeRepoImp).myVideo
    fun getMyVideo(jsonParams: JsonObject) {
        CoroutineScope(ioDispatcher).launch{
            repository.getMyVideo(jsonParams)
        }
    }

    fun updateAttemptPracticeUploadStatus(quizId:Long) {
        CoroutineScope(Dispatchers.IO).launch {
             repository.updateAttemptPracticeUploadStatus(quizId)
        }
    }
     suspend fun  insertAllVideos(myVideos: List<MyVideos>) {
            repository.insertAllVideos(myVideos)
    }
    fun updateMyVideos(practiceLocalFilePath:String?,practicesNo:Long){
        CoroutineScope(Dispatchers.IO).launch {
            repository.updateMyVideos(practiceLocalFilePath,practicesNo)
        }
    }
    fun  deleteMyVideosByPracticesNo(practicesNo:Long,quizId:Long){
        CoroutineScope(Dispatchers.IO).launch {
            repository.deleteMyVideosByPracticesNo(practicesNo,quizId)
        }
    }

    suspend fun updateMyVideoUploadStatus(practicesNo:Long,practiceOnlineFilePath:String,practiceLocalFilePath:String,quizId:Long):Int {
       return repository.updateMyVideoUploadStatus(practicesNo,practiceOnlineFilePath,practiceLocalFilePath,quizId)
    }
    suspend fun updateMyVideoPracticeLocalFilePath(practiceLocalFilePath:String,practicesNo:Long,quizId:Long):Int {
        return repository.updateMyVideoPracticeLocalFilePath(practiceLocalFilePath,practicesNo,quizId)
    }

    fun getMyVideos(): List<MyVideos> = repository.getMyVideos()
    fun getAllMyVideosLiveData():LiveData<List<MyVideos>> = repository.getAllMyVideosLiveData()
    class Factory(private val repository: PracticeRepo) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return if (modelClass.isAssignableFrom(PracticeViewModel::class.java)) {
                PracticeViewModel(repository) as T
            } else {
                throw IllegalArgumentException("ViewModel Not Found")
            }
        }
    }
}