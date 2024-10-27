package com.example.pr32_24

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.example.pr32_24.databinding.FragmentActionBinding
import kotlinx.coroutines.launch
import java.util.*

class ActionFragment : Fragment() {

    private var _binding: FragmentActionBinding? = null
    private val binding get() = _binding!!
    private lateinit var taskDao: TaskDao
    private var selectedDate: String? = null // Хранит выбранную дату для задачи
    private var taskId: Int = -1 // ID задачи для обновления или удаления

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentActionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val db = Room.databaseBuilder(
            requireContext(),
            AppDatabase::class.java, "tasks_database"
        ).build()
        taskDao = db.taskDao()
        val taskInfoEditText: EditText = binding.editTextTaskInfo
        val selectDateButton: Button = binding.buttonSelectDate
        val updateTaskButton: Button = binding.buttonUpdateTask
        val deleteTaskButton: Button = binding.buttonDeleteTask


        // Получение переданных данных задачи (если передаются через Bundle)
        arguments?.let { bundle ->
            taskInfoEditText.setText(bundle.getString("taskInfo"))
            selectedDate = bundle.getString("taskDate")
            taskId = bundle.getInt("taskId", -1)
            Log.d("Loaded","Loaded id:$taskId,desc:${taskInfoEditText.text},date:$selectedDate ")
            // Установка выбранной даты на кнопку
            selectedDate?.let {
                selectDateButton.text = it
            }
        }

        // Обработка выбора даты
        selectDateButton.setOnClickListener {
            showDatePickerDialog()
        }

        // Обработка нажатия на кнопку "Обновить задачу"
        updateTaskButton.setOnClickListener {
            val taskInfo = taskInfoEditText.text.toString()
            if (taskInfo.isBlank() || selectedDate == null) {
                Toast.makeText(requireContext(), "Заполните все поля", Toast.LENGTH_SHORT).show()
            } else {
                if(isAdded)
                {
                    updateTask(taskId, taskInfo, selectedDate!!)
                }
                else
                    Log.d("DataBase","Не прикреплен")
            }
        }

        // Обработка нажатия на кнопку "Удалить задачу"
        deleteTaskButton.setOnClickListener {


            Log.d("DELETE","Selected id:$taskId")
            deleteTask(taskId,db)


        }
    }

    // Показ DatePicker для выбора даты
    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
            selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
            binding.buttonSelectDate.text = selectedDate // Устанавливаем дату на кнопке
        }, year, month, day)

        datePickerDialog.show()
    }

    private fun updateTask(id: Int, info: String, date: String) {
        Log.d("DataBase", "updateTask called with info: $info, date: $date")
        lifecycleScope.launch {
            try {
                // Загружаем текущую задачу перед обновлением
                val currentTask = taskDao.getAllTasks().find { it.id == id }

                if (currentTask != null) {
                    // Обновляем задачу
                    taskDao.update(id, info, date)
                    Log.d("DataBase", "Task updated successfully.")

                    // Проверяем обновленную задачу
                    val updatedTask = taskDao.getAllTasks().find { it.id == id }
                    Log.d("DataBase", "Updated Task: $updatedTask")

                    Toast.makeText(requireContext(), "Задача обновлена", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()
                } else {
                    Log.e("DataBase", "Task with ID $id not found.")
                    Toast.makeText(requireContext(), "Задача не найдена", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("DataBase", "UPDATE Failed: ${e.message}")
                Toast.makeText(requireContext(), "Ошибка при обновлении задачи: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }




    private fun deleteTask(id: Int,db: AppDatabase) {
        Log.d("DataBase", "Attempting to delete task with id: $id")
        lifecycleScope.launch {
            try {
                val allTasksBefore = getTasksFromDatabase(db = db) // Получаем список задач перед удалением
                Log.d("DataBase", "Tasks before deletion: $allTasksBefore") // Логируем их

                taskDao.deletebyId(id)
                Log.d("DataBase", "Delete operation called for ID: $id")

                // Перезагрузка задач после удаления
                val tasks = taskDao.getAllTasks()
                Log.d("DataBase", "Tasks after deletion: $tasks") // Логируем задачи после удаления

                // Проверяем, существует ли задача
                val taskAfterDeletion = tasks.find { it.id == id }
                if (taskAfterDeletion == null) {
                    Log.d("DataBase", "Task with ID $id deleted successfully")
                    Toast.makeText(requireContext(), "Задача удалена", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()
                } else {
                    Log.e("DataBase", "Task with ID $id still exists")
                    Toast.makeText(requireContext(), "Не удалось удалить задачу", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("DataBase", "Error deleting task: ${e.message}")
                Toast.makeText(requireContext(), "Ошибка при удалении задачи: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    suspend fun getTasksFromDatabase(db: AppDatabase): List<Task> {
        return db.taskDao().getAllTasks()


    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
