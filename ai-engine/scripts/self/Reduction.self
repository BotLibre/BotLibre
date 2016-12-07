// Reduce specific phrases to ones that can be understood.
state Reduction {
	pattern "(can) (you) tell me who * is" template redirect(Template("who is {star}"));
	pattern "do you know who * is" template redirect(Template("who is {star}"));
	pattern "tell me who * is" template redirect(Template("who is {star}"));
	
	pattern "what do you [think know] [about of] *" template redirect(Template("what is {star}"));
	pattern "I [want would] (like) to know (something anything everything) (about) *" template redirect(Template("what is {star}"));
	pattern "(can) (you) tell me what * is" template redirect(Template("what is {star}"));
	pattern "(what) (can) (you) tell me (something anything everything) about *" template redirect(Template("what is {star}"));
	pattern "do you know what * is" template redirect(Template("what is {star}"));
	pattern "do you know (something anything everything) about *" template redirect(Template("what is {star}"));
	pattern "* is what" template redirect(Template("what is {star}"));	
	pattern "what does * mean" template redirect(Template("what is {star}"));
	pattern "what does it mean to be *" template redirect(Template("what is {star}"));
	
	pattern "do you [know think remember] (that) *" template redirect(Template("{star}?"));
	pattern "remember (that) *" template redirect(star);
	pattern "please *" template redirect(star);
	pattern "pls *" template redirect(star);
	
	pattern "where in the world is *" template redirect(Template("where is {star}"));
}
