suspend fun main(args: Array<String>) {
    val token = args.first()
    val channelId = args[1]
    val replicateToken = args[2]
    val catApiToken = args[3]

    val network = Network(
        catApiToken = catApiToken,
        replicateToken = replicateToken
    )

    when (TESTING) {
        Type.Testing -> {
            network.stableDiffusionModels()
                .onSuccess { println(it) }
                .onFailure { it.printStackTrace() }
        }

        Type.DiscordBot -> {
            DiscordBot(
                token = token,
                channelId = channelId,
                network = network
            )
        }

        Type.DiscordExtension -> {
            DiscordBotExtension(
                token = token,
                network = network
            )
        }
    }
}

private val TESTING = Type.DiscordExtension

enum class Type {
    Testing,
    DiscordBot,
    DiscordExtension
}