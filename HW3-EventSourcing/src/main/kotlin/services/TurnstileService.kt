package services

import events.AccountCreated
import events.Entered
import events.Exited
import events.MembershipExtended
import storage.EventStorage
import java.time.Clock
import java.time.LocalDateTime

class TurnstileService(
    private val storage: EventStorage,
    private val clock: Clock
) {
    fun enter(accountId: ULong): Boolean {
        val now = LocalDateTime.now(clock)

        if (now.isBefore(expirationTime(accountId))) {
            storage.save(Entered(accountId, now))
            return true
        }
        return false
    }

    fun exit(accountId: ULong): Boolean {
        val now = LocalDateTime.now(clock)

        if (storage.accountExists(accountId)) {
            storage.save(Exited(accountId, now))
            return true
        }
        return false
    }

    private fun expirationTime(accountId: ULong): LocalDateTime {
        val events = storage.loadEvents(accountId)
        var expirationTime = LocalDateTime.MIN

        events?.forEach {
            when (it) {
                is AccountCreated -> expirationTime = it.creationTime
                is MembershipExtended -> expirationTime = it.expirationTime
            }
        }

        return expirationTime
    }
}