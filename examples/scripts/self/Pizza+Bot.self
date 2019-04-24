/**
 * This script sends the user's pizza order to the pizza restaurant via email.
 */
state OrderPizza {
	pattern "*" that "Would you like a <button>small</button>, <button>medium</button>, or <button>large</button>?" template setSize();
	
	pattern "*" that "What type of pizza would you like, <select><option></option><option>pepperoni</option><option>hawaiian</option><option>vegetarian</option><option>cheese</option>?" template setType();
	
	pattern "^ delivery ^" that "Is this for delivery, or pickup?" template setDelivery();
	
	pattern "*" that "Is this for <button>delivery</button>, or <button>pickup</button>?" template setPickup();
	
	pattern "*" that "Please enter your address." template setAddress();
	
	pattern "*" that "Please enter your phone number." template setPhone();

	function setSize() {
	    conversation.order = new Object();
	    conversation.order.size = conversation.input[-1];
	    return "What type of pizza would you like, <select><option></option><option>pepperoni</option><option>hawaiian</option><option>vegetarian</option><option>cheese</option>?";
	}
	
	function setType() {
	    conversation.order.type = conversation.input[-1];
	    return "Is this for <button>delivery</button>, or <button>pickup</button>?";
	}
	
	function setDelivery() {
	    conversation.order.delivery = conversation.input[-1];
	    return "Please enter your address.";
	}
	
	function setPickup() {
	    conversation.order.delivery = conversation.input[-1];
	    return "Please enter your phone number.";
	}
	
	function setAddress() {
	    conversation.order.address = conversation.input[-1];
	    return "Please enter your phone number.";
	}
	
	function setPhone() {
	    conversation.order.phone = conversation.input[-1];
	    message = "New order: " + conversation.order.size + " : " + conversation.order.type + " : " + conversation.order.delivery + " : " + conversation.order.delivery + " : " + conversation.order.phone + " : " + conversation.order.address;
		Email.email("orders@pizza.com", "New pizza order", message);
	    return "Thank you, your order has been placed, it should be ready in 30 minutes. The current time is " + Date.time();
	}
}
