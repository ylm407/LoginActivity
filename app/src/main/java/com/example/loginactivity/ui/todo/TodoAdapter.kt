package com.example.loginactivity.ui.todo

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.loginactivity.R
import com.example.loginactivity.TimeUtils
import com.example.loginactivity.data.Todo
import com.google.android.material.card.MaterialCardView
import com.google.android.material.checkbox.MaterialCheckBox

class TodoAdapter(
    private val onClick: (Todo) -> Unit,
    private val onLongClick: (Todo) -> Unit,
    private val onDoneChanged: (Todo, Boolean) -> Unit
) : RecyclerView.Adapter<TodoAdapter.TodoViewHolder>() {

    private val items = mutableListOf<Todo>()

    fun submitList(list: List<Todo>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_todo, parent, false)
        return TodoViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
        holder.bind(items[position])
    }

    inner class TodoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val card: MaterialCardView = itemView.findViewById(R.id.cardTodo)
        private val cbDone: MaterialCheckBox = itemView.findViewById(R.id.cbDone)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvDesc: TextView = itemView.findViewById(R.id.tvDesc)
        private val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        private val tvDeadline: TextView = itemView.findViewById(R.id.tvDeadline)

        fun bind(todo: Todo) {
            val context = itemView.context

            tvTitle.text = todo.title
            tvDesc.text = todo.description.ifBlank { context.getString(R.string.todo_no_desc) }
            tvCategory.text = todo.category

            val overdue = !todo.isDone && todo.deadline < System.currentTimeMillis()
            val timeText = TimeUtils.formatShort(todo.deadline)
            tvDeadline.text = if (overdue) {
                context.getString(R.string.todo_overdue, timeText)
            } else {
                context.getString(R.string.todo_deadline, timeText)
            }
            tvDeadline.setTextColor(
                ContextCompat.getColor(context, if (overdue) R.color.danger else R.color.text_secondary)
            )

            tvTitle.paintFlags = if (todo.isDone) {
                tvTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                tvTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }

            // 复用时先摘掉监听再改状态，避免触发上一次的 onDoneChanged
            cbDone.setOnCheckedChangeListener(null)
            cbDone.isChecked = todo.isDone
            cbDone.setOnCheckedChangeListener { _, checked -> onDoneChanged(todo, checked) }

            card.setOnClickListener { onClick(todo) }
            card.setOnLongClickListener {
                onLongClick(todo)
                true
            }
        }
    }
}
