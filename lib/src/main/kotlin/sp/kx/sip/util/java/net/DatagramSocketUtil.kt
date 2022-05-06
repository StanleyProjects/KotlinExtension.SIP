package sp.kx.sip.util.java.net

import sp.kx.sip.entity.RFC8866
import sp.kx.sip.entity.method.SipInviteMethod
import sp.kx.sip.entity.method.SipMethod
import sp.kx.sip.entity.method.SipRegisterMethod
import sp.kx.sip.foundation.entity.SipAuthenticate
import sp.kx.sip.foundation.entity.SipCode
import sp.kx.sip.foundation.entity.response.SipAbstractResponse
import sp.kx.sip.implementation.entity.address
import sp.kx.sip.implementation.util.java.net.getHostAddress
import sp.kx.sip.implementation.util.requireHeader
import sp.kx.sip.implementation.util.toAuthenticate
import sp.kx.sip.implementation.util.toCommandSequence
import sp.kx.sip.implementation.util.toSipResponse
import sp.kx.sip.implementation.util.toVia
import sp.kx.sip.util.next
import sp.kx.sip.util.toBody
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.Inet4Address
import java.net.SocketTimeoutException
import java.util.Random
import kotlin.math.absoluteValue

private val packet = DatagramPacket(ByteArray(0), 0)

private fun DatagramSocket.send(data: String) {
    println("\t-->\n$data")
    packet.data = data.toByteArray(Charsets.UTF_8)
    send(packet)
}

private fun DatagramSocket.receive(): String {
    packet.data = ByteArray(1024)
    receive(packet)
    val result = String(packet.data, Charsets.UTF_8)
    println("\t<--\n$result")
    return result
}

private fun SipMethod.check(response: SipAbstractResponse) {
    check(response.requireHeader("Via").toVia() == via)
    check(response.requireHeader("CSeq").toCommandSequence() == cs)
    check(response.requireHeader("Call-ID") == callId)
}

private fun DatagramSocket.request(
    method: SipRegisterMethod,
    password: String,
    authenticate: SipAuthenticate
) {
    send(method.toBody(password, authenticate))
    val response = receive().toSipResponse()
    method.check(response)
    when (response.top.code) {
        SipCode.Unauthorized -> error("Unauthorized!")
        SipCode.OK -> return
        else -> error("Code ${response.top.code} is not supported!")
    }
}

fun DatagramSocket.request(
    method: SipRegisterMethod,
    password: String
) {
    send(method.toBody())
    val response = receive().toSipResponse()
    method.check(response)
    when (response.top.code) {
        SipCode.Unauthorized -> {
            val authenticate = response.requireHeader("WWW-Authenticate").toAuthenticate()
            request(method.next(), password, authenticate)
        }
        SipCode.OK -> return
        else -> error("Code ${response.top.code} is not supported!")
    }
}

fun DatagramSocket.request(method: SipInviteMethod) {
    val sdpVersion = 0
    val sdpSessionId = Random().nextInt().absoluteValue
    val audio = DatagramSocket()
    val aa = address(
        host = (audio.localAddress as Inet4Address).hostAddress,
        port = audio.localPort
    )
    println("audio: $aa")
    val ci = RFC8866.getConnectionInformation(protocol = "IN", addressType = "IP4", host = getHostAddress())
    val sd = RFC8866.getSessionDescription(originatorId = "-", sessionId = sdpSessionId, version = sdpVersion, connectionInformation = ci)
    val sdp = listOf(
        "v" to sdpVersion.toString(),
        "o" to sd,
        "c" to ci,
        "m" to "audio ${audio.localPort} RTP/AVP 8"
    )
    send(method.toBody(sdp))
    var code: Int? = null
    while (true) {
        val data = try {
            println("try receive...")
            receive()
        } catch (e: Throwable) {
            when (code) {
                SipCode.Ringing -> {
                    if (e is SocketTimeoutException) continue
                    throw e
                }
                else -> throw e
            }
        }
        val response = data.toSipResponse()
        method.check(response)
        code = response.top.code
        when (response.top.code) {
            SipCode.Trying -> {
                // ignored
            }
            SipCode.Ringing -> {
                // ignored
            }
            SipCode.Busy -> return
            else -> error("Code ${response.top.code} is not supported!")
        }
    }
}
