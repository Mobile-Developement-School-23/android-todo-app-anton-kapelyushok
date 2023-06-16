package home.android.todo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity


class TodoAppActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}