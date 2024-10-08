package practice.english.n2l.ui.profile

import android.app.Activity
import android.app.Dialog
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
import androidx.lifecycle.ViewModelProvider
import practice.english.n2l.R
import practice.english.n2l.api.RetrofitService
import practice.english.n2l.bridge.ConfirmationImp
import practice.english.n2l.bridge.SuccessImp
import practice.english.n2l.databinding.DialogPinChangeBinding
import practice.english.n2l.dialog.ConfirmationDialog
import practice.english.n2l.dialog.SuccessDialog
import practice.english.n2l.dialog.WarningDialog
import practice.english.n2l.repository.UserAuthRepo
import practice.english.n2l.repository.UserAuthRepoImp
import practice.english.n2l.util.Constants
import practice.english.n2l.viewmodel.UserAuthViewModel

class PinChangeDialog(private val context: Activity): DialogFragment() {
    private val repository          : UserAuthRepo by lazy { UserAuthRepoImp(RetrofitService.service,requireActivity()) }
    private val viewModel           : UserAuthViewModel by lazy { ViewModelProvider(this, UserAuthViewModel.Factory(repository))[UserAuthViewModel::class.java] }
    private lateinit var  binding: DialogPinChangeBinding
    companion object { const val TAG = "PinChangeDialog" }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding= DialogPinChangeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            BtnVerify.setOnClickListener {
                if(binding.EtNewPin.text.toString().isEmpty())
                    showValidationMessage("New MPin Not Empty")
                else if(binding.EtNewConfirmPin.text.toString().isEmpty())
                    showValidationMessage("Confirm MPin Not Empty")
                else if (binding.EtNewPin.text.toString().length!=4 || binding.EtNewConfirmPin.text.toString().length!=4)
                    showValidationMessage("Pin length must be 4 digit")
                else if(binding.EtNewConfirmPin.text.toString()!=(binding.EtNewPin.text.toString()))
                    showValidationMessage("Confirm MPin Not Match New MPin")
                else {
                    ConfirmationDialog(requireActivity(), getString(R.string.heading_information), getString(R.string.msg_pin_change), object : ConfirmationImp {
                        override fun onClickYes(context: Activity, dialog: Dialog?) {
                            viewModel.updatePin(binding.EtNewPin.text.toString().trim())
                            dialog!!.dismiss()
                            SuccessDialog(requireActivity(), Constants.Information, getString(R.string.msg_success_pin_change), object : SuccessImp {
                                override fun onYes(context: Activity, dialog: Dialog?) {
                                    dialog!!.dismiss()
                                    this@PinChangeDialog.dismiss()
                                }
                            }).show(requireActivity().supportFragmentManager, SuccessDialog.TAG)
                        }
                        override fun onClickNo(context: Activity, dialog: Dialog?) {
                            dialog!!.dismiss()
                        }
                    }).show(requireActivity().supportFragmentManager, ConfirmationDialog.TAG)
                }
            }
            BtnClose.setOnClickListener {
                dismiss()
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
        dialog?.setCancelable(false);
        dialog?.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog?.window?.attributes = lp
    }
    private fun showValidationMessage(msg:String) {
        WarningDialog(requireActivity(), Constants.Information,msg).show(requireActivity().supportFragmentManager,WarningDialog.TAG)
    }
}