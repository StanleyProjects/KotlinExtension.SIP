package sp.kx.sip.android

import android.content.Context
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView

fun Context.authorizedView(authorized: Authorized, onExit: () -> Unit, onCall: (String) -> Unit): FrameLayout {
    val root = LinearLayout(this).also { root ->
        root.orientation = LinearLayout.VERTICAL
        TextView(this).also {
            it.text = authorized.address.host + " | " + authorized.user.name
            root.addView(it)
        }
        TextView(this).also {
            it.text = "Call to:"
            root.addView(it)
        }
        val et = EditText(this).also {
            it.layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            root.addView(it)
        }
        Button(this).also {
            it.layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            it.text = "call"
            it.setOnClickListener {
                val name = et.text.toString()
                if (name.isNotEmpty()) {
                    onCall(name)
                }
            }
            root.addView(it)
        }
        Button(this).also {
            it.layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            it.text = "exit"
            it.setOnClickListener {
                onExit()
            }
            root.addView(it)
        }
    }
    return FrameLayout(this).also {
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
}
