package com.aditasha.myapplication.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.getSystemService
import androidx.core.util.Pair
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.aditasha.myapplication.R
import com.aditasha.myapplication.Result
import com.aditasha.myapplication.databinding.ActivityLoginBinding
import com.aditasha.myapplication.preferences.UserPreference
import com.aditasha.myapplication.ui.home.HomeActivity
import com.aditasha.myapplication.ui.register.RegisterActivity
import com.aditasha.myapplication.worker.CredWorker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import java.util.concurrent.TimeUnit

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var loginViewModel: LoginViewModel
    private lateinit var preference: UserPreference
    private lateinit var workManager: WorkManager

    private val resultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.data != null) {
            if (result.resultCode == RegisterActivity.RESULT) {
                val emailText = result.data?.getStringExtra(RegisterActivity.EMAIL).toString()
                binding.email.emailMaterialEdit.setText(emailText)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val view: LoginViewModel by viewModels { LoginViewModelFactory(this@LoginActivity) }
        loginViewModel = view

        workManager = WorkManager.getInstance(this)
        workManager.cancelAllWork()

        loginViewModel.isLoading.observe(this@LoginActivity, ::showLoading)

        loginViewModel.isFailed.observe(this@LoginActivity) {
            if (it) {
                showFailed(it, loginViewModel.errorText.value.toString())
                showLoading(!it)
            }
        }

        binding.apply {

            loginButton.setOnClickListener {
                val emailOk = check(email.emailMaterialEdit)
                val passwordOk = check(password.passwordMaterialEdit)

                if (emailOk && passwordOk) {
                    val imm = getSystemService<InputMethodManager>()
                    imm?.hideSoftInputFromWindow(loginButton.windowToken, 0)
                    val email = email.emailMaterialEdit.text.toString()
                    val pass = password.passwordMaterialEdit.text.toString()
                    execute(email, pass)
                    startWorker()
                } else {
                    errorDialog(getString(R.string.login_wrong_format)).show()
                }
            }

            registerPortal.setOnClickListener {
                val optionsCompat: ActivityOptionsCompat =
                    ActivityOptionsCompat.makeSceneTransitionAnimation(
                        this@LoginActivity,
                        Pair(imageView, "dicoding"),
                        Pair(email.emailTextLayout, "email"),
                        Pair(password.passwordTextLayout, "password"),
                        Pair(loginButton, "button"),
                    )
                val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
                resultLauncher.launch(intent, optionsCompat)
            }
        }
    }

    private fun execute(emal: String, pass: String) {
        loginViewModel.login(emal, pass).observe(this@LoginActivity) { result ->
            when (result) {
                is Result.Loading -> {
                    showLoading(true)
                    showFailed(false, "")
                }
                is Result.Error -> {
                    showLoading(false)
                    showFailed(true, result.error)
                }
                is Result.Success -> {
                    showLoading(false)
                    showFailed(false, "")

                    if (result.data != null) {
                        preference = UserPreference(this@LoginActivity)
                        preference.setCred(result.data)
                        intent = Intent(this@LoginActivity, HomeActivity::class.java)
                        Toast.makeText(this@LoginActivity, R.string.login_success, Toast.LENGTH_LONG)
                            .show()
                        startActivity(intent)
                    }
                }
            }
        }
    }

    private fun check(edit: TextInputEditText): Boolean {
        val value = edit.text
        if (value != null && value.isNotEmpty()) {
            if (edit.error == null) {
                return true
            }
        }
        return false
    }

    private fun startWorker() {
        val periodic = PeriodicWorkRequest.Builder(CredWorker::class.java, 60, TimeUnit.MINUTES)
            .build()
        workManager.enqueue(periodic)
//        workManager.getWorkInfoByIdLiveData(periodic.id)
//            .observe(this@LoginActivity) { workInfo ->
//                workInfo.state.name
//            }
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.loading.visibility = View.VISIBLE
            binding.group.visibility = View.INVISIBLE
        } else {
            binding.loading.visibility = View.INVISIBLE
            binding.group.visibility = View.VISIBLE
        }
    }

    private fun showFailed(isFailed: Boolean, e: String) {
        if (isFailed) {
            val text = getString(R.string.error, e)
            errorDialog(text).show()
        }
    }

    private fun errorDialog(e: String): MaterialAlertDialogBuilder {
        return MaterialAlertDialogBuilder(this@LoginActivity)
            .setMessage(e)
            .setPositiveButton(resources.getString(R.string.close_dialog)) { dialog, _ ->
                dialog.dismiss()
            }
    }
}