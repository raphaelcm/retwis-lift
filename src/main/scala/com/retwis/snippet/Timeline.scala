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
		println("Timeline.scala a1")
		val uIter = User.getLastUsers.iterator
			println("Timeline.scala a2")
		val result = new NodeBuffer
		while(uIter.hasNext) {
			println("Timeline.scala a3")
			val user = uIter.next
				println("Timeline.scala a4")
			val uname = user.getUsername
			println("Timeline.scala a5")
			result &+ (<a class="username" href={ "user?u=" + uname }>{uname}</a><br />)
		}
		result
	}

	def latestTweets (content : NodeSeq) : NodeSeq = {
		println("Timeline.scala b1")
		val tIter = Tweet.getLastTweets.iterator
			println("Timeline.scala b2")
		val result = new NodeBuffer
		while(tIter.hasNext) {
			println("Timeline.scala b3")
			val tweet = tIter.next
				println("Timeline.scala b4")
			val tmsg = tweet.getMessage
			val ttime = tweet.getTime
			val tuser = tweet.getUsername
			val elapsed = Tweet.strElapsed(ttime)
			println("Timeline.scala b5")
			result &+ (<a id="user" href={ "user?u=" + tuser }>{tuser}</a>
			<div class="post">{tmsg}<br />
		    <i>posted {elapsed} ago via web</i></div>)
		}
		result
	}
}