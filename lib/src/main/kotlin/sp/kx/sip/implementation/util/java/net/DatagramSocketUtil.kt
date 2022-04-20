package sp.kx.sip.implementation.util.java.net

import sp.kx.sip.foundation.entity.Via
import sp.kx.sip.foundation.entity.response.SipAbstractResponse
import sp.kx.sip.implementation.util.requireHeader
import sp.kx.sip.implementation.util.toSipResponse
import sp.kx.sip.implementation.util.toVia
import java.net.DatagramPacket
import java.net.DatagramSocket

fun DatagramSocket.send(buffer: ByteArray) {
    send(DatagramPacket(buffer, 0, buffer.size))
}

infix fun DatagramSocket.send(data: String) {
    println("\t-->\n$data")
    send(data.toByteArray(Charsets.UTF_8))
}

fun DatagramSocket.receive(buffer: ByteArray): DatagramPacket {
    val result = DatagramPacket(buffer, 0 ,buffer.size)
    receive(result)
    return result
}

fun DatagramSocket.receive(size: Int = 1024): DatagramPacket {
    return receive(buffer = ByteArray(size))
}

fun DatagramSocket.receive(expected: Via): SipAbstractResponse {
    while (true) {
        val data = String(receive().data)
        println("\t<--\n$data")
        val response = data.toSipResponse()
        val actual = response.requireHeader("Via").toVia()
        if (actual == expected) return response
    }
}
