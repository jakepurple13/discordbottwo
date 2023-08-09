import com.kotlindiscord.kord.extensions.ExtensibleBot
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.edit
import dev.kord.core.entity.channel.TextChannel
import dev.kord.rest.builder.message.create.embed
import dev.kord.rest.builder.message.modify.actionRow
import dev.kord.rest.builder.message.modify.embed
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock

suspend fun DiscordBotExtension(
    token: String,
    channelId: String,
    network: Network
) {
    val bot = ExtensibleBot(token) {
        chatCommands {
            enabled = true
        }

        extensions {
            add { NekoExtension(network) }
            add { MarvelSnapExtension(network) }
            add { StableDiffusionExtension(network) }
            help {
                pingInReply = true
                color { Purple }
            }
        }

        hooks {
            kordShutdownHook = true
        }
    }

    val c = bot.kordRef.getChannelOf<TextChannel>(Snowflake(channelId))

    c?.createSilentMessage("NekoBot is booting up...Please wait...")
        ?.also { delay(1500) }
        ?.edit {
            content = "NekoBot is Online!"
            embed {
                title = "NekoBot is Online!"
                description = """
                    Meow is back online!
                    
                    To get more Stable Diffusion models to suggest, press on the button below!
                    To use Stable Diffusion, type `/stablediffusion`
                    Here are the extensions we have access to. To use them, use <lora:[alias here]>
                    ${network.stableDiffusionLoras().getOrNull().orEmpty().joinToString("\n") { it.alias }}
                    
                    To get a random neko image, type `/neko random`
                    To get a random cat image, type `/neko cat`
                    To view Marvel Snap cards, type `/snapcards`
                """.trimIndent()
                color = Emerald
            }
            actionRow { linkButton("https://huggingface.co") { label = "Stable Diffusion Models" } }
        }

    Runtime.getRuntime().addShutdownHook(
        Thread {
            runBlocking {
                c?.createMessage {
                    embed {
                        title = "Shutting Down for maintenance and updates..."
                        timestamp = Clock.System.now()
                        description = "Please wait while I go through some maintenance."
                        thumbnail {
                            url = "https://media.tenor.com/YTPLqiB6gLsAAAAC/sowwy-sorry.gif"
                        }
                        color = Red
                    }
                }
                bot.stop()
            }
        }
    )

    bot.start()

    Thread.currentThread().join()
}