package com.retwis.model

import net.liftweb.http._
import net.liftweb.http.provider._
import _root_.redis.clients.jedis._
import scala.collection.JavaConversions._
import compat.Platform
import java.security.MessageDigest
import java.security.SecureRandom
import java.math.BigInteger
import scala.collection.JavaConversions._

//Static User methods
object User {
	private val random = new SecureRandom();

	//get last 50 users
	def getLastUsers(): List[String] = {
		val jedis = Retwis.pool.getResource

		try {
			val sortParams = new SortingParams().desc.limit(0, 50).get("uid:*:username")
			val usernameList = jedis.sort("global:users", sortParams)
			// debugging
			val debugIter = usernameList.toList.iterator
			while(debugIter.hasNext) print(debugIter.next)
			// end debugging
			return usernameList.toList
		} catch {
			case e => e.printStackTrace
		} finally {
			Retwis.pool.returnResource(jedis)
		}
		return null
	}

	//return a random MD5 hash value (for session keys)
	private def getRand(): String = {
		val s = new BigInteger(130, random).toString(32)
		return new String(MessageDigest.getInstance("MD5").digest(s.getBytes))
	}

	def createUser(username: String, password: String): Boolean = {
		val jedis = Retwis.pool.getResource

		if(username != null && password != null) {
			try {
				if(jedis.get("username:" + username + ":uid") != null) return false
				val nextUserId = jedis.incr("glabal:nextUserId")
				jedis.set("username:" + username + ":uid", nextUserId.toString)
				jedis.set("uid:" + nextUserId.toString + ":username", username)
				jedis.set("uid:" + nextUserId.toString + ":password", password)
				jedis.sadd("global:users", nextUserId.toString)
				return true
			} catch {
				case e => e.printStackTrace
			} finally {
				Retwis.pool.returnResource(jedis)
			}
		}
		return false
	}

	//return TRUE and set AUTH hash if login info is value, otherwise return FALSE
	def login(username: String, password: String): Boolean = {
		val jedis = Retwis.pool.getResource()
		try {
			val userid = jedis.get("username:" + username + ":uid")
			if(userid != null && password == jedis.get("uid:" + userid + ":password")) {
				val authToken = getRand()
				jedis.set("uid:" + userid + ":auth", authToken)
				jedis.set("auth:" + authToken, userid)
				val cookie = HTTPCookie("auth", authToken)
				S.addCookie(cookie)
				return true
			}
		} catch {
			case e => e.printStackTrace()
		} finally {
			Retwis.pool.returnResource(jedis)
		}
		return false
	}

	//return TRUE if logged in
	def isLoggedIn(): Boolean = {
		val jedis = Retwis.pool.getResource()
		val authCookie = S.findCookie("auth")
		var retVal = false

		if(!authCookie.isEmpty) {
			try {
				val auth = authCookie.openTheBox.value.openTheBox //this seems awfully complicated
				val userid = jedis.get("auth:" + auth)
				if(userid != null && jedis.get("uid:" + userid + ":auth") == auth) {
					retVal = true
				}
			} catch {
				case e => e.printStackTrace()
			} finally {
				Retwis.pool.returnResource(jedis)
			}
		}
		return retVal
	}

	//return a User object corresponding to userid
	def getUserById(userid: String): User = {
		val jedis = Retwis.pool.getResource()
		try {
			var username = jedis.get("uid:" + userid + ":username")
			var password = jedis.get("uid:" + userid + ":password")
			if (username != null && password != null) {
				return new User(userid, username, password)
			}
		} catch {
			case e => e.printStackTrace()
		} finally {
			Retwis.pool.returnResource(jedis)
		}
		return null
	}

	//return a User object corresponding to username
	def getUserByName(username: String): User = {
		val jedis = Retwis.pool.getResource()
		try {
			val userid = jedis.get("username:" + username + ":uid")
			val password = jedis.get("uid:" + userid + ":password")
			if (userid != null && password != null) {
				return new User(userid, username, password)
			}
		} catch {
			case e => e.printStackTrace()
		} finally {
			Retwis.pool.returnResource(jedis)
		}
		return null
	}
}

class User(id: String, username: String, password: String) {	
	/* Getters */
	def getId(): String = return id
	def getUsername(): String = return username
	def getPassword(): String = return password

	//get all tweets belonging to user
	def getAllTweets(): List[Tweet] = {
		val jedis = Retwis.pool.getResource

		try {
			val numPosts = jedis.llen("uid:" + id + "posts")
			return this.getNRecentTweets(numPosts.toInt)
		} catch {
			case e => e.printStackTrace
			return null
		} finally {
			Retwis.pool.returnResource(jedis)
		}
	}

	//get N most recent tweets
	def getNRecentTweets(n: Int): List[Tweet] = {
		val jedis = Retwis.pool.getResource

		try {
			val postIdIter = jedis.lrange("uid:" + id + "posts", 0, n).iterator
			var tweets: List[Tweet] = null
			while(postIdIter.hasNext) {
				val postId = postIdIter.next
				val postTime = jedis.get("pid:" + postId + "time")
				val postMessage = jedis.get("pid:" + postId + "message")
				tweets = tweets :+ new Tweet(postId, postTime.toLong, postMessage)
			}
			return tweets
		} catch {
			case e => e.printStackTrace
			return null
		} finally {
			Retwis.pool.returnResource(jedis)
		}
	}

	//post new tweet
	def newTweet(message: String) = {
		val jedis = Retwis.pool.getResource

		try {
			val nextPostId = jedis.incr("global:nextPostId")
			val setTimeResponse = jedis.set("pid:" + nextPostId.toString + ":time", Platform.currentTime.toString)
			val setMessageResponse = jedis.set("pid:" + nextPostId + ":message", message)
			val setUserPostsResponse = jedis.rpush("uid:" + id + ":posts", nextPostId.toString)
			if(setTimeResponse != "OK" || setMessageResponse != "OK" || setUserPostsResponse < 1)
			throw new Exception("Response *not* OK. setTimeResponse=" + setTimeResponse + " setMessageResponse=" + setMessageResponse + " setUserPostsResponse=" + setUserPostsResponse)
		} catch {
			case e => e.printStackTrace
		} finally {
			Retwis.pool.returnResource(jedis)
		}
	}

	//begin following user
	def follow(followeeId: String) = {
		val jedis = Retwis.pool.getResource

		try {
			jedis.sadd("uid:" + id + ":following", followeeId)
		} catch {
			case e => e.printStackTrace
		} finally {
			Retwis.pool.returnResource(jedis)
		}
	}

	//stop following user
	def unFollow(followeeId: String) = {
		val jedis = Retwis.pool.getResource

		try {
			jedis.srem("uid:" + id + ":following", followeeId)
		} catch {
			case e => e.printStackTrace
		} finally {
			Retwis.pool.returnResource(jedis)
		}
	}

	//return a list of all followers
	def getFollowers(): List[User] = {
		return this.getRelatedUsers("followers")
	}

	//return a list of all followees
	def getFollowing(): List[User] = {
		return this.getRelatedUsers("following")
	}

	def getRelatedUsers(relation: String): List[User] = {
		val jedis = Retwis.pool.getResource

		try {
			val relatedIdIterator = jedis.smembers("uid:" + id + ":" + relation).iterator
			var related: List[User] = null
			while(relatedIdIterator.hasNext) {
				related = related :+ new User(relatedIdIterator.next, jedis.get("uid:" + id + ":username"), "") //don't include password
			}
			return related
		} catch {
			case e => e.printStackTrace
			return null
		} finally {
			Retwis.pool.returnResource(jedis)
		}
	}
}