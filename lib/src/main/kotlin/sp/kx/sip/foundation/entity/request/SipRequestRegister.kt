package sp.kx.sip.foundation.entity.request

import sp.kx.sip.foundation.entity.NetworkAddress
import sp.kx.sip.foundation.entity.SipEnvironment
import sp.kx.sip.foundation.entity.SipUser
import sp.kx.sip.foundation.entity.TransportProtocol
import sp.kx.sip.foundation.entity.Via
import sp.kx.sip.implementation.util.SipHeaderBuilder
import sp.kx.sip.implementation.util.contact
import sp.kx.sip.implementation.util.from
import sp.kx.sip.implementation.util.to
import sp.kx.sip.implementation.util.via

data class SipRequestRegister(
    val version: String,
    val protocol: TransportProtocol,
    val number: Int,
    val rAddress: NetworkAddress,
    val lAddress: NetworkAddress,
    val branch: String,
    val callId: String,
    val user: SipUser,
    val tag: String
) {
    val method = "REGISTER"

    fun toBody(): String {
        return SipRequestBuilder(method = method, version = version, address = rAddress).build {
            via(version = version, protocol = protocol, address = lAddress, branch = branch)
            addHeader(key = "Call-ID", value = callId)
            addHeader(key = "CSeq", value = "$number $method")
            from(user = user, host = rAddress.host, tag = tag)
            to(user = user, host = rAddress.host)
            contact(user = user, address = lAddress)
        }
    }
}

fun SipRequestRegister.getVia(): Via {
    return Via(
        version = version,
        protocol = protocol,
        address = lAddress,
        branch = branch
    )
}
