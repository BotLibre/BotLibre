(function($) {
	

	//Home Background Slider

	$(function() {

		$.mbBgndGallery.buildGallery({
			containment: "#intro",
			timer: 3000,
			effTimer: 1000,
			controls: "#controls",
			grayScale: false,
			shuffle: false,
			preserveWidth: false,
			effect: "fade",
			effect: {
				enter: {
					left: 0,
					opacity: 0
				},
				exit: {
					left: 0,
					opacity: 0
				},
				enterTiming: "ease-in",
				exitTiming: "ease-in"
			},

			// If your server allow directory listing you can use:
			// (however this doesn't work locally on your computer)

			//folderPath:"testImage/",

			// else:

			images: [
				"background/1.jpg",
				"background/2.jpg",
				"background/3.jpg"
			],

			onStart: function() {},
			onPause: function() {},
			onPlay: function(opt) {},
			onChange: function(opt, idx) {},
			onNext: function(opt) {},
			onPrev: function(opt) {}
		});


	});

	// featured text
	$("#rotator .1strotate").textrotator({
		animation: "dissolve",
		speed: 4000
	});
	$("#rotator .2ndrotate").textrotator({
		animation: "dissolve",
		speed: 4000
	});
		
		
	//parallax
	if ($('#parallax1').length || $('#parallax2').length) {

		$(window).stellar({
			responsive: true,
			scrollProperty: 'scroll',
			parallaxElements: false,
			horizontalScrolling: false,
			horizontalOffset: 0,
			verticalOffset: 0
		});

	}

	// Carousel
	$('.service .carousel').carousel({
		interval: 4000
	})

	//works
	$(function() {
		Grid.init();
	});

	//animation
	new WOW().init();

})(jQuery);
