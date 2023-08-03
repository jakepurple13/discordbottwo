import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class Network {
    private val json = Json {
        isLenient = true
        prettyPrint = true
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    private val client by lazy {
        HttpClient {
            install(ContentNegotiation) { json(json) }
        }
    }

    suspend fun loadRandomImage() = runCatching {
        client.get("https://api.catboys.com/img")
            .bodyAsText()
            .let { json.decodeFromString<CatboyModel>(it) }
            .let { NekoImage(it.url, it.artist) }
    }
}

@Serializable
data class CatboyModel(
    val url: String,
    val artist: String,
    @SerialName("artist_url")
    val artistUrl: String,
    @SerialName("source_url")
    val sourceUrl: String,
    val error: String,
)