package sp.service.sample

import sp.kx.sip.entity.RFC3261
import sp.kx.sip.entity.method.sipInviteMethod
import sp.kx.sip.entity.method.sipRegisterMethod
import sp.kx.sip.foundation.entity.NetworkAddress
import sp.kx.sip.foundation.entity.SipUser
import sp.kx.sip.foundation.entity.TransportProtocol
import sp.kx.sip.foundation.entity.Via
import sp.kx.sip.foundation.entity.method.Authenticate
import sp.kx.sip.foundation.entity.method.Register
import sp.kx.sip.implementation.entity.address
import sp.kx.sip.implementation.entity.sipUser
import sp.kx.sip.implementation.util.build
import sp.kx.sip.implementation.util.java.net.getHostAddress
import sp.kx.sip.implementation.util.java.net.receive
import sp.kx.sip.implementation.util.java.net.send
import sp.kx.sip.implementation.util.requireHeader
import sp.kx.sip.implementation.util.toAuthenticate
import sp.kx.sip.util.RFC3261Util
import sp.kx.sip.util.java.net.request
import sp.kx.sip.util.sipEnvironment
import sp.kx.sip.util.toVia
import java.net.DatagramSocket
import java.net.InetAddress
import java.util.UUID

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
	val rAddress = address(
		host = arguments["rh"] ?: error("Remote host is unknown!"),
		port = arguments["rp"]?.toInt() ?: error("Remote port is unknown!")
	)
	val fName = arguments["fn"] ?: error("From user name is unknown!")
	val password = arguments["pw"] ?: error("From user password is unknown!")
	val tName = arguments["tn"] ?: error("To user name is unknown!")
//	val sServer = arguments["ss"]!!
//	AppEnvironment(
//		version = "2.0",
//		protocol = TransportProtocol.UDP
//	).run(
//		rAddress = rAddress,
//		fUser = sipUser(name = fName),
//		tUser = sipUser(name = tName),
//		password = fPassword
//	)
	val fUser = sipUser(name = fName)
	val tUser = sipUser(name = tName)
	DatagramSocket().use { socket ->
		socket.soTimeout = 5_000
		socket.connect(InetAddress.getByName(rAddress.host), rAddress.port)
		val environment = socket.sipEnvironment(version = "2.0")
		socket.request(
			sipRegisterMethod(
				via = environment.toVia(branch = RFC3261Util.newBranch()),
				callId = UUID.randomUUID().toString(),
				number = 1,
				address = rAddress,
				user = fUser
			),
			password = password
		)
		socket.request(
			sipInviteMethod(
				via = environment.toVia(branch = RFC3261Util.newBranch()),
				callId = UUID.randomUUID().toString(),
				number = 2,
				address = rAddress,
				fUser = fUser,
				tUser = tUser
			)
		)
	}
}
