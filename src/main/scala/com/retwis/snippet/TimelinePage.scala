package com.retwis.snippet

import com.retwis.model._
import _root_.net.liftweb._
import http._
import mapper._
import S._
import SHtml._
import common._
import util._
import Helpers._
import _root_.scala.xml.{NodeSeq, Text, Group, NodeBuffer}

class TimelinePage {
	def latestRegisteredUsers (content : NodeSeq) : NodeSeq = {
		val uIter = User.getLastUsers.iterator
		val result = new NodeBuffer
		while(uIter.hasNext) {
			val user = uIter.next
			result &+ User.renderUserHTML(user.getUsername)
		}
		result
	}

	def latestTweets (content : NodeSeq) : NodeSeq = {
		val tIter = Tweet.getLastTweets.iterator
		var result = new NodeBuffer
		while(tIter.hasNext) {
			val tweet = tIter.next
			result &+ Tweet.renderTweetHTML(tweet)
		}
		result
	}
}