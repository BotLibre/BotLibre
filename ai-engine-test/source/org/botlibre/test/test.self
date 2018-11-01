// test comment
state TestSelf {
	// test comment
	pattern "date" template Template("{Date.date()}");
	
	pattern "hours * *" template hours();
	
	pattern "time" template Template("{Date.time()}");
	
	pattern "timestamp" template Template("{Date.timestamp()}");
	// test comment
	pattern "timezone" template Template("{Date.getTimeZone()}");
	
	pattern "any date *" template Template("{Date.any(star)}");
	/* test comment */
	pattern "email * * *" template email();
	
	pattern "tweet *" template tweet();
	
	pattern "facebook *" template facebook();
	/*
	Test comment
	*/
	pattern "telegram *" template telegram();
	
	pattern "define *" template wiktionary();
	
	pattern "lookup * on *" template wikidata2();
	
	pattern "lookup *" template wikidata();
	
	pattern "rss *" template rss();
	
	pattern "rss feed *" template rssFeed();
	
	pattern "xml * *" template xml2();
	
	pattern "xml *" template xml();
	
	pattern "xpath * *" template xpath();
	
	pattern "html list * * *" template htmlList();
	
	pattern "html text * *" template htmlText();
	
	pattern "html xml * *" template htmlXML();
	
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
	
	pattern "test dates" template testDates();
	
	pattern "test operations" template testOperations();
	
	pattern "test language" template testLanguage();
	
	pattern "test arrays" template testArrays();
	
	pattern "test empty" template testEmpty();
	
	pattern "test conversation" template testConversation();
	
	pattern "test * equals hello world" template testStarEqualHelloWorld();
	
	pattern "test * equals *" template testStarEquals();
	
	pattern "^" topic "empty" template "success";
	
	pattern "test nested json" template testNestedJSON();
	
	pattern "/\d+" template "number";
	
	pattern "/[-+]?[0-9]*\.?[0-9]+" template "float";
	
	pattern "another #number" template "number";
	
	pattern "/\b\d[\d,.]+\b" template "numeric";
	
	pattern "/.+\@.+\..+" template "email";
	
	pattern "another #email" template "email";
	
	pattern "another #url" template "url";
	
	pattern "/^(19|20)\d\d[-/.](0[1-9]|1[012])[-/.](0[1-9]|[12][0-9]|3[01])$" template "date";
		
	pattern "/\d+ / /\d+" template "{star[0].toNumber() / star[1].toNumber()}";
	
	pattern "^ /\d+ \* /\d+ ^" template "{star[1].toNumber() * star[2].toNumber()}";
	
	pattern "/(?i)what\sis\s(.*)" template "I have no idea what {star} is.";
	
	// test comment
	var foo {
		// test comment
		name : "foo";
	}
	
	function testStarEqualHelloWorld() {
		if (star == "hello world") {
			return star;
		}
		return "fail";
	}
	
	function testStarEquals() {
		if (star[0] == star[1]) {
			return star;
		}
		return "fail";
	}
	
	function testEmpty() {
		return Template("");
	}
	
	// test comment
	function testLanguage() {
		// test comment
		x = 123;
		if ("hello 123" != Template("hello {x}")) {
			return "fail1";
		}
		learn ({pattern: "email-template", template: Template("hello {x}")});
		if ((srai "email-template") != "hello 123") {
			return "fail2";
		}
		x = 456;
		if ((srai "email-template") != "hello 456") {
			return "fail3";
		}
		Language.learn("email-template2", "hello world");
		if ((srai "email-template2") != "hello world") {
			return "fail4";
		}
		
		var indexes = Language.keywordIndexes("a hot hello world grass green b x loves hats", "a hat love hello");
		if (indexes[0] != 0) {
			// test comment
			return indexes[0];
		} else if (indexes[1] != 2) {
			return indexes[1];
		} else if (indexes[2] != 8) {
			return indexes[2];
		} else if (indexes[3] != 9) {
			return indexes[3];
		}
		if (Language.sentence("hello world").word == null) {
			return "fail5";
		} else {
			// test comment
		}
		return "ok";
	}
	
	function testArrays() {
		var array = new Array();
		array.set(#element, "a", 0);
		array[1] = "b";
		var index = 2;
		array[index] = "c";
		if (array[0] != "a") {
			return "fail";
		} else if (array[1] != "b") {
			return "fail";
		} else if (array[index] != "c") {
			return "fail";
		} else if (array.element[1] != "b") {
			return "fail";
		}
		var index = 0;
		for (element in array.element) {
			index++;
		}
		if (index != 3) {
			return "fail";
		}
		index = 0;
		for (element in array) {
			index++;
		}
		if (index != 3) {
			return "fail";
		}
		index = 0;
		null.test = 1;
		null.test =+ 2;
		for (element in x.test) {
			index++;
		}
		if (index != 0) {
			return "fail";
		}
		index = 0;
		var object = new Object();
		object[#foo] = #bar;
		object.blee = #bla;
		for (key in object) {
			key.toString();
			if (object[key] == null) {
				return "fail";
			}
			index++;
		}
		if (index != 2) {
			return "fail";
		}
		index = 0;
		for (element in array) {
			for (element2 in array) {
				index++;
			}
		}
		if (index != 9) {
			return "fail";
		}
		index = 0;
		for (element in array) {
			for (element2 in array) {
				index++;
				break;
			}
		}
		if (index != 3) {
			return "fail";
		}
		var ok = false;
		for (index = 0; index < 10; index++) {
			ok = true;
		}
		if (index != 10 || !ok) {
			return "fail";
		}
		ok = false;
		index = 0;
		for (; index < 10;) {
			index++;
			ok = true;
		}
		if (index != 10 || !ok) {
			return "fail";
		}
		return "ok";
	}
	
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
		if (!true) {
			return "fail";
		}
		if (!false && true == false) {
		} else {
			return "fail";
		}
		if (!conversation.hasAny(#input)) {
			return "fail";
		}
		var x = new Object();
		x.y = #z;
		if (!x.has(#y, #z)) {
			return "fail";
		}
		if (x.has(#y, #q)) {
			return "fail";
		}
		if (!x.has(#y, #q)) {
		} else {
			return "fail";
		}
		if (x.has(#y, #z) != true) {
			return "fail";
		}
		if (!(x instanceof #object)) {
			return "fail";
		}
		x = new Keyword();
		if (!(x instanceof #keyword)) {
			return "fail";
		}
		if (x instanceof #object || x instanceof #word) {
			return "fail";
		}
		if ("helloworld".length() != 10) {
			return "fail";
		}
		if ("helloworld".size() != 10) {
			return "fail";
		}
		if ("helloworld".charAt(0) != "h") {
			return "fail";
		}
		if ("helloworld".charAt(5) != "w") {
			return "fail";
		}
		if ("helloworld".setCharAt(5, "x") != "helloxorld") {
			return "fail";
		}
		if ("helloworld".substring(0, 3) != "hel") {
			return "fail";
		}
		if ("helloworld".substring(3, 5) != "lo") {
			return "fail";
		}
		if ("helloworld".substring(5, 10) != "world") {
			return "fail";
		}
		if ("helloworld".substr(5, 10) != "world") {
			return "fail";
		}
		#date.toString();
		#email.toString();
		
		if (Utils.lowercase("HEllo") != "hello") {
			return "fail lowercase";
		}
		
		if (!Utils.matches("hello", "hello")) {
			return "fail matches 1";
		}
		
		if (Utils.matches("hello", "\d")) {
			return "fail matches 2";
		}
		
		if (!Utils.matches("12345", "\d+")) {
			return "fail matches 3";
		}
		
		if ("\d".test("hello")) {
			return "fail matches 4";
		}
		
		if (!"\d".test("hello 123")) {
			return "fail matches 5";
		}
		
		if ("\d+".exec("hello 123") != "123") {
			return "fail matches 6";
		}
		result = "hello 123".match("\d+");
		if (result[0] != "123") {
			return "fail matches 7";
		}
		result = "hello 123 456".match("\d+");
		if (result[1] != "456") {
			return "fail matches 8";
		}
		
		if (Utils.extract("that will cost $12345", "\d+") != "12345") {
			return "fail extract 1";
		}
		
		if (Utils.extract("my birthday is 1970-01-01", #date) != "1970-01-01") {
			return "fail extract 2";
		}
		
		if (Utils.extract("my email is joe@foo.com.au.", #email) != "joe@foo.com.au") {
			return "fail extract 3";
		}
		
		if (Utils.extract("check out http://www.foo.com.au it is a cool website", #url) != "http://www.foo.com.au") {
			return "fail extract 4";
		}
		
		debug("debug: 1");
		debug(#self, "debug: 1", conversation, conversation.topic);
		debug(#warning, "error: 1", conversation, conversation.topic);
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
		//x = {};
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
		if (Math.min(3, 5, 7, 2, 9) != 2) {
			return "fail";
		}
		if (Math.max(3, 5, 7, 2, 9) != 9) {
			return "fail";
		}
		if (Math.add("3".toNumber(), "4".toNumber()) != 7) {
			return "fail";
		}
		if (Math.add("3.2".toNumber(), "4.1".toNumber()) != 7.3) {
			return "fail";
		}
		
		return "pass";
	}
	
	function testDates() {
		var offset = 4;
		
		var today = Date.timestamp();
		var hour = Date.get(today, #hour) + offset;
		if (hour > 24) {
			hour = hour - 24;
		}
		today = Date.setTimeZone(today, "GMT");
		var gmt = Date.get(today, #hour);
		if (gmt != hour) {
			return "fail";
		}
		
		today = Date.time();
		hour = Date.get(today, #hour) + offset;
		if (hour > 24) {
			hour = hour - 24;
		}
		today = Date.setTimeZone(today, "GMT");
		gmt = Date.get(today, #hour);
		if (gmt != hour) {
			return "fail2";
		}
		var timestamp = Date.timestamp("2013-01-01 12:00:00");
		var date = Date.date(timestamp);
		if (timestamp == date) {
			return "fail3";
		}
		timestamp = Date.timestamp("2014-01-01");
		date = Date.date(timestamp);
		if (timestamp == date) {
			return "fail4";
		}
		date = Date.date("2017-01-01");
		date2 = Date.date(date);
		if (date2 != date) {
			return "fail5";
		}
		date = Date.timestamp();
		date2 = Date.add(date, #second, 5);
		diff = Date.difference(date, date2, #millisecond);
		if (diff != 5000) {
			return "fail6";
		}
		date = "2017-10-11";
		date2 = "2017-11-21";
		diff = Date.interval("days", date, date2, "yyyy-MM-dd");
		if (diff != 41) {
			return "fail7";
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
	
	function testConversation() {
		conversation.topic = "test";
		conversation.board = "_________";
		return "pass" + conversation.board;
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
		var data = WikiData.details(star);
		return data.sentence;
	}
	
	function wikidata2() {
		var data = WikiData.details(star[1]);
		return data.get(Symbol(star[0]));
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
	
	function htmlText() {
		var value = Http.requestHTML(star[0], star[1], #text);
		return value;
	}
	
	function htmlXML() {
		var value = Http.requestHTML(star[0], star[1], #xml);
		return value;
	}
	
	function htmlList() {
		var value = Http.requestHTML(star[1], star[2], #array);
		return value.get(#element, star[0].toNumber());
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
		var url = "https://www.botlibre.com/browse";
		var params = new Object();
		params.application = star[1];
		params.id = "165";
		params.message = star[0];
		var value = Http.postHTML(url, params, "//h1/span");
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
	
	function testNestedJSON() {
		var json  = { name: "Joe", age: 44, contactinfo: { phone: "123-4567", address: { city: "Ottawa", street: "123" + " " + "Main Street" } } };
		return json.contactinfo.address.street;
	}

}
