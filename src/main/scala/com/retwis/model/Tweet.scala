package com.retwis.model

object Tweet {
	//get Tweet object based on tweet id
	def getTweet(id: String): Tweet = {
		val jedis = Retwis.pool.getResource()
		
		try {
			var time = jedis.get("pid:" + id + ":time")
			var message = jedis.get("pid:" + id + ":message")
			if (time != null && message != null) {
				return new Tweet(id, time.toLong, message)
			}
		} catch {
			case e => e.printStackTrace()
		} finally {
			Retwis.pool.returnResource(jedis)
		}
		
		return null
	}
}

class Tweet(id: String, time: Long, message: String) {
	/* Getters */
	def getId(): String = return id
	def getTime(): Long = return time
	def getMessage(): String = return message
}