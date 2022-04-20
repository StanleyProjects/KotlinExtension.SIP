package sp.kx.sip.foundation.entity

data class SipAuthenticate(
    val realm: String,
    val nonce: String,
    val algorithm: String
)
