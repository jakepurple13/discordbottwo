import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.delay
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import java.util.*

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

    suspend fun stableDiffusionModels() = runCatching {
        client.get("http://127.0.0.1:7860/sdapi/v1/sd-models") {
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

    suspend fun stableDiffusion(prompt: String, modelName: String? = null) = runCatching {
        client.post("http://127.0.0.1:7860/sdapi/v1/txt2img") {
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            setBody(
                StableDiffusionBody(
                    prompt = prompt,
                    styles = listOf("Anime"),
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
                val f = File("output.png")
                f.writeBytes(Base64.getDecoder().decode(it))
                LocalNekoImage(
                    path = f.path,
                    artist = "Stable Diffusion"
                )
            }
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

@Serializable
data class StableDiffusionBody(
    val prompt: String,
    val steps: Int = 5,
    val styles: List<String>,
    @SerialName("override_settings")
    val overrideOptions: OverriddenOptions?
)

@Serializable
data class OverriddenOptions(
    @SerialName("sd_model_checkpoint")
    val sdModelCheckpoint: String,
    @SerialName("filter_nsfw")
    val filterNsfw: Boolean = false
)

@Serializable
data class StableDiffusionResponse(
    val images: List<String>,
    val parameters: Parameters,
    val info: String,
)

@Serializable
data class Parameters(
    @SerialName("enable_hr")
    val enableHr: Boolean,
    @SerialName("denoising_strength")
    val denoisingStrength: Long,
    @SerialName("firstphase_width")
    val firstphaseWidth: Long,
    @SerialName("firstphase_height")
    val firstphaseHeight: Long,
    @SerialName("hr_scale")
    val hrScale: Double,
    @SerialName("hr_second_pass_steps")
    val hrSecondPassSteps: Long,
    @SerialName("hr_resize_x")
    val hrResizeX: Long,
    @SerialName("hr_resize_y")
    val hrResizeY: Long,
    @SerialName("hr_prompt")
    val hrPrompt: String,
    @SerialName("hr_negative_prompt")
    val hrNegativePrompt: String,
    val prompt: String,
    val seed: Long,
    val subseed: Long,
    @SerialName("subseed_strength")
    val subseedStrength: Long,
    @SerialName("seed_resize_from_h")
    val seedResizeFromH: Long,
    @SerialName("seed_resize_from_w")
    val seedResizeFromW: Long,
    @SerialName("batch_size")
    val batchSize: Long,
    @SerialName("n_iter")
    val nIter: Long,
    val steps: Long,
    @SerialName("cfg_scale")
    val cfgScale: Double,
    val width: Long,
    val height: Long,
    @SerialName("restore_faces")
    val restoreFaces: Boolean,
    val tiling: Boolean,
    @SerialName("do_not_save_samples")
    val doNotSaveSamples: Boolean,
    @SerialName("do_not_save_grid")
    val doNotSaveGrid: Boolean,
//    @SerialName("negative_prompt")
//    val negativePrompt: Any?,
    @SerialName("s_min_uncond")
    val sMinUncond: Double,
    @SerialName("s_churn")
    val sChurn: Double,
    @SerialName("s_tmin")
    val sTmin: Double,
    @SerialName("s_noise")
    val sNoise: Double,
    @SerialName("override_settings_restore_afterwards")
    val overrideSettingsRestoreAfterwards: Boolean,
    @SerialName("sampler_index")
    val samplerIndex: String,
    @SerialName("send_images")
    val sendImages: Boolean,
    @SerialName("save_images")
    val saveImages: Boolean,
)

@Serializable
data class StableDiffusionModel(
    val title: String,
    @SerialName("model_name")
    val modelName: String,
    val hash: String,
    val sha256: String,
    val filename: String,
)