package sp.kx.sip.entity

object RFC3261 {
    /**
     * https://datatracker.ietf.org/doc/html/rfc3261#section-8.1.1.7
     *
     * The branch ID inserted by an element compliant with this specification MUST always begin with the characters "z9hG4bK".
     */
    fun branch(postfix: String): String {
        return "z9hG4bK$postfix"
    }
}
