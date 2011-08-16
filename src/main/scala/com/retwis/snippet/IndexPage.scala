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

class IndexPage {
	object username extends RequestVar("")
	
	def login(xhtml : NodeSeq) : NodeSeq = {
		var password = ""

		def processLogin () {
			var noErrors = true
			if(username.toString.length < 1) {
				S.error("usernameLogin", "Username must be at least 1 character long")
				noErrors = false
			}
			if(password.length < 6) {
				S.error("passwordLogin", "Password must be at least 6 characters long")
				noErrors = false
			}
			if(noErrors) {
				if(!User.login(username.toString, password)) {
					S.error("usernameLogin", "Username/Password did not match")
				}
			}
		}

		bind("login", xhtml,
			"username" -> SHtml.text(username.is, username(_)),
			"password" -> SHtml.password(password, password = _),
			"submit" -> SHtml.submit("Login", processLogin))
	}

	def logout(xhtml : NodeSeq) : NodeSeq = {
		def processLogout () {
			User.logout
		}

		val logout = new NodeBuffer &+ "logout"

		bind("logout", xhtml,
			"logoutButton" -> SHtml.link("index", processLogout, logout))
	}

	def register(xhtml : NodeSeq) : NodeSeq = {
		var password = ""
		var passwordRepeat = ""

		def processRegister() {
			var noErrors = true
			if(username.toString.length < 1) {
				println("Username must be at least 1 character long")
				S.error("usernameReg", "Username must be at least 1 character long")
				noErrors = false
			}
			if(password.length < 6) {
				println("Password must be at least 6 characters long")
				S.error("passwordReg", "Password must be at least 6 characters long")
				noErrors = false
			}
			if(password != passwordRepeat) {
				println("Passwords don't match")
				S.error("passwordRepeat", "Passwords don't match")
				noErrors = false
			}
			if(noErrors){
				User.createUser(username, password)
				User.login(username, password)
			}
		}

		bind("register", xhtml,
			"username" -> SHtml.text(username.is, username(_)),
			"password" -> SHtml.password(password, password = _),
			"passwordRepeat" -> SHtml.password(passwordRepeat, passwordRepeat = _),
			"submit" -> SHtml.submit("Register", processRegister))
	}
}