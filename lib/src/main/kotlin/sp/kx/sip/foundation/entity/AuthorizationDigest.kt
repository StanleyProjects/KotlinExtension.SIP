package sp.kx.sip.foundation.entity

data class AuthorizationDigest(
    val authenticate: SipAuthenticate,
    val username: String,
    val uri: String,
    val response: String
)
