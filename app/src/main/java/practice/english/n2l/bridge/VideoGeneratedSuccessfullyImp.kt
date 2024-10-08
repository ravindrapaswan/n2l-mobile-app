package practice.english.n2l.bridge

import android.app.Activity
import android.app.Dialog

interface VideoGeneratedSuccessfullyImp {
    fun onViewVideo(context: Activity, dialog: Dialog?)
    fun onShareVideo(context: Activity, dialog: Dialog?)
    fun onBack(context: Activity, dialog: Dialog?)
}