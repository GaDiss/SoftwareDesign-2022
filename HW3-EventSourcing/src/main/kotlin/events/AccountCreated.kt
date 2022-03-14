package events

import java.time.LocalDateTime

class AccountCreated(
    accountId: ULong,
    val creationTime: LocalDateTime
) : Event(accountId)