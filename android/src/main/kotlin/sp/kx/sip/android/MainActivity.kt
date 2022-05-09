package sp.kx.sip.android

import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import sp.kx.sip.foundation.entity.SipCode
import sp.kx.sip.implementation.entity.address
import sp.kx.sip.implementation.entity.sipUser

class MainActivity : AppCompatActivity() {
    private fun onCancel() {
        SIPViewModel.cancel()
        render()
    }

    private fun onCall(name: String) {
        SIPViewModel.call(user = sipUser(name = name), onCalled = {render()}, onCode = { code ->
            println("code: $code")
            when (code) {
                SipCode.Trying, SipCode.Ringing -> {
                    // ignored
                }
                SipCode.Unavailable -> {
                    showToast("Temporarily Unavailable!")
                }
                SipCode.Busy -> {
                    // ignored
                }
                else -> error("Code $code is not supported!")
            }
        }, onFinish = {render()})
    }

    private fun onExit() {
        SIPViewModel.exit { render() }
    }

    private fun onEnter(values: Map<SipEntity, String>) {
        val host = values[SipEntity.REMOTE_HOST]!!
        val port = values[SipEntity.REMOTE_PORT]!!.toInt()
        val name = values[SipEntity.USER_NAME]!!
        val password = values[SipEntity.PASSWORD]!!
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
            render()
        }
    }

    private fun render() {
        val authorized = SIPViewModel.getAuthorized()
        if (authorized == null) {
            setContentView(enterView(::onEnter))
        } else {
            val called = SIPViewModel.getCalled()
            if (called == null) {
                setContentView(authorizedView(authorized, ::onExit, ::onCall))
            } else {
                setContentView(callView(authorized, called, ::onCancel))
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        render()
    }
}
