package practice.english.n2l.bridge

import practice.english.n2l.database.bao.AttemptPractice

interface PracticeDownloadUploadStatusImp {
    fun onPlay(attemptPractice:AttemptPractice)
    fun onDelete(attemptPractice:AttemptPractice)
    fun onUpload(attemptPractice:AttemptPractice)
}