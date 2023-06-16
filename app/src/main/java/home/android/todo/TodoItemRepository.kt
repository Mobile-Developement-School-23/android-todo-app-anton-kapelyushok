package home.android.todo

import java.time.Instant
import java.time.LocalDate
import kotlin.random.Random

object TodoItemRepository {
    private val data = LinkedHashMap<String, TodoItem>()

    init {
        val loremIpsum =
            """Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum"""

        data["1"] =
            TodoItem("1", loremIpsum, TodoPriority.LOW, null, false, Instant.MAX, Instant.MAX)
        data["2"] = TodoItem("2", "text2", TodoPriority.LOW, null, false, Instant.MAX, Instant.MAX)

        fun randomPriority(): TodoPriority = TodoPriority.values()[Random.nextInt(3)]
        fun randomText(): String {
            return if (Random.nextInt(10) > 7) {
                loremIpsum.take(Random.nextInt(loremIpsum.length))
            } else {
                loremIpsum.take(Random.nextInt(20))
            }
        }

        for (i in 3..30) {
            data["$i"] = TodoItem(
                "$i",
                randomText(),
                randomPriority(),
                if (Random.nextBoolean())
                    LocalDate.now() else null,
                Random.nextBoolean(),
                Instant.MAX,
                Instant.MAX
            )
        }
    }

    fun getAll(): List<TodoItem> {
        return data.values.toList()
    }

    fun get(id: String): TodoItem {
        return data[id]!!
    }

    fun getPending(): List<TodoItem> {
        return data.values.filter { !it.done }
    }

    fun completedCount(): Int {
        return data.values.count { it.done }
    }

    fun addOrUpdate(todoItem: TodoItem): TodoItem? {
        return data.put(todoItem.id, todoItem)
    }

    fun remove(id: String) {
        data.remove(id)
    }
}