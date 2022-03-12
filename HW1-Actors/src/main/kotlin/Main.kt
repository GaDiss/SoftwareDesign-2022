import actors.MasterActor
import actors.Query
import akka.actor.*
import searchers.StubSearcher
import stub.StubServer
import java.time.Duration

fun main(args: Array<String>) {
    val port = 8081
    val google = "google"
    val yandex = "yandex"
    val bing = "bing"

    StubServer(port, listOf(google, yandex, bing), Duration.ofMillis(500)).use {
        StubServer(8080, listOf()).use {
                val system = ActorSystem.create("MySystem")
                val searchers = listOf(
                    StubSearcher(google, "localhost:$port"),
                    StubSearcher(yandex, "localhost:$port"),
                    StubSearcher(bing, "localhost:$port")
                )
                val master = system.actorOf(Props.create(
                    MasterActor::class.java, searchers, Duration.ofSeconds(1), 5
                ), "master")
                val printer = system.actorOf(Props.create(ResponsePrinter::class.java), "printer")

                val query = args[0]

                master.tell(Query(query), printer)
            }}
}

class ResponsePrinter : UntypedAbstractActor() {
    override fun onReceive(message: Any?) {
        if (message is HashMap<*, *>) {
            for (e in message) {
                println("Results of ${e.key}:")
                for (r in e.value as List<*>) {
                    println("\t$r")
                }
            }
        }
    }
}