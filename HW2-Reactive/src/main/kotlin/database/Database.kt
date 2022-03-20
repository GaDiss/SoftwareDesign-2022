package database

import com.mongodb.client.model.Filters.eq
import com.mongodb.rx.client.MongoClient
import com.mongodb.rx.client.MongoClients
import com.mongodb.rx.client.Success
import model.Product
import model.User
import rx.Observable

class Database {
    private val client = createMongoClient()
    private val database = client.getDatabase("reactive")
    private val users = database.getCollection("users")
    private val products = database.getCollection("products")

    fun user(id: Int): Observable<User> =
        users.find(eq(User.ID, id)).first().map { doc -> User(doc) }
            .switchIfEmpty(Observable.error(Error("User $id not fount")))

    fun users(): Observable<User> =
        users.find().toObservable().map { doc -> User(doc) }

    fun addUser(user: User): Observable<Success> =
        users.insertOne(user.toDocument())

    fun products(): Observable<Product> =
        products.find().toObservable().map { doc -> Product(doc) }

    fun addProduct(product: Product): Observable<Success> =
        products.insertOne(product.toDocument())

    private fun createMongoClient(): MongoClient =
        MongoClients.create("mongodb://localhost:27017")
}