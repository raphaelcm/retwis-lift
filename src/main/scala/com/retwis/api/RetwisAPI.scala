package com.retwis.api

import com.retwis.api._
import com.retwis.model._
import scala.collection.immutable.List
import net.liftweb.http._
import net.liftweb.http.provider._
import _root_.redis.clients.jedis._
import scala.collection.JavaConversions._
import compat.Platform
import java.security.MessageDigest
import java.security.SecureRandom
import java.math.BigInteger
import scala.collection.JavaConversions._
import _root_.scala.xml.{NodeSeq, Text, Group, NodeBuffer}


object RetwisAPI {
	val pool = new JedisPool(new JedisPoolConfig(), "localhost");
	private val random = new SecureRandom();
	object auth extends SessionVar[String]("LoggedOut")

	//render User HTML
	def renderUserHTML(username: String): NodeSeq = {
		<a class="username" href={ "user?u=" + username }>{username}</a><br />
	}

	//get last 50 users
	def getLastUsers(): Array[User] = {
		val jedis = pool.getResource

		try {
			val userIds = jedis.lrange("global:users", 0, 50)
			var userArray = new Array[User](userIds.length)
			var i = 0
			for(id<-userIds) {
				val username = jedis.get("uid:" + id + ":username")
				userArray(i) = new User(id, username, "")
				i += 1
			}
			return userArray
		} catch {
			case e => e.printStackTrace
		} finally {
			pool.returnResource(jedis)
		}
		return null
	}

	//return a random MD5 hash value (for session keys)
	private def getRand(): String = {
		val s = new BigInteger(130, random).toString(32)
		return new String(MessageDigest.getInstance("MD5").digest(s.getBytes))
	}

	def createUser(username: String, password: String): Boolean = {
		val jedis = pool.getResource

		if(username != null && password != null) {
			try {
				if(jedis.get("username:" + username + ":uid") != null) return false
				val nextUserId = jedis.incr("glabal:nextUserId")
				jedis.set("username:" + username + ":uid", nextUserId.toString)
				jedis.set("uid:" + nextUserId.toString + ":username", username)
				jedis.set("uid:" + nextUserId.toString + ":password", password)
				jedis.lpush("global:users", nextUserId.toString)
				return true
			} catch {
				case e => e.printStackTrace
			} finally {
				pool.returnResource(jedis)
			}
		}
		return false
	}

	//return TRUE and set AUTH hash if login info is value, otherwise return FALSE
	def login(username: String, password: String): Boolean = {
		val jedis = pool.getResource()
		try {
			val userid = jedis.get("username:" + username + ":uid")
			if(userid != null && password == jedis.get("uid:" + userid + ":password")) {
				val authToken = getRand()
				jedis.set("uid:" + userid + ":auth", authToken)
				jedis.set("auth:" + authToken, userid)
				auth.set(authToken)
				return true
			}
		} catch {
			case e => e.printStackTrace()
		} finally {
			pool.returnResource(jedis)
		}
		return false
	}

	def logout(): Boolean = {
		val jedis = pool.getResource()
		try {
			val userid = jedis.get("auth:" + auth.is)
			if(userid != null) {
				jedis.del("uid:" + userid + ":auth", auth.is)
				jedis.del("auth:" + auth.is, userid)
				auth.set("Logged Out")
				return true
			}
		} catch {
			case e => e.printStackTrace()
		} finally {
			pool.returnResource(jedis)
		}
		return false
	}

	//return TRUE if logged in
	def isLoggedIn(): Boolean = {
		val jedis = pool.getResource()
		var retVal = false

		try {
			val userid = jedis.get("auth:" + auth.is)
			if(userid != null && jedis.get("uid:" + userid + ":auth") == auth.is) {
				retVal = true
			}
		} catch {
			case e => e.printStackTrace()
		} finally {
			pool.returnResource(jedis)
		}
		return retVal
	}
	
	//get User object representing the logged in user
	def getLoggedInUser(): User = {
		val jedis = pool.getResource()
		
		if(isLoggedIn) {
			try {
				val userid = jedis.get("auth:" + auth.is)
				return getUserById(userid)
			} catch {
				case e => e.printStackTrace()
			} finally {
				pool.returnResource(jedis)
			}
		}
		return null
	}

	//return a User object corresponding to userid
	def getUserById(userid: String): User = {
		val jedis = pool.getResource()
		try {
			var username = jedis.get("uid:" + userid + ":username")
			var password = jedis.get("uid:" + userid + ":password")
			if (username != null && password != null) {
				return new User(userid, username, password)
			}
		} catch {
			case e => e.printStackTrace()
		} finally {
			pool.returnResource(jedis)
		}
		return null
	}

	//return a User object corresponding to username
	def getUserByName(username: String): User = {
		val jedis = pool.getResource()
		try {
			val userid = jedis.get("username:" + username + ":uid")
			val password = jedis.get("uid:" + userid + ":password")
			if (userid != null && password != null) {
				return new User(userid, username, password)
			}
		} catch {
			case e => e.printStackTrace()
		} finally {
			pool.returnResource(jedis)
		}
		return null
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
		val jedis = pool.getResource()

		try {
			val time = jedis.get("pid:" + id + ":time")
			val message = jedis.get("pid:" + id + ":message")
			val authorId = jedis.get("pid:" + id + ":uid")
			if (time != null && message != null) {
				return new Tweet(id, time.toLong, message, authorId)
			}
		} catch {
			case e => e.printStackTrace()
		} finally {
			pool.returnResource(jedis)
		}

		return null
	}

	//get last 50 tweets
	def getLastTweets(): Array[Tweet] = {
		val jedis = pool.getResource

		try {
			val tweetIds = jedis.lrange("global:timeline", 0, 50)
			var tweets = new Array[Tweet](tweetIds.length)
			var i = 0
			for(id<-tweetIds) {
				tweets(i) = new Tweet(id, jedis.get("pid:" + id + ":time").toLong, jedis.get("pid:" + id + ":message"), jedis.get("pid:" + id + ":uid"))
				i += 1
			}
			return tweets
		} catch {
			case e => e.printStackTrace
		} finally {
			pool.returnResource(jedis)
		}
		return null
	}
}