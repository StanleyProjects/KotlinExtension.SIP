package sp.kx.sip.implementation.util

import sp.kx.sip.foundation.entity.NetworkAddress
import sp.kx.sip.foundation.entity.SipEnvironment
import sp.kx.sip.foundation.entity.SipUser
import sp.kx.sip.foundation.entity.TransportProtocol

object SipHeaderBuilder {
    fun build(key: String, value: String): String {
        return "$key: $value"
    }
}

fun SipHeaderBuilder.via(environment: SipEnvironment, address: NetworkAddress, branch: String): String {
    return via(
        version = environment.version,
        protocol = environment.protocol,
        address = address,
        branch = branch
    )
}

fun SipHeaderBuilder.via(version: String, protocol: TransportProtocol, address: NetworkAddress, branch: String): String {
    return build(key = "Via", value = "SIP/$version/${protocol.name} ${address.notation()};branch=$branch")
}

fun SipHeaderBuilder.to(user: SipUser, host: String): String {
    return build(key = "To", value = "sip:${user.name}@$host")
}

fun SipHeaderBuilder.from(user: SipUser, host: String, tag: String): String {
    return build(key = "From", value = "sip:${user.name}@$host;tag=$tag")
}

fun SipHeaderBuilder.contact(user: SipUser, address: NetworkAddress): String {
    return build(key = "Contact", value = "sip:${user.name}@${address.notation()}")
}
