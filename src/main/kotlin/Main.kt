suspend fun main(args: Array<String>) {
    val token = args.first()
    val channelId = args[1]
    val replicateToken = args[2]

    DiscordBot(
        token = token,
        channelId = channelId,
        replicateToken = replicateToken
    )
    /*Network().pixrayLoad(replicateToken)
        .onSuccess { println(it) }
        .onFailure { it.printStackTrace() }*/
}

