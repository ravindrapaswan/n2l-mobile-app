package practice.english.n2l.api

import com.google.gson.JsonObject
import io.reactivex.rxjava3.core.Observable
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {
    @Headers("Content-Type:application/json")
    @POST("appApi/GenerateOTPApp")
    fun generateOTP(@Body jsonParams: JsonObject): Call<ResponseInfo>

    @Headers("Content-Type:application/json")
    @POST("appApi/UserRegistrationApp")
    fun userRegistration(@Body jsonParams: JsonObject): Call<ResponseInfo>

    @Headers("Content-Type:application/json")
    @POST("appApi/GetContentType")
    fun getContentType(@Body jsonParams: JsonObject): Call<ResponseInfo>

    @Headers("Content-Type:application/json")
    @POST("appApi/GetContentSubType")
    fun getContentSubType(@Body jsonParams: JsonObject): Call<ResponseInfo>

    @Headers("Content-Type:application/json")
    @POST("appApi/GetQuizDetailsApp")
    fun getQuizDetails(@Body jsonParams: JsonObject): Call<ResponseInfo>

    @Headers("Content-Type:application/json")
    @POST("appApi/GetQuestionImageApp")
    fun getQuestionImageApp(@Body jsonParams: JsonObject): Call<ResponseInfo>

    @Headers("Content-Type:application/json")
    @POST("appApi/GetAnswerImageApp")
    fun getAnswerImageApp(@Body jsonParams: JsonObject): Call<ResponseInfo>

    @Headers("Content-Type:application/json")
    @POST("appApi/GetMasterResources")
    fun getMasterResources(@Body jsonParams: JsonObject): Call<ResponseInfo>

    @Multipart
    @POST("appApi/UploadPracticeVideo")
    fun uploadPracticeVideo(@Part("StudentId") studentId:RequestBody,
                            @Part("DateOfVideoCreation") dateOfVideoCreation: RequestBody,
                            @Part("QuizId") quizId:RequestBody,
                            @Part("single_multi") singleMulti:RequestBody,
                            @Part("friendid") friendId:RequestBody,
                            @Part mp4: MultipartBody.Part): Call<ResponseInfo>

    @Headers("Content-Type:application/json")
    @POST("appApi/getPoints")
    fun getPoints(@Body jsonParams: JsonObject): Call<ResponseInfo>

    @Headers("Content-Type:application/json")
    @POST("appApi/getPoints")
    fun getPointsRx(@Body jsonParams: JsonObject): Observable<ResponseInfo>

    @Headers("Content-Type:application/json")
    @POST("PracticeService/GetFriendUserList")
    fun getFriendUserList(@Body jsonParams: JsonObject): Call<ResponseInfo>

    @Headers("Content-Type:application/json")
    @POST("appApi/sendfriendrequest")
    fun sendFriendRequest(@Body jsonParams: JsonObject): Call<ResponseInfo>

    @Headers("Content-Type:application/json")
    @POST("appApi/getfriendrequests")
    fun getFriendRequests(@Body jsonParams: JsonObject): Call<ResponseInfo>

    @Headers("Content-Type:application/json")
    @POST("appApi/actionfriendrequest")
    fun actionFriendRequest(@Body jsonParams: JsonObject): Call<ResponseInfo>

    @Headers("Content-Type:application/json")
    @POST("appApi/getfriendList")
    fun getFriendList(@Body jsonParams: JsonObject): Call<ResponseInfo>

    @Headers("Content-Type:application/json")
    @POST("appApi/getfriendList")
    fun getFriendListRx(@Body jsonParams: JsonObject): Observable<ResponseInfo>

    @Headers("Content-Type:application/json")
    @POST("appApi/getMyVideo")
    fun getMyVideo(@Body jsonParams: JsonObject): Call<ResponseInfo>

    @Multipart
    @POST("appApi/updateProfile")
    fun updateProfile(@Part("userid") userid:RequestBody,
                      @Part("name") name:RequestBody,
                      @Part("dob") dob:RequestBody,
                      @Part("email") email: RequestBody,
                      @Part ProfilePhoto: MultipartBody.Part): Call<ResponseInfo>


    @Headers("Content-Type:application/json")
    @POST("appApi/unfriend")
    fun getUnfriend(@Body jsonParams: JsonObject): Call<ResponseInfo>

    @Headers("Content-Type:application/json")
    @POST("appApi/AppExceptionLog")
    fun saveAppExceptionLog(@Body jsonParams: JsonObject): Call<ResponseInfo>

    @Multipart
    @POST("appApi/UploadAppPracticeAudio")
    fun uploadAppPracticeAudio(@Part("StudentId") studentId:RequestBody,
                               @Part("DateOfAudioCreation") dateOfAudioCreation: RequestBody,
                               @Part("QuizId") quizId:RequestBody,
                               @Part("single_multi") singleMulti:RequestBody,
                               @Part("friendid") friendId:RequestBody,
                               @Part audio: MultipartBody.Part): Call<ResponseInfo>
}
