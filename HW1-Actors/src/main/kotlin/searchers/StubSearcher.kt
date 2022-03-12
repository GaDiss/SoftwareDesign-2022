package searchers


import actors.Response
import com.google.gson.Gson
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class StubSearcher(override val name: String, private val host: String) : QuerySearcher {
    override fun getTopResults(query: String): Response {
        val encQuery = URLEncoder.encode(query, Charsets.UTF_8)
        val uri = URI.create("http://$host/$name?q=$encQuery")

        val client = HttpClient.newBuilder().build()
        val request = HttpRequest.newBuilder().uri(uri).build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString()).body().intern()

        return Gson().fromJson(response, Response::class.java)
    }
}