package sp.kx.sip.implementation.util

import sp.kx.sip.foundation.entity.SipEnvironment
import sp.kx.sip.foundation.entity.response.SipAbstractResponse
import sp.kx.sip.foundation.entity.response.SipResponseTop

private fun String.toTop(): SipResponseTop {
    val split = split(" ")
    check(split.size > 2) { "Top size is ${split.size}!" }
    val version = split[0].split("/").let {
        check(it.size == 2) { "Top split size is ${it.size}!"}
        check(it.first() == "SIP") { "Top split starts with \"${it.first()}\"!"}
        it[1]
    }
    val code = split[1].toIntOrNull() ?: error("Response code error!")
    val message = substring(startIndex = "SIP/$version $code".length + 1)
    return SipResponseTop(
        version = version,
        code = code,
        message = message
    )
}

private fun String.toHeader(): Pair<String, String> {
    val index = indexOf(":")
    check(index > 0)
    return substring(0, index) to substring(index + 2, length)
}

fun String.toSipResponse(): SipAbstractResponse {
    val split = split("\r\n")
    check(split.size > 6) { "Size is ${split.size}!" }
    val top = split.first().toTop()
    val index = split.indexOfFirst { it.isEmpty() }
    check(index > 0) { "Index is $index!" }
    val headers = split.subList(fromIndex = 1, toIndex = index).associate {
        it.toHeader()
    }
    return SipAbstractResponse(
        top = top,
        headers = headers
    )
}

fun SipAbstractResponse.getEnvironment(): SipEnvironment {
    val split = headers["Via"]!!.split(" ")
    check(split.size > 6) { "Size is ${split.size}!" }
    val (_, version, protocol) = split[0].split("/").also {
        check(it.size == 3)
    }
    return SipEnvironment(version = version, protocol = enumValueOf(protocol))
}

fun SipAbstractResponse.requireHeader(key: String): String {
    return headers[key] ?: error("Value by $key does not exist!")
}
