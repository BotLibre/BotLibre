/******************************************************************************
 *
 *  Copyright 2014 Paphus Solutions Inc.
 *
 *  Licensed under the Eclipse Public License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 ******************************************************************************/
package org.botlibre.knowledge;

import java.io.Serializable;

import org.botlibre.api.avatar.Avatar;
import org.botlibre.emotion.Anger;
import org.botlibre.emotion.Fear;
import org.botlibre.emotion.Happiness;
import org.botlibre.emotion.Humor;
import org.botlibre.emotion.Love;
import org.botlibre.emotion.Sentiment;
import org.botlibre.emotion.Surprise;

/**
 * This type is used as the data portion for primitive vertices.
 * Primitives are common vertices that need to be used within the system,
 * and thus need some global unique identity.
 * These include, classification, instance, relation, extends, etc.
 */
 
public class Primitive implements Serializable {
	
	private static final long serialVersionUID = 1L;

	// Bootstrap
	public static final Primitive INSTANTIATION = new Primitive("instantiation");
	public static final Primitive INSTANCE = new Primitive("instance");
	public static final Primitive RELATIONSHIP = new Primitive("relationship");
	public static final Primitive CONCEPT = new Primitive("concept");
	public static final Primitive CLASSIFICATION = new Primitive("classification");
	public static final Primitive SPECIALIZATION = new Primitive("specialization");
	public static final Primitive ANYTHING = new Primitive("anything");
	public static final Primitive NOTHING = new Primitive("nothing");
	public static final Primitive EVERYTHING = new Primitive("everything");
	public static final Primitive THING = new Primitive("thing");
	public static final Primitive ACTION = new Primitive("action");
	public static final Primitive DESCRIPTION = new Primitive("description");
	public static final Primitive TANGIBLE = new Primitive("tangible");
	public static final Primitive INTANGIBLE = new Primitive("intangible");
	public static final Primitive SENSE = new Primitive("sense");
	public static final Primitive COMPOUND_RELATIONSHIP = new Primitive("compound-relationship");
	public static final Primitive META = new Primitive("meta");
	public static final Primitive PRIMITIVE = new Primitive("primitive");
	public static final Primitive LIST = new Primitive("list");
	public static final Primitive ARRAY = new Primitive("array");
	public static final Primitive TYPE = new Primitive("type");
	public static final Primitive LENGTH = new Primitive("length");
	
	// Context, time
	public static final Primitive BIRTH = new Primitive("birth");
	public static final Primitive CONTEXT = new Primitive("context");
	public static final Primitive NEXT = new Primitive("next");
	public static final Primitive PREVIOUS = new Primitive("previous");
	public static final Primitive CURRENT = new Primitive("current");
	public static final Primitive LAST = new Primitive("last");
	public static final Primitive PARENT = new Primitive("parent");
	
	public static final Primitive DATE = new Primitive("date");
	public static final Primitive DAY = new Primitive("day");
	public static final Primitive DAY_OF_YEAR = new Primitive("day-of-year");
	public static final Primitive DAY_OF_WEEK = new Primitive("day-of-week");
	public static final Primitive MONTH = new Primitive("month");
	public static final Primitive YEAR = new Primitive("year");
	public static final Primitive WEEK = new Primitive("week");
	
	public static final Primitive JANUARY = new Primitive("january");
	public static final Primitive FEBRUARY = new Primitive("february");
	public static final Primitive MARCH = new Primitive("march");
	public static final Primitive APRIL = new Primitive("april");
	public static final Primitive MAY = new Primitive("may");
	public static final Primitive JUNE = new Primitive("june");
	public static final Primitive JULY = new Primitive("july");
	public static final Primitive AUGUST = new Primitive("august");
	public static final Primitive SEPTEMBER = new Primitive("september");
	public static final Primitive OCTOBER = new Primitive("october");
	public static final Primitive NOVEMBER = new Primitive("november");
	public static final Primitive DECEMBER = new Primitive("december");
	
	public static Primitive[] MONTHS = {JANUARY, FEBRUARY, MARCH, APRIL, MAY, JUNE, JULY, AUGUST, SEPTEMBER, OCTOBER, NOVEMBER, DECEMBER};
	
	public static final Primitive MONDAY = new Primitive("monday");
	public static final Primitive TUESDAY = new Primitive("tuesday");
	public static final Primitive WEDNESDAY = new Primitive("wednesday");
	public static final Primitive THURSDAY = new Primitive("thursday");
	public static final Primitive FRIDAY = new Primitive("friday");
	public static final Primitive SATURDAY = new Primitive("saturday");
	public static final Primitive SUNDAY = new Primitive("sunday");
	
	public static Primitive[] DAYS_OF_WEEK = {SUNDAY, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY};

	public static final Primitive TIME = new Primitive("time");
	public static final Primitive HOUR = new Primitive("hour");
	public static final Primitive MINUTE = new Primitive("minute");
	public static final Primitive SECOND = new Primitive("second");
	public static final Primitive MILLISECOND = new Primitive("millisecond");
	public static final Primitive AM_PM = new Primitive("am-pm");
	public static final Primitive AM = new Primitive("am");
	public static final Primitive PM = new Primitive("pm");
	public static final Primitive TIMEZONE = new Primitive("timezone");
	
	// Users
	public static final Primitive ADMINISTRATOR = new Primitive("administrator");
	public static final Primitive ANONYMOUS = new Primitive("anonymous");
	public static final Primitive IP = new Primitive("ip");
	
	// Language
	public static final Primitive WORD = new Primitive("word");
	public static final Primitive COMPOUND_WORD = new Primitive("compound-word");
	public static final Primitive MEANING = new Primitive("meaning");
	public static final Primitive SENTENCE = new Primitive("sentence");
	public static final Primitive FRAGMENT = new Primitive("fragment");
	public static final Primitive PARAGRAPH = new Primitive("paragraph");
	public static final Primitive RESPONSE = new Primitive("response");
	public static final Primitive ASSOCIATED = new Primitive("associated");
	public static final Primitive REDUCTION = new Primitive("reduction");
	
	public static final Primitive NOUN = new Primitive("noun");
	public static final Primitive PRONOUN = new Primitive("pronoun");
	public static final Primitive ARTICLE = new Primitive("article");
	public static final Primitive VERB = new Primitive("verb");
	public static final Primitive ADVERB = new Primitive("adverb");
	public static final Primitive INTERJECTION = new Primitive("interjection");
	public static final Primitive DETERMINER = new Primitive("determiner");
	public static final Primitive NUMERAL = new Primitive("numeral");
	public static final Primitive ORDINAL = new Primitive("ordinal");
	public static final Primitive ADJECTIVE = new Primitive("adjective");
	public static final Primitive PUNCTUATION = new Primitive("punctuation");
	public static final Primitive QUESTION = new Primitive("question");
	public static final Primitive PLURIAL = new Primitive("plurial");
	public static final Primitive CARDINALITY = new Primitive("cardinality");
	public static final Primitive TYPO = new Primitive("typo");

	public static final Primitive RESPONSE_QUESTION = new Primitive("responsequestion");
	public static final Primitive TOPIC_QUESTION = new Primitive("topicquestion");

	public static final Primitive TENSE = new Primitive("tense");
	public static final Primitive PAST = new Primitive("past");
	public static final Primitive PRESENT = new Primitive("present");
	public static final Primitive FUTURE = new Primitive("future");
	public static final Primitive CONJUGATION = new Primitive("conjugation");
	public static final Primitive SYNONYM = new Primitive("synonym");
	public static final Primitive ANTONYM = new Primitive("antonym");
	public static final Primitive PLURAL = new Primitive("plural");
	public static final Primitive SINGULAR = new Primitive("singular");
	public static final Primitive POSSESSIVE = new Primitive("possessive");
	public static final Primitive POSSESSIVEPRONOUN = new Primitive("possessivepronoun");
	public static final Primitive REFLEXIVE = new Primitive("reflexive");
	public static final Primitive SUBJECTIVE = new Primitive("subjective");
	public static final Primitive OBJECTIVE = new Primitive("objective");
	
	public static final Primitive GENDER = new Primitive("gender");
	public static final Primitive MALE = new Primitive("male");
	public static final Primitive FEMALE = new Primitive("female");
		
	public static final Primitive WHAT = new Primitive("what");
	public static final Primitive WHERE = new Primitive("where");
	public static final Primitive WHEN = new Primitive("when");
	public static final Primitive WHY = new Primitive("why");
	public static final Primitive WHO = new Primitive("who");
	public static final Primitive HOW = new Primitive("how");
	
	public static final Primitive LANGUAGE = new Primitive("language");
	public static final Primitive PITCH = new Primitive("pitch");
	public static final Primitive SPEECHRATE = new Primitive("speechrate");
	
	public static final Primitive UNKNOWN = new Primitive("unknown");
	public static final Primitive KNOWN = new Primitive("known");
	public static final Primitive NOT = new Primitive("not");
	public static final Primitive TRUE = new Primitive("true");
	public static final Primitive FALSE = new Primitive("false");
	
	public static final Primitive SPEAKER = new Primitive("speaker");
	public static final Primitive TARGET = new Primitive("target");
	public static final Primitive INPUT = new Primitive("input");
	public static final Primitive MIMIC = new Primitive("mimic");
	public static final Primitive INPUT_VARIABLE = new Primitive("input-varaible");
	public static final Primitive CONVERSATION = new Primitive("conversation");
	
	public static final Primitive IS = new Primitive("is");
	public static final Primitive HAVE = new Primitive("have");
	
	public static final Primitive I = new Primitive("i");
	public static final Primitive YOU = new Primitive("you");
	public static final Primitive HIS = new Primitive("his");
	public static final Primitive HER = new Primitive("her");
	public static final Primitive IT = new Primitive("it");
	public static final Primitive THIS = new Primitive("this");
	public static final Primitive THAT = new Primitive("that");
	public static final Primitive THESE = new Primitive("these");
	public static final Primitive THEY = new Primitive("they");
	public static final Primitive OUR = new Primitive("our");
	
	public static final Primitive HE = new Primitive("he");
	public static final Primitive SHE = new Primitive("she");
	
	public static final Primitive COMMA = new Primitive("comma");
	public static final Primitive QUOTE = new Primitive("quote");
	public static final Primitive PERIOD = new Primitive("period");
	public static final Primitive EXCLAMATION = new Primitive("exclamation");
	public static final Primitive QUESTION_MARK = new Primitive("question-mark");
	public static final Primitive SPACE = new Primitive("space");
	public static final Primitive CASESENSITVE = new Primitive("casesensitive");
	public static final Primitive LEARN = new Primitive("learn");
	public static final Primitive EVAL = new Primitive("eval");
	public static final Primitive EVALCOPY = new Primitive("evalcopy");
	
	public static final Primitive THE = new Primitive("the");
	public static final Primitive A = new Primitive("a");	
	public static final Primitive NAME = new Primitive("name");
	
	public static final Primitive CORRECTION = new Primitive("correction");
	public static final Primitive OFFENDED = new Primitive("offended");
	public static final Primitive OFFENSIVE = new Primitive("offensive");
	public static final Primitive KEYWORD = new Primitive("keyword");
	public static final Primitive KEYQUESTION = new Primitive("keyquestion");
	public static final Primitive REQUIRED = new Primitive("required");
	public static final Primitive REQUIRED_TEXT = new Primitive("requiredtext");
	public static final Primitive EXCLUSIVE = new Primitive("exclusive");
	public static final Primitive REQUIRE = new Primitive("require");
	public static final Primitive NOREPEAT = new Primitive("norepeat");
	public static final Primitive ONREPEAT = new Primitive("onrepeat");
	public static final Primitive LABEL = new Primitive("label");
	public static final Primitive COMMAND = new Primitive("command");
	
	public static final Primitive GREETING = new Primitive("greeting");
	public static final Primitive ENABLED = new Primitive("enabled");
	public static final Primitive DEFAULT_SEQUENTIAL = new Primitive("default-sequential");
	public static final Primitive LANGUAGESTATE = new Primitive("languagestate");
	public static final Primitive EMOTE = new Primitive("emote");
	public static final Primitive VOICE = new Primitive("voice");
	public static final Primitive NATIVEVOICE = new Primitive("nativevoice");
	public static final Primitive NATIVEVOICENAME = new Primitive("nativevoicename");
	public static final Primitive NATIVEVOICEAPIKEY = new Primitive("nativevoiceapikey");
	public static final Primitive NATIVEVOICEAPPID = new Primitive("nativevoiceappid");
	public static final Primitive VOICEAPIENDPOINT = new Primitive("voiceapiendpoint");
	public static final Primitive LEARNING = new Primitive("learning");
	public static final Primitive MAXRESPONSEMATCHPROCESS = new Primitive("maxresponsematchprocess");
	public static final Primitive MAXSTATEPROCESS = new Primitive("maxstateprocess");
	public static final Primitive LEARNINGRATE = new Primitive("learningrate");
	public static final Primitive DISCUSSIONMATCHPERCENTAGE = new Primitive("discussionmatchpercentage");	
	public static final Primitive CONVERSATIONMATCHPERCENTAGE = new Primitive("conversationmatchpercentage");
	public static final Primitive ENABLERESPONSEMATCH = new Primitive("enableresponsematch");
	public static final Primitive CHECKEXACTMATCHFIRST = new Primitive("checkexactmatchfirst");
	public static final Primitive LEARNGRAMMAR = new Primitive("learngrammar");
	public static final Primitive FIXFORMULACASE = new Primitive("fixformulacase");

	public static final Primitive UNKNOWNWORD = new Primitive("unknownword");
	public static final Primitive UNKOWNWORD = new Primitive("unkownword"); // todo - unknownword - typo

	// HTTP
	public static final Primitive URL = new Primitive("url");
	public static final Primitive TOPIC = new Primitive("topic");
	public static final Primitive CONTENT = new Primitive("content");
	
	// HTML
	public static final Primitive TAG = new Primitive("tag");	
	public static final Primitive BR = new Primitive("br");
	
	// Self awareness
	public static final Primitive SELF = new Primitive("self");

	// Self programming model
	public static final Primitive SELF2 = new Primitive("self2");
	public static final Primitive SELF4 = new Primitive("self4");
	public static final Primitive COMMENT = new Primitive("comment");
	public static final Primitive SOURCECODE = new Primitive("sourcecode");
	public static final Primitive RULE = new Primitive("rule");
	public static final Primitive VARIABLE = new Primitive("variable");
	public static final Primitive PREDICATE = new Primitive("predicate");
	public static final Primitive QUOTIENT = new Primitive("quotient");
	public static final Primitive POSSIBLE_QUOTIENT = new Primitive("possible-quotient");
	public static final Primitive EQUATION = new Primitive("equation");
	public static final Primitive OPERATOR = new Primitive("operator");
	public static final Primitive RELATION = new Primitive("relation");
	public static final Primitive RELATED = new Primitive("related");
	public static final Primitive ASSOCIATE = new Primitive("associate");
	public static final Primitive WEAKASSOCIATE = new Primitive("weakassociate");
	public static final Primitive DISSOCIATE = new Primitive("dissociate");
	public static final Primitive AND = new Primitive("and");
	public static final Primitive OR = new Primitive("or");
	public static final Primitive CONDITION = new Primitive("condition");
	public static final Primitive ARGUMENT = new Primitive("argument");
	public static final Primitive STATE = new Primitive("state");
	public static final Primitive IF = new Primitive("if");
	public static final Primitive GREATER = new Primitive("greater");
	public static final Primitive LESS = new Primitive("less");
	public static final Primitive EQUAL = new Primitive("equal");
	public static final Primitive CASE = new Primitive("case");
	public static final Primitive AS = new Primitive("as");
	public static final Primitive THEN = new Primitive("then");
	public static final Primitive GOTO = new Primitive("goto");
	public static final Primitive FINALLY = new Primitive("finally");
	public static final Primitive PUSH = new Primitive("push");
	public static final Primitive POP = new Primitive("pop");
	public static final Primitive RETURN = new Primitive("return");
	public static final Primitive BREAK = new Primitive("break");
	public static final Primitive CONTINUE = new Primitive("continue");
	public static final Primitive ELSE = new Primitive("else");
	public static final Primitive ELSEIF = new Primitive("elseif");
	public static final Primitive FOR = new Primitive("for");
	public static final Primitive WHILE = new Primitive("while");
	public static final Primitive DO = new Primitive("do");
	public static final Primitive GET = new Primitive("get");
	public static final Primitive SET = new Primitive("set");
	public static final Primitive ADD = new Primitive("add");
	public static final Primitive REMOVE = new Primitive("remove");
	public static final Primitive ALL = new Primitive("all");
	public static final Primitive APPEND = new Primitive("append");
	public static final Primitive ASSIGN = new Primitive("assign");
	public static final Primitive INSTANCEOF = new Primitive("instanceof");
	public static final Primitive NEW = new Primitive("new");
	public static final Primitive SOURCE = new Primitive("source");
	public static final Primitive LINE_NUMBER = new Primitive("line-number");
	public static final Primitive NULL = new Primitive("null");
	public static final Primitive CALL = new Primitive("call");
	public static final Primitive DEFINE = new Primitive("define");
	public static final Primitive REDIRECT = new Primitive("redirect");
	public static final Primitive TEMPLATE = new Primitive("template");
	public static final Primitive RANDOM = new Primitive("random");
	public static final Primitive DEBUG = new Primitive("debug");
	public static final Primitive FINEST = new Primitive("finest");
	public static final Primitive FINER = new Primitive("finer");
	public static final Primitive FINE = new Primitive("fine");
	public static final Primitive INFO = new Primitive("info");
	public static final Primitive WARNING = new Primitive("warning");
	public static final Primitive SEVERE = new Primitive("severe");
	public static final Primitive INDEX = new Primitive("index");
	public static final Primitive LASTINDEX = new Primitive("lastindex");
	public static final Primitive UPPERCASE = new Primitive("uppercase");
	public static final Primitive LOWERCASE = new Primitive("lowercase");
	public static final Primitive FORMAT = new Primitive("format");
	public static final Primitive FORMAL = new Primitive("formal");
	public static final Primitive PERSON = new Primitive("person");
	public static final Primitive PERSON2 = new Primitive("person2");
	public static final Primitive EXPLODE = new Primitive("explode");
	public static final Primitive NORMALIZE = new Primitive("normalize");
	public static final Primitive DENORMALIZE = new Primitive("denormalize");
	public static final Primitive THINK = new Primitive("think");
	public static final Primitive ELEMENT = new Primitive("element");
	public static final Primitive DATA = new Primitive("data");

	// AIML specific.
	public static final Primitive AIML = new Primitive("aiml");
	public static final Primitive SRAI = new Primitive("srai");
	public static final Primitive SRAIX = new Primitive("sraix");
	public static final Primitive REQUEST = new Primitive("request");
	public static final Primitive BOT = new Primitive("bot");
	public static final Primitive LIMIT = new Primitive("limit");
	public static final Primitive SERVICE = new Primitive("service");
	public static final Primitive APIKEY = new Primitive("apikey");
	public static final Primitive BOTID = new Primitive("botid");
	public static final Primitive SERVER = new Primitive("server");
	public static final Primitive HINT = new Primitive("hint");
	public static final Primitive DEFAULT = new Primitive("default");
	
	public static final Primitive PANNOUS = new Primitive("pannous");
	public static final Primitive FORGE = new Primitive("forge");
	public static final Primitive WIKIDATA = new Primitive("wikidata");
	public static final Primitive FREEBASE = new Primitive("freebase");
	public static final Primitive BOTLIBRE = new Primitive("botlibre");
	public static final Primitive WIKTIONARY = new Primitive("wiktionary");
	public static final Primitive BOTLIBRETWITTER = new Primitive("botlibretwitter");
	public static final Primitive PAPHUS = new Primitive("paphus");

	public static final Primitive XML = new Primitive("xml");
	public static final Primitive JSON = new Primitive("json");
	public static final Primitive HTML = new Primitive("html");
	public static final Primitive ROOT = new Primitive("root");
	
	// Learning
	public static final Primitive FORMULA = new Primitive("formula");
	public static final Primitive PATTERN = new Primitive("pattern");
	public static final Primitive WILDCARD = new Primitive("wildcard");
	public static final Primitive THATWILDCARD = new Primitive("thatwildcard");
	public static final Primitive TOPICWILDCARD = new Primitive("topicwildcard");
	public static final Primitive UNDERSCORE = new Primitive("underscore");
	public static final Primitive HATWILDCARD = new Primitive("hatwildcard");
	public static final Primitive POUNDWILDCARD = new Primitive("poundwildcard");
	public static final Primitive PRECEDENCE = new Primitive("precedence");
	
	// Math
	public static final Primitive CONTAINS = new Primitive("contains");
	public static final Primitive FINGER = new Primitive("finger");
	public static final Primitive INTEGER = new Primitive("integer");
	public static final Primitive NUMBER = new Primitive("number");
	public static final Primitive DECIMAL = new Primitive("decimal");
	public static final Primitive NEGATIVE = new Primitive("negative");
	public static final Primitive DIGIT = new Primitive("digit");
	public static final Primitive SEQUENCE = new Primitive("sequence");
	public static final Primitive EXPRESSION = new Primitive("expression");
	public static final Primitive REGEX = new Primitive("regex");
	
	public static final Primitive PLUS = new Primitive("plus");
	public static final Primitive MINUS = new Primitive("minus");
	public static final Primitive MULTIPLY = new Primitive("multiply");
	public static final Primitive DIVIDE = new Primitive("divide");

	public static final Primitive INCREMENT = new Primitive("increment");
	public static final Primitive DECREMENT = new Primitive("decrement");

	public static final Primitive PI = new Primitive("pi");
	
	public static final Primitive POWER = new Primitive("power");
	public static final Primitive SQRT = new Primitive("sqrt");
	public static final Primitive ABS = new Primitive("abs");
	public static final Primitive SIN = new Primitive("sin");
	public static final Primitive COS = new Primitive("cos");
	public static final Primitive TAN = new Primitive("tan");
	public static final Primitive ASIN = new Primitive("asin");
	public static final Primitive ACOS = new Primitive("acos");
	public static final Primitive ATAN = new Primitive("atan");
	public static final Primitive ATAN2 = new Primitive("atan2");
	public static final Primitive COSH = new Primitive("cosh");
	public static final Primitive SINH = new Primitive("sinh");
	public static final Primitive TANH = new Primitive("tanh");
	public static final Primitive FLOOR = new Primitive("floor");
	public static final Primitive CEIL = new Primitive("ceil");
	public static final Primitive ROUND = new Primitive("round");
	public static final Primitive LOG = new Primitive("log");
	public static final Primitive LN = new Primitive("ln");
	public static final Primitive MAX = new Primitive("max");
	public static final Primitive MIN = new Primitive("min");
	
	public static final Primitive SYMBOL = new Primitive("symbol");
	public static final Primitive VALUE = new Primitive("value");
	public static final Primitive OPERATION = new Primitive("operation");
	public static final Primitive MATHFUNCTION = new Primitive("mathfunction");
	public static final Primitive FUNCTION = new Primitive("function");
	public static final Primitive INFINITY = new Primitive("infinity");
	public static final Primitive NINFINITY = new Primitive("ninfinity");
	public static final Primitive UNDEFINED = new Primitive("undefined");

	public static final Primitive COMPARISON = new Primitive("comparison");
	public static final Primitive EQUALS = new Primitive("equals");
	public static final Primitive NOTEQUALS = new Primitive("notequals");
	public static final Primitive LESSTHAN = new Primitive("lessthan");
	public static final Primitive GREATERTHAN = new Primitive("greaterthan");
	public static final Primitive LESSTHANEQUAL = new Primitive("lessthanequal");
	public static final Primitive GREATERTHANEQUAL = new Primitive("greaterthanequal");

	public static final Primitive BRACKET = new Primitive("bracket");
	public static final Primitive LEFTBRACKET = new Primitive("leftbracket");
	public static final Primitive RIGHTBRACKET = new Primitive("rightbracket");
	
	// Facebook
	public static final Primitive POST = new Primitive("post");
	public static final Primitive LIKEKEYWORDS = new Primitive("likekeywords");
	public static final Primitive AUTOFRIEND = new Primitive("autofriend");
	public static final Primitive RSS = new Primitive("rss");
	public static final Primitive AUTOPOST = new Primitive("autopost");
	public static final Primitive AUTOPOSTHOURS = new Primitive("autoposthours");
	public static final Primitive AUTOPOSTS = new Primitive("autoposts");
	public static final Primitive AUTOFRIENDKEYWORDS = new Primitive("autofriendkeywords");
	public static final Primitive LASTPOST = new Primitive("lastpost");
	public static final Primitive PAGE = new Primitive("page");
	
	public static final Primitive FACEBOOKMESSENGER = new Primitive("facebookmessenger");
	public static final Primitive FACEBOOK = new Primitive("facebook");
	public static final Primitive SMS = new Primitive("sms");
	public static final Primitive IVR = new Primitive("ivr");
	public static final Primitive TELEGRAM = new Primitive("telegram");
	public static final Primitive DISCORD = new Primitive("discord");
	public static final Primitive TWITTER = new Primitive("twitter");
	
	//Instagram
	public static final Primitive IMAGEURLS = new Primitive("imageURLS");
	public static final Primitive CAPTIONS = new Primitive("captions");
	//public static final Primitive ANSWEREDCOMMENTS = new Primitive("answeredComments");
	public static final Primitive COMMENTKEYWORDS = new Primitive("commentKeywords");
	public static final Primitive LASTIGCOMMENT = new Primitive("lastIGComment");
	
	
	public static final Primitive INSTAGRAM = new Primitive("instagram");
	
	// Twitter
	public static final Primitive TWITTERADDRESS = new Primitive("twitteraddress");
	public static final Primitive TWEET = new Primitive("tweet");
	public static final Primitive DIRECTMESSAGE = new Primitive("directmessage");
	public static final Primitive TREND = new Primitive("trend");
	public static final Primitive LASTMENTION = new Primitive("lastmention");
	public static final Primitive LASTSEARCH = new Primitive("lastsearch");
	public static final Primitive LASTAUTOFOLLOWSEARCH = new Primitive("lastautofollowsearch");
	public static final Primitive LASTRSS = new Primitive("lastrss");
	public static final Primitive LASTTWEET = new Primitive("lasttweet");
	public static final Primitive LASTLEARN = new Primitive("lastlearn");
	public static final Primitive LASTTIMELINE = new Primitive("lasttimeline");
	public static final Primitive LASTNEWSFEED = new Primitive("lastnewsfeed");
	public static final Primitive LASTCOMMENT = new Primitive("lastcomment");
	public static final Primitive LASTDIRECTMESSAGE = new Primitive("lastdirectmessage");
	public static final Primitive ID = new Primitive("id");
	public static final Primitive MESSAGE = new Primitive("message");
	public static final Primitive GID = new Primitive("gid");
	public static final Primitive CREATEDAT = new Primitive("createdat");
	public static final Primitive USER = new Primitive("user");
	public static final Primitive SECRET = new Primitive("secret");
	public static final Primitive TOKEN = new Primitive("token");
	public static final Primitive TOKENEXPIRY = new Primitive("tokenexpiry");
	public static final Primitive FOLLOWED = new Primitive("followed");
	public static final Primitive WELCOME = new Primitive("welcome");
	public static final Primitive AUTOFOLLOW = new Primitive("autofollow");
	public static final Primitive AUTOFOLLOWFRIENDSFRIENDS = new Primitive("autofollowfriendsfriends");
	public static final Primitive AUTOFOLLOWFRIENDSFOLLOWERS = new Primitive("autofollowfriendsfollowers");
	public static final Primitive FOLLOWMESSAGES = new Primitive("followmessages");
	public static final Primitive MAXFRIENDS = new Primitive("maxfriends");
	public static final Primitive MAXSTATUSCHECKS = new Primitive("maxstatuschecks");
	public static final Primitive MAXTIMELNE = new Primitive("maxtimeline");
	public static final Primitive PROCESSSTATUS = new Primitive("processstatus");
	public static final Primitive STATUSKEYWORDS = new Primitive("statuskeywords");
	public static final Primitive NEWSFEEDKEYWORDS = new Primitive("newsfeedkeywords");
	public static final Primitive TWEETCHATS = new Primitive("tweetChats");
	public static final Primitive REPLYTOMENTIONS = new Primitive("replytomentions");
	public static final Primitive REPLYTOMESSAGES = new Primitive("replytomessages");
	public static final Primitive RETWEET = new Primitive("retweet");
	public static final Primitive TWEETRSS = new Primitive("tweetrss");
	public static final Primitive RSSKEYWORDS = new Primitive("rsskeywords");
	public static final Primitive TWEETSEARCH = new Primitive("tweetsearch");
	public static final Primitive AUTOFOLLOWSEARCH = new Primitive("autofollowsearch");
	public static final Primitive AUTOFOLLOWKEYWORDS = new Primitive("autofollowkeywords");
	public static final Primitive AUTOTWEET = new Primitive("autotweet");
	public static final Primitive AUTOTWEETHOURS = new Primitive("autotweethours");
	public static final Primitive AUTOTWEETS = new Primitive("autotweets");
	public static final Primitive GROUP = new Primitive("group");

	// Email
	public static final Primitive EMAIL = new Primitive("email");
	public static final Primitive PASSWORD = new Primitive("password");
	public static final Primitive EMAILADDRESS = new Primitive("emailaddress");
	public static final Primitive SSL = new Primitive("ssl");
	public static final Primitive SIGNATURE = new Primitive("signature");
	public static final Primitive INCOMINGHOST = new Primitive("incominghost");
	public static final Primitive INCOMINGPORT = new Primitive("incomingport");
	public static final Primitive OUTGOINGHOST = new Primitive("outgoinghost");
	public static final Primitive OUTGOINGPORT = new Primitive("outgoingport");
	public static final Primitive PROTOCOL = new Primitive("protocol");
	public static final Primitive LASTMESSAGE = new Primitive("lastmessage");
	
	// Timers
	public static final Primitive TIMER = new Primitive("timer");
	public static final Primitive MESSAGES = new Primitive("messages");
	
	// Chat
	public static final Primitive CHAT = new Primitive("chat");

	// Forgetfulness
	public static final Primitive COUNT = new Primitive("count");
	
	// Emotion
	public static final Primitive EMOTION = new Primitive("emotion");
	public static final Primitive ANGER = new Primitive(Anger.class.getName());
	public static final Primitive SURPRISE = new Primitive(Surprise.class.getName());
	public static final Primitive HAPPINESS = new Primitive(Happiness.class.getName());
	public static final Primitive SENTIMENT = new Primitive(Sentiment.class.getName());
	public static final Primitive LOVE = new Primitive(Love.class.getName());
	public static final Primitive FEAR = new Primitive(Fear.class.getName());
	public static final Primitive HUMOR = new Primitive(Humor.class.getName());

	// Vision
	public static final Primitive IMAGE = new Primitive("image");
	
	// Avatar
	public static final Primitive POSE = new Primitive("pose");	
	public static final Primitive AVATAR = new Primitive(Avatar.class.getName());

	// IRC
	public static final Primitive NICK = new Primitive("nick");
	public static final Primitive WHISPER = new Primitive("whisper");
	
	//WhatsApp
	public static final Primitive WHATSAPP = new Primitive("whatsapp");
	
	//Slack
	public static final Primitive SLACK = new Primitive("slack");
	
	//Skype
	public static final Primitive SKYPE = new Primitive("skype");
	
	//WeChat
	public static final Primitive WECHAT = new Primitive("wechat");
	
	//Kik
	public static final Primitive KIK = new Primitive("kik");
	
	//Alexa
	public static final Primitive ALEXA = new Primitive("alexa");
	
	//Google Assistant
	public static final Primitive GOOGLEASSISTANT = new Primitive("google-assistant");
	
	private String identity;
	  
	public Primitive() {
	}
  
	public Primitive(String identity) {
		if (identity == null) {
			identity = "null";
		}
		this.identity = identity;
	}

	public int hashCode() {
		return this.identity.hashCode();
	}
	
	public boolean equals(Object primitive) {
		if (primitive == this) {
			return true;
		}

		if (! (primitive instanceof Primitive)) {
			return false;
		}

		return ((Primitive) primitive).identity.equals(this.identity);
	}
  
	public String getIdentity() {
		return identity;
	}

	public String toString() {
		return "#" + this.identity;
	}
  
}