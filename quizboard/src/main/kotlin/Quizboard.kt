import redis.clients.jedis.Jedis

class Quizboard {
    private val redisClient : Jedis = Jedis("localhost", 6379)

    companion object {
        const val RESULTS_KEY = "results"
        const val TEAMS_KEY = "teams"
    }

    fun getResults(teamName : String): String {
        return redisClient.get("$RESULTS_KEY:$teamName")
    }

    fun getTeams(): Set<String> {
        return redisClient.smembers(TEAMS_KEY)
    }

}