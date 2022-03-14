package storage

import events.Event

interface EventSubscriber {
    fun processNewEvent(event: Event)
}