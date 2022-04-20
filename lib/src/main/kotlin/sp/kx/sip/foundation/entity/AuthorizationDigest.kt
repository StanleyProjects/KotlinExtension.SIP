package sp.kx.sip.foundation.entity

/**
 * https://datatracker.ietf.org/doc/html/rfc3261#section-20.7
 */
data class AuthorizationDigest(
    val authenticate: SipAuthenticate,
    val username: String,
    val uri: String,
    val response: String
)
