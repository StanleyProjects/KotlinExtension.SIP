package sp.service.sample

import sp.kx.functional.subject.PublishSubject
import sp.kx.functional.subject.Subject
import sp.kx.functional.subscription.Subscription
import sp.kx.sip.foundation.entity.NetworkAddress
import sp.kx.sip.foundation.entity.SipCode
import sp.kx.sip.foundation.entity.SipUser
import sp.kx.sip.foundation.entity.TransportProtocol
import sp.kx.sip.foundation.entity.Via
import sp.kx.sip.foundation.entity.method.Authenticate
import sp.kx.sip.foundation.entity.method.Invite
import sp.kx.sip.foundation.entity.method.Register
import sp.kx.sip.foundation.entity.response.SipAbstractResponse
import sp.kx.sip.implementation.entity.address
import sp.kx.sip.implementation.util.build
import sp.kx.sip.implementation.util.java.net.getHostAddress
import sp.kx.sip.implementation.util.java.net.receive
import sp.kx.sip.implementation.util.java.net.send
import sp.kx.sip.implementation.util.notation
import sp.kx.sip.implementation.util.requireHeader
import sp.kx.sip.implementation.util.toAuthenticate
import sp.kx.sip.implementation.util.toCommandSequence
import sp.kx.sip.implementation.util.toSipResponse
import sp.kx.sip.implementation.util.toVia
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.SocketTimeoutException
import java.util.UUID
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

class AppEnvironment(
    private val version: String,
    private val protocol: TransportProtocol
) {
    private sealed interface In {
        class SendRequest(val data: String) : In
        object ExpectResponse : In
    }

    private sealed interface Out {
        class OnResponse(val response: SipAbstractResponse) : Out
    }

    companion object {
        private val requester: Subject<In> = PublishSubject()

        private fun DatagramSocket.send(data: String) {
            println("\t-->\n$data")
            send(data.toByteArray(Charsets.UTF_8))
        }
    }

    private enum class State {
        AUTHENTICATE, AUTHORIZED
    }

    private enum class CallState {
        NONE, TRYING, RINGING
    }

    private var executor: ExecutorService? = null
    private val expected = AtomicInteger(0)
    private var lAddress: NetworkAddress? = null
    private var rAddress: NetworkAddress? = null
    private val finish = AtomicBoolean(false)
    private var state = State.AUTHENTICATE
    private var callState = CallState.NONE
    private var fUser: SipUser? = null
    private var tUser: SipUser? = null
    private var password: String? = null
    private var number = 0

    private fun onResponse(response: SipAbstractResponse) {
        when (state) {
            State.AUTHENTICATE -> {
                val cs = response.requireHeader("CSeq").toCommandSequence()
                when (cs.method) {
                    Authenticate.method -> {
                        when (response.top.code) {
                            SipCode.Unauthorized -> {
                                val password = password
                                if (password == null) {
                                    TODO("Unauthorized")
                                } else {
                                    val authenticate = response.requireHeader("WWW-Authenticate").toAuthenticate()
                                    val data = Register.Request.build(
                                        via = response.requireHeader("Via").toVia(),
                                        callId = response.requireHeader("Call-ID"),
                                        number = ++number,
                                        address = requireNotNull(rAddress),
                                        user = requireNotNull(fUser),
                                        authenticate = authenticate,
                                        password = password
                                    )
                                    this.password = null
                                    requester next In.SendRequest(data)
                                    requester next In.ExpectResponse
                                }
                            }
                            SipCode.OK -> {
                                state = State.AUTHORIZED
                                val data = Invite.Request.build(
                                    via = newVia(),
                                    callId = UUID.randomUUID().toString(),
                                    number = ++number,
                                    address = requireNotNull(rAddress),
                                    fUser = requireNotNull(fUser),
                                    tUser = requireNotNull(tUser)
                                )
                                requester next In.SendRequest(data)
                                requester next In.ExpectResponse
                            }
                            else -> error("Code ${response.top.code} is not supported!")
                        }
                    }
                    else -> error("Method ${cs.method} is not supported!")
                }
            }
            State.AUTHORIZED -> {
                val cs = response.requireHeader("CSeq").toCommandSequence()
                when (cs.method) {
                    Invite.method -> {
                        when (response.top.code) {
                            SipCode.Trying -> {
                                callState = CallState.TRYING
                                requester next In.ExpectResponse
                            }
                            SipCode.Ringing -> {
                                callState = CallState.RINGING
                                requester next In.ExpectResponse
                            }
                            SipCode.Busy -> {
                                callState = CallState.NONE
                                finish.set(true)
                            }
                            SipCode.OK -> {
                                TODO()
                            }
                            else -> error("Code ${response.top.code} is not supported!")
                        }
                    }
                }
            }
            else -> error("State $state is not supported!")
        }
    }

    private fun newVia(): Via {
        return Via(
            version = version,
            protocol = protocol,
            address = requireNotNull(lAddress),
            branch = "z9hG4bK" + UUID.randomUUID().toString()
        )
    }

    fun run(
        rAddress: NetworkAddress,
        fUser: SipUser,
        password: String,
        tUser: SipUser
    ) {
        val stop = AtomicBoolean(false)
        val executor = Executors.newFixedThreadPool(2)
        this.executor = executor
        executor.execute {
            try {
                DatagramSocket().use { socket ->
                    val subject: Subject<Out.OnResponse> = PublishSubject()
                    socket.soTimeout = 5_000
                    println("connect: ${rAddress.notation()}...")
                    socket.connect(InetAddress.getByName(rAddress.host), rAddress.port)
                    this.fUser = fUser
                    this.tUser = tUser
                    this.password = password
                    lAddress = address(host = getHostAddress(), port = socket.port)
                    this.rAddress = rAddress
                    val subscriptions = mutableListOf<Subscription>()
                    try {
                        subscriptions + requester.subscribe(
                            Subject.action {
                                when (it) {
                                    In.ExpectResponse -> {
                                        expected.incrementAndGet()
                                    }
                                    is In.SendRequest -> socket.send(it.data)
                                }
                            }
                        )
                        subscriptions + subject.subscribe(
                            Subject.action {
                                onResponse(it.response)
                            }
                        )
                        val data = Authenticate.Request.build(
                            via = newVia(),
                            callId = UUID.randomUUID().toString(),
                            number = ++number,
                            address = rAddress,
                            user = fUser
                        )
                        requester next In.SendRequest(data)
                        requester next In.ExpectResponse
                        while (!finish.get()) {
                            while (expected.get() > 0) {
                                println("try receive...")
                                val packet = try {
                                    socket.receive()
                                } catch (e: Throwable) {
                                    when (callState) {
                                        CallState.RINGING -> {
                                            if (e is SocketTimeoutException) continue
                                            throw e
                                        }
                                        else -> throw e
                                    }
                                }
                                val data = String(packet.data)
                                println("\t<--\n$data")
                                expected.decrementAndGet()
                                subject next Out.OnResponse(data.toSipResponse())
                            }
                        }
                    } finally {
                        subscriptions.forEach {
                            it.unsubscribe()
                        }
                        subscriptions.clear()
                        socket.disconnect()
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
}
