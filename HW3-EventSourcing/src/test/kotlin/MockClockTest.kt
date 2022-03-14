import org.junit.jupiter.api.Test
import utils.MockClock
import java.time.LocalDateTime
import kotlin.test.assertEquals

class MockClockTest {
    @Test
    fun mockClockTest() {
        val clock = MockClock()

        val now = LocalDateTime.now(clock)
        clock.plusSeconds(1)
        Thread.sleep(100)
        val nowPlus = LocalDateTime.now(clock)
        assertEquals(now.plusSeconds(1), nowPlus)
    }

    @Test
    fun startsAtZeroTest() {
        val clock = MockClock()

        val now = LocalDateTime.now(clock)
        clock.plusSeconds(60 * 60 * 24 - 1)
        val nowPlus = LocalDateTime.now(clock)
        assertEquals(now.toLocalDate(), nowPlus.toLocalDate())
    }
}