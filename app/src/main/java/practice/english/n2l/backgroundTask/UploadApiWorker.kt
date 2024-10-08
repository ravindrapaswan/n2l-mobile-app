package practice.english.n2l.backgroundTask

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import practice.english.n2l.api.ResponseHandle
import practice.english.n2l.api.ResponseInfo
import practice.english.n2l.api.RetrofitService
import practice.english.n2l.database.AppDatabase
import practice.english.n2l.util.TimestampConverter
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class UploadApiWorker(context: Context,workerParams: WorkerParameters) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result {
        return try {
            val appDatabase         =   AppDatabase.getInstance(applicationContext)
            val attemptPracticeList =   appDatabase.attemptPracticeDao().getAttemptPracticeNotUpload()
            val user =appDatabase.userDetailDao().getUserInfo()

            var singleMulti:RequestBody
            var friendId:RequestBody
            for (attemptPractice in attemptPracticeList) {
                val file = File(attemptPractice.practiceFilePath)
                val requestFile= file.absoluteFile.asRequestBody("video/mp4".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("mp4", file.name, requestFile)

                val studentId = user.userId.toRequestBody("application/json".toMediaTypeOrNull())
                val dateOfVideoCreation = TimestampConverter.dateToString(attemptPractice.practiceDate).toRequestBody("application/json".toMediaTypeOrNull())
                val quizId = attemptPractice.quizId.toString().toRequestBody("application/json".toMediaTypeOrNull())
                if(attemptPractice.singleMulti==1) {
                    singleMulti = attemptPractice.singleMulti.toString().toRequestBody("application/json".toMediaTypeOrNull())
                    friendId ="".toRequestBody("application/json".toMediaTypeOrNull())
                }
                else {
                    singleMulti= attemptPractice.singleMulti.toString().toRequestBody("application/json".toMediaTypeOrNull())
                    friendId= attemptPractice.friendId.toString().toRequestBody("application/json".toMediaTypeOrNull())
                }

                RetrofitService.service.uploadPracticeVideo(studentId,dateOfVideoCreation, quizId,singleMulti,friendId,body)
                    .enqueue(object :
                    Callback<ResponseInfo> {
                    override fun onResponse(call: Call<ResponseInfo>, response: Response<ResponseInfo>) {
                        val it =ResponseHandle.Success(response.body())
                        when (it.data!!.responseCode) {
                            800 ->{
                                val practiceSno=it.data.responseData.uploadResult[0].practiceSno
                                CoroutineScope(Dispatchers.IO).launch {
                                    appDatabase.attemptPracticeDao().updateAttemptPracticeUploadStatus(attemptPractice.quizId)
                                    appDatabase.questionsDao().deleteAllQuestionsExistQuestionImageAsBlank(attemptPractice.quizId)
                                    appDatabase.answersDao().deleteAllAnswersExistQuestionImageAsBlank(attemptPractice.quizId)
                                }
                            }
                            in 300.. 302 ->{}
                            500 ->{}
                            else ->{}
                        }
                        Log.d("Response", "onResponseBody: " + response.body())
                    }
                    override fun onFailure(call: Call<ResponseInfo>, t: Throwable) {
                        Log.d("ResponseFailure", "onFailureMessage: " + t.message!!)
                    }
                })
            }
            Result.success()
        } catch (e: Exception) {
            Log.e("NetworkRequestWorker", "Exception: ${e.message}")
            Result.failure()
        }
    }
}

/*val response= RetrofitService.service.uploadPracticeVideo(user.userId.toLong(),
                   TimestampConverter.dateToString(attemptPractice.practiceDate),
                   attemptPractice.quizId,body).execute()
               if (response.isSuccessful) {
                   val it = response.body()
                   it.data!!.responseData.uploadResult

               } else {
                   //Log.e("NetworkRequestWorker", "Unsuccessful response: ${response.code()}")
                   //Result.retry()
               }*/