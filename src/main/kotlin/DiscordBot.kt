import dev.kord.common.Color
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.entity.channel.TextChannel
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.on
import dev.kord.gateway.Intent
import dev.kord.gateway.Intents
import dev.kord.gateway.PrivilegedIntent
import dev.kord.rest.builder.interaction.string
import dev.kord.rest.builder.message.create.embed
import dev.kord.rest.builder.message.modify.embed
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock

@OptIn(PrivilegedIntent::class)
suspend fun DiscordBot(
    token: String,
    channelId: String,
) {
    val network = Network()

    val kord = Kord(token)

    val c = kord.getChannelOf<TextChannel>(Snowflake(channelId))

    c?.createSilentMessage("NekoBot is booting up...Please wait...")

    kord.on<MessageCreateEvent> {
        if (message.content == "!ping") message.channel.createMessage("Pong!")
    }

    kord.on<MessageCreateEvent> {
        runCatching {
            val messageInfo = message.content.split(" ")
            when (messageInfo.firstOrNull()) {
                "!setDelay" -> {

                }

                else -> {}
            }
        }
    }

    /*runCatching {
        val image = Image.fromUrl(
            HttpClient(),
            "https://raw.githubusercontent.com/jakepurple13/OtakuWorld/develop/otakumanager/src/main/res/drawable/otakumanager_logo.png"
        )

        kord.editSelf {
            avatar = image
            username = "NekoBot"
        }
    }*/

    kord.createGlobalChatInputCommand(
        "neko",
        "Get a neko image"
    ) {
        string("neko", "What kind of neko") {
            choice("random", "random")
            choice("Amun", "Amun")
            required = true
        }
    }

    kord.on<ChatInputCommandInteractionCreateEvent> {
        val response = interaction.deferPublicResponse()
        val command = interaction.command
        when (command.strings["neko"]!!) {
            "random" -> network.loadRandomImage()

            "Amun" -> runCatching {
                error("")
            }

            else -> Result.failure(Exception(""))
        }
            .onSuccess { model ->
                response.respond {
                    content = "Here is your neko image!"
                    embed {
                        image = model.url
                        footer {
                            text = "By: ${model.artist}"
                        }
                        color = Blue
                    }
                }
            }
            .onFailure {
                response.respond {
                    content = "Here is your neko image!"
                    embed {
                        title = "Something went wrong"
                        description = it.stackTraceToString()
                        color = Red
                    }
                }
            }
    }

    c?.createSilentMessage("NekoBot is Online!")

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
                kord.shutdown()
            }
        }
    )

    kord.login {
        intents += Intents(Intent.MessageContent, Intent.DirectMessages, Intent.GuildMessages)
    }

    Thread.currentThread().join()
}

suspend fun TextChannel.createSilentMessage(content: String) {
    createMessage {
        this.content = content
        suppressNotifications = true
    }
}

val Purple = Color(0xFF4a148c.toInt())
val Blue = Color(0xFF42a5f5.toInt())
val Red = Color(0xFFe74c3c.toInt())

data class NekoImage(
    val url: String,
    val artist: String
)