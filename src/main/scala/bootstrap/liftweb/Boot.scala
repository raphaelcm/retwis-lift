
package bootstrap.liftweb

import com.retwis.model._
import com.retwis.api._
import net.liftweb._
import util._
import Helpers._

import common._
import http._
import sitemap._
import Loc._


/**
 * A class that's instantiated early and run.  It allows the application
 * to modify lift's environment
 */
class Boot {
  def boot {
    // where to search snippet
    LiftRules.addToPackages("com.retwis")

    // Build SiteMap
    val entries = List(
      		Menu.i("Home") / "index", // the simple way to declare a menu
			Menu.i("Timeline") / "timeline", // the simple way to declare a menu
			Menu.i("User") / "user", // the simple way to declare a menu
		

      // more complex because this menu allows anything in the
      // /static path to be visible
      Menu(Loc("Static", Link(List("static"), true, "/static/index"), 
         "Static Content")))

    // set the sitemap.  Note if you don't want access control for
    // each page, just comment this line out.
    LiftRules.setSiteMap(SiteMap(entries:_*))

    //Show the spinny image when an Ajax call starts
    LiftRules.ajaxStart =
      Full(() => LiftRules.jsArtifacts.show("ajax-loader").cmd)
    
    // Make the spinny image go away when it ends
    LiftRules.ajaxEnd =
      Full(() => LiftRules.jsArtifacts.hide("ajax-loader").cmd)

    // Use jQuery 1.4
    LiftRules.jsArtifacts = net.liftweb.http.js.jquery.JQuery14Artifacts

    // Force the request to be UTF-8
    LiftRules.early.append(_.setCharacterEncoding("UTF-8"))

	//Logged in/out test
	LiftRules.loggedInTest = Full( 
	  () => {
	    	Retwis.isLoggedIn()
	  }
	)
	
	LiftRules.dispatch.append {
	  case Req("follow" :: "id" :: followId :: Nil, _, _) =>
	    Retwis.follow(followId)
	}

    // Use HTML5 for rendering
/* //commented out so I can use mixed case in my snippets
    LiftRules.htmlProperties.default.set((r: Req) =>
      new Html5Properties(r.userAgent))
*/
  }
}

