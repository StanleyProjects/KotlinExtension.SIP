package sp.kx.sip.foundation.entity.method

import sp.kx.sip.entity.method.SipMethod
import sp.kx.sip.foundation.entity.response.SipAbstractResponse
import sp.kx.sip.implementation.util.requireHeader
import sp.kx.sip.implementation.util.toCommandSequence
import sp.kx.sip.implementation.util.toVia

fun SipMethod.check(response: SipAbstractResponse) {
    response.requireHeader("Via").toVia().also { actual ->
        check(actual == via) {
            "Expected is $via, but actual is $actual!"
        }
    }
    response.requireHeader("CSeq").toCommandSequence().also { actual ->
        check(actual == cs) {
            "Expected is $cs, but actual is $actual!"
        }
    }
    check(response.requireHeader("Call-ID") == callId)
}
