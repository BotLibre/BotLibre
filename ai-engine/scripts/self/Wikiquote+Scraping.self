/**
 * This example shows how to scrape quotes from Wikiquotes.
 */
state Wikiquotes {
    pattern "quote" template quote();
    pattern "init data" template initData();
    
    function quote() {
        if (!#quotes.hasAny(#quote)) {
            initData();
        }
        return random (#quotes.quote[0], #quotes.quote[1], #quotes.quote[2]);
    }
    
    function initData() {
        var quote = Http.requestHTML("https://en.wikiquote.org/wiki/Fight_Club_(film)", "body/div[3]/div[3]/div[4]/ul[1]/li");
        #quotes.quote = quote;
        quote = Http.requestHTML("https://en.wikiquote.org/wiki/Fight_Club_(film)", "body/div[3]/div[3]/div[4]/ul[2]/li");
        #quotes.quote =+ quote;
        quote = Http.requestHTML("https://en.wikiquote.org/wiki/Fight_Club_(film)", "body/div[3]/div[3]/div[4]/ul[3]/li");
        #quotes.quote =+ quote;
        
        return Language.details(#quotes);
    }
}
