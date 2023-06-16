package home.android.todo

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.time.Instant


class TodoAppActivity : AppCompatActivity() {

    private lateinit var todosView: RecyclerView

    //    private lateinit var fabView: FloatingActionButton
    private lateinit var navbarView: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        todosView = findViewById(R.id.todos)
//        fabView = findViewById(R.id.floating_action_button)
        navbarView = findViewById(R.id.navbar)

        Log.i("poupa", Thread.currentThread().name)

        val adapter = TodosAdapter()
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        todosView.adapter = adapter
        todosView.layoutManager = layoutManager

        var longString =
            "Очень большой длинный текст пожалуйста укоротись ну пожалуйста еще перенесись на другую строку мы договорились?"
        longString = "$longString $longString"


        val todos = mutableListOf(
            TodoItem("1", longString, TodoPriority.LOW, null, false, Instant.MAX, Instant.MAX),
            TodoItem("2", "text2", TodoPriority.LOW, null, false, Instant.MAX, Instant.MAX),
        )

        for (i in 3..30) {
            todos += TodoItem(
                "$i",
                "text$i",
                TodoPriority.LOW,
                null,
                false,
                Instant.MAX,
                Instant.MAX
            )
        }

        Handler(Looper.getMainLooper()).postDelayed({
            adapter.items.add(
                adapter.items.lastIndex, TodoListItem.Preview(
                    TodoItem("2", "text2", TodoPriority.LOW, null, false, Instant.MAX, Instant.MAX)
                )
            )

            adapter.notifyItemInserted(adapter.items.lastIndex - 1)

            adapter.items.withIndex().forEach { (id, v) ->
                if (v is TodoListItem.Preview) {
                    Log.d("mytag", "$id -> ${v.model.text}")
                } else {
                    Log.d("mytag", "$id -> addNew")
                }

            }

        }, 4000L)

        adapter.items =
            (todos.map { TodoListItem.Preview(it) } + listOf(TodoListItem.AddNew)).toMutableList()

    }
}

sealed class TodoItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

class PreviewHolder(itemView: View) : TodoItemViewHolder(itemView) {
    fun onBind(item: TodoListItem) {
        if (item is TodoListItem.Preview) {
            val textView = itemView.findViewById<TextView>(R.id.todo_item_text)
            textView.text = item.model.text
        }
    }
}

class AddNewHolder(itemView: View) : TodoItemViewHolder(itemView) {
    fun onBind() {}
}

class TodosAdapter : RecyclerView.Adapter<TodoItemViewHolder>() {
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
                )
            )

            PREVIEW -> PreviewHolder(
                layoutInflater.inflate(
                    R.layout.todo_item,
                    parent,
                    false,
                )
            )

            else -> error("Unknown viewType $viewType")
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: TodoItemViewHolder, position: Int) {
        when (holder) {
            is PreviewHolder -> holder.onBind(items[position])
            is AddNewHolder -> holder.onBind()
        }
    }
}
