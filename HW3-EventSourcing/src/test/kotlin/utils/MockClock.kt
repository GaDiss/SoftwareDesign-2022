package utils

import java.time.Clock
import java.time.Instant
import java.time.ZoneId

class MockClock(
    private var now: Instant = Instant.EPOCH,
    private val zone: ZoneId? = ZoneId.of("UTC")
) : Clock() {

    override fun instant(): Instant {
        return now
    }

    override fun getZone() = zone

    override fun withZone(zone: ZoneId?): Clock {
        return MockClock(now, zone)
    }

    fun plusSeconds(s: Long) {
        now = now.plusSeconds(s)
    }

    fun reset() {
        now = Instant.EPOCH
    }
}