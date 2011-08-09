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

class UserPage {
	def userTitle (content : NodeSeq) : NodeSeq = {
		val result = new NodeBuffer
		val userBox = S.param("u")
		if(userBox.isEmpty) result &+ <h2 class="username">No user by that name.</h2>
		else {
			val uname = userBox.openTheBox
			result &+ <h2 class="username">{uname}</h2>
		}
		result
	}

	def userPosts (content : NodeSeq) : NodeSeq = {
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

	def followButton (content : NodeSeq) : NodeSeq = {
		val result = new NodeBuffer
		val userBox = S.param("u")
		if(!userBox.isEmpty && User.isLoggedIn()) {
			val uid = userBox.openTheBox
			val user = User.getLoggedInUser
			if(user isFollowing uid) result &+ <a href={"follow?u=" + uid} class="button">Follow this user</a>
			else result &+ <a href={"follow?u=" + uid} class="button">Stop following</a>
		}
		result
	}
}