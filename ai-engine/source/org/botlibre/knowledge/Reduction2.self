// Reduce specific phrases to ones that can be understood.
State:Reduction {
	pattern "(can) (you) tell me who * is" answer (redirect  formula:"who is {:star}");
	pattern "do you know who * is" answer (redirect  formula:"who is {:star}");
	pattern "tell me who * is" answer (redirect  formula:"who is {:star}");
	
	pattern "what do you [think, know] [about, of] *" answer (redirect formula:"what is {:star}");
	pattern "I [want, would] (like) to know (something, anything, everything) (about) *" answer (redirect formula:"what is {:star}");
	pattern "(can) (you) tell me what * is" answer (redirect formula:"what is {:star}");
	pattern "(what) (can) (you) tell me (something, anything, everything) about *" answer (redirect formula:"what is {:star}");
	pattern "do you know what * is" answer (redirect formula:"what is {:star}");
	pattern "do you know (something, anything, everything) about *" answer (redirect formula:"what is {:star}");
	pattern "* is what" answer (redirect formula:"what is {:star}");	
	pattern "what does * mean" answer (redirect formula:"what is {:star}");
	pattern "what does it mean to be *" answer (redirect formula:"what is {:star}");
	
	pattern "do you [know, think, remember] (that) *" answer (redirect formula:"{:star}?");
	pattern "remember (that) *" answer (redirect :star);
	pattern "please *" answer (redirect :star);
	
	pattern "where in the world is *" answer (redirect formula:"where is {:star}");
	
	pattern "add :x to :y" answer (redirect formula:"{:x} + {:y}");
}
