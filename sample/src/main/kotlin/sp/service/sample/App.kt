package sp.service.sample

import sp.kx.sip.foundation.entity.NetworkAddress
import sp.kx.sip.foundation.entity.SipUser
import sp.kx.sip.foundation.entity.TransportProtocol
import sp.kx.sip.foundation.entity.Via
import sp.kx.sip.foundation.entity.method.Authenticate
import sp.kx.sip.foundation.entity.method.Register
import sp.kx.sip.implementation.entity.address
import sp.kx.sip.implementation.entity.sipUser
import sp.kx.sip.implementation.util.java.net.receive
import sp.kx.sip.implementation.util.java.net.send
import sp.kx.sip.implementation.util.notation
import sp.kx.sip.implementation.util.requireHeader
import sp.kx.sip.implementation.util.toAuthenticate
import sp.kx.sip.implementation.util.build
import sp.kx.sip.implementation.util.java.net.getHostAddress
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.util.UUID
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

private fun DatagramSocket.send(data: String) {
	println("\t-->\n$data")
	send(data.toByteArray(Charsets.UTF_8))
}

private fun process(
	socket: DatagramSocket,
	rAddress: NetworkAddress,
	fUser: SipUser,
	password: String
) {
	val via = Via(
		version = "2.0",
		protocol = TransportProtocol.UDP,
		address = address(host = getHostAddress(), port = socket.localPort),
		branch = "z9hG4bK" + UUID.randomUUID().toString()
	)
	val callId = UUID.randomUUID().toString()
	var number = 0
	val authenticate = Authenticate.Request.build(
		via = via,
		callId = callId,
		number = ++number,
		address = rAddress,
		user = fUser
	).let { request ->
		socket.send(request)
		val response = socket.receive(via)
		when (response.top.code) {
			401 -> response.requireHeader("WWW-Authenticate").toAuthenticate()
			else -> error("Unknown code ${response.top.code}")
		}
	}
	Register.Request.build(
		via = via,
		callId = callId,
		number = ++number,
		address = rAddress,
		user = fUser,
		authenticate = authenticate,
		password = password
	).also { request ->
		socket.send(request)
		val response = socket.receive(via)
		when (response.top.code) {
			200 -> TODO()
			else -> error("Unknown code ${response.top.code}")
		}
	}
}

fun main(args: Array<String>) {
	val arguments = args.single().split(",").associate {
		val array = it.split("=")
		check(array.size == 2)
		array[0] to array[1]
	}
	val rAddress = address(host = arguments["rh"]!!, port = arguments["rp"]!!.toInt())
	val fName = arguments["fn"]!!
	val fPassword = arguments["fp"]!!
	val tName = arguments["tn"]!!
//	val sServer = arguments["ss"]!!
	AppEnvironment(
		version = "2.0",
		protocol = TransportProtocol.UDP
	).run(
		rAddress = rAddress,
		fUser = sipUser(name = fName),
		tUser = sipUser(name = tName),
		password = fPassword
	)
}
