package actors

import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.ReceiveTimeout
import akka.actor.UntypedAbstractActor
import searchers.QuerySearcher
import java.time.Duration

class MasterActor(
    private val searchers: List<QuerySearcher>,
    private val timeout: Duration,
    private val numOfResults: Int
) : UntypedAbstractActor() {
    private val actors = arrayListOf<ActorRef>()
    private lateinit var initiator: ActorRef
    private val resultsMap = HashMap<String, List<String>>()

    override fun onReceive(message: Any?) {
        when (message) {
            is Query -> {
                initiator = sender
                for (searcher in searchers) {
                    val queryActor = context.actorOf(
                        Props.create(QueryActor::class.java, searcher),
                        searcher.name
                    )
                    actors.add(queryActor)
                }
                actors.forEach { it.tell(message, self) }
                context.receiveTimeout = timeout
            }
            is Response -> resultsMap[sender.path().name()] = message.results.take(numOfResults)
            is ReceiveTimeout -> stop()
        }
        if (resultsMap.size == actors.size) stop()
    }

    private fun stop() {
        initiator.tell(resultsMap, self)
        actors.forEach { context.stop(it) }
        context.stop(self)
        context.system().terminate()
    }
}