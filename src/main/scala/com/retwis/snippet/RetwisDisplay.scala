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

import _root_.scala.xml.{NodeSeq, Text, Group}
import _root_.java.util.Locale

class RetwisDisplay {
	def headerLinks (content : NodeSeq) : NodeSeq = {
		<div id="navbar">
		<a href="index">home</a>
		| <a href="timeline">timeline</a>
		
		<lift:RetwisDisplay.logoutLink />
		<br /><div class="ui-widget">
				<form name="input" action="user" method="get">
				<label for="usersearch">User Search: </label>
				<input id="usersearch" type="text" name="u" />
				<input type="submit" />
				</form>
			</div>
		</div>
	}
	
	def logoutLink (content : NodeSeq) : NodeSeq = {
		if(User.isLoggedIn()) {
			<span class="logoutlink">| <a href="logout">logout</a></span>
		} else {
			<span class="logoutlink"></span>
		}
	}
}