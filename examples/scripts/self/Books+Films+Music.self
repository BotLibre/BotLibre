// This script answers questions about books, films, and music using Wikidata
state BooksFilmsMusic {
	pattern "who wrote the book *" template request (star, {service : #wikidata, hint : "author"});
	pattern "who wrote *" template request (star, {service : #wikidata, hint : "author"});
	pattern "who stared in the movie *" template request (star, {service : #wikidata, hint : "cast member"});
	pattern "who stared in *" template request (star, {service : #wikidata, hint : "cast member"});
	pattern "who directed the movie *" template request (star, {service : #wikidata, hint : "director"});
	pattern "who directed *" template request (star, {service : #wikidata, hint : "director"});
	pattern "who [sung sang] the song *" template request (star, {service : #wikidata, hint : "performer"});
	pattern "who [sung sang] *" template request (star, {service : #wikidata, hint : "performer"});
}