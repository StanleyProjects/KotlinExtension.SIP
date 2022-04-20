package sp.kx.sip.implementation.util

import sp.kx.sip.foundation.entity.NetworkAddress
import sp.kx.sip.foundation.entity.SipAuthenticate
import sp.kx.sip.foundation.entity.SipUser
import sp.kx.sip.foundation.entity.Via
import sp.kx.sip.foundation.entity.method.Register

fun Register.Request.build(
    via: Via,
    callId: String,
    number: Int,
    address: NetworkAddress,
    user: SipUser,
    authenticate: SipAuthenticate,
    password: String
): String {
    return build(
        via = via,
        callId = callId,
        number = number,
        address = address,
        user = user,
        digest = authenticate.digest(
            address = address,
            method = Register.method,
            user = user,
            password = password
        )
    )
}
