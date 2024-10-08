package practice.english.n2l.dialog

import android.app.Activity
import android.app.AlertDialog
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.LayoutInflater
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import practice.english.n2l.R
import practice.english.n2l.databinding.DialogCustomPercentageProgressbarBinding


class CustomPercentageProgressbarDialog ( val context: Activity) {
    private var currentProgress=0
    private lateinit var customDialog:AlertDialog
    private lateinit var dialogBinding:DialogCustomPercentageProgressbarBinding
    companion object { const val TAG = "CustomPercentageProgressbarDialog" }

    private  var message:String=context.getString(R.string.video_status_msg,"","")

    fun setMessage(message:String) { this.message=message }

    fun startLoading(){
         dialogBinding  =
            DataBindingUtil.inflate(
                LayoutInflater.from(context),
                R.layout.dialog_custom_percentage_progressbar,
                null,
                false
            )

        dialogBinding.cpTitle.text=message
        dialogBinding.cpCardview.setCardBackgroundColor(Color.parseColor("#70000000"))
        setColorFilter(dialogBinding.cpPbar.indeterminateDrawable, ResourcesCompat.getColor(context.resources, R.color.colorPrimary, null))
        dialogBinding.cpTitle.setTextColor(Color.WHITE)

        /**set Dialog*/
        customDialog = AlertDialog.Builder(context, 0).create()
        customDialog.apply {
            setView(dialogBinding.root)
            setCancelable(false)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }.show()
    }
    private fun setColorFilter(drawable: Drawable, color: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            drawable.colorFilter = BlendModeColorFilter(color, BlendMode.SRC_ATOP)
        } else {
            @Suppress("DEPRECATION")
            drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
        }
    }
    fun dismissLoading(){
        if(this::customDialog.isInitialized) customDialog.dismiss()
    }
    fun setProgress(progress: Int) {
        currentProgress += progress
        dialogBinding.cpPbar.progress = currentProgress
        dialogBinding.cpTitle.text=context.getString(R.string.video_status_msg,currentProgress.toString(),"%")
    }
}