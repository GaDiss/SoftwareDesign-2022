package events

import java.time.LocalDateTime

class MembershipExtended(
    accountId: ULong,
    val expirationTime: LocalDateTime
) : Event(accountId)