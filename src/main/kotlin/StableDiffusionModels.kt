import io.ktor.utils.io.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class StableDiffusionBody(
    val prompt: String,
    val cfgScale: Double,
    val steps: Int,
    @SerialName("sampler_index")
    val samplerIndex: String,
    @SerialName("negative_prompt")
    val negativePrompt: String = "",
    @SerialName("batch_size")
    val batchSize: Long = 1,
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
) {
    fun imagesAsByteChannel() = images.map {
        ByteReadChannel(Base64.getDecoder().decode(it))
    }
}

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
    @SerialName("negative_prompt")
    val negativePrompt: String?,
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
    @SerialName("sampler_name")
    val samplerName: String?,
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
    val filename: String,
)

@Serializable
data class StableDiffusionLora(
    val name: String,
    val alias: String
)

@Serializable
data class StableDiffusionSamplers(
    val name: String,
    val aliases: List<String>,
)

@Serializable
data class StableDiffusionProgress(
    val progress: Double,
    @SerialName("eta_relative")
    val etaRelative: Double,
    val state: State,
)

@Serializable
data class State(
    val skipped: Boolean,
    val interrupted: Boolean,
    val job: String,
    @SerialName("job_count")
    val jobCount: Long,
    @SerialName("job_timestamp")
    val jobTimestamp: String,
    @SerialName("job_no")
    val jobNo: Long,
    @SerialName("sampling_step")
    val samplingStep: Long,
    @SerialName("sampling_steps")
    val samplingSteps: Long,
)
