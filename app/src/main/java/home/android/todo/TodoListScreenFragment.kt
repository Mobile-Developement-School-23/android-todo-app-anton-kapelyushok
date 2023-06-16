package home.android.todo

import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class TodoListScreenFragment : Fragment(R.layout.todo_list_fragment) {
    private val repo = TodoItemRepository

    private lateinit var todosView: RecyclerView

    private lateinit var fabView: FloatingActionButton
    private lateinit var navbarView: LinearLayout

    private lateinit var eyeView: ImageView

    private lateinit var adapter: TodosAdapter

    private lateinit var completedCountView: TextView

    var allVisible = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        todosView = view.findViewById(R.id.todos)
        fabView = view.findViewById(R.id.floating_action_button)
        navbarView = view.findViewById(R.id.navbar)

        eyeView = view.findViewById(R.id.eye_button)

        completedCountView = view.findViewById(R.id.completed_count)

        Log.i("poupa", Thread.currentThread().name)

        fun modifyTodo(todoItem: TodoItem? = null) {
            val fragmentManager: FragmentManager = requireActivity().supportFragmentManager
            val t = fragmentManager.beginTransaction()
            t.replace(
                R.id.main_fragment, ModifyTodoItemFragment(
                    todoItem = todoItem,
                    onTodoChanged = {
                        repo.addOrUpdate(it)
                        updateState()
                    },
                    onTodoRemoved = {
                        repo.remove(it)
                        updateState()
                    })
            )
            t.addToBackStack(null)
            t.commit()
        }

        adapter = TodosAdapter(
            onAddTodo = {
                modifyTodo()
            },
            onTodoToggle = { id ->
                val todo = repo.get(id)
                repo.addOrUpdate(todo.copy(done = !todo.done))
                updateState()
            },
            onOpenTodo = { id ->
                val todo = repo.get(id)
                modifyTodo(todo)
            },
        )
        val layoutManager = LinearLayoutManager(this.context, LinearLayoutManager.VERTICAL, false)
        todosView.adapter = adapter
        todosView.layoutManager = layoutManager

        fabView.setOnClickListener {
            modifyTodo(null)
        }

        eyeView.setOnClickListener {
            allVisible = !allVisible

            if (allVisible) eyeView.setImageResource(R.drawable.eye_open)
            else eyeView.setImageResource(R.drawable.eye_closed)

            updateState()
        }
        updateState()
    }

    fun getViewItems(): MutableList<TodoListItem> {
        val data = if (allVisible) repo.getAll() else repo.getPending()
        return (data.map { TodoListItem.Preview(it) } + listOf(TodoListItem.AddNew)).toMutableList()
    }

    fun updateState() {
        val list = getViewItems()
        adapter.submitList(list)
        adapter.items = list
        completedCountView.text = resources.getString(R.string.completed_count).format(
            repo.completedCount()
        )
    }
}

sealed class TodoItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

class PreviewHolder(
    itemView: View, val onToggle: (String) -> Unit, val onOpenTodo: (String) -> Unit
) : TodoItemViewHolder(itemView) {
    fun onBind(item: TodoListItem) {
        if (item !is TodoListItem.Preview) error("")
        val model = item.model
        val textView = itemView.findViewById<TextView>(R.id.todo_item_text)


        when (model.priority) {
            TodoPriority.LOW -> textView.setTextWithIcon(model.text, R.drawable.arrow_down, 11, 14)
            TodoPriority.REGULAR -> textView.text = model.text
            TodoPriority.URGENT -> textView.setTextWithIcon(
                model.text, R.drawable.exclamations, 10, 16
            )
        }

        val completeIcon = itemView.findViewById<ImageView>(R.id.todo_item_complete_icon)
        Log.d("", model.priority.toString())
        val imgResource = if (model.done) {
            (R.drawable.square_complete)
        } else {
            if (model.priority == TodoPriority.URGENT) {
                R.drawable.square_warn
            } else {
                R.drawable.square
            }
        }
        completeIcon.setImageResource(imgResource)
        completeIcon.setOnClickListener {
            onToggle(model.id)
        }


        val infoButton = itemView.findViewById<ImageView>(R.id.todo_item_info_button)
        infoButton.setOnClickListener {
            onOpenTodo(model.id)
        }
    }

    private fun TextView.setTextWithIcon(text: String, iconId: Int, w: Int, h: Int) {
        val buffer = SpannableStringBuilder("  " + text)
        val icon = ContextCompat.getDrawable(itemView.context, iconId)!!
        val x = (context.resources.displayMetrics.density * w).toInt()
        val y = (context.resources.displayMetrics.density * h).toInt()
        icon.setBounds(0, 0, x, y)
        buffer.setSpan(VerticalImageSpan(icon), 0, 1, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        this.text = buffer
    }
}

class AddNewHolder(itemView: View, val onAddTodo: () -> Unit) : TodoItemViewHolder(itemView) {
    fun onBind() {
        itemView.setOnClickListener { onAddTodo() }
    }
}

class DiffCallback : DiffUtil.ItemCallback<TodoListItem>() {
    override fun areItemsTheSame(oldItem: TodoListItem, newItem: TodoListItem): Boolean {
        if (oldItem is TodoListItem.AddNew && newItem is TodoListItem.AddNew) return true
        if (oldItem is TodoListItem.Preview && newItem is TodoListItem.Preview) {
            return oldItem.model.id == newItem.model.id
        }
        return false
    }

    override fun areContentsTheSame(oldItem: TodoListItem, newItem: TodoListItem): Boolean {
        return oldItem == newItem
    }

}

class TodosAdapter(
    private val onAddTodo: () -> Unit,
    private val onTodoToggle: (id: String) -> Unit,
    private val onOpenTodo: (id: String) -> Unit,
) : ListAdapter<TodoListItem, TodoItemViewHolder>(DiffCallback()) {
    companion object {
        val ADD_NEW = 0
        val PREVIEW = 1
    }

    var items = mutableListOf<TodoListItem>()

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is TodoListItem.AddNew -> ADD_NEW
            is TodoListItem.Preview -> PREVIEW
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoItemViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)

        return when (viewType) {
            ADD_NEW -> AddNewHolder(
                layoutInflater.inflate(
                    R.layout.add_new_item,
                    parent,
                    false,
                ), onAddTodo = onAddTodo
            )

            PREVIEW -> PreviewHolder(
                layoutInflater.inflate(
                    R.layout.todo_item,
                    parent,
                    false,
                ),
                onOpenTodo = onOpenTodo,
                onToggle = onTodoToggle,
            )

            else -> error("Unknown viewType $viewType")
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: TodoItemViewHolder, position: Int) {
        println("onBindViewHolder $position")
        when (holder) {
            is PreviewHolder -> holder.onBind(items[position])
            is AddNewHolder -> holder.onBind()
        }
    }
}