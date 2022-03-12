package stub

import org.mockserver.integration.ClientAndServer
import org.mockserver.model.HttpRequest
import org.mockserver.model.HttpResponse
import org.mockserver.model.HttpStatusCode
import java.lang.Exception
import java.time.Duration

class StubServer(
    port: Int,
    hosts: List<String>,
    duration: Duration = Duration.ZERO,
    responseSize: Int = 10
) : AutoCloseable {
    private val stubServer = ClientAndServer.startClientAndServer(port)

    @Throws(Exception::class)
    override fun close() {
        stubServer.close()
        Thread.currentThread().interrupt()
    }

    init {
        for (host in hosts) {
            stubServer.`when`(
                HttpRequest.request()
                    .withMethod("GET")
                    .withPath("/$host")
            ).respond { httpRequest: HttpRequest ->
                Thread.sleep(duration.toMillis())
                val name = httpRequest.getFirstQueryStringParameter("q")
                val jsonResponse = (1..responseSize).joinToString(
                    separator = ",",
                    prefix = "{\"results\":[",
                    postfix = "]}"
                ) { i -> "\"$name $i\"" }
                HttpResponse.response()
                    .withStatusCode(HttpStatusCode.OK_200.code())
                    .withBody(jsonResponse)
            }
        }
    }
}
