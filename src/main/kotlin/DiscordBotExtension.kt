import com.kotlindiscord.kord.extensions.ExtensibleBot
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.edit
import dev.kord.core.entity.channel.TextChannel
import dev.kord.rest.builder.message.create.embed
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

            help {
                pingInReply = false
                color { Purple }
            }
        }
    }

    val c = bot.kordRef.getChannelOf<TextChannel>(Snowflake(channelId))

    c?.createSilentMessage("NekoBot is booting up...Please wait...")
        ?.also { delay(1500) }
        ?.edit { content = "NekoBot is Online!" }

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