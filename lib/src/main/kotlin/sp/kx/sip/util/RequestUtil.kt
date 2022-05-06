package sp.kx.sip.util

import sp.kx.sip.entity.method.SipInviteMethod
import sp.kx.sip.entity.method.SipRegisterMethod
import sp.kx.sip.entity.method.clone
import sp.kx.sip.foundation.entity.SipAuthenticate
import sp.kx.sip.implementation.util.SipRequestBuilder
import sp.kx.sip.implementation.util.by
import sp.kx.sip.implementation.util.contact
import sp.kx.sip.implementation.util.digest
import sp.kx.sip.implementation.util.from
import sp.kx.sip.implementation.util.to

fun SipRegisterMethod.next(): SipRegisterMethod {
    return clone(number = cs.number + 1)
}

fun SipRegisterMethod.toBody(): String {
    return SipRequestBuilder(method = cs.method, version = via.version, uri = "sip:${address.host}").build {
        by(via)
        addHeader(key = "Call-ID", value = callId)
        by(cs)
        from(user = user, host = address.host)
        to(user = user, host = address.host)
    }
}

fun SipRegisterMethod.toBody(password: String, authenticate: SipAuthenticate): String {
    val digest = authenticate.digest(
        address = address,
        method = cs.method,
        user = user,
        password = password
    )
    return SipRequestBuilder(method = cs.method, version = via.version, uri = "sip:${address.host}").build {
        by(via)
        addHeader(key = "Call-ID", value = callId)
        by(cs)
        from(user = user, host = address.host)
        to(user = user, host = address.host)
        contact(user = user, address = via.address)
        by(digest)
    }
}

fun SipInviteMethod.toBody(sdp: List<Pair<String, String>>): String {
    return SipRequestBuilder(
        method = cs.method,
        version = via.version,
        uri = "sip:${tUser.name}@${address.host}"
    ).build {
        by(via)
        addHeader(key = "Call-ID", value = callId)
        by(cs)
        from(user = fUser, host = address.host)
        to(user = tUser, host = address.host)
        setSDP(sdp)
    }
}
