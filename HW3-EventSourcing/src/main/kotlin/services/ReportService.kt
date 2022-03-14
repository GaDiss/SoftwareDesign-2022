package services

import events.Entered
import events.Event
import events.Exited
import storage.EventStorage
import storage.EventSubscriber
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.TreeMap

class ReportService(
    storage: EventStorage
) : EventSubscriber {
    init {
        storage.addSubscriber(this)
    }

    private val lastVisits = HashMap<ULong, LocalDateTime>()
    private val dailyStats = TreeMap<LocalDate, Stats>()
    private val globalStats = Stats()

    override fun processNewEvent(event: Event) {
        when (event) {
            is Entered -> lastVisits[event.accountId] = event.entryTime
            is Exited -> {
                val entered = lastVisits[event.accountId] ?: return
                val exited = event.exitTime
                val duration = Duration.between(entered, exited)

                val dayOfVisit = entered.toLocalDate()
                dailyStats.computeIfAbsent(dayOfVisit) { Stats() }
                dailyStats[dayOfVisit]!!.addVisit(duration)
                globalStats.addVisit(duration)

                lastVisits.remove(event.accountId)
            }
        }
    }

    fun dailyStats() = dailyStats

    fun meanDuration() = globalStats.meanDuration()

    fun numberOfVisits() = globalStats.numberOfVisits

    fun meanDailyFrequency(from: LocalDate, to: LocalDate): Double {
        val toPlusOne = to.plusDays(1)
        assert(from.isBefore(toPlusOne))

        if (dailyStats.isEmpty()) return 0.0
        var totalNumberOfVisits = 0.0
        dailyStats.subMap(from, toPlusOne).forEach { (_, stats) -> totalNumberOfVisits += stats.numberOfVisits }

        return totalNumberOfVisits / (ChronoUnit.DAYS.between(from, toPlusOne))
    }
}

class Stats {
    private var duration: Duration = Duration.ZERO
    var numberOfVisits: Long = 0

    fun meanDuration(): Duration = duration.dividedBy(numberOfVisits)

    fun addVisit(duration: Duration) {
        numberOfVisits++
        this.duration += duration
    }
}
