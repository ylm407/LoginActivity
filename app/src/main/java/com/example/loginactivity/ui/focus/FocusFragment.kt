package com.example.loginactivity.ui.focus

import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.view.WindowManager
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.loginactivity.R
import com.example.loginactivity.TimeUtils
import com.example.loginactivity.notify.NotificationHelper
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class FocusFragment : Fragment(R.layout.fragment_focus) {

    private lateinit var tilTarget: TextInputLayout
    private lateinit var etTarget: TextInputEditText
    private lateinit var tilMinutes: TextInputLayout
    private lateinit var etMinutes: TextInputEditText
    private lateinit var tvTimer: TextView
    private lateinit var progressTimer: ProgressBar
    private lateinit var btnStart: MaterialButton
    private lateinit var btnStop: MaterialButton

    private var countDownTimer: CountDownTimer? = null
    private var targetText: String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tilTarget = view.findViewById(R.id.tilFocusTarget)
        etTarget = view.findViewById(R.id.etFocusTarget)
        tilMinutes = view.findViewById(R.id.tilFocusMinutes)
        etMinutes = view.findViewById(R.id.etFocusMinutes)
        tvTimer = view.findViewById(R.id.tvTimer)
        progressTimer = view.findViewById(R.id.progressTimer)
        btnStart = view.findViewById(R.id.btnStartFocus)
        btnStop = view.findViewById(R.id.btnStopFocus)

        btnStart.setOnClickListener { startFocus() }
        btnStop.setOnClickListener { stopFocus(showToast = true) }
    }

    private fun startFocus() {
        if (countDownTimer != null) return

        val target = etTarget.text?.toString()?.trim().orEmpty()
        val minutes = etMinutes.text?.toString()?.toIntOrNull() ?: 0

        tilTarget.error = null
        tilMinutes.error = null

        if (target.isEmpty()) {
            tilTarget.error = getString(R.string.error_target_empty)
            return
        }
        if (minutes !in 1..180) {
            tilMinutes.error = getString(R.string.error_minutes_invalid)
            return
        }

        targetText = target
        val totalSeconds = minutes * 60

        progressTimer.max = totalSeconds
        progressTimer.progress = totalSeconds
        tvTimer.text = TimeUtils.formatClock(totalSeconds)
        btnStart.isEnabled = false
        btnStop.isEnabled = true

        // 计时期间别让屏幕熄灭
        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        Toast.makeText(requireContext(), R.string.toast_focus_started, Toast.LENGTH_SHORT).show()

        countDownTimer = object : CountDownTimer(totalSeconds * 1000L, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                val remainSeconds = (millisUntilFinished / 1000).toInt()
                tvTimer.text = TimeUtils.formatClock(remainSeconds)
                progressTimer.progress = remainSeconds
            }

            override fun onFinish() {
                countDownTimer = null
                resetUi()
                if (!isAdded) return
                NotificationHelper.showFocusDone(requireContext(), targetText)
                Toast.makeText(requireContext(), R.string.toast_focus_done, Toast.LENGTH_LONG).show()
            }
        }.start()
    }

    private fun stopFocus(showToast: Boolean) {
        val wasRunning = countDownTimer != null
        countDownTimer?.cancel()
        countDownTimer = null
        resetUi()
        if (showToast && wasRunning) {
            Toast.makeText(requireContext(), R.string.toast_focus_stopped, Toast.LENGTH_SHORT).show()
        }
    }

    private fun resetUi() {
        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        btnStart.isEnabled = true
        btnStop.isEnabled = false
        tvTimer.text = TimeUtils.formatClock(0)
        progressTimer.progress = 0
    }

    override fun onDestroyView() {
        countDownTimer?.cancel()
        countDownTimer = null
        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        super.onDestroyView()
    }
}
