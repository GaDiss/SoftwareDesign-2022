package searchers

import actors.Response

interface QuerySearcher {
    val name: String

    fun getTopResults(query: String): Response
}