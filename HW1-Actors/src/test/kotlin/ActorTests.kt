import actors.MasterActor
import actors.Query
import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.UntypedAbstractActor
import org.junit.FixMethodOrder
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.junit.runners.MethodSorters
import searchers.QuerySearcher
import searchers.StubSearcher
import stub.StubServer
import java.time.Duration
import kotlin.test.assertEquals



@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class ActorTests {
    @Test
    fun testSingleServer() {
        StubServer(PORT, listOf(GOOGLE)).use {
            runRequest(setOf(GOOGLE), listOf(StubSearcher(GOOGLE, "localhost:$PORT")))
        }
    }

    @Test
    fun testThreeServer() {
        StubServer(PORT, listOf(GOOGLE, YANDEX, BANG)).use {
            runRequest(
                setOf(GOOGLE, YANDEX, BANG), listOf(
                    StubSearcher(GOOGLE, "localhost:$PORT"),
                    StubSearcher(YANDEX, "localhost:$PORT"),
                    StubSearcher(BANG, "localhost:$PORT")
                )
            )
        }
    }

    @Test
    fun testTimeoutServer() {
        StubServer(PORT, listOf(GOOGLE), LONG_TIMEOUT).use {
            runRequest(setOf(), listOf(StubSearcher(GOOGLE, "localhost:$PORT")))
        }
    }

    @Test
    @Timeout(100)
    fun testNoWaitServer() {
        StubServer(PORT, listOf(GOOGLE), Duration.ofDays(999)).use {
            runRequest(setOf(), listOf(StubSearcher(GOOGLE, "localhost:$PORT")))
        }
    }

    @Test
    fun testShortTimeoutServer() {
        StubServer(PORT2, listOf(GOOGLE, BANG), LONG_TIMEOUT).use {
            StubServer(PORT, listOf(YANDEX), SHORT_TIMEOUT).use {
                runRequest(
                    setOf(YANDEX), listOf(
                        StubSearcher(GOOGLE, "localhost:$PORT"),
                        StubSearcher(YANDEX, "localhost:$PORT"),
                        StubSearcher(BANG, "localhost:$PORT")
                    )
                )
            }
        }
    }

    private fun runRequest(expected: Set<String>, searchers: List<QuerySearcher>) {
        StubServer(8080, listOf()).use {
            val system = ActorSystem.create("MySystem")

            val master = system.actorOf(
                Props.create(
                    MasterActor::class.java, searchers, DEFAULT_TIMEOUT, 5
                ), "master"
            )

            val asserter = system.actorOf(
                Props.create(
                    ResponseAsserter::class.java, expected
                ), "asserter"
            )

            val query = "query"
            master.tell(Query(query), asserter)


        }
    }

    class ResponseAsserter(private val expected: Set<String>) : UntypedAbstractActor() {
        override fun onReceive(message: Any?) {
            if (message is HashMap<*, *>) {
                assertEquals(
                    expected.size, message.size,
                    "Expected responses ${expected.size} servers"
                )
                for (e in message) {
                    assert(expected.contains(e.key)) { "Unexpected response" }
                    val results = e.value as List<*>
                    assertEquals(5, results.size)
                }
            }
        }
    }

    companion object {
        private val SHORT_TIMEOUT = Duration.ofMillis(500)
        private val DEFAULT_TIMEOUT = Duration.ofSeconds(1)
        private val LONG_TIMEOUT = Duration.ofMillis(1500)
        private const val PORT = 8088
        private const val PORT2 = 8091
        private const val GOOGLE = "google"
        private const val YANDEX = "yandex"
        private const val BANG = "bing"
    }
}
