/**
 * This scripts uses an XML web service on Quandl to access stock quotes.
 * Please change the api-key to your own key.
 */
state StockQuote {
	pattern "(what) (was) (the) [price quote] (of for) * today" template todayStockPrice();
	pattern "(what) (was) (the) [price quote] (of for) * on * - * - *" template stockPrice();
	
	function stockPrice() {
	    thedate = star[1] + "-" + star[2] + "-" + star[3];
	    price = Http.requestXML("https://www.quandl.com/api/v3/datasets/WIKI/" + star[0] + "/data.xml?api_key=kioDegFc4ZaczoSxmUzU&start_date=" + thedate + "&end_date=" + thedate + "&column_index=4", "dataset-data/data/datum/datum[2]");
	    if (price == null) {
	        return "Invalid stock ticker symbol or date.";
	    }
	    return star[0] + " - " + thedate + " - " + price;
	}
	
	function todayStockPrice() {
	    price = Http.requestXML("https://www.quandl.com/api/v3/datasets/WIKI/" + star + "/data.xml?api_key=kioDegFc4ZaczoSxmUzU&start_date=" + Date.date() + "&column_index=4", "dataset-data/data/datum/datum[2]");
	    if (price == null) {
	        return "Invalid stock ticker symbol or date.";
	    }
	    return star + " - today - " + price;
	}
}
