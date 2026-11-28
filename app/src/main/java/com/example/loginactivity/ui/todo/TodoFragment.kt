package com.example.loginactivity.ui.todo

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.loginactivity.R
import com.example.loginactivity.SessionManager
import com.example.loginactivity.data.Todo
import com.example.loginactivity.data.TodoRepository
import com.example.loginactivity.notify.AlarmScheduler
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class TodoFragment : Fragment(R.layout.fragment_todo) {

    private lateinit var adapter: TodoAdapter
    private lateinit var repository: TodoRepository
    private lateinit var emptyView: TextView
    private var userId: Long = -1L

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repository = TodoRepository(requireContext())
        userId = SessionManager(requireContext()).currentUserId()
        emptyView = view.findViewById(R.id.tvEmpty)

        adapter = TodoAdapter(
            onClick = { todo -> openEditor(todo.id) },
            onLongClick = { todo -> confirmDelete(todo) },
            onDoneChanged = { todo, done -> updateDone(todo, done) }
        )

        val recycler = view.findViewById<RecyclerView>(R.id.recyclerTodo)
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adapter

        view.findViewById<FloatingActionButton>(R.id.fabAdd).setOnClickListener {
            openEditor(-1L)
        }
    }

    override fun onResume() {
        super.onResume()
        // 从新增/编辑页回来时会走到这里，重新查一遍数据库
        if (::adapter.isInitialized) loadTodos()
    }

    private fun loadTodos() {
        viewLifecycleOwner.lifecycleScope.launch {
            val todos = repository.listByUser(userId)
            adapter.submitList(todos)
            emptyView.visibility = if (todos.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun openEditor(todoId: Long) {
        val intent = Intent(requireContext(), AddEditTodoActivity::class.java)
        intent.putExtra(AddEditTodoActivity.EXTRA_TODO_ID, todoId)
        startActivity(intent)
    }

    private fun updateDone(todo: Todo, done: Boolean) {
        viewLifecycleOwner.lifecycleScope.launch {
            val updated = todo.copy(isDone = done)
            repository.update(updated)
            if (done) {
                AlarmScheduler.cancel(requireContext(), updated.id)
            } else {
                AlarmScheduler.schedule(requireContext(), updated)
            }
            loadTodos()
        }
    }

    private fun confirmDelete(todo: Todo) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.confirm_delete_title)
            .setMessage(getString(R.string.confirm_delete_message, todo.title))
            .setPositiveButton(R.string.action_delete) { _, _ ->
                viewLifecycleOwner.lifecycleScope.launch {
                    repository.delete(todo.id)
                    AlarmScheduler.cancel(requireContext(), todo.id)
                    loadTodos()
                }
            }
            .setNegativeButton(R.string.action_cancel, null)
            .show()
    }
}
