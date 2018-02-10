package ui.anwesome.com.kotlinboxunderlineview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import ui.anwesome.com.boxunderlineview.BoxUnderlineView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = BoxUnderlineView.create(this,5)
        view.addBoxSelectionListener {
            Toast.makeText(this,"Selected $it",Toast.LENGTH_SHORT).show()
        }
        fullScreen()
    }
}
fun MainActivity.fullScreen() {
    supportActionBar?.hide()
    window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
}