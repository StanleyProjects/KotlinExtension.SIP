package sp.kx.sip.implementation.util

import sp.kx.sip.foundation.entity.SipEnvironment
import sp.kx.sip.foundation.entity.request.SipRequest

fun SipEnvironment.toRequestBody(type: SipRequest.Register): String {
    TODO()
//    return listOf(
//        "${type.method} sip:$rHost SIP/$version",
//        SipHeaderBuilder.via(environment = this, address = lAddress, branch = branch),
//        SipHeaderBuilder.build(key = "Call-ID", value = callId),
//        SipHeaderBuilder.build(key = "CSeq", value = "$number ${type.method}"),
//        SipHeaderBuilder.from(user = user, host = rHost, tag = tag),
//        SipHeaderBuilder.to(user = user, host = rHost),
//        SipHeaderBuilder.contact(user = user, address = lAddress)
//    ).joinToString(separator = "\r\n", postfix = "\r\n\r\n")
}
