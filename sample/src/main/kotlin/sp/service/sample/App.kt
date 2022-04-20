package sp.service.sample

import sp.kx.sip.foundation.entity.NetworkAddress
import sp.kx.sip.foundation.entity.SipUser
import sp.kx.sip.foundation.entity.TransportProtocol
import sp.kx.sip.foundation.entity.Via
import sp.kx.sip.foundation.entity.method.Register
import sp.kx.sip.implementation.entity.address
import sp.kx.sip.implementation.entity.sipUser
import sp.kx.sip.implementation.util.java.net.receive
import sp.kx.sip.implementation.util.java.net.send
import sp.kx.sip.implementation.util.notation
import sp.kx.sip.implementation.util.requireHeader
import sp.kx.sip.implementation.util.toAuthenticate
import sp.kx.sip.implementation.util.build
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.util.UUID
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

private var packet: DatagramPacket? = null

private fun getHostAddress(): String {
	return NetworkInterface.getNetworkInterfaces().toList()
		.flatMap { it.inetAddresses.toList() }
		.filterIsInstance<Inet4Address>()
		.filterNot { it.isLoopbackAddress }
		.single().hostAddress
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
	val authenticate = Register.Request.build(
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
	val executor = Executors.newFixedThreadPool(3)
	val stop = AtomicBoolean(false)
	executor.execute {
		try {
			DatagramSocket().use { socket ->
				socket.soTimeout = 5_000
				println("connect: ${rAddress.notation()}...")
				packet = DatagramPacket(ByteArray(0), 0, 0, InetAddress.getByName(rAddress.host), rAddress.port)
				socket.connect(InetAddress.getByName(rAddress.host), rAddress.port)
				try {
					process(
						socket = socket,
						rAddress = rAddress,
						fUser = sipUser(name = fName),
						password = fPassword
					)
				} finally {
					socket.disconnect()
					packet = null
				}
			}
		} finally {
			stop.set(true)
		}
	}
	while (!stop.get()) {
		//
	}
	executor.shutdown()
}
