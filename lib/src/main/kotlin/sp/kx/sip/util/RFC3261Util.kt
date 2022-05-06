package sp.kx.sip.util

import sp.kx.sip.entity.RFC3261
import java.util.UUID

object RFC3261Util {
    fun newBranch(): String {
        return RFC3261.branch(postfix = UUID.randomUUID().toString())
    }
}
