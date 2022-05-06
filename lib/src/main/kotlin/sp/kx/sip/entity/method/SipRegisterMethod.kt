package sp.kx.sip.entity.method

import sp.kx.sip.foundation.entity.CommandSequence
import sp.kx.sip.foundation.entity.NetworkAddress
import sp.kx.sip.foundation.entity.SipUser
import sp.kx.sip.foundation.entity.Via

interface SipRegisterMethod : SipMethod {
    companion object {
        const val name = "REGISTER"
    }

    val address: NetworkAddress
    val user: SipUser
}

private class SipRegisterMethodImpl(
    override val via: Via,
    override val callId: String,
    override val address: NetworkAddress,
    override val user: SipUser,
    number: Int
) : SipRegisterMethod {
    override val cs = CommandSequence(number = number, method = SipRegisterMethod.name)
}

fun sipRegisterMethod(
    via: Via,
    callId: String,
    address: NetworkAddress,
    user: SipUser,
    number: Int
): SipRegisterMethod {
    return SipRegisterMethodImpl(
        via = via,
        callId = callId,
        address = address,
        user = user,
        number = number
    )
}

fun SipRegisterMethod.clone(number: Int): SipRegisterMethod {
    return sipRegisterMethod(
        via = via,
        number = number,
        callId = callId,
        address = address,
        user = user
    )
}
