package sp.kx.sip.foundation.entity.request

sealed class SipRequest(val method: String) {
    object Register : SipRequest(method = "REGISTER")
}
