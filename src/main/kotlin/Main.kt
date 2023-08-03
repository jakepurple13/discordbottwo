suspend fun main(args: Array<String>) {
    val token = args.first()
    val channelId = args[1]

    DiscordBot(token, channelId)
    /*Network().loadRandomImage()
        .onSuccess { println(it) }
        .onFailure { it.printStackTrace() }*/
}

