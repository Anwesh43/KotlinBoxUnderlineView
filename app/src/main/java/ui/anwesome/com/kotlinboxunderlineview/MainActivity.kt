package ui.anwesome.com.kotlinboxunderlineview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import ui.anwesome.com.boxunderlineview.BoxUnderlineView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        BoxUnderlineView.create(this,5)
    }
}
