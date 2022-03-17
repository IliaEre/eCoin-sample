import org.litote.kmongo.reactivestreams.*
import org.litote.kmongo.coroutine.*
import org.litote.kmongo.eq

object MongoClient {

    private const val DATABASE = "wallets"
    private val client = KMongo.createClient().coroutine
    private val db = client.getDatabase(DATABASE)

    suspend fun findUserByUsername(username: String) = db.getCollection<User>().findOne(User::username eq username)

}
