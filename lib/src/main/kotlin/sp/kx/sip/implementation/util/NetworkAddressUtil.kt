package sp.kx.sip.implementation.util

import sp.kx.sip.foundation.entity.NetworkAddress

fun NetworkAddress.notation(): String {
    return "$host:$port"
}
