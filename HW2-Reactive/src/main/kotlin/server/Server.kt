package server

import database.Database
import io.reactivex.netty.protocol.http.server.HttpServer
import model.Currency
import model.Product
import model.User
import rx.Observable

class Server(private val port: Int) {
    private val database = Database()

    fun start() {
        HttpServer
            .newServer(port)
            .start r@{ req, resp ->
                val path = req.decodedPath.substring(1)
                val params = req.queryParameters

                val response = when (path) {

                    "register" -> register(params)
                    "users" -> users()
                    "products" -> products(params)
                    "add_product" -> addProduct(params)
                    else -> Observable.just(PAGE_NOT_FOUND)
                }

                return@r resp.writeString(response)
            }
            .awaitShutdown()
    }

    private fun products(params: Map<String, MutableList<String>>): Observable<String> {
        return try {
            val uid = params["uid"]!![0].toInt()
            database
                .user(uid)
                .map { it.currency }
                .flatMap { currency ->
                    database
                        .products()
                        .map { "${it.toString(currency)}\n" }
                }
                .onErrorReturn { NO_SUCH_USER }
        } catch (e: Exception) {
            e.printStackTrace()
            Observable.just(NO_SUCH_USER)
        }
    }

    private fun addProduct(params: Map<String, MutableList<String>>): Observable<String> {
        return try {
            val name = params["name"]!![0]
            val price = params["price"]!![0].toDouble()
            database
                .addProduct(Product(name, price))
                .map { "Added product <name: $name>, <USD price: $price>" }
                .onErrorReturn { ADD_PRODUCT_ERROR }
        } catch (e: Exception) {
            e.printStackTrace()
            Observable.just(ADD_PRODUCT_ERROR)
        }
    }

    private fun register(params: MutableMap<String, MutableList<String>>): Observable<String> {
        return try {
            val id = params["id"]!![0].toInt()
            val currency = Currency.valueOf(params["cur"]!![0])
            database
                .addUser(User(id, currency))
                .map { "Created user <id: $id>, <currency: $currency>" }
                .onErrorReturn { REGISTER_ERROR }
        } catch (e: Exception) {
            e.printStackTrace()
            Observable.just(REGISTER_ERROR)
        }
    }

    private fun users(): Observable<String> =
        database
            .users()
            .map { "$it\n" }


    companion object {
        const val NO_SUCH_USER = "No such user"
        const val PAGE_NOT_FOUND = "Page not found"
        const val REGISTER_ERROR = "Failed to register user"
        const val ADD_PRODUCT_ERROR = "Failed to add a product"
    }
}