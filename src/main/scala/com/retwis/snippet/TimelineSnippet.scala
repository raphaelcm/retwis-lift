package com.retwis.snippet

import com.retwis.model._
import com.retwis.api._
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
		val usrs = Retwis.getUsersInRange(0, 20) //get last 20 users
		if(usrs != null) {
			val uIter = usrs.iterator
			while(uIter.hasNext) {
				val userid = uIter.next
				result &+ Retwis.getUserLink(userid)
			}	
		}
		result
	}

	def getGlobalTimeline(xhtml: NodeSeq) : NodeSeq = {
		latestTweets(Retwis.getAllGlobalTweets, xhtml) //since this is just a demo and few people will use it, this is ok
	}

	def getPersonalTimeline(xhtml: NodeSeq) : NodeSeq = {
		latestTweets(Retwis.getAllUserTweets, xhtml)
	}

	def getUserTimeline(xhtml: NodeSeq) : NodeSeq = {
		val userBox = S.param("u")
		if(!userBox.isEmpty)
			latestTweets(Retwis.getAllUserTweets(userBox.openTheBox), xhtml)
		else
			xhtml
	}

	def latestTweets (latestTweets: Array[Tweet], xhtml: NodeSeq) : NodeSeq = {
		if(latestTweets == null) return xhtml
		def bindTweets(template: NodeSeq): NodeSeq = {
			latestTweets.toList.flatMap{ t => bind("post", template, "authorlink" -> Retwis.getUserLink(t.getAuthorId), "message" -> t.getMessage, "elapsedtime" -> Retwis.strElapsed(t.getTime))}
		}
		bind("latestTweets", xhtml, "timeline" -> bindTweets _)
	}
}