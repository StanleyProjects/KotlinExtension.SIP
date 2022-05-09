package sp.kx.sip.implementation.util

import sp.kx.sip.foundation.entity.AuthorizationDigest
import sp.kx.sip.foundation.entity.CommandSequence
import sp.kx.sip.foundation.entity.NetworkAddress
import sp.kx.sip.foundation.entity.SipUser
import sp.kx.sip.foundation.entity.TransportProtocol
import sp.kx.sip.foundation.entity.Via
import sp.kx.sip.implementation.util.notation

class SipRequestBuilder(
    method: String,
    uri: String,
    version: String
) {
    private val top = "$method $uri SIP/$version"
    private val headers = mutableMapOf<String, String>()
    private var sdp: List<Pair<String, String>> = emptyList()

    fun addHeader(key: String, value: String): SipRequestBuilder {
        headers[key] = value
        return this
    }

    fun setSDP(values: List<Pair<String, String>>): SipRequestBuilder {
        sdp = values
        return this
    }

    fun build(): String {
        val sdp = sdp
        if (sdp.isEmpty()) {
            return (listOf(top) + headers.map { (k, v) -> "$k: $v" }).joinToString(separator = "\r\n", postfix = "\r\n\r\n")
        }
        val content = sdp.joinToString(separator = "\r\n") { (key, value) -> "$key=$value" }
        val additional = mapOf(
            "Content-Type" to "application/sdp",
            "Content-Length" to (content.length + 2).toString()
        )
        return (listOf(top) + (headers + additional).map { (k, v) -> "$k: $v" })
            .joinToString(separator = "\r\n", postfix = "\r\n\r\n$content\r\n")
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

fun SipRequestBuilder.by(sequence: CommandSequence): SipRequestBuilder {
    return addHeader(key = "CSeq", value = "${sequence.number} ${sequence.method}")
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
