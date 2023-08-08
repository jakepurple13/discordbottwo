@file:Suppress("INLINE_FROM_HIGHER_PLATFORM")

import com.kotlindiscord.kord.extensions.commands.Arguments
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
        publicSlashCommand(::DiffusionArgs) {
            name = "stablediffusion"
            description = "Get a ai generated image"

            action {
                respond {
                    network.stableDiffusion(arguments.prompt)
                        .onSuccess { model ->
                            content = "Here is your neko image!\nPrompt: `${arguments.prompt}`"
                            addFile(Path.of(model.path))
                        }
                        .respondWithError()
                }
            }
        }
    }

    inner class DiffusionArgs : Arguments() {
        val prompt by string {
            name = "prompt"
            description = "Give me a prompt!"
        }
    }
}