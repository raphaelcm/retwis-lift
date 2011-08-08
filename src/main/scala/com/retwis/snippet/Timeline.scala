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

class Timeline {
	def latestRegisteredUsers (content : NodeSeq) : NodeSeq = {
		val uIter = User.getLastUsers.iterator
		val result = new NodeBuffer
		while(uIter.hasNext) {
			val user = uIter.next
			val uname = user.getUsername
			result &+ (<a class="username" href={ "user?u=" + uname }>{uname}</a><br />)
		}
		result
	}

	def latestTweets (content : NodeSeq) : NodeSeq = {
		val tIter = Tweet.getLastTweets.iterator
		val result = new NodeBuffer
		while(tIter.hasNext) {
			val tweet = tIter.next
			val tmsg = tweet.getMessage
			val ttime = tweet.getTime
			val tuser = tweet.getUsername
			val elapsed = Tweet.strElapsed(ttime)
			result &+ (<a id="user" href={ "user?u=" + tuser }>{tuser}</a>
			<div class="post">{tmsg}<br />
		    <i>posted {elapsed} ago via web</i></div>)
		}
		result
	}
}