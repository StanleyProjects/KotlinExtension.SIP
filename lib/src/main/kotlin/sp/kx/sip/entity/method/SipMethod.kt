package sp.kx.sip.entity.method

import sp.kx.sip.foundation.entity.CommandSequence
import sp.kx.sip.foundation.entity.Via

interface SipMethod {
    val via: Via
    val cs: CommandSequence
    val callId: String
}
