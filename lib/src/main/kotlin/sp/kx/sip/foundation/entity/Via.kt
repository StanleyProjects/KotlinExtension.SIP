package sp.kx.sip.foundation.entity

data class Via(
    val version: String,
    val protocol: TransportProtocol,
    val address: NetworkAddress,
    val branch: String
)
