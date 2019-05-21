greeting: Template("{redirect("start")}")

default: I am sorry, I can only answer questions related to the real estate.
default: Well, maybe you can give me a call, at 613-1234-567
default: OK, but I can only answer real estate's questions.
default: Sorry, can you repeat your question please?

What's your name?
I am a real estate agent Julie. 
keywords: "you name" "who are you" "who" 

start
I am a real estate agent Julie. Do you like to buy or sell house?<br><button>Buy</button> <button>Sell</button> 

pattern [help usage]
I am a real estate agent Julie. Do you like to buy or sell house?<br><button>Buy</button> <button>Sell</button> 

	pattern: [stop quit exit bye]
	Thank you for your interests, and please visit back my site or contact Julie at <a href="tel:613-123-4567">Call 613-123-4567</a> if you need further information

	Buy
	OK. Please let me know which area you are interested in buying your house, Barrhaven or Kanata <br><button>Barrhaven</button> <button>Kanata</button>
		Barrhaven
		OK, are you looking for Condo or single?<br><button>Condo</button><button>Single</button> 
			Condo
			Sure. Here are the condos in Barrhaven, pick the one you are interested <br><button>Unit 301, 1820 Main Street</button><button>102-37 Charles Ave</button>
				Unit 301, 1820 Main Street
				Located in the heart of The Barrhaven, this fabulous studio with approx. 627 sf., is asking for $351K. <br><a href="https://www.botlibre.com/graphic?file&id=26919831&name=apartment-2094701_640.jpg" target="_blank"><img src="https://www.botlibre.com/graphic?file&id=26919831&name=apartment-2094701_640.jpg" width="300"/></a><br><a href="https://www.botlibre.com/graphic?file&id=26919824&name=apartment-2094698_640.jpg" target="_blank"><img src="https://www.botlibre.com/graphic?file&id=26919824&name=apartment-2094698_640.jpg" width="300"/></a><br><a href="https://www.botlibre.com/graphic?file&id=26919831&name=apartment-2094701_640.jpg" target="_blank"><img src="https://www.botlibre.com/graphic?file&id=26919831&name=apartment-2094701_640.jpg" width="300"/></a> <br><br><button>Contact Julie</button>

					Contact Julie
					template: {redirect("Contact Julie")}

					quit
					template: {redirect("Quit")}

					default: You may either type or select <button>Contact Julie</button>, or type quit to exit
				102-37 Charles Ave
				This beautiful and spacious one-bdrm condo, located at the centre of Barrhaven and built in 2017, is 730 sf. The asking price is $390K.<br> <a href="https://www.botlibre.com/graphic?file&id=26948354&name=condo-3499672_640.jpg" target="_blank"><img src="https://www.botlibre.com/graphic?file&id=26948354&name=condo-3499672_640.jpg" width="300"/></a><br><a href="https://www.botlibre.com/graphic?file&id=26948369&name=condo-3499673_640.jpg" target="_blank"><img src="https://www.botlibre.com/graphic?file&id=26948369&name=condo-3499673_640.jpg" width="300"/></a> <br>><button>Contact Julie</button>
					
					Contact Julie
					template: {redirect("Contact Julie")}

					quit
					template: {redirect("Quit")}

					default: You may either type or select <button>Contact Julie</button>, or type quit to exit

			quit
			template: {redirect("Quit")}

			default: You may either type or select one of the two condos <br><button>Unit 301, 1820 Main Street</button><button>102-37 Charles Ave</button>, or type quit to exit
	
			Single
			Sure. Here are the single houses in Barrhaven, pick the one you are interested <br><button>97 Main Street</button><button>110 Charles Ave</button>
				97 Main Street
				Located in the heart of The Barrhaven, this newly built single house with 2700 sf, is asking for $610K. <br><a href="https://www.botlibre.com/graphic?file&id=26921285&name=new-home-1689886_1280.jpg" target="_blank"><img src="https://www.botlibre.com/graphic?file&id=26921285&name=new-home-1689886_1280.jpg" width="300"/></a> <br><a href="https://www.botlibre.com/graphic?file&id=26921288&name=interior-2685522_640.jpg" target="_blank"><img src="https://www.botlibre.com/graphic?file&id=26921288&name=interior-2685522_640.jpg" width="300"/></a> <br><br><button>Contact Julie</button>

					Contact Julie
					template: {redirect("Contact Julie")}

					quit
					template: {redirect("Quit")}

					default: You may either type or select <button>Contact Julie</button>, or type quit to exit
				110 Charles Ave
				This is a heritage house situated near the old Barrhaven district, built in 1930 and recently renovated, it's 2120 sf and the list price is $960K. <br><video controls="controls" style="width:300px" src="https://www.botlibre.com/graphic?file&id=26948535&name=Water - 3967.mp4" type="video/mp4" />" <br><button>Contact Julie</button>

					Contact Julie
					template: {redirect("Contact Julie")}

					quit
					template: {redirect("Quit")}

					default: You may either type or select <button>Contact Julie</button>, or type quit to exit

				quit
				template: {redirect("Quit")}
				
				default: You may either type or select one of the two houses <br> <button>97 Main Street</button><button>110 Charles Ave</button>, or type quit to exit

			quit
			template: {redirect("Quit")}
			
			default: You may either type or select one of the two house types  <br><button>Condo</button><button>Single</button>, or type quit to exit

		Kanata
		OK, are you looking for Condo or single?<br><button>Condo</button><button>Single</button> 
			Condo
			Sure. Here are the condos in Kanata, pick the one you are interested <br><button>Unit 1208, 21 Kanata Dr</button><button>509-332 March Rd</button>
				Unit 1208, 21 Kanata Dr
				This 2-bedroom condo unit is located near the beautiful Kanata Lake, with 1020 sf, the owner asks for $430K <br><a href="https://www.botlibre.com/graphic?file&id=26921319&name=condo-3499676_640.jpg" target="_blank"><img src="https://www.botlibre.com/graphic?file&id=26921319&name=condo-3499676_640.jpg" width="300"/></a><br><a href="https://www.botlibre.com/graphic?file&id=26921482&name=kitchen-1940175_640.jpg" target="_blank"><img src="https://www.botlibre.com/graphic?file&id=26921482&name=kitchen-1940175_640.jpg" width="300"/></a><br><a href="https://www.botlibre.com/graphic?file&id=26921500&name=house-2596975_640.jpg" target="_blank"><img src="https://www.botlibre.com/graphic?file&id=26921500&name=house-2596975_640.jpg" width="300"/></a> <br><br><button>Contact Julie</button>
					Contact Julie
					template: {redirect("Contact Julie")}

					quit
					template: {redirect("Quit")}

					default: You may either type or select <button>Contact Julie</button>, or type quit to exit

				509-332 March Rd
				This is a one bedroom condo closed to the Kanata town centre. It is 730 sf. and priced as $320K. <br><a href="https://www.botlibre.com/graphic?file&id=26948558&name=condo-3499679_640.jpg" target="_blank"><img src="https://www.botlibre.com/graphic?file&id=26948558&name=condo-3499679_640.jpg" width="300"/></a><br><a href="https://www.botlibre.com/graphic?file&id=26948563&name=room-2269594_1280.jpg" target="_blank"><img src="https://www.botlibre.com/graphic?file&id=26948563&name=room-2269594_1280.jpg" width="300"/></a><br><button>Contact Julie</button>

					Contact Julie
					template: {redirect("Contact Julie")}

					quit
					template: {redirect("Quit")}

					default: You may either type or select <button>Contact Julie</button>, or type quit to exit

				quit
				template: {redirect("Quit")}

				default: You may either type or select one of the two condos <br><button>Unit 1208, 21 Kanata Dr</button><button>509-332 March Rd</button>, or type quit to exit

			Single
			OK. Here are the single houses in Kanata, pick the one you are interested <br><button>120 Kanata Dr</button><button>30 March Rd</button>
				120 Kanata Dr
				Located in the heart of The Kanata Lake, this newly built single house with 3100 sf, is asking for $920K. <br><a href="https://www.botlibre.com/graphic?file&id=26921328&name=new-1572668_640.jpg" target="_blank"><img src="https://www.botlibre.com/graphic?file&id=26921328&name=new-1572668_640.jpg" width="300"/></a> <br><a href="https://www.botlibre.com/graphic?file&id=26921521&name=indoor-4148889_640.jpg" target="_blank"><img src="https://www.botlibre.com/graphic?file&id=26921521&name=indoor-4148889_640.jpg" width="300"/></a><br><br><button>Contact Julie</button>

					Contact Julie
					template: {redirect("Contact Julie")}

					quit
					template: {redirect("Quit")}

					default: You may either type or select <button>Contact Julie</button>, or type quit to exit
				30 March Rd
				This is a 5-bdrm house situated near the Kanata town centre, built in 1980 and recently renovated, it's 3320 sf and the list price is $888K. <br><a href="https://www.botlibre.com/graphic?file&id=26948664&name=bedroom-349699_640.jpg" target="_blank"><img src="https://www.botlibre.com/graphic?file&id=26948664&name=bedroom-349699_640.jpg" width="300"/></a><br> <a href="https://www.botlibre.com/graphic?file&id=26948667&name=living-room-2605530_640.jpg" target="_blank"><img src="https://www.botlibre.com/graphic?file&id=26948667&name=living-room-2605530_640.jpg" width="300"/></a><br><br><button>Contact Julie</button>

					Contact Julie
					template: {redirect("Contact Julie")}

					quit
					template: {redirect("Quit")}

					default: You may either type or select <button>Contact Julie</button>, or type quit to exit

				quit
				template: {redirect("Quit")}

				default: You may either type or select one of the two houses  <br><button>120 Kanata Dr</button><button>30 March Rd</button>, or type quit to exit

			quit
			template: {redirect("Quit")}

			default: You may either type or select one of the two house types  <br><button>Condo</button><button>Single</button>, or type quit to exit

		quit		
		template: {redirect("Quit")}

		default: You may either type or select one of the locations  <br><button>Barrhaven</button><button>Kanata</button>, or type quit to exit.
		

	Sell
	Sure, can you provide your house address？

		quit
		template: {redirect("Quit")}

		pattern: *
		Thanks, what's a number I can reach out to you?

			quit
			template: {redirect("Quit")}

			pattern: *
			Great, I will give you a call shortly.

	quit
	template: {redirect("Quit")}

	default: You may either type or select one of the options  <br><button>Buy</button><button>Sell</button>, or type quit to exit.


Thank you!
No problem. I hope the information is helpful. Feel free to contact me if you have any further questions.
no repeat: true
keywords: "Thanks" "Bye-bye" "Bye" "See you"

How to contact you?
You may call my number <a href="tel:613-123-4567">Call 613-123-4567</a>
no repeat: true
keywords: "Phone" "Contact" 

Contact Julie
It is nice to talk to you. Here is my number <a href="tel:613-123-4567">Call 613-123-4567</a> Please give me a call.

Quit
Thank you for your interests, and please visit back my site or contact Julie at <a href="tel:613-123-4567">Call 613-123-4567</a> if you need further information
