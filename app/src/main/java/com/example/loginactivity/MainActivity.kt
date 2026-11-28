package com.example.loginactivity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import com.example.loginactivity.ui.focus.FocusFragment
import com.example.loginactivity.ui.profile.ProfileFragment
import com.example.loginactivity.ui.todo.TodoFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var todoFragment: TodoFragment
    private lateinit var focusFragment: FocusFragment
    private lateinit var profileFragment: ProfileFragment
    private var activeFragment: Fragment? = null

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (!granted) {
                Toast.makeText(this, R.string.notify_permission_denied, Toast.LENGTH_LONG).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 没登录就别进来，直接打回登录页
        if (!SessionManager(this).isLoggedIn()) {
            Toast.makeText(this, R.string.error_session_expired, Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }
        // 底部导航自己顶上系统导航栏，否则会贴在屏幕最底下被手势条压住
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.bottomNav)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(bottom = systemBars.bottom)
            insets
        }

        requestNotificationPermission()

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)

        if (savedInstanceState == null) {
            todoFragment = TodoFragment()
            focusFragment = FocusFragment()
            profileFragment = ProfileFragment()

            // 三个 Fragment 只加一次，之后用 show/hide 切换，专注计时不会因为切页被清掉
            supportFragmentManager.beginTransaction()
                .add(R.id.fragmentContainer, profileFragment, TAG_PROFILE).hide(profileFragment)
                .add(R.id.fragmentContainer, focusFragment, TAG_FOCUS).hide(focusFragment)
                .add(R.id.fragmentContainer, todoFragment, TAG_TODO)
                .commit()

            activeFragment = todoFragment
        } else {
            // 屏幕旋转 / 进程被回收后重建，把原来的实例找回来
            todoFragment = supportFragmentManager.findFragmentByTag(TAG_TODO) as TodoFragment
            focusFragment = supportFragmentManager.findFragmentByTag(TAG_FOCUS) as FocusFragment
            profileFragment = supportFragmentManager.findFragmentByTag(TAG_PROFILE) as ProfileFragment
            activeFragment = todoFragment

            supportFragmentManager.beginTransaction()
                .hide(focusFragment)
                .hide(profileFragment)
                .show(todoFragment)
                .commit()
            bottomNav.selectedItemId = R.id.nav_todo
        }

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_todo -> showFragment(todoFragment)
                R.id.nav_focus -> showFragment(focusFragment)
                R.id.nav_profile -> showFragment(profileFragment)
            }
            true
        }
    }

    private fun showFragment(target: Fragment) {
        val current = activeFragment ?: return
        if (current === target) return
        supportFragmentManager.beginTransaction()
            .hide(current)
            .show(target)
            .commit()
        activeFragment = target
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    companion object {
        private const val TAG_TODO = "tag_todo"
        private const val TAG_FOCUS = "tag_focus"
        private const val TAG_PROFILE = "tag_profile"
    }
}
