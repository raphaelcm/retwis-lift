package com.retwis.util

import _root_.redis.clients.jedis._
import scala.collection.JavaConversions._

//TODO Refactor so all DB calls go through here!

// Provide static access to JedisPool
object RetwisDB {
	val pool = new JedisPool(new JedisPoolConfig(), "localhost");

/*
	def nextUserId(): Int = {
		val jedis = pool.getResource

		try {
			return jedis.incr("global:nextUserId")
		} catch {
			case e => e.printStackTrace
		} finally {
			RetwisDB.pool.returnResource(jedis)
		}
		return -1
	}

	def getUidByUsername(username: String): Int = {
		val jedis = RetwisDB.pool.getResource

		try {
			return jedis.get("username:" + username + ":uid").toInt
		} catch {
			case e => e.printStackTrace
		} finally {
			RetwisDB.pool.returnResource(jedis)
		}
		return -1
	}

	def setUidForUsername(uid: Int, username: String) = {
		val jedis = RetwisDB.pool.getResource

		try {
			jedis.set("username:" + username + ":uid", uid.toString)
		} catch {
			case e => e.printStackTrace
		} finally {
			RetwisDB.pool.returnResource(jedis)
		}
	}

	def setUsernameForUid(username: String, uid: Int) = {
		val jedis = RetwisDB.pool.getResource

		try {
			jedis.set("uid:" + uid.toString + ":username", username)
		} catch {
			case e => e.printStackTrace
		} finally {
			RetwisDB.pool.returnResource(jedis)
		}
	}

	def setPasswordForUid(password: String, uid: Int) = {
		val jedis = RetwisDB.pool.getResource

		try {
			jedis.set("uid:" + uid.toString + ":password", password)
		} catch {
			case e => e.printStackTrace
		} finally {
			RetwisDB.pool.returnResource(jedis)
		}
	}

	def addUidToGlobalUsers(uid: Int) = {
		val jedis = RetwisDB.pool.getResource

		try {
			jedis.sadd("global:users", uid.toString)
		} catch {
			case e => e.printStackTrace
		} finally {
			RetwisDB.pool.returnResource(jedis)
		}
	}
*/
}