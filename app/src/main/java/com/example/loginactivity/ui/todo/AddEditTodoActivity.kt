package com.example.loginactivity.ui.todo

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import com.example.loginactivity.R
import com.example.loginactivity.SessionManager
import com.example.loginactivity.TimeUtils
import com.example.loginactivity.data.Todo
import com.example.loginactivity.data.TodoRepository
import com.example.loginactivity.notify.AlarmScheduler
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * 新增和编辑共用这一个页面：
 * todoId == -1 表示新增，否则是编辑。
 */
class AddEditTodoActivity : AppCompatActivity() {

    private lateinit var tilTitle: TextInputLayout
    private lateinit var etTitle: TextInputEditText
    private lateinit var etDesc: TextInputEditText
    private lateinit var spinnerCategory: Spinner
    private lateinit var spinnerRemind: Spinner
    private lateinit var btnPickDeadline: MaterialButton
    private lateinit var tvDeadline: TextView
    private lateinit var btnSave: MaterialButton

    private lateinit var categories: Array<String>

    private val repository by lazy { TodoRepository(this) }
    private val session by lazy { SessionManager(this) }

    // 与 R.array.remind_options 一一对应
    private val remindValues = intArrayOf(0, 5, 15, 30, 60)

    private var todoId: Long = -1L
    private var deadlineMillis: Long = System.currentTimeMillis() + 60 * 60 * 1000
    private var existing: Todo? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_todo)
        applyImeInsets()

        todoId = intent.getLongExtra(EXTRA_TODO_ID, -1L)
        if (session.currentUserId() == -1L) {
            Toast.makeText(this, R.string.error_session_expired, Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        bindViews()
        setupToolbar()
        setupSpinners()
        updateDeadlineText()

        btnPickDeadline.setOnClickListener { pickDeadline() }
        btnSave.setOnClickListener { save() }

        if (todoId != -1L) {
            loadExistingTodo()
        }
    }

    private fun bindViews() {
        tilTitle = findViewById(R.id.tilTitle)
        etTitle = findViewById(R.id.etTitle)
        etDesc = findViewById(R.id.etDesc)
        spinnerCategory = findViewById(R.id.spinnerCategory)
        spinnerRemind = findViewById(R.id.spinnerRemind)
        btnPickDeadline = findViewById(R.id.btnPickDeadline)
        tvDeadline = findViewById(R.id.tvDeadline)
        btnSave = findViewById(R.id.btnSave)
    }

    /**
     * 全屏边到边后 windowSoftInputMode=adjustResize 不再生效，
     * 需要自己把根布局的底部抬起一个键盘高度，否则保存按钮会被键盘盖住。
     */
    private fun applyImeInsets() {
        val root = findViewById<View>(R.id.root)
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            view.updatePadding(bottom = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom)
            insets
        }
    }

    private fun setupToolbar() {
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener { finish() }
        toolbar.setTitle(if (todoId == -1L) R.string.action_new_todo else R.string.action_edit_todo)
    }

    private fun setupSpinners() {
        categories = resources.getStringArray(R.array.categories)
        spinnerCategory.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            categories
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        val remindOptions = resources.getStringArray(R.array.remind_options)
        spinnerRemind.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            remindOptions
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
    }

    private fun loadExistingTodo() {
        lifecycleScope.launch {
            val todo = repository.getById(todoId) ?: return@launch
            existing = todo

            etTitle.setText(todo.title)
            etDesc.setText(todo.description)
            deadlineMillis = todo.deadline
            updateDeadlineText()

            val categoryIndex = categories.indexOf(todo.category)
            if (categoryIndex >= 0) spinnerCategory.setSelection(categoryIndex)

            val remindIndex = remindValues.indexOf(todo.remindBeforeMinutes)
            if (remindIndex >= 0) spinnerRemind.setSelection(remindIndex)
        }
    }

    private fun updateDeadlineText() {
        tvDeadline.text = TimeUtils.formatDateTime(deadlineMillis)
    }

    private fun pickDeadline() {
        val calendar = Calendar.getInstance().apply { timeInMillis = deadlineMillis }

        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                TimePickerDialog(
                    this,
                    { _, hourOfDay, minute ->
                        calendar.set(year, month, dayOfMonth, hourOfDay, minute, 0)
                        calendar.set(Calendar.MILLISECOND, 0)
                        deadlineMillis = calendar.timeInMillis
                        updateDeadlineText()
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
                ).show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun save() {
        val title = etTitle.text?.toString()?.trim().orEmpty()
        tilTitle.error = null
        if (title.isEmpty()) {
            tilTitle.error = getString(R.string.error_title_empty)
            return
        }

        val todo = Todo(
            id = todoId,
            userId = session.currentUserId(),
            title = title,
            description = etDesc.text?.toString()?.trim().orEmpty(),
            category = spinnerCategory.selectedItem?.toString().orEmpty(),
            deadline = deadlineMillis,
            remindBeforeMinutes = remindValues[spinnerRemind.selectedItemPosition],
            isDone = existing?.isDone ?: false
        )

        btnSave.isEnabled = false
        lifecycleScope.launch {
            if (todoId == -1L) {
                val newId = repository.insert(todo)
                if (newId > 0) {
                    AlarmScheduler.schedule(this@AddEditTodoActivity, todo.copy(id = newId))
                }
            } else {
                repository.update(todo)
                AlarmScheduler.cancel(this@AddEditTodoActivity, todo.id)
                AlarmScheduler.schedule(this@AddEditTodoActivity, todo)
            }
            Toast.makeText(this@AddEditTodoActivity, R.string.toast_saved, Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    companion object {
        const val EXTRA_TODO_ID = "extra_todo_id"
    }
}
