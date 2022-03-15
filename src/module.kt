import org.litote.kmongo.reactivestreams.*
import org.litote.kmongo.coroutine.*

object MongoClient {
    val client = KMongo.createClient().coroutine
    val database = client.getDatabase("wallets")

    suspend fun findUserByPublicKey(key: String) = database.getCollection<User>()



}