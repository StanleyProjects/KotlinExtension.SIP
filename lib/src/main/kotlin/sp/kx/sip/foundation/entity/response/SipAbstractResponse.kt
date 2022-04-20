package sp.kx.sip.foundation.entity.response

data class SipAbstractResponse(
    val top: SipResponseTop,
    val headers: Map<String, String>
)
