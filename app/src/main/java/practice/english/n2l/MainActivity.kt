package practice.english.n2l

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import practice.english.n2l.api.RetrofitService
import practice.english.n2l.databinding.ActivityMainBinding
import practice.english.n2l.repository.UserAuthRepo
import practice.english.n2l.repository.UserAuthRepoImp
import practice.english.n2l.ui.BaseActivity
import practice.english.n2l.viewmodel.UserAuthViewModel

class MainActivity : BaseActivity() {

    private val repository          : UserAuthRepo by lazy { UserAuthRepoImp(RetrofitService.service,this) }
    private val viewModel           : UserAuthViewModel by lazy { ViewModelProvider(this, UserAuthViewModel.Factory(repository))[UserAuthViewModel::class.java] }
    private lateinit var binding    : ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        Handler(Looper.getMainLooper()).postDelayed({
            viewModel.getUserUserDetail.observe(this) {
                if(it!=null) {
                    startActivity( Intent().setClassName(getString(R.string.project_package_name), "practice.english.n2l.ui.LoginActivity").apply {
                        action = "Login"
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
                    finish()
                }
                else {
                    startActivity(Intent().setClassName(getString(R.string.project_package_name), "practice.english.n2l.ui.RegistrationActivity").apply {
                        action = "Registration"
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
                    finish()
                }
            }
        }, 3000)
    }
}