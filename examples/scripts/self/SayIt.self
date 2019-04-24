// Respond to "Say x", "Yell x"
state SayIt {
	pattern "say (that) *" template Utils.person(star);
	pattern "[yell scream] (that) *" template Utils.person(star).toUpperCase();
}
