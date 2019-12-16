(function($) {

	//Fixed banner 
	$(window).scroll(function() {
		var scrollTop = $(window).scrollTop();
		if (scrollTop > 0) {
			$('.navbar1').css('position', 'fixed');
			$('.navbar1').css('width', '100%');
		} else if (scrollTop == 0) {
			
		 $('.navbar1').css('position', 'static');
		}
	});

	function navbar() {
		if ($(window).scrollTop() > 1) {
		$('#navigation').addClass('show-nav');
		} else {
			$('#navigation').removeClass('show-nav');
		}
	}

	$(document).ready(function() {
		var browserWidth = $(window).width();
		if (browserWidth > 930) {
			$(window).scroll(function() {
				navbar();
			});
		}
	});

	$(window).resize(function() {
		var browserWidth = $(window).width();
		if (browserWidth > 930) {
			$(window).scroll(function() {
				navbar();
			});
		}
	});

})(jQuery);
