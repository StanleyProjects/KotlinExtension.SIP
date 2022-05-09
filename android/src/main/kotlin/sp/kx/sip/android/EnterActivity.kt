package sp.kx.sip.android

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import sp.kx.sip.implementation.entity.address
import sp.kx.sip.implementation.entity.sipUser

class EnterActivity : AppCompatActivity() {
    private var edits: Map<SipEntity, EditText>? = null

    private fun onEnter() {
        val host = edits!![SipEntity.REMOTE_HOST]!!.text.toString()
        val port = edits!![SipEntity.REMOTE_PORT]!!.text.toString().toInt()
        val name = edits!![SipEntity.USER_NAME]!!.text.toString()
        val password = edits!![SipEntity.PASSWORD]!!.text.toString()
        println("try enter...")
        SIPViewModel.enter(
            address = address(host = host, port = port),
            user = sipUser(name = name),
            password = password
        ) {
            getSharedPreferences()
                .edit()
                .putString(SipEntity.REMOTE_HOST.name, host)
                .putInt(SipEntity.REMOTE_PORT.name, port)
                .putString(SipEntity.USER_NAME.name, name)
                .putString(SipEntity.PASSWORD.name, password)
                .apply()
            finish()
            startActivity(Intent(this, EnterActivity::class.java))
        }
    }

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
                    onEnter()
                }
                root.addView(it)
            }
        }
        getSharedPreferences().also {
            setOf(
                SipEntity.REMOTE_HOST,
                SipEntity.USER_NAME,
                SipEntity.PASSWORD
            ).forEach { type ->
                it.getString(type.name, null).also { value ->
                    if (!value.isNullOrEmpty()) {
                        edits!![type]!!.setText(value)
                    }
                }
            }
            val port = it.getInt(SipEntity.REMOTE_PORT.name, -1)
            if (port > 0) {
                edits!![SipEntity.REMOTE_PORT]!!.setText(port.toString())
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
