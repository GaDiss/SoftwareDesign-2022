import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import services.*
import storage.EventStorage
import utils.MockClock
import java.time.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ServicesTest {
    private var storage = EventStorage()
    private var clock = MockClock()
    private var manager = ManagerService(storage, clock)
    private var turnstile = TurnstileService(storage, clock)
    private var reporter = ReportService(storage)

    @BeforeEach
    fun init() {
        clock.reset()
    }

    private fun addNewAccount(seconds: Long): ULong {
        val id = manager.createAccount()
        assertTrue { manager.extendMembership(id, Duration.ofSeconds(seconds)) }
        return id
    }

    @Test
    fun oneAccountTest() {
        val created = LocalDateTime.now(clock)
        val id = addNewAccount(2)

        assertTrue { turnstile.enter(id) }
        clock.plusSeconds(1)
        assertTrue { turnstile.exit(id) }
        clock.plusSeconds(2)
        assertFalse { turnstile.enter(id) }

        assertTrue { manager.extendMembership(id, Duration.ofSeconds(2)) }
        assertTrue { turnstile.enter(id) }
        val lastVisited = LocalDateTime.now(clock)
        val expiration = lastVisited.plusSeconds(2)
        clock.plusSeconds(3)
        assertTrue { turnstile.exit(id) }

        assertEquals(
            manager.collectAccountInfo(id)!!,
            Account(id, created, expiration, lastVisited, 2)
        )

        assertEquals(reporter.meanDuration(), Duration.ofSeconds(2))
        assertEquals(reporter.meanDailyFrequency(created.toLocalDate(), created.toLocalDate()), 2.0)
    }

    @Test
    fun intruderAmongUsTest() {
        assertFalse { turnstile.enter(0u) }
        assertFalse { turnstile.exit(1u) }

        val id = manager.createAccount()
        assertFalse { turnstile.enter(id) }
        assertTrue { turnstile.exit(id) }
        assertFalse { turnstile.exit(id + 1u) }

        assertEquals(reporter.numberOfVisits(), 0)
    }

    @Test
    fun twoAccountsTest() {
        val start = LocalDateTime.now(clock)

        val id1 = addNewAccount(60)

        assertTrue { turnstile.enter(id1) }
        clock.plusSeconds(30)

        val id2 = addNewAccount(60)

        assertTrue { turnstile.enter(id2) }
        clock.plusSeconds(10)
        assertTrue(turnstile.exit(id1))
        clock.plusSeconds(70)
        assertTrue(turnstile.exit(id2))

        assertEquals(reporter.meanDuration(), Duration.ofSeconds(60))
        assertEquals(reporter.meanDailyFrequency(start.toLocalDate(), start.toLocalDate()), 2.0)
    }

    @Test
    fun oneWeekLoadTest() {
        val startDay = LocalDateTime.now(clock).toLocalDate()
        val ids = arrayListOf<ULong>(0u, 0u, 0u)
        val month = Duration.ofDays(30).toSeconds()
        val day = Duration.ofDays(1).toSeconds()

        for (i in 0..2) ids[i] = addNewAccount(month)

        for (d in 0..5) {
            ids[d % 3] = addNewAccount(month)

            for (i in 1..10) {
                assertTrue { turnstile.enter(ids[i % 3]) }
                clock.plusSeconds(10)
                assertTrue(turnstile.exit(ids[i % 3]))
            }

            clock.plusSeconds(day)
        }

        val endDay = startDay.plusDays(6)

        assertEquals(reporter.meanDailyFrequency(startDay, endDay), 60.0 / 7.0)
        val dailyStats = reporter.dailyStats()
        for (i in 0L .. 5L) {
            val today = startDay.plusDays(i)
            assertEquals(dailyStats[today]!!.numberOfVisits, 10)
            assertEquals(dailyStats[today]!!.meanDuration(), Duration.ofSeconds(10))
        }

        assertFalse(dailyStats.containsKey(endDay))
    }
}