package com.retwis.model

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

//Static User methods
object User {
	private val random = new SecureRandom();

	//render User HTML
	def renderUserHTML(username: String): NodeSeq = {
		<a class="username" href={ "user?u=" + username }>{username}</a><br />
	}

	//get last 50 users
	def getLastUsers(): Array[User] = {
		val jedis = RetwisDB.pool.getResource

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
			RetwisDB.pool.returnResource(jedis)
		}
		return null
	}

	//return a random MD5 hash value (for session keys)
	private def getRand(): String = {
		val s = new BigInteger(130, random).toString(32)
		return new String(MessageDigest.getInstance("MD5").digest(s.getBytes))
	}

	def createUser(username: String, password: String): Boolean = {
		val jedis = RetwisDB.pool.getResource

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
				RetwisDB.pool.returnResource(jedis)
			}
		}
		return false
	}

	//return TRUE and set AUTH hash if login info is value, otherwise return FALSE
	def login(username: String, password: String): Boolean = {
		val jedis = RetwisDB.pool.getResource()
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
			RetwisDB.pool.returnResource(jedis)
		}
		return false
	}

	//return TRUE if logged in
	def isLoggedIn(): Boolean = {
		val jedis = RetwisDB.pool.getResource()
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
				RetwisDB.pool.returnResource(jedis)
			}
		}
		return retVal
	}
	
	//get User object representing the logged in user
	def getLoggedInUser(): User = {
		val jedis = RetwisDB.pool.getResource()
		val authCookie = S.findCookie("auth")
		
		if(isLoggedIn) {
			try {
				val auth = authCookie.openTheBox.value.openTheBox //this seems awfully complicated
				val userid = jedis.get("auth:" + auth)
				return getUserById(userid)
			} catch {
				case e => e.printStackTrace()
			} finally {
				RetwisDB.pool.returnResource(jedis)
			}
		}
		return null
	}

	//return a User object corresponding to userid
	def getUserById(userid: String): User = {
		val jedis = RetwisDB.pool.getResource()
		try {
			var username = jedis.get("uid:" + userid + ":username")
			var password = jedis.get("uid:" + userid + ":password")
			if (username != null && password != null) {
				return new User(userid, username, password)
			}
		} catch {
			case e => e.printStackTrace()
		} finally {
			RetwisDB.pool.returnResource(jedis)
		}
		return null
	}

	//return a User object corresponding to username
	def getUserByName(username: String): User = {
		val jedis = RetwisDB.pool.getResource()
		try {
			val userid = jedis.get("username:" + username + ":uid")
			val password = jedis.get("uid:" + userid + ":password")
			if (userid != null && password != null) {
				return new User(userid, username, password)
			}
		} catch {
			case e => e.printStackTrace()
		} finally {
			RetwisDB.pool.returnResource(jedis)
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
	def getAllTweets(): Array[Tweet] = {
		val jedis = RetwisDB.pool.getResource

		try {
			val numPosts = jedis.llen("uid:" + id + "posts")
			return this.getNRecentTweets(numPosts.toInt)
		} catch {
			case e => e.printStackTrace
			return null
		} finally {
			RetwisDB.pool.returnResource(jedis)
		}
	}

	//get N most recent tweets
	def getNRecentTweets(n: Int): Array[Tweet] = {
		val jedis = RetwisDB.pool.getResource

		try {
			val postIds = jedis.lrange("uid:" + id + ":posts", 0, n)
			var tweets = new Array[Tweet](postIds.length)
			var i = 0
			for(postId<-postIds) {
				val postTime = jedis.get("pid:" + postId + ":time")
				val postMessage = jedis.get("pid:" + postId + ":message")
				tweets(i) = new Tweet(postId, postTime.toLong, postMessage, username)
				i += 1
			}
			return tweets
		} catch {
			case e => e.printStackTrace
			return null
		} finally {
			RetwisDB.pool.returnResource(jedis)
		}
	}

	//post new tweet
	def newTweet(message: String) = {
		val jedis = RetwisDB.pool.getResource

		try {
			val nextPostId = jedis.incr("global:nextPostId")
			val setTimeResponse = jedis.set("pid:" + nextPostId + ":time", Platform.currentTime.toString)
			val setMessageResponse = jedis.set("pid:" + nextPostId + ":message", message)
			val setUsernameResponse = jedis.set("pid:" + nextPostId + ":username", username)
			val setUserPostsResponse = jedis.rpush("uid:" + id + ":posts", nextPostId.toString)
			if(setTimeResponse != "OK" || setMessageResponse != "OK" || setUsernameResponse != "OK" || setUserPostsResponse < 1)
			throw new Exception("Response *not* OK. setTimeResponse=" + setTimeResponse + " setMessageResponse=" + setMessageResponse + " setUsernameResponse=" + setUsernameResponse + " setUserPostsResponse=" + setUserPostsResponse)
		} catch {
			case e => e.printStackTrace
		} finally {
			RetwisDB.pool.returnResource(jedis)
		}
	}

	//begin following user
	def follow(followeeId: String) = {
		val jedis = RetwisDB.pool.getResource

		try {
			jedis.sadd("uid:" + id + ":following", followeeId)
		} catch {
			case e => e.printStackTrace
		} finally {
			RetwisDB.pool.returnResource(jedis)
		}
	}

	//stop following user
	def unFollow(followeeId: String) = {
		val jedis = RetwisDB.pool.getResource

		try {
			jedis.srem("uid:" + id + ":following", followeeId)
		} catch {
			case e => e.printStackTrace
		} finally {
			RetwisDB.pool.returnResource(jedis)
		}
	}

	//return an array of all followers
	def getFollowers(): Array[User] = {
		return this.getRelatedUsers("followers")
	}

	//return an array of all followees
	def getFollowing(): Array[User] = {
		return this.getRelatedUsers("following")
	}

	def getRelatedUsers(relation: String): Array[User] = {
		val jedis = RetwisDB.pool.getResource

		try {
			val relatedIds = jedis.smembers("uid:" + id + ":" + relation)
			var related = new Array[User](relatedIds.size)
			var i = 0
			for(id<-relatedIds) {
				related(i) = new User(id, jedis.get("uid:" + id + ":username"), "") //don't include password
				i += 1
			}
			return related
		} catch {
			case e => e.printStackTrace
			return null
		} finally {
			RetwisDB.pool.returnResource(jedis)
		}
	}
	
	//return TRUE if user is following uid
	def isFollowing(uid: String):Boolean = {
		val following = getFollowing
		for(i <- 0 until following.length) if(following(i).getId == uid) return true
		return false
	}
	
	//return TRUE if user is followed by uid
	def isFollowedBy(uid: String):Boolean = {
		val followers = getFollowers
		for(i <- 0 until followers.length) if(followers(i).getId == uid) return true
		return false
	}
}