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
import practice.english.n2l.databinding.DialogProgressBinding

class CustomProgressDialog(val mActivity: Activity)
{
    private lateinit var customDialog: AlertDialog
    private  var message:String="Loading ...."

    fun setMessage(message:String) { this.message=message }

    fun startLoading(){
        val dialogBinding: DialogProgressBinding =
            DataBindingUtil.inflate(
                LayoutInflater.from(mActivity),
                R.layout.dialog_progress,
                null,
                false
            )

        dialogBinding.cpTitle.text=message
        dialogBinding.cpCardview.setCardBackgroundColor(Color.parseColor("#70000000"))
        setColorFilter(dialogBinding.cpPbar.indeterminateDrawable, ResourcesCompat.getColor(mActivity.resources, R.color.colorPrimary, null))
        dialogBinding.cpTitle.setTextColor(Color.WHITE)


        /**set Dialog*/
        customDialog = AlertDialog.Builder(mActivity, 0).create()
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
}