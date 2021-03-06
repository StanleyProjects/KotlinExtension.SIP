package sp.kx.sip.foundation.entity.method

import sp.kx.sip.foundation.entity.AuthorizationDigest
import sp.kx.sip.foundation.entity.NetworkAddress
import sp.kx.sip.foundation.entity.SipUser
import sp.kx.sip.foundation.entity.Via
import sp.kx.sip.implementation.util.SipRequestBuilder
import sp.kx.sip.implementation.util.by
import sp.kx.sip.implementation.util.contact
import sp.kx.sip.implementation.util.from
import sp.kx.sip.implementation.util.to

object Register {
    const val method = "REGISTER"

    object Request {
        fun build(
            via: Via,
            callId: String,
            number: Int,
            address: NetworkAddress,
            user: SipUser,
            digest: AuthorizationDigest
        ): String {
            return SipRequestBuilder(method = method, version = via.version, uri = "sip:${address.host}").build {
                by(via)
                addHeader(key = "Call-ID", value = callId)
                addHeader(key = "CSeq", value = "$number $method")
                from(user = user, host = address.host)
                to(user = user, host = address.host)
                contact(user = user, address = via.address)
                by(digest)
            }
        }
    }
}
