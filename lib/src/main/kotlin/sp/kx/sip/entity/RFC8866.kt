package sp.kx.sip.entity

/**
 * https://www.rfc-editor.org/rfc/rfc8866.html
 *
 * An example SDP description is:
 *
 * v=0
 * o=jdoe 3724394400 3724394405 IN IP4 198.51.100.1
 * s=Call to John Smith
 * i=SDP Offer #1
 * u=http://www.jdoe.example.com/home.html
 * e=Jane Doe <jane@jdoe.example.com>
 * p=+1 617 555-6011
 * c=IN IP4 198.51.100.1
 * t=0 0
 * m=audio 49170 RTP/AVP 0
 * m=audio 49180 RTP/AVP 0
 * m=video 51372 RTP/AVP 99
 * c=IN IP6 2001:db8::2
 * a=rtpmap:99 h263-1998/90000
 */
object RFC8866 {
    fun getSessionDescription(originatorId: String, sessionId: Int, version: Int, connectionInformation: String): String {
        return "$originatorId $sessionId $version $connectionInformation"
    }

    fun getConnectionInformation(protocol: String, addressType: String, host: String): String {
        return "$protocol $addressType $host"
    }
}
