package practice.english.n2l.database.bao

import android.app.Activity
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.google.gson.annotations.SerializedName
import practice.english.n2l.util.TimestampConverter
import java.util.Date

data class Users (
    var username: String,
    var connectionid: String
)

data class OTPInfo (
    @SerializedName("OtpRecordId")
    var otpRecordId: Long,
    var userMobileNumber: String,
    var userName:String,
    var userNickName:String,
    var referralId:String
    //val userId:String,
    //var otp:String?
)

data class ContentType (
    @SerializedName("courseid")
    var courseId: Int,
    @SerializedName("contentid")
    var contentId: Int,
    @SerializedName("contentname")
    var contentName:String,
)
{
    override fun toString(): String {
        return contentName
    }
}
data class SubContentType (
    @SerializedName("contentid")
    val contentId:Long,
    @SerializedName("subcontentid")
    var subContentId: Long,
    @SerializedName("subcontentname")
    var subContentName: String
)
{
    override fun toString(): String {
        return subContentName
    }
}
data class QuizDetails (
    @SerializedName("quizid")
    val quizid:Long,
    @SerializedName("contentid")
    val contentId:Long,
    @SerializedName("subcontentid")
    var subContentId: Long,
    @SerializedName("quiztopicname")
    val quizTopicName:String,
    @SerializedName("videopausetime")
    val videoPauseTime:Int

)
{
    override fun toString(): String {
        return quizTopicName
    }
}

data class MsFlag (
    var flag: Int
)

data class MyFriends (
    @SerializedName("recordid"    ) var recordId     : Int,
    @SerializedName("userid"      ) var userId       : String,
    @SerializedName("username"    ) var userName     : String,
    @SerializedName("usernickname") var userNickName : String,
    @SerializedName("usermobileno") var userMobileNo : String,
)
{
    override fun toString(): String {
        return userName
    }
}

data class SpeakerRole(
    var speakerRoleId       : Int,
    var speakerRoleName     : String,
)
{
    override fun toString(): String {
        return speakerRoleName
    }
}
data class SingleUserMessageEvent (
    var questionInfo      : QuestionInfo,
    var pauseTimer        : Int
)
data class MultiUserMessageEvent (
    var questionInfo      : QuestionInfo,
    var pauseTimer        : Int,
    var friendList        : FriendList,
    var speakerRole       : SpeakerRole
)

data class PointMessageEvent (
    var pointData      : PointData,
    var pointDetails   : List<PointDetails>
)
data class MyFriendMessageEvent (
    var friendList   : MutableList<FriendList>
)

data class ResourceData(
    @SerializedName("audio")
    var audio: List<Audio>,
    @SerializedName("SingleTemplates")
    var singleTemplates: List<SingleTemplates>,
    @SerializedName("MultiTemplates")
    var multiTemplates: List<MultiTemplates>,
    @SerializedName("blank")
    var blankTemplates: List<BlankTemplates>,
    @SerializedName("LastPage")
    var lastPageTemplates: List<LastPageTemplates>,
    @SerializedName("MainUrl")
    var MainUrl: List<MainUrl>
)

data class UploadResult(
    @SerializedName("practicesno")
    val practiceSno: Long,
    @SerializedName("filepath")
    val filePath: String,
    @SerializedName("quizid")
    val quizId: Long,
)

data class PointData(
    @SerializedName("TotalPoint")
    val totalPoint: String,
    @SerializedName("Url")
    val url: String,
)

data class PointDetails(
    @SerializedName("assignmentid")
    val assignmentId: Long,
    @SerializedName("quiztopicname")
    val quiztopicname: String,
    @SerializedName("uploaddate")
    val uploadDate: String,
    @SerializedName("filepath")
    val filePath: String,
    @SerializedName("assignedpoints")
    val assignedPoints: Long,
)
data class FriendRequests(
    @SerializedName("recordid")
    val recordId: Long,
    @SerializedName("friendid")
    val friendId: Long,
    @SerializedName("friendname")
    val friendName: String,
    @SerializedName("mobilenumber")
    val mobileNumber: Long,
    @SerializedName("requestdate")
    val requestDate: String,
)

data class FriendList(
    @SerializedName("recordid")
    val recordId: Long,
    @SerializedName("friendid")
    val friendId: Long,
    @SerializedName("friendname")
    var friendName: String,
    @SerializedName("mobilenumber")
    val mobileNumber: Long,
    @SerializedName("photoPath")
    var photoPath: String,
)
{
    override fun toString(): String {
        return friendName
    }
}
data class MainUrl(
    @SerializedName("url")
    val url: String,
)
data class SendFriendRequest(
    @SerializedName("recordid")
    val recordId: Long,
)
data class SelfPracticeObject(
    val context: Activity,
    val practiceLocalFilePath: String
)
data class MultiPracticeObject(
    val context: Activity,
    val filePath:String
)
data class SelfPracticeBack(
    val back: String
)
data class MultiPracticeBack(
    val back: String
)
data class UserDetailObject(val userDetail: UserDetail)
data class PracticeAudioExceptionLog(
    var userId              : String,
    var quizId              : Long,
    val exception           : String,
    val stackException      : String,
    val appPage             : String,
    val createDate          : Date=Date()
)
/*Start Entity*/

@Entity(tableName = "Audio")
data class Audio(@PrimaryKey(autoGenerate = true) val audioId: Long,
   @SerializedName("id")
   val Id: Long,
   @SerializedName("filetype")
   val fileType: String,
   @SerializedName("videooneslidetime")
   val videoOneSlideTime: Int,
   @SerializedName("name")
   val name: String,
   @SerializedName("url")
   val url: String,
   var fileData: String?,
   var fileLocalPath: String,
)

@Entity(tableName = "SingleTemplates")
data class SingleTemplates(@PrimaryKey(autoGenerate = true) val templateId: Long,
    @SerializedName("id")
    val Id: Long,
    @SerializedName("filetype")
    val fileType: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("url")
    val url: String,
    var fileData: String?,
    var fileLocalPath: String,
)
@Entity(tableName = "MultiTemplates")
data class MultiTemplates(@PrimaryKey(autoGenerate = true) val templateId: Long,
                           @SerializedName("id")
                           val Id: Long,
                           @SerializedName("filetype")
                           val fileType: String,
                           @SerializedName("name")
                           val name: String,
                           @SerializedName("url")
                           val url: String,
                           var fileData: String?,
                           var fileLocalPath: String,
)
@Entity(tableName = "blankTemplates")
data class BlankTemplates(@PrimaryKey(autoGenerate = true) val templateId: Long,
                          @SerializedName("id")
                          val Id: Long,
                          @SerializedName("filetype")
                          val fileType: String,
                          @SerializedName("name")
                          val name: String,
                          @SerializedName("url")
                          val url: String,
                          var fileData: String?,
                          var fileLocalPath: String,
)
@Entity(tableName = "LastPageTemplates")
data class LastPageTemplates(@PrimaryKey(autoGenerate = true) val templateId: Long,
                          @SerializedName("id")
                          val Id: Long,
                          @SerializedName("filetype")
                          val fileType: String,
                          @SerializedName("name")
                          val name: String,
                          @SerializedName("url")
                          val url: String,
                          var fileData: String?,
                          var fileLocalPath: String,
)


@Entity(tableName = "StudentProfile")
data class StudentProfile(@PrimaryKey(autoGenerate = true) val studentProfileId: Long,
    @SerializedName("userid")
    val userId: Long,
    @SerializedName("name")
    val name: String?,
    @SerializedName("PhotoPath")
    val photoPath: String,
    var photoData: String?,
    var photoLocalPath: String,
    @SerializedName("email")
    var emailId:String?
)

@Entity(tableName = "AppExceptionLog")
data class AppExceptionLog(@PrimaryKey(autoGenerate = true) val id: Long,
    val quizId:Long,
    val exception: String,
    val stackException: String,
    val uploadStatus:String="N",
    val createDate:Date=Date(),
)

@Entity(tableName = "UserDetail")
data class UserDetail(@PrimaryKey @SerializedName("recordid") val recordId: Long,
    @SerializedName("userId")
    val userId:String,
    var userName:String,
    var userNickName:String,
    var userMobileNumber:String,
    var dateTimeStamp:String?,
    var pinNo:String?
)

@Entity(tableName = "QuestionInfo")
data class QuestionInfo(@PrimaryKey(autoGenerate = true)  val questionInfoId: Long,
    @SerializedName("QuizId")
    val quizId: Long,
    @SerializedName("NoofQuestions")
    val noOfQuestions: Int?,
    @SerializedName("Topic")
    val topic: String,
    @SerializedName("VideoOneSlideTime")
    val videoOneSlideTime: Int,
    @SerializedName("URL")
    val uRL: String?,
    @SerializedName("ComprehensionType")
    val comprehensionType: String?,
    @SerializedName("ComprehensionFile")
    val comprehensionFile: String?,
    @SerializedName("VideoPauseTime")
    val videoPauseTime: Int,
    var singleMultiQuestionType: Int,
    @SerializedName("InstructionLink")
    var instructionLink: String?,
    var instructionLinkLocalPath: String?,
)

@Entity(tableName = "Questions")
data class Questions(@PrimaryKey(autoGenerate = true)  val questionsId: Long,
   @SerializedName("QuizId")
   var quizId: Long,
   @SerializedName("QuestionId")
   val questionId: Long,
   @SerializedName("QuestionSNO")
   val questionSNO: Int,
   @SerializedName("QuestionImage")
   val questionImage: String,
   var questionImageBase64: String?,
   var questionImageAsBlank: Int?=null,
   var singleMultiQuestionType: Int
)

@Entity(tableName = "AnswerInfo")
data class AnswerInfo(@PrimaryKey(autoGenerate = true)  val answerInfoId: Long,
   @SerializedName("QuizId")
   val quizId: Long,
   @SerializedName("Topic")
   val topic: String,
   @SerializedName("VideoOneSlideTime")
   val videoOneSlideTime: Int,
   @SerializedName("NoofQuestions")
   val noOfQuestions: Int?,
   @SerializedName("URL")
   val uRL: String?
)

@Entity(tableName = "Answers")
data class Answers(@PrimaryKey(autoGenerate = true)  val answersId: Long,
    @SerializedName("QuizId")
    var quizId: Long,
    @SerializedName("QuestionId")
    val questionId: Long,
    @SerializedName("QuestionSNO")
    val questionSNO: Int,
    @SerializedName("AnswerImagePath")
    val answerImagePath: String,
    var answerImageBase64: String?,
    var answerImageAsBlank: Int?=null
)
// MyVideos(id=0, quizId=1001,
// practiceName=Pronouns - Am, is, are,
// practiceOnlineFilePath=https://nodeapitesting.s3.ap-south-1.amazonaws.com/StudentPracticeVideos/10010900088.mp4,
// practiceLocalFilePath=/data/data/practice.english.n2l/files/practiceVideo/900088.mp4,
// practiceDate=10-03-2024, practiceTime=null, singleMulti=1, friendId=null, friendName=null, practicesNo=900088, uploadStatus=null)
@Entity(tableName = "MyVideos")
data class MyVideos(@PrimaryKey(autoGenerate = true)  val id: Long,
    @SerializedName("quizid")
    val quizId: Long,
    @SerializedName("practiceName")
    val practiceName: String,
    @SerializedName("practiceFilePath")
    var practiceOnlineFilePath: String?,
    var practiceLocalFilePath: String?,
    @SerializedName("practiceDate")
    val practiceDate: String,
    @SerializedName("practiceTime")
    var practiceTime: String?,
    @SerializedName("single_multi")
    val singleMulti: Int,
    @SerializedName("friendid")
    val friendId: Long?,
    @SerializedName("friendname")
    val friendName: String?,
    @SerializedName("practicesno")
    val practicesNo: Long?,
    var uploadStatus:String="N"
)

@Entity(tableName = "AttemptPractice")
data class AttemptPractice(@PrimaryKey(autoGenerate = true) val attemptPracticeId: Long,
                           val quizId: Long,
                           val practiceName:String,
                           val practiceFilePath:String,
                           @TypeConverters(TimestampConverter::class)
                           var practiceDate: Date=Date(),
                           var singleMulti:Int,
                           var friendId:Long,
                           var uploadStatus:String="N"
)

/*Stop Entity*/
