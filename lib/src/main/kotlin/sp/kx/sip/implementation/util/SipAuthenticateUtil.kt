package sp.kx.sip.implementation.util

import sp.kx.sip.foundation.entity.AuthorizationDigest
import sp.kx.sip.foundation.entity.SipAuthenticate
import java.security.MessageDigest

fun String.toAuthenticate(): SipAuthenticate {
    check(startsWith("Digest "))
    val split = substring("Digest ".length).split(", ")
    check(split.size > 1)
    val map = split.associate {
        val pair = it.split("=")
        check(pair.size == 2)
        pair[0] to pair[1]
    }
    val nonce = map["nonce"]?.let {
        check(it.startsWith("\"") && it.endsWith("\""))
        it.substring(1, it.length - 1)
    }
    check(!nonce.isNullOrEmpty())
    val realm = map["realm"]?.let {
        check(it.startsWith("\"") && it.endsWith("\""))
        it.substring(1, it.length - 1)
    }
    check(!realm.isNullOrEmpty())
    val algorithm = map["algorithm"]
    check(!algorithm.isNullOrEmpty())
    return SipAuthenticate(
        nonce = nonce,
        realm = realm,
        algorithm = algorithm
    )
}

private fun ByteArray.toHexString(): String {
    val chars = "0123456789abcdef"
    val builder = StringBuilder(size * 2)
    forEach {
        val i = it.toInt()
        builder
            .append(chars[i shr 4 and 0x0f])
            .append(chars[i and 0x0f])
    }
    return builder.toString()
}

fun SipAuthenticate.digest(
    uri: String,
    method: String,
    name: String,
    password: String
): AuthorizationDigest {
    val md = MessageDigest.getInstance(algorithm)
    val uDigest = md.digest(
        "$name:$realm:$password".toByteArray(Charsets.UTF_8)
    ).toHexString()
    val mDigest = md.digest(
        "$method:$uri".toByteArray(Charsets.UTF_8)
    ).toHexString()
    return AuthorizationDigest(
        authenticate = this,
        username = name,
        response = md.digest(
            "$uDigest:$nonce:$mDigest".toByteArray(Charsets.UTF_8)
        ).toHexString(),
        uri = uri
    )
}
