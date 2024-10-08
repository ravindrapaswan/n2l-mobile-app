package practice.english.n2l.bridge

import practice.english.n2l.database.bao.PointDetails

interface PracticePointDetailsImp {
    fun onPlay(pointDetails:PointDetails, position:Int)
}