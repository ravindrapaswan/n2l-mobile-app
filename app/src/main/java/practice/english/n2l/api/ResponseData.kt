package practice.english.n2l.api

import com.google.gson.annotations.SerializedName
import practice.english.n2l.database.bao.AnswerInfo
import practice.english.n2l.database.bao.Answers
import practice.english.n2l.database.bao.ContentType
import practice.english.n2l.database.bao.FriendList
import practice.english.n2l.database.bao.FriendRequests
import practice.english.n2l.database.bao.MainUrl
import practice.english.n2l.database.bao.MsFlag
import practice.english.n2l.database.bao.MyFriends
import practice.english.n2l.database.bao.MyVideos
import practice.english.n2l.database.bao.OTPInfo
import practice.english.n2l.database.bao.PointData
import practice.english.n2l.database.bao.PointDetails
import practice.english.n2l.database.bao.QuestionInfo
import practice.english.n2l.database.bao.Questions
import practice.english.n2l.database.bao.QuizDetails
import practice.english.n2l.database.bao.ResourceData
import practice.english.n2l.database.bao.SendFriendRequest
import practice.english.n2l.database.bao.StudentProfile
import practice.english.n2l.database.bao.SubContentType
import practice.english.n2l.database.bao.UploadResult
import practice.english.n2l.database.bao.UserDetail

data class ResponseData (
    @SerializedName("User")
    val userDetail: UserDetail,
    @SerializedName("OTPInfo")
    val oTPInfo: OTPInfo,
    @SerializedName("ContentType")
    val contentType: List<ContentType>,
    @SerializedName("SubContentType")
    val subContentType: List<SubContentType>,
    @SerializedName("QuizDetails")
    val quizDetails: List<QuizDetails>,
    @SerializedName("QuestionInfo")
    val questionInfo: QuestionInfo,
    @SerializedName("Questions")
    val questions: List<Questions>,
    @SerializedName("AnswerInfo")
    val answerInfo: AnswerInfo,
    @SerializedName("Answers")
    val answers: List<Answers>,
    @SerializedName("MsFlag")
    val msFlag: MsFlag,
    @SerializedName("MyFriends")
    val myFriends: List<MyFriends>,
    @SerializedName("ResourceData")
    val resourceData: ResourceData,
    @SerializedName("StudentProfile")
    val studentProfile: StudentProfile,
    @SerializedName("UploadResult")
    val uploadResult: List<UploadResult>,
    @SerializedName("PointData")
    val pointData: PointData,
    @SerializedName("PointDetails")
    val pointDetails: List<PointDetails>,
    @SerializedName("FriendRequests")
    val friendRequests: List<FriendRequests>,
    @SerializedName("FriendList")
    val friendList: List<FriendList>,
    @SerializedName("MainUrl")
    val mainUrl: List<MainUrl>,
    @SerializedName("MyVideos")
    val myVideos: List<MyVideos>,
    @SerializedName("SendFriendRequest")
    val sendFriendRequest: SendFriendRequest
    )
