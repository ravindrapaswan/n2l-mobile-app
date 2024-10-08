package practice.english.n2l.bridge

import android.app.Activity
import android.app.Dialog

interface ShowInstructionConfirmationImp {
    fun onClickYes(context: Activity, dialog: Dialog?)
    fun onClickShowInstruction(context: Activity, dialog: Dialog?)
}