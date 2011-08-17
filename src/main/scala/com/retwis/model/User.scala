package com.retwis.model

import com.retwis.api._
import com.retwis.api._
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

class User(id: String, username: String, password: String) {	
	/* Getters */
	def getId(): String = return id
	def getUsername(): String = return username
	def getPassword(): String = return password

	//get all tweets belonging to user
	def getAllTweets(): Array[Tweet] = {
		val jedis = RetwisAPI.pool.getResource

		try {
			val numPosts = jedis.llen("uid:" + id + "posts")
			return getNRecentTweets(numPosts.toInt)
		} catch {
			case e => e.printStackTrace
			return null
		} finally {
			RetwisAPI.pool.returnResource(jedis)
		}
	}

	//get N most recent tweets
	def getNRecentTweets(n: Int): Array[Tweet] = {
		val jedis = RetwisAPI.pool.getResource

		try {
			val postIds = jedis.lrange("uid:" + id + ":posts", 0, n)
			var tweets = new Array[Tweet](postIds.length)
			var i = 0
			for(postId<-postIds) {
				val postTime = jedis.get("pid:" + postId + ":time")
				val postMessage = jedis.get("pid:" + postId + ":message")
				tweets(i) = new Tweet(postId, postTime.toLong, postMessage, id)
				i += 1
			}
			return tweets
		} catch {
			case e => e.printStackTrace
			return null
		} finally {
			RetwisAPI.pool.returnResource(jedis)
		}
	}

	//post new tweet
	def newTweet(message: String) = {
		val jedis = RetwisAPI.pool.getResource

		try {
			val nextPostId = jedis.incr("global:nextPostId")
			jedis.set("pid:" + nextPostId + ":time", Platform.currentTime.toString)
			jedis.set("pid:" + nextPostId + ":message", message)
			jedis.set("pid:" + nextPostId + ":uid", id)
			jedis.lpush("global:timeline", nextPostId.toString)
			jedis.lpush("uid:" + id + ":posts", nextPostId.toString)
		} catch {
			case e => e.printStackTrace
		} finally {
			RetwisAPI.pool.returnResource(jedis)
		}
	}

	//begin following user
	def follow(followeeId: String) = {
		val jedis = RetwisAPI.pool.getResource

		try {
			jedis.sadd("uid:" + id + ":following", followeeId)
		} catch {
			case e => e.printStackTrace
		} finally {
			RetwisAPI.pool.returnResource(jedis)
		}
	}
	
	//follow user by username
	def followUsername(followeeName: String) = {
		val jedis = RetwisAPI.pool.getResource

		try {
			val uid = jedis.get("username:" + followeeName + ":uid")
			this.follow(uid)
		} catch {
			case e => e.printStackTrace
		} finally {
			RetwisAPI.pool.returnResource(jedis)
		}
	}

	//stop following user
	def unFollow(followeeId: String) = {
		val jedis = RetwisAPI.pool.getResource

		try {
			jedis.srem("uid:" + id + ":following", followeeId)
		} catch {
			case e => e.printStackTrace
		} finally {
			RetwisAPI.pool.returnResource(jedis)
		}
	}

	//return an array of all followers
	def getFollowers(): Array[User] = {
		return getRelatedUsers("followers")
	}

	//return an array of all followees
	def getFollowing(): Array[User] = {
		return getRelatedUsers("following")
	}

	def getRelatedUsers(relation: String): Array[User] = {
		val jedis = RetwisAPI.pool.getResource

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
			RetwisAPI.pool.returnResource(jedis)
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