package events

import java.time.LocalDateTime

class Exited(
    accountId: ULong,
    val exitTime: LocalDateTime
) : Event(accountId)