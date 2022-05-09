package sp.kx.sip.android

import sp.kx.sip.foundation.entity.NetworkAddress
import sp.kx.sip.foundation.entity.SipUser

data class Authorized(val address: NetworkAddress, val user: SipUser)
