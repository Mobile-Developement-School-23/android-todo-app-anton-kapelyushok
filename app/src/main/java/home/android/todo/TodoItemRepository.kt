package home.android.todo

import java.util.concurrent.ConcurrentHashMap

class TodoItemRepository {
    private val data = ConcurrentHashMap<String, TodoItem>()

    fun data(): List<TodoItem> {
        return listOf()
    }

    fun addOrUpdate(todoItem: TodoItem): TodoItem? {
        return data.put(todoItem.id, todoItem)
    }
}