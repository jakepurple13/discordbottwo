import stablediffusion.StableDiffusionNetwork

suspend fun main(args: Array<String>) {
    val token = args.first()
    val channelId = args[1]
    val replicateToken = args[2]
    val catApiToken = args[3]

    val network = Network(
        catApiToken = catApiToken,
        replicateToken = replicateToken
    )

    val stableDiffusionNetwork = StableDiffusionNetwork()

    when (TESTING) {
        Type.Testing -> {
            stableDiffusionNetwork.stableDiffusion("cat")
                .onSuccess { println(it.info) }
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
                network = network,
                stableDiffusionNetwork = stableDiffusionNetwork
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