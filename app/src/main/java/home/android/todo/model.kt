package home.android.todo

import java.time.Instant
import java.time.LocalDate

enum class TodoPriority {
    LOW, REGULAR, URGENT
}

data class TodoItem(
    val id: String,
    val text: String,
    val priority: TodoPriority,
    val deadline: LocalDate?,
    val done: Boolean,

    val createdAt: Instant,
    val modifiedAt: Instant,
)

sealed interface TodoListItem {
    data class Preview(val model: TodoItem) : TodoListItem
    object AddNew : TodoListItem
}