package practice.english.n2l.bridge

import practice.english.n2l.database.bao.PointData
import practice.english.n2l.database.bao.PointDetails

interface PointDetailsApiWorkerImp {
    fun onResultReceivedPointDetailsApiWorker(pointData: PointData,pointDetails: List<PointDetails>)
}