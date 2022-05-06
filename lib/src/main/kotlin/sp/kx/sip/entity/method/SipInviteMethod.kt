package sp.kx.sip.entity.method

import sp.kx.sip.foundation.entity.CommandSequence
import sp.kx.sip.foundation.entity.NetworkAddress
import sp.kx.sip.foundation.entity.SipUser
import sp.kx.sip.foundation.entity.Via

interface SipInviteMethod : SipMethod {
    companion object {
        const val name = "INVITE"
    }

    val address: NetworkAddress
    val fUser: SipUser
    val tUser: SipUser
}

private class SipInviteMethodImpl(
    override val callId: String,
    override val address: NetworkAddress,
    override val fUser: SipUser,
    override val tUser: SipUser,
    override val via: Via,
    number: Int
) : SipInviteMethod {
    override val cs = CommandSequence(number = number, method = SipInviteMethod.name)
}

fun sipInviteMethod(
    callId: String,
    address: NetworkAddress,
    fUser: SipUser,
    tUser: SipUser,
    via: Via,
    number: Int
): SipInviteMethod {
    return SipInviteMethodImpl(
        callId = callId,
        address = address,
        fUser = fUser,
        tUser = tUser,
        via = via,
        number = number
    )
}
