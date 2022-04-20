package sp.kx.sip.foundation.entity.method

import sp.kx.sip.foundation.entity.AuthorizationDigest
import sp.kx.sip.foundation.entity.NetworkAddress
import sp.kx.sip.foundation.entity.SipUser
import sp.kx.sip.foundation.entity.Via
import sp.kx.sip.foundation.entity.request.SipRequestBuilder
import sp.kx.sip.foundation.entity.request.by
import sp.kx.sip.foundation.entity.request.contact
import sp.kx.sip.foundation.entity.request.from
import sp.kx.sip.foundation.entity.request.to

object Register {
    const val method = "REGISTER"

    object Request {
        private fun builder(
            via: Via,
            callId: String,
            number: Int,
            address: NetworkAddress,
            user: SipUser
        ): SipRequestBuilder {
            return SipRequestBuilder(method = method, version = via.version, address = address)
                .by(via)
                .addHeader(key = "Call-ID", value = callId)
                .addHeader(key = "CSeq", value = "$number $method")
                .from(user = user, host = address.host)
                .to(user = user, host = address.host)
        }

        fun build(
            via: Via,
            callId: String,
            number: Int,
            address: NetworkAddress,
            user: SipUser
        ): String {
            return builder(
                via = via,
                callId = callId,
                number = number,
                address = address,
                user = user
            ).build()
        }

        fun build(
            via: Via,
            callId: String,
            number: Int,
            address: NetworkAddress,
            user: SipUser,
            digest: AuthorizationDigest
        ): String {
            return builder(
                via = via,
                callId = callId,
                number = number,
                address = address,
                user = user
            ).by(digest).contact(user = user, address = via.address).build()
        }
    }
}
