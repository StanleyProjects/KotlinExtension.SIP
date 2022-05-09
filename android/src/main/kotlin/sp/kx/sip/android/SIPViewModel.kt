package sp.kx.sip.android

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sp.kx.sip.entity.SipEnvironment
import sp.kx.sip.entity.method.SipMethod
import sp.kx.sip.entity.method.sipInviteMethod
import sp.kx.sip.entity.method.sipRegisterMethod
import sp.kx.sip.foundation.entity.NetworkAddress
import sp.kx.sip.foundation.entity.SipCode
import sp.kx.sip.foundation.entity.SipUser
import sp.kx.sip.foundation.entity.method.check
import sp.kx.sip.implementation.util.SipRequestBuilder
import sp.kx.sip.implementation.util.by
import sp.kx.sip.implementation.util.from
import sp.kx.sip.implementation.util.to
import sp.kx.sip.implementation.util.toSipResponse
import sp.kx.sip.util.RFC3261Util
import sp.kx.sip.util.java.net.request
import sp.kx.sip.util.sipEnvironment
import sp.kx.sip.util.toBody
import sp.kx.sip.util.toVia
import java.net.DatagramSocket
import java.net.InetAddress
import java.util.UUID
import sp.kx.sip.util.java.net.send
import sp.kx.sip.util.java.net.receive
import java.net.SocketTimeoutException

object SIPViewModel {
    private val scope = CoroutineScope(Job() + Dispatchers.Main)
//    private val scope = CoroutineScope(Dispatchers.IO)
    private var socket: DatagramSocket? = null
    private var environment: SipEnvironment? = null
    private var authorized: Authorized? = null
    private var called: SipUser? = null

    fun enter(address: NetworkAddress, user: SipUser, password: String, onSuccess: () -> Unit) {
        check(socket == null)
        scope.launch {
            withContext(Dispatchers.IO) {
                val socket = DatagramSocket().also { socket = it }
                socket.soTimeout = 5_000
                socket.connect(InetAddress.getByName(address.host), address.port)
                val environment = socket.sipEnvironment(version = "2.0").also { environment = it }
                socket.request(
                    sipRegisterMethod(
                        via = environment.toVia(branch = RFC3261Util.newBranch()),
                        callId = UUID.randomUUID().toString(),
                        number = 1,
                        address = address,
                        user = user
                    ),
                    password = password
                )
            }
            authorized = Authorized(address = address, user = user)
            onSuccess()
        }
    }

    fun getAuthorized(): Authorized? {
        return authorized
    }

    fun getCalled(): SipUser? {
        return called
    }

    fun exit(onSuccess: () -> Unit) {
        scope.launch {
            withContext(Dispatchers.IO) {
                requireNotNull(socket).use {
                    it.disconnect()
                }
                socket = null
                environment = null
                authorized = null
            }
            onSuccess()
        }
    }

    fun cancel() {
        requireNotNull(socket)
        requireNotNull(environment)
        requireNotNull(authorized)
        val called = requireNotNull(called)
        this.called = null
    }

    private suspend fun receive(
        method: SipMethod,
        onCode: (Int) -> Unit,
        onFinish: () -> Unit
    ) {
        val socket = requireNotNull(socket)
        var code: Int? = null
        while (true) {
            if (called == null) {
                val authorized = requireNotNull(authorized)
                val body = SipRequestBuilder(
                    method = "BYE",
                    version = method.via.version,
//                    uri = "sip:${tUser.name}@${method.via.address.host}"
                    uri = "sip:${method.via.address.host}"
                ).build {
                    by(method.via)
                    addHeader(key = "Call-ID", value = method.callId)
                    addHeader(key = "CSeq", value = "${method.cs.number + 1} BYE")
                    from(user = authorized.user, host = method.via.address.host)
//                    to(user = tUser, host = address.host)
                }
                socket.send(body)
                val data = withContext(Dispatchers.IO) {
                    socket.receive()
                }
                val response = data.toSipResponse()
//                method.check(response) // todo
                when (response.top.code) {
                    SipCode.OK -> return
                }
                error("Code ${response.top.code} is not supported!")
            }
            val data = try {
                println("try receive...")
                withContext(Dispatchers.IO) {
                    socket.receive()
                }
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
            onCode(response.top.code)
            when (response.top.code) {
                SipCode.Trying, SipCode.Ringing -> {
                    // ignored
                }
                SipCode.Unavailable, SipCode.Busy -> {
                    called = null
                    onFinish()
                    return
                }
                else -> error("Code ${response.top.code} is not supported!")
            }
        }
    }

    fun call(user: SipUser, onCalled: (SipUser) -> Unit, onCode: (Int) -> Unit, onFinish: () -> Unit) {
        scope.launch {
            val socket = requireNotNull(socket)
            val environment = requireNotNull(environment)
            val authorized = requireNotNull(authorized)
            val method = withContext(Dispatchers.IO) {
                sipInviteMethod(
                    via = environment.toVia(branch = RFC3261Util.newBranch()),
                    callId = UUID.randomUUID().toString(),
                    number = 1,
                    address = environment.address,
                    fUser = authorized.user,
                    tUser = user
                )
            }
            called = user
            onCalled(user)
            withContext(Dispatchers.IO) {
                socket.send(method.toBody(emptyList()))
            }
            receive(method, onCode, onFinish)
        }
    }
}
