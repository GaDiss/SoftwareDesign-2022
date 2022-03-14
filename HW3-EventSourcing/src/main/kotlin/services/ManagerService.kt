package services

import events.AccountCreated
import events.Entered
import events.MembershipExtended
import storage.EventStorage
import java.time.Clock
import java.time.Duration
import java.time.LocalDateTime

class ManagerService(
    private val storage: EventStorage,
    private val clock: Clock
) {
    fun createAccount(): ULong {
        val accountId = storage.getAndIncrementAccountCount()
        storage.save(AccountCreated(accountId, LocalDateTime.now(clock)))
        return accountId
    }

    fun extendMembership(accountId: ULong, subscriptionDuration: Duration): Boolean {
        val expirationTime = LocalDateTime.now(clock) + subscriptionDuration
        if (storage.accountExists(accountId)) {
            storage.save(MembershipExtended(accountId, expirationTime))
            return true
        }
        return false
    }

    fun collectAccountInfo(accountId: ULong): Account? {
        val events = storage.loadEvents(accountId)
        if (events.isNullOrEmpty() || events[0] !is AccountCreated) return null

        lateinit var creationTime: LocalDateTime
        lateinit var expirationTime: LocalDateTime
        var lastVisited: LocalDateTime? = null
        var numberOfVisits = 0

        events.forEach {
            when (it) {
                is AccountCreated -> {
                    creationTime = it.creationTime
                    expirationTime = it.creationTime
                }
                is MembershipExtended -> expirationTime = it.expirationTime
                is Entered -> {
                    lastVisited = it.entryTime
                    numberOfVisits++
                }
            }
        }

        return Account(accountId, creationTime, expirationTime, lastVisited, numberOfVisits)
    }
}

class Account(
    val accountId: ULong,
    val creationTime: LocalDateTime,
    val expirationTime: LocalDateTime,
    val lastVisited: LocalDateTime?,
    val numberOfVisits: Int
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Account

        if (accountId != other.accountId) return false
        if (creationTime != other.creationTime) return false
        if (expirationTime != other.expirationTime) return false
        if (lastVisited != other.lastVisited) return false
        if (numberOfVisits != other.numberOfVisits) return false

        return true
    }

    override fun hashCode(): Int {
        var result = accountId.hashCode()
        result = 31 * result + creationTime.hashCode()
        result = 31 * result + expirationTime.hashCode()
        result = 31 * result + (lastVisited?.hashCode() ?: 0)
        result = 31 * result + numberOfVisits
        return result
    }
}
