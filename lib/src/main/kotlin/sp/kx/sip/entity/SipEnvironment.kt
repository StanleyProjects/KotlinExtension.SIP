package sp.kx.sip.entity

import sp.kx.sip.foundation.entity.NetworkAddress
import sp.kx.sip.foundation.entity.TransportProtocol

interface SipEnvironment {
    /**
     * https://datatracker.ietf.org/doc/html/rfc3261#section-7.1
     *
     * SIP-Version: Both request and response messages include the
     * version of SIP in use, and follow H3.1 (with HTTP replaced
     * by SIP, and HTTP/1.1 replaced by SIP/2.0) regarding version
     * ordering, compliance requirements, and upgrading of version
     * numbers. To be compliant with this specification,
     * applications sending SIP messages MUST include a SIP-Version
     * of "SIP/2.0". The SIP-Version string is case-insensitive,
     * but implementations MUST send upper-case.
     */
    val version: String
    val protocol: TransportProtocol
    val address: NetworkAddress
}

private class SipEnvironmentImpl(
    override val version: String,
    override val protocol: TransportProtocol,
    override val address: NetworkAddress
) : SipEnvironment

fun sipEnvironment(
    version: String,
    protocol: TransportProtocol,
    address: NetworkAddress
): SipEnvironment {
    return SipEnvironmentImpl(
        version = version,
        protocol = protocol,
        address = address
    )
}
