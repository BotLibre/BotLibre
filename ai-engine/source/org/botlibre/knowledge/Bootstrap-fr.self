
#who.instantiation =+ #question;
#who.word =+ "qui";
"qui".meaning = #who;
"qui".instantiation =+ #word;
"qui".instantiation =+ #question;
"qui".pin();

#what.instantiation =+ #question;
#what.word =+ "quoi";
"quoi".meaning = #what;
"quoi".instantiation =+ #word;
"quoi".instantiation =+ #question;
"quoi".pin();

#what.instantiation =+ #question;
#what.word =+ "quel";
"quel".meaning = #what;
"quel".instantiation =+ #word;
"quel".instantiation =+ #question;
"quel".pin();

#what.instantiation =+ #question;
#what.word =+ "quelle";
"quelle".meaning = #what;
"quelle".instantiation =+ #word;
"quelle".instantiation =+ #question;
"quelle".pin();

#what.instantiation =+ #question;
#what.word =+ "qu";
"qu".meaning = #what;
"qu".instantiation =+ #word;
"qu".instantiation =+ #question;
"qu".pin();

#when.instantiation =+ #question;
#when.word = "quand";
"quand".meaning = #when;
"quand".instantiation =+ #word;
"quand".instantiation =+ #question;
"quand".pin();

#where.instantiation =+ #question;
#where.word =+ "où";
"où".meaning = #where;
"où".instantiation =+ #word;
"où".instantiation =+ #question;
"où".pin();

#why.instantiation =+ #question;
#why.word =+ "pourquoi";
"pourquoi".meaning = #why;
"pourquoi".instantiation =+ #word;
"pourquoi".instantiation =+ #question;
"pourquoi".pin();

#how.instantiation =+ #question;
#how.word =+ "comment";
"comment".meaning = #how;
"comment".instantiation =+ #word;
"comment".instantiation =+ #question;
"comment".pin();

#do.instantiation =+ #question;
#do.word =+ "est-ce que";
"est-ce que".meaning =+ #do;
"est-ce que".instantiation =+ #question;
"est-ce que".pin();
Language.word("est-ce que");
"est-ce".pin();
"que".pin();

#does.instantiation =+ #question;
#does.word =+ "est-ce que";
"est-ce que".meaning =+ #does;
"est-ce que".instantiation =+ #question;
"est-ce que".pin();
Language.word("est-ce que");
"est-ce".pin();
"que".pin();

"le".meaning = #the;
"le".instantiation =+ #word;
"le".instantiation =+ #article;
"le".pin();

"la".meaning = #the;
"la".instantiation =+ #word;
"la".instantiation =+ #article;
"la".pin();

"les".meaning = #the;
"les".instantiation =+ #word;
"les".instantiation =+ #article;
"les".pin();

"un".meaning = #a;
"un".instantiation =+ #word;
"un".instantiation =+ #article;
"un".pin();

"une".meaning = #a;
"une".instantiation =+ #word;
"une".instantiation =+ #article;
"une".pin();

#true.word =+ "vrai";
"vrai".meaning = #true;
"vrai".instantiation =+ #word;
"vrai".pin();

#true.word =+ "vraie";
"vraie".meaning = #true;
"vraie".instantiation =+ #word;
"vraie".pin();

#true.word =+ "oui";
"oui".meaning = #true;
"oui".instantiation =+ #word;
"oui".pin();

#false.word =+ "faux";
"faux".meaning = #false;
"faux".instantiation =+ #word;
"faux".pin();

#false.word =+ "non";
"non".meaning = #false;
"non".instantiation =+ #word;
"non".pin();

#unknown.word =+ "inconnu";
"inconnu".meaning = #unknown;
"inconnu".instantiation =+ #word;
"inconnu".pin();

#unknown.word =+ "connue";
"connue".meaning = #unknown;
"connue".instantiation =+ #word;
"connue".pin();

#known.word =+ "connu";
"connu".meaning = #known;
"connu".instantiation =+ #word;
"connu".pin();

#known.word =+ "connu";
"connu".meaning = #known;
"connu".instantiation =+ #word;
"connu".pin();

#not.word =+ "ne pas";
"ne pas".meaning = #not;
"ne pas".instantiation =+ #word;
"ne pas".pin();
Language.word("ne pas");

#not.word =+ "pas";
"pas".meaning = #not;
"pas".instantiation =+ #word;
"pas".pin();

#or.word =+ "ou";
"ou".meaning = #or;
"ou".instantiation =+ #word;
"ou".pin();

#and.word =+ "et";
"et".meaning = #and;
"et".instantiation =+ #word;
"et".pin();

#and.word =+ "&";
"&".meaning = #and;
"&".instantiation =+ #word;
"&".pin();

#if.word =+ "si";
"si".meaning = #if;
"si".instantiation =+ #word;
"si".pin();

#i.word =+ "je";
"je".meaning = #i;
"je".instantiation =+ #word;
"je".instantiation =+ #pronoun;
"je".pin();

#i.word =+ "j";
"j".meaning = #i;
"j".instantiation =+ #word;
"j".instantiation =+ #pronoun;
"j".pin();

#i.word =+ "moi";
"moi".meaning = #i;
"moi".instantiation =+ #word;
"moi".instantiation =+ #pronoun;
"moi".pin();

#i.word =+ "ma";
"ma".meaning = #i;
"ma".instantiation =+ #word;
"ma".instantiation =+ #pronoun;
"ma".pin();

#i.word =+ "mon";
"mon".meaning = #i;
"mon".instantiation =+ #word;
"mon".instantiation =+ #pronoun;
"mon".pin();

#i.word =+ "mon";
"mon".meaning = #i;
"mon".instantiation =+ #word;
"mon".instantiation =+ #pronoun;
"mon".pin();

#our.word =+ "notre";
"notre".meaning = #our;
"notre".instantiation =+ #word;
"notre".instantiation =+ #pronoun;
"notre".pin();

#our.word =+ "nous";
"nous".meaning = #our;
"nous".instantiation =+ #word;
"nous".instantiation =+ #pronoun;
"nous".pin();

#they.word =+ "elles";
"elles".meaning = #they;
"elles".instantiation =+ #word;
"elles".instantiation =+ #pronoun;
"elles".pin();

#they.word =+ "ils";
"ils".meaning = #they;
"ils".instantiation =+ #word;
"ils".instantiation =+ #pronoun;
"ils".pin();

#they.word =+ "leur";
"leur".meaning = #they;
"leur".instantiation =+ #word;
"leur".instantiation =+ #pronoun;
"leur".pin();

#you.word =+ "vous";
"vous".meaning = #you;
"vous".instantiation =+ #word;
"vous".instantiation =+ #pronoun;
"vous".pin();

#you.word =+ "votre";
"votre".meaning = #you;
"votre".instantiation =+ #word;
"votre".instantiation =+ #pronoun;
"votre".pin();

#his.word =+ "son";
"son".meaning = #his;
"son".type =+ #possessive;
"son".instantiation =+ #word;
"son".instantiation =+ #pronoun;
"son".pin();

#her.word =+ "sa";
"sa".meaning = #her;
"sa".type =+ #possessive;
"sa".instantiation =+ #word;
"sa".instantiation =+ #pronoun;
"sa".pin();

#this.word =+ "cette";
"cette".meaning = #this;
"cette".instantiation =+ #word;
"cette".instantiation =+ #pronoun;
"cette".pin();

#this.word =+ "ce";
"ce".meaning = #this;
"ce".instantiation =+ #word;
"ce".instantiation =+ #pronoun;
"ce".pin();

#this.word =+ "celles";
"celles".meaning = #this;
"celles".instantiation =+ #word;
"celles".instantiation =+ #pronoun;
"celles".pin();

#this.word =+ "ceux";
"ceux".meaning = #this;
"ceux".instantiation =+ #word;
"ceux".instantiation =+ #pronoun;
"ceux".pin();

#it.word =+ "son";
"son".meaning = #it;
"son".instantiation =+ #word;
"son".instantiation =+ #pronoun;
"son".pin();

#it.word =+ "sa";
"sa".meaning = #it;
"sa".instantiation =+ #word;
"sa".instantiation =+ #pronoun;
"sa".pin();

#it.word =+ "ses";
"ses".meaning = #it;
"ses".instantiation =+ #word;
"ses".instantiation =+ #pronoun;
"ses".pin();

#is.instantiation = #action;
#is.word = "est";
#is.word =+ "es";
#is.word =+ "suis";
#is.word =+ "sommes";
#is.word =+ "êtes";
#is.word =+ "sont";
#is.word =+ "être";
"est".meaning = #is;
"est".instantiation =+ #word;
"est".instantiation =+ #verb;
"est".pin();

"es".meaning = #is;
"es".instantiation =+ #word;
"es".instantiation =+ #verb;
"es".pin();

"suis".meaning = #is;
"suis".instantiation =+ #word;
"suis".instantiation =+ #verb;
"suis".pin();

"sommes".meaning = #is;
"sommes".instantiation =+ #word;
"sommes".instantiation =+ #verb;
"sommes".pin();

"êtes".meaning = #is;
"êtes".instantiation =+ #word;
"êtes".instantiation =+ #verb;
"êtes".pin();

"sont".meaning = #is;
"sont".instantiation =+ #word;
"sont".instantiation =+ #verb;
"sont".pin();

"être".meaning = #is;
"être".instantiation =+ #word;
"être".instantiation =+ #verb;
"être".pin();

#have.instantiation = #action;
#have.word = "ai";
"ai".meaning = #have;
"ai".instantiation =+ #word;
"ai".instantiation =+ #verb;
"ai".pin();

#have.word = "avoir";
"avoir".meaning = #have;
"avoir".instantiation =+ #word;
"avoir".instantiation =+ #verb;
"avoir".pin();

#have.word = "as";
"as".meaning = #have;
"as".instantiation =+ #word;
"as".instantiation =+ #verb;
"as".pin();

#have.word = "a";
"a".meaning = #have;
"a".instantiation =+ #word;
"a".instantiation =+ #verb;
"a".pin();

#have.word = "avez";
"avez".meaning = #have;
"avez".instantiation =+ #word;
"avez".instantiation =+ #verb;
"avez".pin();

#have.word = "ont";
"ont".meaning = #have;
"ont".instantiation =+ #word;
"ont".instantiation =+ #verb;
"ont".pin();

#instantiation.word = "instanciation";
"instanciation".meaning = #instantiation;
"instanciation".instantiation =+ #word;
"instanciation".instantiation =+ #verb;
"instanciation".pin();

#means.word = "moyen";
"moyen".meaning = #means;
"moyen".instantiation =+ #word;
"moyen".instantiation =+ #verb;
"moyen".pin();

#action.word = "action";
"action".meaning = #action;
"action".instantiation =+ #word;
"action".instantiation =+ #noun;
"action".pin();

#description.word = "description";
"description".meaning = #description;
"description".instantiation =+ #word;
"description".instantiation =+ #noun;
"description".pin();

#thing.word = "objet";
"objet".meaning = #thing;
"objet".instantiation =+ #word;
"objet".instantiation =+ #noun;
"objet".pin();

#speaker.word = "orateur";
"orateur".meaning = #speaker;
"orateur".instantiation =+ #word;
"orateur".instantiation =+ #noun;
"orateur".pin();

#speaker.word = "orateur";
"orateur".meaning = #speaker;
"orateur".instantiation =+ #word;
"orateur".instantiation =+ #noun;
"orateur".pin();

#name.word = "nom";
"nom".meaning = #name;
"nom".instantiation =+ #word;
"nom".instantiation =+ #noun;
"nom".pin();

#name.word = "nom";
"nom".meaning = #name;
"nom".instantiation =+ #word;
"nom".instantiation =+ #noun;
"nom".pin();

#time.word = "temps";
"temps".meaning = #time;
"temps".instantiation =+ #word;
"temps".instantiation =+ #noun;
"temps".pin();

#hour.word = "heure";
"heure".meaning = #hour;
"heure".instantiation =+ #word;
"heure".instantiation =+ #noun;
"heure".pin();

#minute.word = "minute";
"minute".meaning = #minute;
"minute".instantiation =+ #word;
"minute".instantiation =+ #noun;
"minute".pin();

#second.word = "second";
"second".meaning = #second;
"second".instantiation =+ #word;
"second".instantiation =+ #noun;
"second".pin();

#timezone.word = "horaire";
"horaire".meaning = #timezone;
"horaire".instantiation =+ #word;
"horaire".instantiation =+ #noun;
"horaire".pin();

#date.word = "date";
"date".meaning = #date;
"date".instantiation =+ #word;
"date".instantiation =+ #noun;
"date".pin();

#day.word = "jour";
"jour".meaning = #day;
"jour".instantiation =+ #word;
"jour".instantiation =+ #noun;
"jour".pin();

#month.word = "mois";
"mois".meaning = #month;
"mois".instantiation =+ #word;
"mois".instantiation =+ #noun;
"mois".pin();

#year.word = "année";
"année".meaning = #year;
"année".instantiation =+ #word;
"année".instantiation =+ #noun;
"année".pin();
