package com.retwis.model

import com.retwis.util._
import scala.collection.JavaConversions._
import compat.Platform
import _root_.scala.xml.{NodeSeq, Text, Group, NodeBuffer}

object Tweet {
	//render Tweet HTML
	def renderTweetHTML(username: String, message: String, time: Long) : NodeSeq = {
		val elapsed = strElapsed(time)
		<a id="user" href={ "user?u=" + username }>{username}</a>
		<div class="post">{message}<br />
		<i>posted {elapsed} ago via web</i></div>
	}

	def renderTweetHTML(tweet: Tweet) : NodeSeq = {
		renderTweetHTML(tweet.getUsername, tweet.getMessage, tweet.getTime)
	}

	//create a string showing the time elapsed
	def strElapsed(time: Long): String = {
		val elapsedSeconds = (Platform.currentTime - time) / 1000
		if(elapsedSeconds < 60) return elapsedSeconds + " seconds"
		if(elapsedSeconds < 3600) {
			val m = elapsedSeconds / 60
			return pluralize(m, " minute")
		}
		if(elapsedSeconds < 3600*24) {
			val h = elapsedSeconds / 3600
			return pluralize(h, " hour")
		}
		val d = elapsedSeconds / (3600*24)
		return pluralize(d, " day")
	}

	private def pluralize(i: Long, s: String): String = {
		if(i>1) return i + s + "s"
		return i + s
	}

	//get Tweet object based on tweet id
	def getTweet(id: String): Tweet = {
		val jedis = RetwisDB.pool.getResource()

		try {
			val time = jedis.get("pid:" + id + ":time")
			val message = jedis.get("pid:" + id + ":message")
			val username = jedis.get("pid:" + id + ":username")
			if (time != null && message != null) {
				return new Tweet(id, time.toLong, message, username)
			}
		} catch {
			case e => e.printStackTrace()
		} finally {
			RetwisDB.pool.returnResource(jedis)
		}

		return null
	}

	//get last 50 tweets
	def getLastTweets(): Array[Tweet] = {
		val jedis = RetwisDB.pool.getResource

		try {
			val tweetIds = jedis.lrange("global:timeline", 0, 50)
			var tweets = new Array[Tweet](tweetIds.length)
			var i = 0
			for(id<-tweetIds) {
				tweets(i) = new Tweet(id, jedis.get("pid:" + id + ":time").toLong, jedis.get("pid:" + id + ":message"), jedis.get("pid:" + id + ":username"))
				i += 1
			}
			return tweets
		} catch {
			case e => e.printStackTrace
		} finally {
			RetwisDB.pool.returnResource(jedis)
		}
		return null
	}
}

class Tweet(id: String, time: Long, message: String, username: String) {
	/* Getters */
	def getId(): String = return id
	def getTime(): Long = return time
	def getMessage(): String = return message
	def getUsername(): String = return username
}