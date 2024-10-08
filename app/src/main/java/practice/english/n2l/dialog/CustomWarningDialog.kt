package practice.english.n2l.dialog

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Display
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.hardware.display.DisplayManagerCompat
import androidx.fragment.app.DialogFragment
import practice.english.n2l.bridge.CustomWarningImp
import practice.english.n2l.databinding.DialogWarningBinding

class CustomWarningDialog(private val context: Activity, private val title:String, private val subTitle:String,private val customWarningImp: CustomWarningImp) : DialogFragment() {
    private lateinit var binding: DialogWarningBinding
    companion object { const val TAG = "CustomWarningDialog" }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogWarningBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            Title.text = title
            SubTitle.text = subTitle
            btnConfirm.setOnClickListener {
                customWarningImp.onClickNo(context,dialog)
            }
        }
    }
    override fun onStart() {
        super.onStart()
        val width:Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val defaultDisplay = DisplayManagerCompat.getInstance(context).getDisplay(Display.DEFAULT_DISPLAY)
            val displayContext = context.createDisplayContext(defaultDisplay!!)
            displayContext.resources.displayMetrics.widthPixels
        } else {

            val displayMetrics = DisplayMetrics()
            @Suppress("DEPRECATION")
            context.windowManager.defaultDisplay.getMetrics(displayMetrics)
            displayMetrics.widthPixels
        }
        val lp = WindowManager.LayoutParams()
        lp.copyFrom(dialog!!.window!!.attributes)
        lp.width = (width * 0.98f).toInt()
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        lp.gravity = Gravity.CENTER
        lp.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        dialog?.setCancelable(false)
        dialog?.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog?.window?.attributes = lp
    }
}