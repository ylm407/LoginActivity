package com.example.loginactivity
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.loginactivity.data.UserRepository
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var tilUsername: TextInputLayout
    private lateinit var tilPassword: TextInputLayout
    private lateinit var etUsername: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var cbRemember: MaterialCheckBox
    private lateinit var btnLogin: MaterialButton
    private lateinit var tvGoRegister: TextView

    private val session by lazy { SessionManager(this) }
    private val repo by lazy { UserRepository(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 已经登录过：跳过登录页，直接进主页
        if (session.isLoggedIn()) {
            openMain()
            return
        }

        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // 键盘弹起时把内容顶上去，否则输入框会被键盘盖住
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, maxOf(systemBars.bottom, ime.bottom))
            insets
        }

        bindViews()
        fillRememberedAccount()

        btnLogin.setOnClickListener { login() }
        tvGoRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    // 从注册页返回时，把刚注册的账号自动填进输入框
    override fun onResume() {
        super.onResume()
        fillRememberedAccount()
    }

    private fun bindViews() {
        tilUsername = findViewById(R.id.tilUsername)
        tilPassword = findViewById(R.id.tilPassword)
        etUsername = findViewById(R.id.etUsername)
        etPassword = findViewById(R.id.etPassword)
        cbRemember = findViewById(R.id.cbRemember)
        btnLogin = findViewById(R.id.btnLogin)
        tvGoRegister = findViewById(R.id.tvGoRegister)
    }

    private fun fillRememberedAccount() {
        if (!session.isRemembered()) return
        etUsername.setText(session.rememberedUsername())
        etPassword.setText(session.rememberedPassword())
        cbRemember.isChecked = true
    }

    private fun login() {
        val username = etUsername.text?.toString()?.trim().orEmpty()
        val password = etPassword.text?.toString().orEmpty()

        tilUsername.error = null
        tilPassword.error = null

        if (username.isEmpty()) {
            tilUsername.error = getString(R.string.error_username_empty)
            return
        }
        if (password.isEmpty()) {
            tilPassword.error = getString(R.string.error_password_empty)
            return
        }

        btnLogin.isEnabled = false
        lifecycleScope.launch {

            // 真正查库：账号密码同时匹配才拿到 User，否则 null
            val user = repo.login(username, password)
            btnLogin.isEnabled = true

            if (user == null) {
                tilPassword.error = getString(R.string.error_login_failed)
                return@launch
            }

            session.saveLogin(user.id)
            if (cbRemember.isChecked) {
                session.saveRemembered(username, password)
            } else {
                session.clearRemembered()
            }

            Toast.makeText(
                this@LoginActivity,
                getString(R.string.toast_welcome, user.username),
                Toast.LENGTH_SHORT
            ).show()
            openMain()
        }
    }

    private fun openMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
