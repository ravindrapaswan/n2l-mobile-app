package practice.english.n2l.bridge

import practice.english.n2l.database.bao.MyVideos

interface MyVideosImp {
    fun onPlay(myVideos: MyVideos)
    fun onDelete(myVideos:MyVideos)
    fun onUpload(myVideos:MyVideos)
    fun onDownload(myVideos:MyVideos)
    fun onShare(myVideos:MyVideos)
}