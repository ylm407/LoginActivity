package com.example.loginactivity

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class RegisterActivity : AppCompatActivity() {

    private lateinit var tilUsername: TextInputLayout
    private lateinit var tilPassword: TextInputLayout
    private lateinit var tilConfirm: TextInputLayout
    private lateinit var etUsername: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var etConfirm: TextInputEditText
    private lateinit var btnRegister: MaterialButton
    private lateinit var tvGoLogin: TextView

    private val session by lazy { SessionManager(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        bindViews()

        btnRegister.setOnClickListener { register() }
        tvGoLogin.setOnClickListener { finish() }
    }

    private fun bindViews() {
        tilUsername = findViewById(R.id.tilRegUsername)
        tilPassword = findViewById(R.id.tilRegPassword)
        tilConfirm = findViewById(R.id.tilRegConfirm)
        etUsername = findViewById(R.id.etRegUsername)
        etPassword = findViewById(R.id.etRegPassword)
        etConfirm = findViewById(R.id.etRegConfirm)
        btnRegister = findViewById(R.id.btnRegister)
        tvGoLogin = findViewById(R.id.tvGoLogin)
    }

    private fun register() {
        val username = etUsername.text?.toString()?.trim().orEmpty()
        val password = etPassword.text?.toString().orEmpty()
        val confirm = etConfirm.text?.toString().orEmpty()

        tilUsername.error = null
        tilPassword.error = null
        tilConfirm.error = null

        if (username.isEmpty()) {
            tilUsername.error = getString(R.string.error_username_empty)
            return
        }
        if (username.length < 3) {
            tilUsername.error = getString(R.string.error_username_short)
            return
        }
        if (password.isEmpty()) {
            tilPassword.error = getString(R.string.error_password_empty)
            return
        }
        if (password.length < 6) {
            tilPassword.error = getString(R.string.error_password_short)
            return
        }
        if (confirm.isEmpty()) {
            tilConfirm.error = getString(R.string.error_confirm_empty)
            return
        }
        if (password != confirm) {
            tilConfirm.error = getString(R.string.error_password_mismatch)
            return
        }

        session.saveRemembered(username, password)

        Toast.makeText(this, getString(R.string.toast_register_success), Toast.LENGTH_SHORT).show()
        setResult(RESULT_OK)
        finish()
    }
}
