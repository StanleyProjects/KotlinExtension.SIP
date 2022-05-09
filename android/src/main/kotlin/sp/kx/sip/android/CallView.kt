package sp.kx.sip.android

import android.content.Context
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import sp.kx.sip.foundation.entity.SipUser

fun Context.callView(
    authorized: Authorized,
    called: SipUser,
    onCancel: () -> Unit
): FrameLayout {
    val root = LinearLayout(this).also { root ->
        root.orientation = LinearLayout.VERTICAL
        TextView(this).also {
            it.text = authorized.address.host + " | " + authorized.user.name + " -> " + called.name
            root.addView(it)
        }
        Button(this).also {
            it.layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            it.text = "cancel"
            it.setOnClickListener {
                onCancel()
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
