package sp.kx.sip.foundation.entity

/**
 * https://datatracker.ietf.org/doc/html/rfc3261#section-8.1.1.7
 */
data class Via(
    val version: String,
    val protocol: TransportProtocol,
    val address: NetworkAddress,
    val branch: String
)
