package sp.kx.sip.android

import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class EnterActivity : AppCompatActivity() {
    private var edits: Map<SipEntity, EditText>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).also { root ->
            root.orientation = LinearLayout.VERTICAL
            edits = setOf(
                SipEntity.REMOTE_HOST,
                SipEntity.REMOTE_PORT,
                SipEntity.USER_NAME,
                SipEntity.PASSWORD
            ).associateWith { key ->
                TextView(this).also {
                    it.layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    it.text = key.name
                    root.addView(it)
                }
                EditText(this).also {
                    it.layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    it.isSingleLine = true
                    it.setLines(1)
                    root.addView(it)
                }
            }
            Button(this).also {
                it.layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                it.text = "enter"
                it.setOnClickListener {
                    // todo
                }
                root.addView(it)
            }
        }
        setContentView(
            FrameLayout(this).also {
                it.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                root.layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    Gravity.CENTER_VERTICAL
                )
                it.addView(root)
            }
        )
    }
}
