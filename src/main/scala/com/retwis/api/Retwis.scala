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

object Retwis {
	val pool = new JedisPool(new JedisPoolConfig(), "localhost");
	private val random = new SecureRandom();
	object auth extends SessionVar[String]("LoggedOut")

	//render User HTML
	def getUserLink(userid: String, username: String): NodeSeq = {
		<a class="username" href={ "user?u=" + userid }>{username}</a>
	}
	
	def getUserLink(userid: String) : NodeSeq = {
		return getUserLink(userid, getUsernameById(userid))
	}

	//render Follow HTML
	def getFollowLink(userid: String, following: Boolean): NodeSeq = {
		if(following)
			<a href={ "follow/id/" + userid } class="button">Unfollow</a>
		else
			<a href={ "follow/id/" + userid } class="button">Follow</a>
	}

	//get Array of userIds
	def getUsersInRange(start: Int, end: Int): Array[String] = {
		val jedis = pool.getResource

		try {
			val userIds = jedis.lrange("global:users", start, end)
			if(userIds != null) return Array[String](userIds: _*)
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

	//create a new user
	def createUser(username: String, password: String) = {
		val jedis = pool.getResource

		if(username != null && password != null && jedis.get("username:" + username + ":uid") == null) {
			try {
				val nextUserId = jedis.incr("glabal:nextUserId")
				jedis.set("username:" + username + ":uid", nextUserId.toString)
				jedis.set("uid:" + nextUserId.toString + ":username", username)
				jedis.set("uid:" + nextUserId.toString + ":password", password)
				jedis.lpush("global:users", nextUserId.toString)
			} catch {
				case e => e.printStackTrace
			} finally {
				pool.returnResource(jedis)
			}
		}
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
				jedis.expire("uid:" + userid + ":auth", 60*60*24)
				jedis.expire("auth:" + authToken, 60*60*24)
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
	
	//get userid representing the logged in user
	def getLoggedInId(): String = {
		val jedis = pool.getResource()
		
		if(isLoggedIn) {
			try {
				return jedis.get("auth:" + auth.is)
			} catch {
				case e => e.printStackTrace()
			} finally {
				pool.returnResource(jedis)
			}
		}
		return null
	}
	
	//get username representing the logged in user
	def getLoggedInUsername(): String = {
		val jedis = pool.getResource()
		
		if(isLoggedIn) {
			try {
				val userid = jedis.get("auth:" + auth.is)
				return getUsernameById(userid)
			} catch {
				case e => e.printStackTrace()
			} finally {
				pool.returnResource(jedis)
			}
		}
		return null
	}

	def getUsernameById(userid: String): String = {
		val jedis = pool.getResource()
		var username = ""
		try {
			username = jedis.get("uid:" + userid + ":username")
		} catch {
			case e => e.printStackTrace()
		} finally {
			pool.returnResource(jedis)
		}
		return username
	}

	def getIdByUsername(username: String): String = {
			val jedis = pool.getResource()
			var uid = ""
			try {
				uid = jedis.get("username:" + username + ":uid")
			} catch {
				case e => e.printStackTrace()
			} finally {
				pool.returnResource(jedis)
			}
			return uid
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

	//get tweets in range [start .. end]
	def getGlobalTweetsInRange(start: Int, end: Int): Array[Tweet] = {
		val jedis = pool.getResource

		try {
			val tweetIds = jedis.lrange("global:timeline", start, end)
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
	
	def getAllGlobalTweets(): Array[Tweet] = {
		return getGlobalTweetsInRange(0, -1)
	}
	
	//get tweets in range [start .. end]
	def getUserTweetsInRange(uid: String, start: Int, end: Int): Array[Tweet] = {
		val jedis = pool.getResource

		try {
			val tweetIds = jedis.lrange("uid:" + uid + ":posts", start, end)
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
	
	def getAllUserTweets(uid: String): Array[Tweet] = {
		return getUserTweetsInRange(uid, 0, -1)
	}

	def getAllUserTweets(): Array[Tweet] = {
		return getUserTweetsInRange(getLoggedInId, 0, -1)
	}
	
	//post new tweet and get author Id from Session
	def newTweet(message: String) = {
		val jedis = pool.getResource
		val uid = getLoggedInId
		try {
			val nextPostId = jedis.incr("global:nextPostId")
			jedis.set("pid:" + nextPostId + ":time", Platform.currentTime.toString)
			jedis.set("pid:" + nextPostId + ":message", message)
			jedis.set("pid:" + nextPostId + ":uid", uid)
			jedis.lpush("global:timeline", nextPostId.toString)
			jedis.lpush("uid:" + uid + ":posts", nextPostId.toString)
		} catch {
			case e => e.printStackTrace
		} finally {
			pool.returnResource(jedis)
		}
	}
	
	def follow(targetId: String) = {
		val jedis = pool.getResource
		try {
			val id = getLoggedInId
		if(isFollowing(targetId)) {
			println("SREM " + "uid:" + id + ":following " + targetId)
			jedis.srem("uid:" + id + ":following", targetId)
			println("SREM " + "uid:" + targetId + ":following " + id)
			jedis.srem("uid:" + targetId + ":followers", id)
		} else {
			println("SADD " + "uid:" + id + ":following " + targetId)
			jedis.sadd("uid:" + id + ":following", targetId)
			println("SADD " + "uid:" + targetId + ":followers " + id)
			jedis.sadd("uid:" + targetId + ":followers", id)
		}	
		} catch {
			case e => e.printStackTrace
		} finally {
			pool.returnResource(jedis)
		}
		S.redirectTo("/user?u=" + targetId)
	}
	
	//return an array of all followers for logged in user
	def getFollowers(): Array[String] = {
		return getRelatedUsers(getLoggedInId, "followers")
	}

	//return an array of all followees for logged in user
	def getFollowing(): Array[String] = {
		return getRelatedUsers(getLoggedInId, "following")
	}

	def getRelatedUsers(uid: String, relation: String): Array[String] = {
		val jedis = pool.getResource

		try {
			val relatedIds = jedis.smembers("uid:" + uid + ":" + relation).toList
			if(relatedIds != null) return Array[String](relatedIds: _*)
		} catch {
			case e => e.printStackTrace
		} finally {
			pool.returnResource(jedis)
		}
		return null
	}
	
	def isTargetRelated(target: String, relation:String): Boolean = {
			val jedis = pool.getResource
			var retVal = false
			try {
				retVal =  jedis.sismember("uid:" + getLoggedInId + ":" + relation, target)
			} 	catch {
				case e => e.printStackTrace
			} finally {
				pool.returnResource(jedis)
			}
			return retVal
	}
	
	def isFollowing(target: String): Boolean = {
		return isTargetRelated(target, "following")
	}
	
	def isFollower(target: String): Boolean = {
		return isTargetRelated(target, "follower")
	}
}