package sp.kx.sip.util

import sp.kx.sip.entity.SipEnvironment
import sp.kx.sip.entity.sipEnvironment
import sp.kx.sip.foundation.entity.TransportProtocol
import sp.kx.sip.foundation.entity.Via
import sp.kx.sip.implementation.entity.address
import java.net.DatagramSocket

fun DatagramSocket.sipEnvironment(version: String): SipEnvironment {
    return sipEnvironment(
        version = version,
        protocol = TransportProtocol.UDP,
        address = address(host = localAddress.hostName, port = port)
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
