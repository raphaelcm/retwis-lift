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

class TimelineSnippet {
	def latestRegisteredUsers (content : NodeSeq) : NodeSeq = {
		val result = new NodeBuffer
		val usrs = User.getLastUsers
		if(usrs != null) {
			val uIter = usrs.iterator
			while(uIter.hasNext) {
				val user = uIter.next
				result &+ User.renderUserHTML(user.getUsername)
			}	
		}
		result
	}

	def getGlobalTimeline(xhtml: NodeSeq) : NodeSeq = {
		latestTweets(null, xhtml)
	}

	def getPersonalTimeline(xhtml: NodeSeq) : NodeSeq = {
		latestTweets(User.getLoggedInUser, xhtml)
	}

	def getUserTimeline(xhtml: NodeSeq) : NodeSeq = {
		val userBox = S.param("u")
		if(!userBox.isEmpty)
			latestTweets(User.getUserByName(userBox.openTheBox), xhtml)
		else
			xhtml
	}

	def latestTweets (u: User, xhtml: NodeSeq) : NodeSeq = {
		var latestTweets:List[Tweet] = Nil
		if(u != null) latestTweets = u.getNRecentTweets(20).elements.toList
		else latestTweets = Tweet.getLastTweets.toList
		def bindTweets(template: NodeSeq): NodeSeq = {
			latestTweets.flatMap{ t => bind("post", template, "authorlink" -> t.getAuthorLink, "message" -> t.getMessage, "elapsedtime" -> Tweet.strElapsed(t.getTime))}
		}
		bind("latestTweets", xhtml, "timeline" -> bindTweets _)
	}
}