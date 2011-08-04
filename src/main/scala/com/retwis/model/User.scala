package com.retwis.model

import _root_.net.liftweb.mapper._

object User extends User with MetaMegaProtoUser[User] {
	//stuff goes here
}

class User extends MegaProtoUser[User] {
	def getSingleton = User //what's the "meta" server
}