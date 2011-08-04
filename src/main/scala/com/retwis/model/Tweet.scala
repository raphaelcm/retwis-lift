package com.retwis.model

class Tweet(id: String, time: Long, message: String) {
	/* Getters */
	def getId(): String = return id
	def getTime(): Long = return time
	def getMessage(): String = return message
}