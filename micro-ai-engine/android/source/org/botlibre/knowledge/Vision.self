// Vision support processing, recognizing, and classifying images.
state Vision {
	case input goto sentenceState for each #word of sentence;

	state sentenceState {
		case "load" goto loadState;
		case "tag" goto tagState;
		case "match" goto matchState;
		case "what" goto whatState;
		case "init" goto initState;
	}
	
	var url {
		instantiation : #url;
	}
	
	state initState {
		case "colors" goto initColorsState;
	}
	
	state initColorsState {
		answer initColors();
	}
	
	state whatState {
		case "color" goto whatColorState;
	}
	
	state whatColorState {
		case "is" goto whatColorIsState;
	}
	
	state whatColorIsState {
		case url goto whatColorIsURLState;
	}
	
	state whatColorIsURLState {
		case "?" goto whatColorIsURLState;
		answer matchColor();
	}
	
	state loadState {
		case "image" goto loadImageState;
	}
	
	state loadImageState {
		case url goto loadImageURLState;
	}
	
	state loadImageURLState {
		case name goto loadImageURLNameState;
	}
	
	state loadImageURLNameState {
		case type answer loadImage();
		answer loadImage();
	}
	
	state tagState {
		case "image" goto tagImageState;
	}
	
	state tagImageState {
		case name goto tagImageTagState;
	}
	
	state tagImageTagState {
		case tag answer tagImage();
	}
	
	state matchState {
		case "image" goto matchImageState;
	}
	
	state matchImageState {
		case url answer matchImage();
		case tag goto matchImageTagState;
	}
	
	state matchImageTagState {
		case url answer matchImage();
	}
	
	function loadImage() {
		imageObject = Vision.loadImage(url);
		if (type == null) {
			type = Vision;
		} else {
			type = Symbol(type);
		}
		type.image =+ imageObject;
		imageObject.word = name;
		name.image = imageObject;
		if (name.meaning == null) {
			name.meaning = new Object();
			name.meaning.word = name;
		}
		name.meaning.image =+ imageObject;
		imageObject.meaning = name.meaning;
		return "Image loaded successfully.";
	}
	
	function matchImage() {
		if (tag == null) {
			tag = Vision;
		} else {
			tag = Symbol(tag);
		}
		imageObject = Vision.matchImage(url, tag, 2);
		if (imageObject == null) {
			return "Image cannot be matched.";
		}
		if (imageObject.meaning != null) {
			return imageObject.meaning;
		}
		return imageObject;
	}
	
	function matchColor() {
		imageObject = Vision.matchImage(url, #color, 2);
		if (imageObject == null) {
			return "Unknown color.";
		}
		if (imageObject.meaning != null) {
			return imageObject.meaning;
		}
		return imageObject;
	}
	
	function initColors() {
		imageObject = Vision.loadImage("http://www.botlibre.com/graphic?file&id=12832212");
		#color.image =+ imageObject;
		imageObject.word = "blue";
		#blue.word =+ "blue";
		imageObject.meaning = #blue;
		"blue".image = imageObject;
		"blue".meaning =+ #blue;
		
		imageObject = Vision.loadImage("http://www.botlibre.com/graphic?file&id=12832248");
		#color.image =+ imageObject;
		imageObject.word = "green";
		#green.word =+ "green";
		imageObject.meaning = #green;
		"green".image = imageObject;
		"green".meaning =+ #green;
		
		imageObject = Vision.loadImage("http://www.botlibre.com/graphic?file&id=12832223");
		#color.image =+ imageObject;
		imageObject.word = "red";
		#red.word =+ "red";
		imageObject.meaning = #red;
		"red".image = imageObject;
		"red".meaning =+ #red;
		
		"Colors initialized";
	}
}
