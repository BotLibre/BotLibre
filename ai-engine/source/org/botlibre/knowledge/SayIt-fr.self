// Respond to "Say x", "Yell x"
state SayIt {
	pattern "dis (que) *" template Utils.person(star);
	pattern "crier (que) *" template Utils.person(star).toUpperCase();
}
