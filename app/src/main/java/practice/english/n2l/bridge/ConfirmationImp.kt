package practice.english.n2l.bridge

import android.app.Activity
import android.app.Dialog

interface ConfirmationImp {
    fun onClickYes(context: Activity, dialog: Dialog?)
    fun onClickNo(context: Activity, dialog: Dialog?)
}