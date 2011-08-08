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
			val uname = uIter.next
			result &+ (<a id="user" href={ "user?u=" + uname }>{uname}</a><br />)
		}
		result
	}

	def latestTweets (content : NodeSeq) : NodeSeq = {
		<p>message placeholder</p>
	}
}