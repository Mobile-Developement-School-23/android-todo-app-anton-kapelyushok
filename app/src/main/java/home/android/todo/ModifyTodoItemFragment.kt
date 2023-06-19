package home.android.todo

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.ToggleButton
import androidx.fragment.app.Fragment
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

class ModifyTodoItemFragment() : Fragment(R.layout.modify_todo_item) {

    val repo = TodoItemRepository

    lateinit var id: String
    lateinit var text: String
    lateinit var priority: TodoPriority
    var deadline: LocalDate? = null
    var done: Boolean = false
    var createdAt: Instant? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val idParam = arguments?.getString("todoItemId")
        val todoItem = idParam?.let { repo.get(it) }
        id = idParam ?: UUID.randomUUID().toString()
        text = todoItem?.text ?: ""
        priority = todoItem?.priority ?: TodoPriority.REGULAR
        deadline = todoItem?.deadline
        done = todoItem?.done ?: false
        createdAt = todoItem?.createdAt
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fragmentManager = requireActivity().supportFragmentManager
        val saveButton = view.findViewById<TextView>(R.id.save_button)
        saveButton.setOnClickListener {
            val now = Instant.now()
            if (text.trim().isEmpty()) return@setOnClickListener
            val item = TodoItem(
                id = id,
                text = text,
                priority = priority,
                deadline = deadline,
                done = done,
                createdAt = createdAt ?: now,
                modifiedAt = now,
            )
            repo.addOrUpdate(item)
            fragmentManager.popBackStack()
        }

        val removeButton = view.findViewById<View>(R.id.remove_button)
        removeButton.setOnClickListener {
            repo.remove(id)
            fragmentManager.popBackStack()
        }

        val closeButton = view.findViewById<View>(R.id.close_button)
        closeButton.setOnClickListener {
            fragmentManager.popBackStack()
        }

        val spinner: Spinner = view.findViewById(R.id.spinner)

        val options = optionsToLabel.keys.toTypedArray()

        val adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, options)
        spinner.adapter = adapter

        spinner.setSelection(options.map { optionsToLabel[it] }.indexOf(priority))

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                val selectedOption = options[position]
                priority = optionsToLabel[selectedOption]!!
                // Handle the selected option here
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                Log.d("tag", "what?")
            }
        }

        val editTextView = view.findViewById<EditText>(R.id.edit_text)
        editTextView.setText(text)
        editTextView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                text = s.toString() ?: ""
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

        val textView = view.findViewById<TextView>(R.id.date_picker_selected)

        val toggleButton = view.findViewById<ToggleButton>(R.id.toggle_button)

        fun updateDeadlineState() {
            if (deadline == null) {
                textView.visibility = INVISIBLE
            } else {
                textView.visibility = VISIBLE
                textView.text = deadline!!.toString()
            }


            toggleButton.isChecked = (deadline != null)
        }

        fun invokeDatePicker(date: LocalDate) {
            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    deadline = LocalDate.of(year, month + 1, dayOfMonth)
                    updateDeadlineState()
                    // Handle the selected date here
                },
                date.year,
                date.monthValue,
                date.dayOfMonth,
            )
            datePickerDialog.show()
        }

        toggleButton.setOnClickListener {
            if (deadline != null) {
                deadline = null
                updateDeadlineState()
            } else {
                val today = LocalDate.now()
                invokeDatePicker(today)
            }
        }


        textView.setOnClickListener {
            val date = deadline!!
            invokeDatePicker(date)
        }

        updateDeadlineState()
    }

}

private val optionsToLabel = mapOf(
    "Нет" to TodoPriority.REGULAR,
    "Низкий" to TodoPriority.LOW,
    "Высокий" to TodoPriority.URGENT,
)