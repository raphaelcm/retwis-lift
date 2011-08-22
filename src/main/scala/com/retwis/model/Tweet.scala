package com.retwis.model

import net.liftweb.json._
import net.liftweb.json.JsonAST._
import net.liftweb.json.JsonDSL._

object Tweet {
	def getTweetFromJson(jsonStr: String):Tweet = {
		implicit val formats = net.liftweb.json.DefaultFormats
		val json = parse(jsonStr)
		val id = (json \ "tweet" \ "id").extract[String]
		val time = (json \ "tweet" \ "time").extract[Long]
		val message = (json \ "tweet" \ "message").extract[String]
		val authorId = (json \ "tweet" \ "authorId").extract[String]
		return new Tweet(id, time, message, authorId)
	}
}

class Tweet(val id: String, val time: Long, val message: String, val authorId: String) {	
	/* Serialize tweet to JSON string */
	def toJson(): String = {
		val json = ("tweet" ->
				("id" -> id) ~
				("time" -> time) ~
				("message" -> message) ~
				("authorId" -> authorId))
		
		compact(JsonAST.render(json))
	}
}