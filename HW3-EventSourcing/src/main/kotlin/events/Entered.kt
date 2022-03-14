package events

import java.time.LocalDateTime

class Entered(
    accountId: ULong,
    val entryTime: LocalDateTime
) : Event(accountId)