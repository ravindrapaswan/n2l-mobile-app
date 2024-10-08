package practice.english.n2l.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import practice.english.n2l.database.bao.AnswerInfo
import practice.english.n2l.database.bao.Answers
import practice.english.n2l.database.bao.AppExceptionLog
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
import practice.english.n2l.database.bao.UserDetail


@Dao
interface UserDetailDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUserDetails(userReg: UserDetail)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllUserDetails(userReg: List<UserDetail>)

    @Query("select * from UserDetail")
    fun getUserDetail():LiveData<UserDetail>

    @Query("select * from UserDetail")
    fun getUserInfo():UserDetail

    @Update
    fun updateSingleUser(userReg: UserDetail)

    @Query("Update UserDetail set pinNo=:newPin")
    fun updatePin(newPin:String)

    @Query("Update UserDetail set userName=:name")
    suspend fun updateName(name:String)

    @Query("Delete FROM UserDetail")
    fun deleteAll(): Int
}

@Dao
interface AppExceptionLogDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(appExceptionLog: AppExceptionLog):Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(appExceptionLog: List<AppExceptionLog>)

    @Query("delete from AppExceptionLog")
    suspend fun deleteAllAppExceptionLog()

    @Query("select * from AppExceptionLog")
    fun getAllAppExceptionLogLiveData(): LiveData<List<AppExceptionLog>>
}


@Dao
interface QuestionInfoDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(questionInfo: QuestionInfo)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(questionInfo: List<QuestionInfo>)

    @Query("delete from QuestionInfo")
    suspend fun deleteAllQuestionInfo()

    @Query("select * from QuestionInfo")
    fun getAllQuestionInfoLiveData(): LiveData<List<QuestionInfo>>

    @Query("Select * from  QuestionInfo where quizId = :quizId")
    fun ifExistQuestionInfoInTable(quizId: Long): List<QuestionInfo>

    @Query("Update QuestionInfo set  noOfQuestions= :noOfQuestions, topic= :topic, uRL= :uRL, " +
            "comprehensionType= :comprehensionType, comprehensionFile= :comprehensionFile where quizId = :quizId")
    fun updateQuestionInfoByQuizId(noOfQuestions: Int?,topic: String?,uRL: String?,comprehensionType: String?,comprehensionFile: String?
                              ,quizId: Long): Int
    @Transaction
    suspend fun insertQuestionInfo(questionInfo:QuestionInfo)
    {
        if(ifExistQuestionInfoInTable(questionInfo.quizId).isNotEmpty())
        {
            updateQuestionInfoByQuizId(questionInfo.noOfQuestions,questionInfo.topic,questionInfo.uRL,questionInfo.comprehensionType,
                questionInfo.comprehensionFile,questionInfo.quizId)
        }
        else
            insert(questionInfo)
    }

    @Query("select * from QuestionInfo where quizId = :quizId")
    fun ifQuestionInfoExist(quizId: Long): List<QuestionInfo>
}

@Dao
interface QuestionsDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(questions: Questions)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(questions: List<Questions>)

    @Query("delete from Questions")
    suspend fun deleteAllQuestions()

    @Query("select * from Questions")
    fun getAllQuestionsLiveData(): LiveData<List<Questions>>

    @Query("Delete from Questions where quizId = :quizId")
    fun deleteQuestionsByQuizId(quizId: Long)

    @Query("Select * from  Questions where quizId = :quizId")
    fun ifExistQuestionsInTable(quizId: Long): List<Questions>

    @Query("delete from QuestionInfo where quizId= :quizId")
    suspend fun deleteAllQuestionInfo(quizId: Long)

    @Query("Select * from  Questions where quizId = :quizId and questionImageAsBlank=1")
    fun ifExistQuestionImageAsBlankTable(quizId: Long): List<Questions>

    @Transaction
    suspend fun insertAllQuestions(quizId: Long,questions: List<Questions>)
    {
        if(ifExistQuestionsInTable(quizId).isNotEmpty())
        {
            deleteQuestionsByQuizId(quizId)
            insertAll(questions)
        }
        else
            insertAll(questions)
    }
    @Transaction
    suspend fun deleteAllQuestionsExistQuestionImageAsBlank(quizId: Long)
    {
        if(ifExistQuestionImageAsBlankTable(quizId).isNotEmpty())
        {
            deleteQuestionsByQuizId(quizId)
            deleteAllQuestionInfo(quizId)
        }
    }

    @Transaction
    suspend fun deleteAllQuestions(quizId: Long,questions: List<Questions>)
    {
        if(ifExistQuestionsInTable(quizId).isNotEmpty())
        {
            deleteQuestionsByQuizId(quizId)
            insertAll(questions)
        }
        else
            insertAll(questions)
    }
    @Query("select * from Questions where quizId = :quizId order by questionSNO")
    fun startQuestionsPracticeSession(quizId: Long): List<Questions>
}

@Dao
interface AnswerInfoDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(answerInfo: AnswerInfo)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(answerInfo: List<AnswerInfo>)

    @Query("delete from AnswerInfo")
    suspend fun deleteAllAnswerInfo()

    @Query("select * from AnswerInfo")
    fun getAllAnswerInfoLiveData(): LiveData<List<AnswerInfo>>

    @Query("Select * from  AnswerInfo where quizId = :quizId")
    fun ifExistAnswerInfoInTable(quizId: Long): List<AnswerInfo>

    @Query("Select * from  AnswerInfo where quizId = :quizId")
    fun getAnswerInfoInTable(quizId: Long): AnswerInfo

    @Query("Update AnswerInfo set topic= :topic,videoOneSlideTime= :videoOneSlideTime, noOfQuestions= :noOfQuestions, uRL= :uRL " +
            " where quizId = :quizId")
    fun updateAnswerInfoByQuizId(topic: String,videoOneSlideTime:Int,noOfQuestions: Int?,uRL: String?,quizId: Long): Int

    @Transaction
    suspend fun insertAnswerInfo(answerInfo:AnswerInfo)
    {
        if(ifExistAnswerInfoInTable(answerInfo.quizId).isNotEmpty())
        {
            updateAnswerInfoByQuizId(answerInfo.topic,answerInfo.videoOneSlideTime,answerInfo.noOfQuestions,answerInfo.uRL,answerInfo.quizId)
        }
        else
            insert(answerInfo)
    }

    @Query("select * from AnswerInfo where quizId = :quizId")
    fun ifAnswerInfoExist(quizId: Long): List<AnswerInfo>
}


@Dao
interface AnswersDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(answers: Answers)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(answers: List<Answers>)

    @Query("delete from Answers")
    suspend fun deleteAllAnswers()

    @Query("select * from Answers")
    fun getAllAnswersLiveData(): LiveData<List<Answers>>

    @Query("Delete from Answers where quizId = :quizId")
    fun deleteAnswersByQuizId(quizId: Long)

    @Query("Select * from  Answers where quizId = :quizId")
    fun ifExistAnswersInTable(quizId: Long): List<Answers>


    @Query("delete from AnswerInfo where quizId= :quizId")
    suspend fun deleteAllAnswerInfo(quizId: Long)

    @Query("Select * from  Answers where quizId = :quizId and  answerImageAsBlank=1")
    fun ifExistAnswersImageAsBlankTable(quizId: Long): List<Answers>

    @Query("update Answers set answerImageBase64=:answerImageBase64 where answersId=:answersId and quizId = :quizId")
    suspend fun updateAnswerBase64(answerImageBase64:String?,answersId:Long,quizId: Long)

    @Transaction
    suspend fun insertAllAnswers(quizId: Long,answers: List<Answers>)
    {
        if(ifExistAnswersInTable(quizId).isNotEmpty())
        {
            deleteAnswersByQuizId(quizId)
            insertAll(answers)
        }
        else
            insertAll(answers)
    }
    @Transaction
    suspend fun deleteAllAnswersExistQuestionImageAsBlank(quizId: Long)
    {
        if(ifExistAnswersImageAsBlankTable(quizId).isNotEmpty())
        {
            deleteAnswersByQuizId(quizId)
            deleteAllAnswerInfo(quizId)
        }
    }

    @Query("select * from Answers where quizId = :quizId order by questionSNO")
    fun getAnswersPracticeSession(quizId: Long): List<Answers>
}

@Dao
interface AttemptPracticeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAttemptPractice(attemptPractice: AttemptPractice)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllAttemptPractice(attemptPractice: List<AttemptPractice>)

    @Query("select * from AttemptPractice order By practiceDate")
    fun getAttemptPractice():LiveData<List<AttemptPractice>>


    @Query("Delete FROM AttemptPractice")
    fun deleteAll(): Int

    @Transaction
    suspend fun deleteQuizIdAllTable(quizId: Long) {
        deleteAttemptPracticeById(quizId)
        deleteAnswersByQuizId(quizId)
        deleteAnswerInfoByQuizId(quizId)
        deleteQuestionsByQuizId(quizId)
        deleteQuestionInfoByQuizId(quizId)
    }
    @Query("Delete from AttemptPractice where quizId=:quizId")
    fun deleteAttemptPracticeById(quizId:Long)
    @Query("Delete from Answers where quizId = :quizId")
    fun deleteAnswersByQuizId(quizId: Long)
    @Query("Delete from AnswerInfo where quizId = :quizId")
    fun deleteAnswerInfoByQuizId(quizId: Long)
    @Query("Delete from Questions where quizId = :quizId")
    fun deleteQuestionsByQuizId(quizId: Long)
    @Query("Delete from QuestionInfo where quizId = :quizId")
    fun deleteQuestionInfoByQuizId(quizId: Long)

    @Query("select * from AttemptPractice  where uploadStatus='N' order by practiceDate desc")
    fun getAttemptPracticeNotUpload():List<AttemptPractice>

    @Query("select * from AttemptPractice where  quizId=:quizId")
    fun getAttemptPracticeNotUpload(quizId: Long):AttemptPractice

    @Query("Update AttemptPractice set uploadStatus='Y' where quizId=:quizId")
    fun updateAttemptPracticeUploadStatus(quizId:Long):Int
}

@Dao
interface AudioDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAudio(audio: Audio)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllAudios(audio: List<Audio>)

    @Query("select * from Audio where videoOneSlideTime=:videoOneSlideTime")
    fun getAudio(videoOneSlideTime:Int):Audio

    @Query("Delete FROM Audio")
    fun deleteAllAudio(): Int

    @Transaction
    suspend fun insertAllAudio(audio: List<Audio>)
    {
        deleteAllAudio()
        insertAllAudios(audio)
    }
}

@Dao
interface SingleTemplatesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSingleTemplates(templates: SingleTemplates)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllTemplatesLst(templates: List<SingleTemplates>)

    @Query("select * from SingleTemplates ORDER BY random() LIMIT 1")
    fun getSingleTemplates():SingleTemplates

    @Query("Delete FROM SingleTemplates")
    fun deleteAllSingleTemplates(): Int

    @Transaction
    suspend fun insertAllSingleTemplates(templates: List<SingleTemplates>)
    {
        deleteAllSingleTemplates()
        insertAllTemplatesLst(templates)
    }
}

@Dao
interface MultiTemplatesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMultiTemplates(templates: MultiTemplates)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllMultiTemplatesLst(templates: List<MultiTemplates>)

    @Query("select * from MultiTemplates ORDER BY random() LIMIT 1")
    fun getMultiTemplates():MultiTemplates

    @Query("Delete FROM MultiTemplates")
    fun deleteAllMultiTemplates(): Int

    @Transaction
    suspend fun insertAllMultiTemplates(templates: List<MultiTemplates>)
    {
        deleteAllMultiTemplates()
        insertAllMultiTemplatesLst(templates)
    }
}

@Dao
interface BlankTemplatesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertBlankTemplates(templates: BlankTemplates)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllBlankTemplatesLst(templates: List<BlankTemplates>)

    @Query("select * from BlankTemplates")
    fun getBlankTemplates():BlankTemplates

    @Query("Delete FROM BlankTemplates")
    fun deleteAllTemplates(): Int

    @Transaction
    suspend fun insertAllBlankTemplates(templates: List<BlankTemplates>)
    {
        deleteAllTemplates()
        insertAllBlankTemplatesLst(templates)
    }
}

@Dao
interface LastPageTemplatesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertLastPageTemplates(templates: LastPageTemplates)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllLastPageTemplatesLst(templates: List<LastPageTemplates>)

    @Query("select * from LastPageTemplates")
    fun getLastPageTemplates():LastPageTemplates

    @Query("Delete FROM LastPageTemplates")
    fun deleteAllTemplates(): Int

    @Transaction
    suspend fun insertAllLastPageTemplates(templates: List<LastPageTemplates>)
    {
        deleteAllTemplates()
        insertAllLastPageTemplatesLst(templates)
    }
}

@Dao
interface StudentProfileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertStudentProfile(studentProfile: StudentProfile)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllStudentProfile(studentProfile: List<StudentProfile>)

    @Query("select * from StudentProfile")
    fun getStudentProfile():StudentProfile

    @Query("select studentProfileId,userId,name,photoPath,photoData,photoLocalPath,IFNULL(emailId, '') as emailId from StudentProfile")
    fun getStudentProfileLiveData():LiveData<StudentProfile>

    @Query("Delete FROM StudentProfile")
    fun deleteAllStudentProfile(): Int

    @Query("update StudentProfile set name=:name,emailId=:emailId,photoLocalPath=:photoLocalPath where userId=:userId")
    suspend fun updateStudentProfile(name:String?, emailId:String?,photoLocalPath:String,userId: Long):Int

}

@Dao
interface MyVideosDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMyVideos(myVideos: MyVideos)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(myVideos: MyVideos)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(myVideos: List<MyVideos>)

    @Query("delete from MyVideos")
    suspend fun deleteAllMyVideos()

    @Query("select * from MyVideos")
    fun getAllMyVideosLiveData(): LiveData<List<MyVideos>>

    @Query("select * from MyVideos where practicesNo=:practicesNo")
    fun getMyVideos(practicesNo:Long): MyVideos

    @Query("select * from MyVideos")
    fun getMyVideos(): List<MyVideos>

    @Query("delete from MyVideos where practicesNo=:practicesNo")
    suspend fun deleteMyVideos(practicesNo:Long)

    @Query("delete from MyVideos where practicesNo is null and quizId=:quizId")
    suspend fun deleteMyVideosByQuizId(quizId:Long)

    @Query("delete from MyVideos where practicesNo=:practicesNo and quizId=:quizId")
    suspend fun deleteMyVideosByPracticesNo(practicesNo:Long,quizId:Long)

    @Query("update MyVideos set practiceLocalFilePath=:practiceLocalFilePath where practicesNo=:practicesNo")
    suspend fun updateMyVideos(practiceLocalFilePath:String?,practicesNo:Long)

    @Transaction
    suspend fun insertAllVideos(myVideos: List<MyVideos>)
    {
        for(myVideo in myVideos)
        {
            val myV=getMyVideos(myVideo.practicesNo!!)
            if(myV==null)
                insert(myVideo)
            else
            {
                //
            }
        }
    }
    @Query("Update MyVideos set uploadStatus='Y',practicesNo=:practicesNo,practiceOnlineFilePath=:practiceOnlineFilePath,practiceLocalFilePath=:practiceLocalFilePath where practicesNo is null and quizId=:quizId")
    suspend fun updateMyVideoUploadStatus(practicesNo:Long,practiceOnlineFilePath:String,practiceLocalFilePath:String,quizId:Long):Int

    @Query("Update MyVideos set practiceLocalFilePath=:practiceLocalFilePath where practicesNo=:practicesNo and quizId=:quizId")
    suspend fun updateMyVideoPracticeLocalFilePath(practiceLocalFilePath:String,practicesNo:Long,quizId:Long):Int

    @Query("select * from MyVideos where practicesNo=:practicesNo and quizId=:quizId ")
    fun getMyVideosByPracticeNoAndQuizID(practicesNo:Long,quizId:Long):MyVideos

    @Query("select * from MyVideos where  quizId=:quizId and uploadStatus='N'")
    fun getMyVideosNotUpload(quizId: Long):MyVideos
}