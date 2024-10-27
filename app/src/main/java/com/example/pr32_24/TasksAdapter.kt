package com.example.pr32_24

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView



class TasksAdapter(private var tasks: List<Task>, private val onTaskClick: (Task) -> Unit) :
    RecyclerView.Adapter<TasksAdapter.TaskViewHolder>() {

    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val description: TextView = itemView.findViewById(R.id.taskDescriptionTextView)
        val dueDate: TextView = itemView.findViewById(R.id.taskDueDateTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        holder.description.text = task.description
        holder.dueDate.text = task.dueDate

        holder.itemView.setOnClickListener {
            onTaskClick(task)
        }
    }
    override fun getItemCount(): Int = tasks.size
}
