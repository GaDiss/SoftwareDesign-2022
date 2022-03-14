package storage

import events.Event

class EventStorage {
    private val eventStorage = HashMap<ULong, MutableList<Event>>()
    private var accountCount: ULong = 0u
    private val subscribers = mutableListOf<EventSubscriber>()

    fun getAndIncrementAccountCount() = accountCount++

    fun save(event: Event) {
        val accountId = event.accountId

        eventStorage.computeIfAbsent(accountId) { mutableListOf() }
        eventStorage[accountId]!!.add(event)

        subscribers.forEach { it.processNewEvent(event) }
    }

    fun loadEvents(accountId: ULong) = eventStorage[accountId]

    fun accountExists(accountId: ULong) = eventStorage.containsKey(accountId)

    fun addSubscriber(subscriber: EventSubscriber) {
        eventStorage.forEach { (_, eventList) ->
            eventList.forEach {
                subscriber.processNewEvent(it)
            }
        }
        subscribers.add(subscriber)
    }
}