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

class FollowSnippet {
	object targetUser extends RequestVar(S.param("u").openTheBox)

	def followButton (xhtml : NodeSeq) : NodeSeq = {
		val curUser = RetwisAPI.getLoggedInUser

		def processFollow () {
			curUser.followUsername(targetUser.is)
		}

		if(curUser == null || curUser.getUsername == targetUser.is) {
			bind("follow", xhtml,
			"button" -> "")
		}
		else {
			var followButtonText = new NodeBuffer &+ "Follow"
			if(curUser.isFollowing(targetUser.is))
				followButtonText = new NodeBuffer &+ "Unfollow"
				
			bind("follow", xhtml,
			"button" -> SHtml.link("index", processFollow, followButtonText))
		}
	}
}