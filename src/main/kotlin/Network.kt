import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.delay
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

    suspend fun pixrayLoad(token: String): Result<NekoImage> {
        fun HttpRequestBuilder.setup() {
            header("Authorization", "Token $token")
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            setBody(
                PixrayModel(
                    version = "ac732df83cea7fff18b8472768c88ad041fa750ff7682a21affe81863cbe77e4",
                    input = Prompts("")
                )
            )
        }
        return runCatching {
            client.post("https://api.replicate.com/v1/predictions") { setup() }
                .bodyAsText()
                .let { json.decodeFromString<PixrayResponse>(it) }
                .urls.get
                .let { url ->
                    var res: PixrayResponse?
                    do {
                        val response = client.post(url) { setup() }
                            .bodyAsText()
                            .let { json.decodeFromString<PixrayResponse>(it) }
                        println(response)
                        res = response
                        delay(30000)
                    } while (response.status != "succeeded")

                    client.get(res!!.urls.get) {
                        header("Authorization", "Token $token")
                        contentType(ContentType.Application.Json)
                        accept(ContentType.Application.Json)
                    }
                        .bodyAsText()
                        .let { json.decodeFromString<PixrayOutput>(it) }
                        .let { NekoImage(it.output.random(), "Replicate") }
                }
        }
    }
}

@Serializable
data class PixrayModel(
    val version: String,
    val input: Prompts
)

@Serializable
data class Prompts(val prompts: String)

@Serializable
data class PixrayResponse(
    val id: String,
    val version: String,
    val input: Input,
    val logs: String,
    val status: String,
    @SerialName("created_at")
    val createdAt: String,
    val urls: Urls,
)

@Serializable
data class Input(
    val prompts: String,
)

@Serializable
data class Urls(
    val cancel: String,
    val get: String,
)

@Serializable
data class PixrayOutput(
    val id: String,
    val input: Input,
    val output: List<String>,
    val status: String,
)

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