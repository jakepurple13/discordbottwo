import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.utils.io.*
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import java.util.*

private const val STABLE_DIFFUSION_URL = "http://127.0.0.1:7860/sdapi/v1"

class Network(
    private val catApiToken: String = "",
    private val replicateToken: String = ""
) {
    private val json = Json {
        isLenient = true
        prettyPrint = true
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    private val client by lazy {
        HttpClient {
            install(ContentNegotiation) { json(json) }
            install(HttpTimeout)
        }
    }

    suspend fun loadRandomImage() = runCatching {
        client.get("https://api.catboys.com/img")
            .bodyAsText()
            .let { json.decodeFromString<CatboyModel>(it) }
            .let { NekoImage(it.url, it.artist) }
    }

    suspend fun catApi() = runCatching {
        client.get("https://api.thecatapi.com/v1/images/search")
            .bodyAsText()
            .let { json.decodeFromString<List<CatApiModel>>(it) }
            .first()
            .let { NekoImage(it.url, "TheCatApi") }
    }

    suspend fun showMarvelSnapCards() = runCatching {
        client.get("https://marvelsnapzone.com/getinfo/?searchtype=cards&searchcardstype=true")
            .bodyAsText()
            .let { json.decodeFromString<MarvelSnapModel>(it) }
    }

    suspend fun stableDiffusionLoras() = runCatching {
        client.get("$STABLE_DIFFUSION_URL/loras") {
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            timeout {
                requestTimeoutMillis = Long.MAX_VALUE
                connectTimeoutMillis = Long.MAX_VALUE
            }
        }
            .bodyAsText()
            .let { json.decodeFromString<List<StableDiffusionLora>>(it) }
    }
        .onFailure { it.printStackTrace() }

    suspend fun stableDiffusionSamplers() = runCatching {
        client.get("$STABLE_DIFFUSION_URL/samplers") {
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            timeout {
                requestTimeoutMillis = Long.MAX_VALUE
                connectTimeoutMillis = Long.MAX_VALUE
            }
        }
            .bodyAsText()
            .let { json.decodeFromString<List<StableDiffusionSamplers>>(it) }
    }
        .onFailure { it.printStackTrace() }

    suspend fun stableDiffusionModels() = runCatching {
        client.get("$STABLE_DIFFUSION_URL/sd-models") {
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            timeout {
                requestTimeoutMillis = Long.MAX_VALUE
                connectTimeoutMillis = Long.MAX_VALUE
            }
        }
            .bodyAsText()
            .let { json.decodeFromString<List<StableDiffusionModel>>(it) }
    }
        .onFailure { it.printStackTrace() }

    suspend fun stableDiffusion(
        prompt: String,
        modelName: String? = null,
        cfgScale: Double = 7.0,
        steps: Int = 20,
        negativePrompt: String = "",
        sampler: String? = null
    ) = runCatching {
        client.post("$STABLE_DIFFUSION_URL/txt2img") {
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            setBody(
                StableDiffusionBody(
                    prompt = prompt,
                    negativePrompt = negativePrompt,
                    cfgScale = cfgScale,
                    steps = steps,
                    samplerIndex = sampler ?: "Euler a",
                    overrideOptions = modelName?.let {
                        OverriddenOptions(
                            sdModelCheckpoint = it
                        )
                    }
                )
            )
            timeout {
                requestTimeoutMillis = Long.MAX_VALUE
                connectTimeoutMillis = Long.MAX_VALUE
            }
        }
            .bodyAsText()
            .let { json.decodeFromString<StableDiffusionResponse>(it) }
            .also { println(it) }
            .images
            .first()
            .let {
                LocalNekoImage(
                    byteReadChannel = ByteReadChannel(Base64.getDecoder().decode(it)),
                    artist = "Stable Diffusion"
                )
            }
    }

    suspend fun retrieveStableDiffusion(
        prompt: String,
        modelName: String? = null,
        cfgScale: Double = 7.0,
        steps: Int = 20,
        negativePrompt: String = "",
        sampler: String? = null
    ) = runCatching {
        client.post("$STABLE_DIFFUSION_URL/txt2img") {
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            setBody(
                StableDiffusionBody(
                    prompt = prompt,
                    negativePrompt = negativePrompt,
                    cfgScale = cfgScale,
                    steps = steps,
                    samplerIndex = sampler ?: "Euler a",
                    overrideOptions = modelName?.let {
                        OverriddenOptions(
                            sdModelCheckpoint = it
                        )
                    }
                )
            )
            timeout {
                requestTimeoutMillis = Long.MAX_VALUE
                connectTimeoutMillis = Long.MAX_VALUE
            }
        }
            .bodyAsText()
            .let { json.decodeFromString<StableDiffusionResponse>(it) }
            .also { println(it) }

    }

    suspend fun pixrayLoad(): Result<NekoImage> {
        fun HttpRequestBuilder.setup() {
            header("Authorization", "Token $replicateToken")
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            setBody(
                PixrayModel(
                    version = "ac732df83cea7fff18b8472768c88ad041fa750ff7682a21affe81863cbe77e4",
                    input = Prompts("Anime cat boy with purple hair, purple cat ears, purple cat tail, and green piercing eyes")
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
                        header("Authorization", "Token $replicateToken")
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
