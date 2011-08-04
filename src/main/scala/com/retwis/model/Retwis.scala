package com.retwis.model

import _root_.redis.clients.jedis._

object Retwis {
	val pool = new JedisPool(new JedisPoolConfig(), "localhost");

	//return a random MD5 hash value
	def getRand(): String = {
		return "Stub!"
	}

	//return TRUE if logged in
	def isLoggedIn(auth: String): Boolean = {
		val jedis = pool.getResource()
		try {
			var userid = jedis.get("auth:" + auth)
			if(userid != null) {
				if(jedis.get("uid:" + userid + ":auth") == auth) {
					pool.returnResource(jedis)
					return true
				}
			} else {
				pool.returnResource(jedis)
				return false
			}
		}
	}

	def loadUserInfo(userid: String): User = {
		val jedis = pool.getResource()
		var username = jedis.get("uid:" + userid + ":username")
		var password = jedis.get("uid:" + userid + ":password")
		if (username == null || password == null) {
			pool.returnResource(jedis)
			return null
		}
		return new User(userid, username, password)
	}

/*

function strElapsed($t) {
    $d = time()-$t;
    if ($d < 60) return "$d seconds";
    if ($d < 3600) {
        $m = (int)($d/60);
        return "$m minute".($m > 1 ? "s" : "");
    }
    if ($d < 3600*24) {
        $h = (int)($d/3600);
        return "$h hour".($h > 1 ? "s" : "");
    }
    $d = (int)($d/(3600*24));
    return "$d day".($d > 1 ? "s" : "");
}

function showPost($id) {
    $r = redisLink();
    $postdata = $r->get("post:$id");
    if (!$postdata) return false;

    $aux = explode("|",$postdata);
    $id = $aux[0];
    $time = $aux[1];
    $username = $r->get("uid:$id:username");
    $post = join(array_splice($aux,2,count($aux)-2),"|");
    $elapsed = strElapsed($time);
    $userlink = "<a class=\"username\" href=\"profile.php?u=".urlencode($username)."\">".utf8entities($username)."</a>";

    echo('<div class="post">'.$userlink.' '.utf8entities($post)."<br>");
    echo('<i>posted '.$elapsed.' ago via web</i></div>');
    return true;
}

/*
addPostsToTimeline

Merge $userid's posts with existing set of posts and sort chronologically (by PostID)
*/
function addPostsToTimeline($userid, $timeline, $start, $count) {
	$r = redisLink();
	$posts = $r->lrange("uid:$userid:posts",$start,$start+$count);
	foreach($posts as $p) {
		$timeline[] = $p;
	}
	rsort($timeline, SORT_NUMERIC);
	return($timeline);
}

/*
getRelevantPosts

Returns array of user's and followee's posts, sorted chronologically.
*/
function getRelevantPosts($userid,$start,$count) {
	$r = redisLink();
	$followees = $r->smembers("uid:"."$userid".":following"); //get userids of all followees
	$posts = $r->lrange("uid:$userid:posts",$start,$start+$count);
	foreach($followees as $f) {
		$posts = addPostsToTimeline($f, $posts, $start, $count);
	}
	
	return $posts;
}

/*
showUserPosts

Show only the user's posts if $includeFollowees if false.
Otherwise, show user's and followees' posts.
*/
function showUserPosts($userid,$start,$count,$includeFollowees) {
    $r = redisLink();
    $key = ($userid == -1) ? "global:timeline" : "uid:$userid:posts";
	if($includeFollowees)
		$posts = getRelevantPosts($userid,$start,$count);
	else
		$posts = $r->lrange($key,$start,$start+$count);
    $c = 0;
    foreach($posts as $p) {
        if (showPost($p)) $c++;
        if ($c == $count) break;
    }
    return count($posts) == $count+1;
}

/*
showUserPostsWithPagination

Shows own and followee's posts, formatted and paginated.

$username - User's names
$userid - User's id
$start - Where to start from
$count - How many to show per page
$includeFollowees - include user's followees?
*/
function showUserPostsWithPagination($username,$userid,$start,$count,$includeFollowees) {
    global $_SERVER;
    $thispage = $_SERVER['PHP_SELF'];

    $navlink = "";
    $next = $start+10;
    $prev = $start-10;
    $nextlink = $prevlink = false;
    if ($prev < 0) $prev = 0;

    $u = $username ? "&u=".urlencode($username) : "";
	if (showUserPosts($userid,$start,$count,$includeFollowees))
        $nextlink = "<a href=\"$thispage?start=$next".$u."\">Older posts &raquo;</a>";
    if ($start > 0) {
        $prevlink = "<a href=\"$thispage?start=$prev".$u."\">&laquo; Newer posts</a>".($nextlink ? " | " : "");
    }
    if ($nextlink || $prevlink)
        echo("<div class=\"rightlink\">$prevlink $nextlink</div>");
}

function showLastUsers() {
    $r = redisLink();
    $users = $r->sort("global:users", array('GET' => 'uid:*:username', 
											'DESC' => 0,
											'LIMIT' => array(0 => '0',
															 1 => '10')));
	
    echo("<div>");
	foreach($users as $u) {
        echo("<a class=\"username\" href=\"profile.php?u=".urlencode($u)."\">".utf8entities($u)."</a> ");
    }
    echo("</div><br>");
}

function getAllUsers() {
	$r = redisLink();
	
	$allusers = $r->sort("global:users", array('GET' => 'uid:*:username'));
	return $allusers;
}
*/
}