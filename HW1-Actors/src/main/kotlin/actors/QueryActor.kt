package actors

import akka.actor.UntypedAbstractActor
import searchers.QuerySearcher


class QueryActor(private val searcher: QuerySearcher) : UntypedAbstractActor() {
    override fun onReceive(message: Any?) {
        if (message is Query) {
            val response = searcher.getTopResults(message.query)
            if (response != null) sender().tell(response, self)
        }
    }
}

class Query(val query: String)

class Response(val results: List<String>)