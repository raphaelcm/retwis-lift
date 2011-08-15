package com.retwis.snippet

import com.retwis.model._
import _root_.net.liftweb._
import http._
import mapper._
import S._
import SHtml._
import Req._
import common._
import util._
import Helpers._
import _root_.scala.xml.{NodeSeq, Text, Group, NodeBuffer}

class UserSnippet {
	def userTitle (xhtml : NodeSeq) : NodeSeq = {
		val result = new NodeBuffer
		val userBox = S.param("u")
		if(userBox.isEmpty) result &+ <h2 class="username">No user by that name.</h2>
		else {
			val uname = userBox.openTheBox
			result &+ <h2 class="username">{uname}</h2>
		}
		result
	}

	def userPosts (xhtml : NodeSeq) : NodeSeq = {
		val result = new NodeBuffer
		val userBox = S.param("u")
		if(!userBox.isEmpty) {
			val u = User.getUserByName(userBox.openTheBox)
			val tweets = u.getNRecentTweets(10)
			println("tweets is " + tweets.length + " long.")
			for(i <- 0 until tweets.length) result &+ Tweet.renderTweetHTML(tweets(i))
		}
		result
	}

	def username (xhtml : NodeSeq) : NodeSeq = {
		val result = new NodeBuffer &+ User.getLoggedInUser.getUsername
		result
	}

	def followInfo (xhtml : NodeSeq) : NodeSeq = {
		val u = User.getLoggedInUser
		val followerCount = u.getFollowers.length
		val followingCount = u.getFollowing.length
		bind("followInfo", xhtml,
			"followers" -> followerCount.toString,
			"following" -> followingCount.toString)
		}

		def latestTweets( xhtml: NodeSeq ) : NodeSeq = {
			val latestTweets = User.getLoggedInUser.getNRecentTweets(20).elements.toList
			def bindTweets(template: NodeSeq): NodeSeq = {
				latestTweets.flatMap{ t => bind("usertimeline", template, "tweet" -> t.getMessage, "time" -> Tweet.strElapsed(t.getTime))}
			}
			bind("latestTweets", xhtml, "usertimeline" -> bindTweets _)
		}
	}