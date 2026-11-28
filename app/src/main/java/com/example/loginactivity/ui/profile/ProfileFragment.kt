package com.example.loginactivity.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.loginactivity.LoginActivity
import com.example.loginactivity.R
import com.example.loginactivity.SessionManager
import com.example.loginactivity.data.UserRepository
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private lateinit var tvUsername: TextView
    private lateinit var etNickname: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var repository: UserRepository
    private lateinit var session: SessionManager
    private var userId: Long = -1L

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repository = UserRepository(requireContext())
        session = SessionManager(requireContext())
        userId = session.currentUserId()

        tvUsername = view.findViewById(R.id.tvUsername)
        etNickname = view.findViewById(R.id.etNickname)
        etEmail = view.findViewById(R.id.etEmail)

        view.findViewById<MaterialButton>(R.id.btnSaveProfile).setOnClickListener { saveProfile() }
        view.findViewById<MaterialButton>(R.id.btnLogout).setOnClickListener { confirmLogout() }
    }

    override fun onResume() {
        super.onResume()
        if (::repository.isInitialized) loadUser()
    }

    private fun loadUser() {
        viewLifecycleOwner.lifecycleScope.launch {
            val user = repository.findById(userId) ?: return@launch
            tvUsername.text = getString(R.string.profile_account, user.username)
            etNickname.setText(user.nickname)
            etEmail.setText(user.email)
        }
    }

    private fun saveProfile() {
        val nickname = etNickname.text?.toString()?.trim().orEmpty()
        val email = etEmail.text?.toString()?.trim().orEmpty()
        viewLifecycleOwner.lifecycleScope.launch {
            repository.updateProfile(userId, nickname, email)
            Toast.makeText(requireContext(), R.string.toast_profile_saved, Toast.LENGTH_SHORT).show()
        }
    }

    private fun confirmLogout() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.confirm_logout_title)
            .setMessage(R.string.confirm_logout_message)
            .setPositiveButton(R.string.action_logout) { _, _ -> logout() }
            .setNegativeButton(R.string.action_cancel, null)
            .show()
    }

    private fun logout() {
        session.logout()
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}
