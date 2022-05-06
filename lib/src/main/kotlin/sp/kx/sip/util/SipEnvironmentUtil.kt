package sp.kx.sip.util

import sp.kx.sip.entity.SipEnvironment
import sp.kx.sip.entity.sipEnvironment
import sp.kx.sip.foundation.entity.TransportProtocol
import sp.kx.sip.foundation.entity.Via
import sp.kx.sip.implementation.entity.address
import sp.kx.sip.implementation.util.java.net.getHostAddress
import java.net.DatagramSocket

fun DatagramSocket.sipEnvironment(version: String): SipEnvironment {
    return sipEnvironment(
        version = version,
        protocol = TransportProtocol.UDP,
        address = address(host = getHostAddress(), port = port)
    )
}

fun SipEnvironment.toVia(branch: String): Via {
    return Via(
        version = version,
        protocol = protocol,
        address = address,
        branch = branch
    )
}
