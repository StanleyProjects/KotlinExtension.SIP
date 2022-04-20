package sp.kx.sip.implementation.entity

import sp.kx.sip.foundation.entity.SipUser

private class SipUserImpl(override val name: String) : SipUser

fun sipUser(name: String): SipUser {
    return SipUserImpl(name = name)
}
