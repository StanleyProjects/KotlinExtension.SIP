package sp.service.sample

import sp.kx.sip.foundation.entity.AuthorizationDigest
import sp.kx.sip.foundation.entity.NetworkAddress
import sp.kx.sip.foundation.entity.SipAuthenticate
import sp.kx.sip.foundation.entity.SipUser
import sp.kx.sip.foundation.entity.TransportProtocol
import sp.kx.sip.foundation.entity.Via
import sp.kx.sip.foundation.entity.request.SipRequestBuilder
import sp.kx.sip.foundation.entity.request.SipRequestRegister
import sp.kx.sip.foundation.entity.request.by
import sp.kx.sip.foundation.entity.request.contact
import sp.kx.sip.foundation.entity.request.from
import sp.kx.sip.foundation.entity.request.getVia
import sp.kx.sip.foundation.entity.request.to
import sp.kx.sip.foundation.entity.request.via
import sp.kx.sip.foundation.entity.response.SipAbstractResponse
import sp.kx.sip.implementation.entity.address
import sp.kx.sip.implementation.entity.sipUser
import sp.kx.sip.implementation.util.digest
import sp.kx.sip.implementation.util.notation
import sp.kx.sip.implementation.util.requireHeader
import sp.kx.sip.implementation.util.toAuthenticate
import sp.kx.sip.implementation.util.toSipResponse
import sp.kx.sip.implementation.util.toVia
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.security.MessageDigest
import java.util.UUID
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.pow

private var packet: DatagramPacket? = null

private fun DatagramSocket.send(data: ByteArray) {
	val packet = requireNotNull(packet)
	packet.data = data
	send(packet)
}

private infix fun DatagramSocket.send(data: String) {
	println("\t-->\n$data")
	send(data.toByteArray(Charsets.UTF_8))
}

fun DatagramSocket.receive(buffer: ByteArray): DatagramPacket {
	val packet = requireNotNull(packet)
	packet.data = buffer
	receive(packet)
	return packet
}

fun DatagramSocket.receive(size: Int = 1024): DatagramPacket {
	return receive(buffer = ByteArray(size))
}

private fun getHostAddress(): String {
	return NetworkInterface.getNetworkInterfaces().toList()
		.flatMap { it.inetAddresses.toList() }
		.filterIsInstance<Inet4Address>()
		.filterNot { it.isLoopbackAddress }
		.single().hostAddress
}

private fun Int.toTwoBytes(): ByteArray {
	val result = ByteArray(2)
	if (this > 2.0.pow(31)) error("Integer value $this > 2^31")
	if (this < 0) error("Integer value $this < 0")
	result[0] = ((this shr 8) and 0xFF).toByte()
	result[1] = (this and 0xFF).toByte()
	return result
}

private fun ByteArray.twoToInteger(): Int {
	check(size > 1)
	val f = (this[0].toInt() and 0xFF) shl 8
	val s = this[1].toInt() and 0xFF
	return f + s
}

private fun Byte.toInteger(): Int {
	return toInt() and 0xFF
}

private fun parseAddress(array: ByteArray): Pair<String, Int> {
	if (array.size < 8) error("Data array too short")
	val family = array[1].toInteger()
	if (family != 0x01) error("Family $family is not supported")
	val port = ByteArray(16).also {
		System.arraycopy(array, 2, it, 0, 2)
	}.twoToInteger()
	val address = listOf(4, 5, 6, 7)
		.joinToString(separator = ".") {
			array[it].toInteger().toString()
		}
	return address to port
}

fun ByteArray.getMappedAddress(): Pair<String, Int> {
	val lengthArray = ByteArray(2).also {
		System.arraycopy(this, 2, it, 0, 2)
	}
	var offset = 20
	var length = lengthArray.twoToInteger()
	while (length > 0) {
		val tmpArray = ByteArray(length).also {
			System.arraycopy(this, offset, it, 0, length)
		}
		val type = ByteArray(2).also {
			System.arraycopy(tmpArray, 0, it, 0, 2)
		}.twoToInteger()
		val valueArrayLength = ByteArray(2).also {
			System.arraycopy(tmpArray, 2, it, 0, 2)
		}.twoToInteger()
		val valueArray = ByteArray(valueArrayLength).also {
			System.arraycopy(tmpArray, 4, it, 0, it.size)
		}
		val d = valueArray.size + 4
		when (type) {
			0x0001 -> {
				return parseAddress(valueArray)
			}
		}
		length -= d
		offset += d
	}
	TODO()
}

private infix fun DatagramSocket.request(request: String): SipAbstractResponse {
	println("\t-->\n$request")
	send(data = request.toByteArray(Charsets.UTF_8))
	val response = String(receive().data)
	println("\t<--\n$response")
	return response.toSipResponse()
}

private fun process(
	socket: DatagramSocket,
	rAddress: NetworkAddress,
	fUser: SipUser,
	password: String
) {
	val version = "2.0"
	val protocol = TransportProtocol.UDP
	val via = Via(
		version = version,
		protocol = protocol,
		address = address(host = getHostAddress(), port = socket.localPort),
		branch = "z9hG4bK" + UUID.randomUUID().toString()
	)
	val callId = UUID.randomUUID().toString()
	var number = 0
	val method = "REGISTER"
	val tag = UUID.randomUUID().toString()
	val authenticate = SipRequestBuilder(method = method, version = via.version, address = rAddress).build {
		by(via)
		addHeader(key = "Call-ID", value = callId)
		addHeader(key = "CSeq", value = "${++number} $method")
		from(user = fUser, host = rAddress.host, tag = tag)
		to(user = fUser, host = rAddress.host)
		contact(user = fUser, address = via.address)
	}.let { request ->
		socket.send(request)
		val response = socket.receive(via)
		when (response.top.code) {
			401 -> response.requireHeader("WWW-Authenticate").toAuthenticate()
			else -> error("Unknown code ${response.top.code}")
		}
	}
	val digest = authenticate.digest(
		uri = "sip:${rAddress.notation()}",
		method = method,
		name = fUser.name,
		password = password
	)
	SipRequestBuilder(method = method, version = via.version, address = rAddress).build {
		by(via)
		addHeader(key = "Call-ID", value = callId)
		addHeader(key = "CSeq", value = "${++number} $method")
		from(user = fUser, host = rAddress.host, tag = tag)
		to(user = fUser, host = rAddress.host)
		contact(user = fUser, address = via.address)
		by(digest)
	}.let { request ->
		socket.send(request)
		val response = socket.receive(via)
		when (response.top.code) {
			200 -> TODO()
			else -> error("Unknown code ${response.top.code}")
		}
	}
}

private fun register(
	socket: DatagramSocket,
	via: Via,
	callId: String,
	number: Int,
	rAddress: NetworkAddress,
	fUser: SipUser,
	digest: String
): SipAbstractResponse {
	val method = "REGISTER"
//	val lAddress = address(host = getHostAddress(), port = socket.localPort)
	val request = SipRequestBuilder(method = method, version = via.version, address = rAddress).build {
		by(via)
		addHeader(key = "Call-ID", value = callId)
		addHeader(key = "CSeq", value = "$number $method")
		from(user = fUser, host = rAddress.host, tag = UUID.randomUUID().toString())
		to(user = fUser, host = rAddress.host)
//		contact(user = fUser, address = lAddress)
		addHeader(key = "Authorization", value = digest)
	}
	socket.send(request)
	return socket.receive(via)
}

private fun register(
	socket: DatagramSocket,
	via: Via,
	callId: String,
	number: Int,
	rAddress: NetworkAddress,
	fUser: SipUser
): SipAbstractResponse {
	val method = "REGISTER"
	val request = SipRequestBuilder(method = method, version = via.version, address = rAddress).build {
		by(via)
		addHeader(key = "Call-ID", value = callId)
		addHeader(key = "CSeq", value = "$number $method")
		from(user = fUser, host = rAddress.host, tag = UUID.randomUUID().toString())
		to(user = fUser, host = rAddress.host)
//		contact(user = fUser, address = lAddress)
	}
//	val request = SipRequestRegister(
//		version = version,
//		protocol = protocol,
//		number = number,
//		rAddress = rAddress,
//		lAddress = address(host = getHostAddress(), port = socket.localPort),
//		branch = "z9hG4bK" + UUID.randomUUID().toString(),
//		callId = callId,
//		user = fUser,
//		tag = UUID.randomUUID().toString()
//	)
//	socket.send(request.toBody())
	socket.send(request)
	return socket.receive(via)
}

private fun DatagramSocket.receive(expected: Via): SipAbstractResponse {
	while (true) {
		val data = String(receive().data)
		println("\t<--\n$data")
		val response = data.toSipResponse()
		val actual = response.requireHeader("Via").toVia()
		if (actual == expected) return response
	}
}

private fun register(
	socket: DatagramSocket,
	rAddress: NetworkAddress,
	fUser: SipUser
): SipAbstractResponse {
	val lAddress = address(host = getHostAddress(), port = socket.localPort)
	val version = "2.0"
	val protocol = TransportProtocol.UDP
	val number = 1
	val branch = "z9hG4bK" + UUID.randomUUID().toString()
	val callId = UUID.randomUUID().toString()
	val tag = UUID.randomUUID().toString()
	val request = SipRequestRegister(
		version = version,
		protocol = protocol,
		number = number,
		rAddress = rAddress,
		lAddress = lAddress,
		branch = branch,
		callId = callId,
		user = fUser,
		tag = tag
	)
	socket.send(request.toBody())
	while (true) {
		val data = String(socket.receive().data)
		println("\t<--\n$data")
		val response = data.toSipResponse()
		val via = response.requireHeader("Via").toVia()
		if (via == request.getVia()) {
			return response
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
