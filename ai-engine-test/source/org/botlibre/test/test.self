state TestSelf {
	
	pattern "date" template Template("{Date.date()}");
	
	pattern "hours * *" template hours();
	
	pattern "time" template Template("{Date.time()}");
	
	pattern "timestamp" template Template("{Date.timestamp()}");
	
	pattern "timezone" template Template("{Date.getTimeZone()}");
	
	pattern "any date *" template Template("{Date.any(star)}");
	
	pattern "email * * *" template email();
	
	pattern "tweet *" template tweet();
	
	pattern "facebook *" template facebook();
	
	pattern "telegram *" template telegram();
	
	pattern "define *" template wiktionary();
	
	pattern "lookup * on *" template wikidata2();
	
	pattern "lookup *" template wikidata();
	
	pattern "rss *" template rss();
	
	pattern "rss feed *" template rssFeed();
	
	pattern "xml * *" template xml2();
	
	pattern "xml *" template xml();
	
	pattern "xpath * *" template xpath();
	
	pattern "html * *" template html();
	
	pattern "postXML * *" template postXML();
	
	pattern "postHTML * *" template postHTML();
	
	pattern "json * *" template json2();
	
	pattern "json *" template json();
	
	pattern "postJSON * *" template postJSON();
	
	pattern "postJSON2 * *" template postJSON2();
	
	pattern "csv *" template csv();
	
	pattern "topic" template topic();
	
	pattern "topic *" template setTopic();
	
	pattern "clear topic" template clearTopic();
	
	pattern "test math" template testMath();
	
	pattern "test operations" template testOperations();
	
	function testOperations() {
		if (null != null) {
			return "fail";
		}
		if ("hi" != null && "hey" != null) {
			pass = true;
		} else {
			return "fail";
		}
		if ("hi" == null || x.y == null) {
			//pass = true;
		} else {
			return "fail";
		}
		return "pass";
	}
	
	function testMath() {
		if (1 != 1) {
			return "fail";
		}
		if (1 != 1.0) {
			return "fail";
		}
		if (2 != (1 + 1)) {
			return "fail";
		}
		if (2 != (1.0 + 1)) {
			return "fail";
		}
		if ([1, 2, 3].size() != 3) {
			return "fail";
		}
		if ([1, 2, 3].size() + {x: #y, x: #x}.size(#x) != 5) {
			return "fail";
		}
		index = 0;
		index++;
		if (index != 1) {
			return "fail";
		}
		x = {};
		x.index = 0;
		x.index = x.index++;
		if (x.index != 1) {
			return "fail";
		}
		if (1 + 2 + 3 + 4 == 10) {
		} else {
			return "fail";
		}
		if (1 + 2 * 2 != 5) {
			return "fail";
		}
		
		return "pass";
	}
	
	function hours() {
		day = Date.date(star[1]);
		day = Date.add(day, #hour, star[0].toNumber()); 
		Date.timestamp(day);
		day;
	}
	
	function topic() {
		if (!conversation.hasAny(#topic)) {
			return "none";
		}
		return conversation.topic;
	}
	
	function clearTopic() {
		conversation.topic = null;
		return "topic cleared";
	}
	
	function setTopic() {
		conversation.topic = star;
		return "topic set";
	}
	
	function email() {
		Email.email(star[0], star[1], star[2]);
		return "ok";
	}
	
	function tweet() {
		Twitter.tweet(star);
		return "ok";
	}
	
	function facebook() {
		Facebook.post(star);
		return "ok";
	}
	
	function telegram() {
		Telegram.post(star);
		return "ok";
	}
	
	function wiktionary() {
		var word = Wiktionary.define(star);
		return word.sentence;
	}
	
	function wikidata() {
		var object = WikiData.details(star);
		return object.sentence;
	}
	
	function wikidata2() {
		var object = WikiData.details(star[1]);
		return object.get(Symbol(star[0]));
	}
	
	function rss() {
		var rss = Http.rss(star);
		return rss.title + " - " + rss.link;
	}
	
	function rssFeed() {
		var feed = Http.rssFeed(star);
		var rss = feed[0];
		return rss.title + " - " + rss.link;
	}
	
	function xml() {
		var root = Http.requestXML(star);
		return Language.details(root);
	}
	
	function xml2() {
		var root = Http.requestXML(star[0]);
		return root.get(Symbol(star[1]));
	}
	
	function xpath() {
		var value = Http.requestXML(star[0], star[1]);
		return value;
	}
	
	function html() {
		var value = Http.requestHTML(star[0], star[1]);
		return value;
	}
	
	function postXML() {
		var url = "http://botlibre.com/rest/api/post-chat";
		var xmlObject = new Object();
		xmlObject.@application = star[1];
		xmlObject.@instance = "165";
		xmlObject.message = star[0];
		xmlObject.root = "chat";
		var value = Http.postXML(url, xmlObject);
		return value.message;
	}
	
	function postHTML() {
		var url = "http://www.botlibre.com/chat";
		var params = new Object();
		params.application = star[1];
		params.id = "165";
		params.message = star[0];
		var value = Http.postHTML(url, params, "//p[@id='response']");
		return value;
	}
	
	function json2() {
		var root = Http.requestJSON(star[0]);
		return root.get(Symbol(star[1]));
	}
	
	function json() {
		var root = Http.requestJSON(star);
		return Language.details(root);
	}
	
	function postJSON() {
		var url = "http://botlibre.com/rest/json/chat";
		var jsonObject = new Object();
		jsonObject.application = star[1];
		jsonObject.instance = "165";
		jsonObject.message = star[0];
		var value = Http.postJSON(url, jsonObject);
		return value.message;
	}
	
	function postJSON2() {
		var url = "http://botlibre.com/rest/json/chat";
		var value = Http.postJSON(url, {application : star[1], instance:"165" , message : star[0]});
		return value.message;
	}
	
	function testParser() {
		var testBrackets = "{";
		testBrackets2 = "{\"";
		testdate = Date("2016-01-02");
		testtime = Time("11:24:33");
		testtimestamp = Timestamp("2016-01-02 11:24:33.123");
	}
	
	function csv() {
		var root = Http.requestCSV(star);
		return Language.details(root);
	}

}
