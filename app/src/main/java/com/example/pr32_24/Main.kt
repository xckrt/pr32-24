package com.example.pr32_24

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.navigation.fragment.findNavController
import androidx.room.Room
import com.example.pr32_24.databinding.FragmentMainBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class Main : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var selectPeriodButton: Button
    private lateinit var addTaskButton: Button
    private lateinit var taskAdapter: TasksAdapter
    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!
    private var tasks: List<Task> = listOf()
    private lateinit var db: AppDatabase

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        db = Room.databaseBuilder(
            requireContext(),
            AppDatabase::class.java, "tasks_database"
        ).build()

        selectPeriodButton = binding.selectPeriodButton
        addTaskButton = binding.newTask

        selectPeriodButton.setOnClickListener {
            showDatePickerDialog()
        }
        binding.update.setOnClickListener {
            loadTasks()
        }
        addTaskButton.setOnClickListener {
            addTask()
        }

        loadTasks()
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        // Диалог выбора начальной даты
        val startDatePickerDialog = DatePickerDialog(requireContext(), { _, startYear, startMonth, startDay ->
            val startDate = Calendar.getInstance().apply {
                set(startYear, startMonth, startDay)
            }

            // Диалог выбора конечной даты
            val endDatePickerDialog = DatePickerDialog(requireContext(), { _, endYear, endMonth, endDay ->
                val endDate = Calendar.getInstance().apply {
                    set(endYear, endMonth, endDay)
                }

                if (endDate.timeInMillis >= startDate.timeInMillis) {
                    val selectedPeriod = "С $startDay/${startMonth + 1}/$startYear по $endDay/${endMonth + 1}/$endYear"
                    Toast.makeText(requireContext(), selectedPeriod, Toast.LENGTH_SHORT).show()

                    // Загрузка задач по выбранному периоду
                    loadTasks(startDate.timeInMillis, endDate.timeInMillis)
                } else {
                    Toast.makeText(requireContext(), "Конечная дата не может быть раньше начальной", Toast.LENGTH_SHORT).show()
                }
            }, year, month, day)

            endDatePickerDialog.datePicker.minDate = startDate.timeInMillis
            endDatePickerDialog.show()
        }, year, month, day)

        startDatePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
        startDatePickerDialog.show()
    }
    private fun loadTasks(startDate: Long? = null, endDate: Long? = null) {
        lifecycleScope.launch {
            try {
                val allTasks = getTasksFromDatabase() // Получаем все задачи из базы
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

                // Фильтруем задачи по диапазону дат
                tasks = if (startDate != null && endDate != null) {
                    allTasks.filter { task ->
                        val taskDateInMillis = dateFormat.parse(task.dueDate)?.time ?: 0L
                        taskDateInMillis in startDate..endDate
                    }
                } else {
                    allTasks // Если даты не заданы, показываем все задачи
                }

                taskAdapter = TasksAdapter(tasks) { task ->
                    openTaskInActionFragment(task)
                    Toast.makeText(requireContext(), "Clicked: ${task.description}", Toast.LENGTH_SHORT).show()
                }
                recyclerView.adapter = taskAdapter
            } catch (e: Exception) {
                Log.e("Database", "Error loading tasks: ${e.message}")
                Toast.makeText(requireContext(), "Ошибка при загрузке задач", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private suspend fun getTasksFromDatabase(): List<Task> {
        val tasks = db.taskDao().getAllTasks()
        Log.d("DataBase", "Tasks from database: $tasks") // Логируем задачи, загруженные из базы данных
        return tasks
    }

    private fun openTaskInActionFragment(task: Task) {
        val bundle = Bundle().apply {
            putInt("taskId", task.id)
            putString("taskInfo", task.description)
            putString("taskDate", task.dueDate)
        }
        findNavController().navigate(R.id.action_MainFragment_to_ActionFragment, bundle)
    }

    private fun addTask() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_income, null)
        val dateButton = dialogView.findViewById<Button>(R.id.dateButton)
        val descriptionInput = dialogView.findViewById<EditText>(R.id.editTextTask)

        var selectedDate = ""

        // Установка обработчика для выбора даты
        dateButton.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
                selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                dateButton.text = selectedDate
            }, year, month, day)

            datePickerDialog.show()
        }

        // Создание диалога
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Добавить задачу")
        builder.setView(dialogView)
        builder.setPositiveButton("Добавить") { _, _ ->
            val description = descriptionInput.text.toString()

            if (selectedDate.isNotEmpty() && description.isNotEmpty()) {
                // Создаём задачу
                val task = Task(description = description, dueDate = selectedDate)

                // Добавляем задачу в базу данных асинхронно
                lifecycleScope.launch {

                        val generatedId = db.taskDao().insert(task)  // Сохраняем сгенерированный ID
                        Log.d("Task", "Добавлено id:$generatedId, desc:${task.description}, date:${task.dueDate}")
                        Toast.makeText(requireContext(), "Задача добавлена", Toast.LENGTH_SHORT).show()

                        // Обновляем список задач




                    // Обновляем список задач
                    loadTasks()
                }
            } else {
                Toast.makeText(requireContext(), "Введите описание задачи и выберите дату", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Отмена", null)
        builder.show()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
