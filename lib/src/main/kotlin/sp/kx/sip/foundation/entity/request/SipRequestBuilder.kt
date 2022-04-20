package sp.kx.sip.foundation.entity.request

import sp.kx.sip.foundation.entity.AuthorizationDigest
import sp.kx.sip.foundation.entity.NetworkAddress
import sp.kx.sip.foundation.entity.SipUser
import sp.kx.sip.foundation.entity.TransportProtocol
import sp.kx.sip.foundation.entity.Via
import sp.kx.sip.implementation.util.notation

class SipRequestBuilder(
    method: String,
    address: NetworkAddress,
    version: String
) {
    private val top = "$method sip:${address.host} SIP/$version"
    private val headers = mutableMapOf<String, String>()

    fun addHeader(key: String, value: String): SipRequestBuilder {
        headers[key] = value
        return this
    }

    fun build(): String {
        return (listOf(top) + headers.map { (k, v) -> "$k: $v" }).joinToString(separator = "\r\n", postfix = "\r\n\r\n")
    }

    fun build(block: SipRequestBuilder.() -> Unit): String {
        block()
        return build()
    }
}

fun SipRequestBuilder.via(version: String, protocol: TransportProtocol, address: NetworkAddress, branch: String): SipRequestBuilder {
    return addHeader(key = "Via", value = "SIP/$version/${protocol.name} ${address.notation()};branch=$branch")
}

fun SipRequestBuilder.by(value: Via): SipRequestBuilder {
    return via(version = value.version, protocol = value.protocol, address = value.address, branch = value.branch)
}

fun SipRequestBuilder.to(user: SipUser, host: String): SipRequestBuilder {
    return addHeader(key = "To", value = "sip:${user.name}@$host")
}

//fun SipRequestBuilder.from(user: SipUser, host: String, tag: String): SipRequestBuilder {
//    return addHeader(key = "From", value = "sip:${user.name}@$host;tag=$tag")
//} // todo

fun SipRequestBuilder.from(user: SipUser, host: String): SipRequestBuilder {
    return addHeader(key = "From", value = "sip:${user.name}@$host")
}

fun SipRequestBuilder.contact(user: SipUser, address: NetworkAddress): SipRequestBuilder {
    return addHeader(key = "Contact", value = "sip:${user.name}@${address.notation()}")
}

fun SipRequestBuilder.by(value: AuthorizationDigest): SipRequestBuilder {
    return addHeader(
        key = "Authorization",
        value = mapOf(
            "username" to value.username,
            "realm" to value.authenticate.realm,
            "nonce" to value.authenticate.nonce,
            "algorithm" to value.authenticate.algorithm,
            "uri" to value.uri,
            "response" to value.response
        ).toList().joinToString(separator = ", ") { (k, v) -> "$k=\"$v\"" }
    )
}
