@file:Suppress("INLINE_FROM_HIGHER_PLATFORM")

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.converters.impl.optionalStringChoice
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.rest.builder.message.create.addFile
import java.nio.file.Path

class StableDiffusionExtension(
    private val network: Network
) : Extension() {
    override val name: String = "stablediffusion"

    override suspend fun setup() {
        val argOptions = network.stableDiffusionModels().getOrNull()
        publicSlashCommand(arguments = { DiffusionArgs(argOptions) }) {
            name = "stablediffusion"
            description = "Get a ai generated image"

            action {
                respond {
                    network.stableDiffusion(arguments.prompt, arguments.model)
                        .onSuccess { model ->
                            content = "Here is your neko image!\nPrompt: `${arguments.prompt}`"
                            addFile(Path.of(model.path))
                        }
                        .respondWithError()
                }
            }
        }
    }

    inner class DiffusionArgs(options: List<StableDiffusionModel>?) : Arguments() {
        val prompt by string {
            name = "prompt"
            description = "Give me a prompt!"
        }

        val model by optionalStringChoice {
            name = "modeltype"
            description = "If you don't want to use the default model, you can change it!"
            options?.forEach {
                choice(it.title, it.title)
            }
        }
    }
}