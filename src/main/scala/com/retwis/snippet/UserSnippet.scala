package com.retwis.snippet

import com.retwis.model._
import com.retwis.api._
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
		val error = new NodeBuffer &+ <h2 class="username">No user by that name.</h2>
		val userBox = S.param("u")
		if(userBox.isEmpty) result &+ error
		else {
			val uname = Retwis.getUsernameById(userBox.openTheBox)
			if(uname != null) result &+ <h2 class="username">{uname}</h2>
			else result &+ error
		}
		result
	}

	def username (xhtml : NodeSeq) : NodeSeq = {
		val result = new NodeBuffer &+ Retwis.getLoggedInUsername
		result
	}

	def followInfo (xhtml : NodeSeq) : NodeSeq = {
		val followerCount = Retwis.getFollowers.length
		val followingCount = Retwis.getFollowing.length
		bind("followInfo", xhtml,
			"followers" -> followerCount.toString,
			"following" -> followingCount.toString)
	}

	def followLink(): NodeSeq = {
		val uid = Retwis.getLoggedInId
		val userBox = S.param("u")
		if(!userBox.isEmpty) {
			val targetId = userBox.openTheBox
			if (uid != targetId)
				return Retwis.getFollowLink(targetId, Retwis.isFollowing(targetId))
		}
		return new NodeBuffer
	}
}