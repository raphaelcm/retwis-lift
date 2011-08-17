package com.retwis.model

import com.retwis.api._
import com.retwis.api._
import scala.collection.JavaConversions._
import compat.Platform
import _root_.scala.xml.{NodeSeq, Text, Group, NodeBuffer}

class Tweet(id: String, time: Long, message: String, authorId: String) {
	/* Getters */
	def getId(): String = return id
	def getTime(): Long = return time
	def getMessage(): String = return message
	def getAuthorId(): String = return authorId
	
	def getAuthorLink(): NodeSeq = {
		val u = RetwisAPI.getUserById(authorId)
		return RetwisAPI.renderUserHTML(u.getId, u.getUsername)
	}
}