package com.aditasha.myapplication.ui.register

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.aditasha.myapplication.R
import com.aditasha.myapplication.Result
import com.aditasha.myapplication.databinding.ActivityRegisterBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var registerViewModel: RegisterViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val view: RegisterViewModel by viewModels { RegisterViewModelFactory(this@RegisterActivity) }
        registerViewModel = view

        binding.apply {
            registerButton.setOnClickListener {
                val nameOk = check(name.nameMaterialEdit)
                val emailOk = check(email.emailMaterialEdit)
                val passwordOk = check(password.passwordMaterialEdit)

                if (nameOk && emailOk && passwordOk) {
                    val name = name.nameMaterialEdit.text.toString()
                    val email = email.emailMaterialEdit.text.toString()
                    val pass = password.passwordMaterialEdit.text.toString()
                    registerViewModel.register(name, email, pass)
                        .observe(this@RegisterActivity) { result ->
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

                                    Toast.makeText(
                                        this@RegisterActivity,
                                        R.string.register_success,
                                        Toast.LENGTH_SHORT
                                    )
                                        .show()
                                    intent = Intent()
                                    intent.putExtra(
                                        EMAIL,
                                        binding.email.emailMaterialEdit.text.toString()
                                    )
                                    setResult(RESULT, intent)
                                    finishAfterTransition()
                                }
                            }
                        }
                } else {
                    errorDialog(getString(R.string.register_wrong_format)).show()
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
        return MaterialAlertDialogBuilder(this@RegisterActivity)
            .setTitle("Error")
            .setMessage(e)
            .setPositiveButton(resources.getString(R.string.close_dialog)) { dialog, _ ->
                dialog.dismiss()
            }
    }

    companion object {
        const val RESULT = 1
        const val EMAIL = "email"
    }
}