package sp.kx.sip.implementation.util

import sp.kx.sip.foundation.entity.CommandSequence
import sp.kx.sip.foundation.entity.Via
import sp.kx.sip.implementation.entity.address

fun String.toVia(): Via {
    val split = split(" ")
    check(split.size == 2)
    val (_, version, protocol) = split.first().split("/").also {
        check(it.size == 3)
    }
    check(protocol.isNotEmpty())
    check(version.isNotEmpty())
    val params = split[1].split(";")
    check(params.size > 1)
    val uri = params.first()
    check(uri.isNotEmpty())
    val (host, port) = uri.split(":").also {
        check(it.size == 2)
    }
    val branch = params.filter {
        it.startsWith("branch=")
    }.also {
        check(it.size == 1)
    }.first().split("=").also {
        check(it.size == 2)
    }[1]
    check(branch.isNotEmpty())
    return Via(
        version = version,
        protocol = enumValueOf(protocol),
        address = address(host = host, port = port.toInt()),
        branch = branch
    )
}

fun String.toCommandSequence(): CommandSequence {
    val split = split(" ")
    check(split.size == 2)
    val number = split.first().let {
        it.toIntOrNull() ?: error("Value \"$it\" is not a number!")
    }
    val method = split[1]
    check(method.isNotEmpty())
    return CommandSequence(
        number = number,
        method = method
    )
}
