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

class TweetSnippet {
	object postText extends RequestVar("")
	
	def post(xhtml : NodeSeq) : NodeSeq = {
		def processPost () {
			var noErrors = true
			if(postText.toString.length < 1) {
				S.error("postTextError", "Post must be at least 1 character long")
				noErrors = false
			}
			if(postText.toString.length > 140) {
				S.error("passwordLogin", "Post must be 140 characters or less")
				noErrors = false
			}
			if(noErrors) {
				Retwis.newTweet(postText.toString)
			}
		}

		bind("post", xhtml,
			"postText" -> SHtml.textarea(postText.is, postText(_), "cols" -> "70", "rows" -> "3"),
			"submit" -> SHtml.submit("Update", processPost))
	}
}