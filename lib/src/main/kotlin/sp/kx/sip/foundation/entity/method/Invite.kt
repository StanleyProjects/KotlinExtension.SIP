package sp.kx.sip.foundation.entity.method

import sp.kx.sip.foundation.entity.NetworkAddress
import sp.kx.sip.foundation.entity.SipUser
import sp.kx.sip.foundation.entity.Via
import sp.kx.sip.implementation.util.SipRequestBuilder
import sp.kx.sip.implementation.util.by
import sp.kx.sip.implementation.util.from
import sp.kx.sip.implementation.util.to

object Invite {
    const val method = "INVITE"

    object Request {
        fun build(
            via: Via,
            callId: String,
            number: Int,
            address: NetworkAddress,
            fUser: SipUser,
            tUser: SipUser
        ): String {
            return SipRequestBuilder(
                method = method,
                version = via.version,
                uri = "sip:${tUser.name}@${address.host}"
            ).build {
                by(via)
                addHeader(key = "Call-ID", value = callId)
                addHeader(key = "CSeq", value = "$number $method")
                from(user = fUser, host = address.host)
                to(user = tUser, host = address.host)
            }
        }
    }
}
