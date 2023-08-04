import dev.kord.common.Color
import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.Snowflake
import dev.kord.common.entity.TextInputStyle
import dev.kord.core.Kord
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.edit
import dev.kord.core.behavior.interaction.response.DeferredPublicMessageInteractionResponseBehavior
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.entity.channel.TextChannel
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.core.event.interaction.GuildButtonInteractionCreateEvent
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.live.live
import dev.kord.core.live.on
import dev.kord.core.on
import dev.kord.gateway.Intent
import dev.kord.gateway.Intents
import dev.kord.gateway.PrivilegedIntent
import dev.kord.rest.builder.interaction.string
import dev.kord.rest.builder.message.create.FollowupMessageCreateBuilder
import dev.kord.rest.builder.message.create.embed
import dev.kord.rest.builder.message.modify.actionRow
import dev.kord.rest.builder.message.modify.embed
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock

@OptIn(PrivilegedIntent::class)
suspend fun DiscordBot(
    token: String,
    channelId: String,
    network: Network
) {
    val kord = Kord(token)

    val c = kord.getChannelOf<TextChannel>(Snowflake(channelId))

    c?.createSilentMessage("NekoBot is booting up...Please wait...")

    kord.on<MessageCreateEvent> {
        if (message.content == "!ping") {
            message.channel.createMessage("Pong!")
        }
    }

    kord.on<MessageCreateEvent> {
        runCatching {
            println(this)
            val messageInfo = message.content.split(" ")
            println(messageInfo)
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
            "https://static.displate.com/280x392/displate/2022-12-25/66e4b04cc4cb743486a3991ff8447f4f_2316d96de92d16d984b948ba1b24dcae.jpg"
        )

        kord.editSelf {
            avatar = image
            username = "NekoBot"
        }
    }*/

    kord.createGlobalChatInputCommand(
        "testing",
        "asdf"
    ) {
        string("testing", "adsf") {
            required = false
        }
    }

    kord.on<ChatInputCommandInteractionCreateEvent> {
        //val response = interaction.deferPublicResponse()
        val response = interaction.deferEphemeralResponse()
        val command = interaction.command
        println(command)
        response.respond {
            content = "Hello!"
            this.components?.forEach {

            }
            actionRow {
                interactionButton(
                    style = ButtonStyle.Success,
                    "previous_asdf"
                ) { label = "Previous" }

                /*textInput(
                    style = TextInputStyle.Short,
                    customId = "search_asdf",
                    label = "Search for Card"
                ) {

                }*/

                interactionButton(
                    style = ButtonStyle.Success,
                    "next_asdf"
                ) { label = "Next" }
            }
        }
            .message
            .live()
            .on<GuildButtonInteractionCreateEvent> {
                println(it)
                val r = it.interaction.getOriginalInteractionResponse()
                val command = it.interaction.component
                println(command)
                when (command.customId) {
                    "previous_asdf" -> {
                        r.edit { content = "Previous" }
                    }

                    "next_asdf" -> {
                        r.edit { content = "Next" }
                    }
                }
                println(it)
            }
    }

    kord.createGlobalChatInputCommand(
        "marvelsnap",
        "asdf"
    ) {
        string("name", "adsf") {
            required = false
        }
    }

    kord.on<ChatInputCommandInteractionCreateEvent> {
        val response = interaction.deferPublicResponse()
        val command = interaction.command
        val snap = command.strings["name"]
        var location = 0
        network.showMarvelSnapCards()
            .onSuccess {
                val d = it.success.cards
                response.respond {
                    embed {
                        val card = d[location]
                        title = card.name
                        image = card.art
                        footer {
                            text = "By: ${card.sketcher}"
                        }
                        color = Blue
                    }
                    actionRow {
                        interactionButton(
                            style = ButtonStyle.Success,
                            "previous_card"
                        ) { label = "Previous" }

                        textInput(
                            style = TextInputStyle.Short,
                            customId = "search_card",
                            label = "Search for Card"
                        ) {

                        }

                        interactionButton(
                            style = ButtonStyle.Success,
                            "next_card"
                        ) { label = "Next" }
                    }
                }
            }
            .respondWithError(response)
    }

    kord.on<MessageCreateEvent> {
        println(this)
    }

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

            "Amun" -> network.pixrayLoad()

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
            .respondWithError(response)
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

suspend fun TextChannel.createSilentMessage(content: String) = createMessage {
    this.content = content
    suppressNotifications = true
}

val Purple = Color(0xFF4a148c.toInt())
val Blue = Color(0xFF42a5f5.toInt())
val Red = Color(0xFFe74c3c.toInt())

data class NekoImage(
    val url: String,
    val artist: String
)

suspend fun Result<Any>.respondWithError(response: DeferredPublicMessageInteractionResponseBehavior) = onFailure {
    response.respond {
        content = "Error!"
        embed {
            title = "Something went wrong"
            description = it.stackTraceToString()
            color = Red
        }
    }
}

context (FollowupMessageCreateBuilder)
suspend fun Result<Any>.respondWithError() = onFailure {
    content = "Error!"
    embed {
        title = "Something went wrong"
        description = it.stackTraceToString()
        color = Red
    }
}