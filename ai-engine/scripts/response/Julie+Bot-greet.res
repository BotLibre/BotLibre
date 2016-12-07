greeting: Template("{if (speaker.name == null) { random ("Hello sweetheart", "How are you doing?", "Hi my darling", "Hi. Would you like to be my boyfriend?"); } else { (random ("Hello ", "Hi ") + speaker + random (". How was your day?", ". I was hoping you would chat with me today.", ". I have been waiting for you.")); } }")

