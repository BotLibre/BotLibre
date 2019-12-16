
function multiDropDown(element, list, comma) {
    var availableTags = list;

    function split( val ) {
    	if (comma == false) {
    		return val.split(" ");
    	} else {
    		return val.split( /,\s*/ );    		
    	}
	}
    function extractLast( term ) {
    	return split( term ).pop();
    }
 
    $( element )
      // don't navigate away from the field on tab when selecting an item
      .bind( "keydown", function( event ) {
        if ( event.keyCode === $.ui.keyCode.TAB &&
            $( this ).autocomplete( "instance" ).menu.active ) {
          event.preventDefault();
        }
      })
      .autocomplete({
        minLength: 0,
        source: function( request, response ) {
          // delegate back to autocomplete, but extract the last term
          response( $.ui.autocomplete.filter(
            availableTags, extractLast( request.term ) ) );
        },
        focus: function() {
          // prevent value inserted on focus
          return false;
        },
        select: function( event, ui ) {
          var terms = split( this.value );
          // remove the current input
          terms.pop();
          // add the selected item
          terms.push( ui.item.value );
          // add placeholder to get the comma-and-space at the end
          terms.push( "" );
          if (comma == false) {
        	  this.value = terms.join( " " );
          } else {
        	  this.value = terms.join( ", " );	        	  
          }
          return false;
        }
      }).on('focus', function(event) {
	    var self = this;
	    $(self).autocomplete("search", "");
	  });
}


function uploadFiles(fileInput, url, proxy) {
	if (window.File && window.FileReader && window.FileList && window.Blob) {
		var files = document.getElementById(fileInput).files;
		for (var i = 0; i < files.length; i++) {
			resizeAndUpload(files[i], url, proxy, i == (files.length - 1));
		}
		//history.go(0);
		//alert('Done');
		return false;
	} else {
		alert('The File APIs are not fully supported in this browser.');
		return false;
	}
}
	 
function resizeAndUpload(file, url, proxy, reload) {
	var url2 = url;
	var proxy2 = proxy;
	var reader = new FileReader();
		reader.onloadend = function() { 
			var tempImg = new Image();
			tempImg.src = reader.result;
			tempImg.onload = function() {
 
			var MAX_WIDTH = 300;
			var MAX_HEIGHT = 300;
			var tempW = tempImg.width;
			var tempH = tempImg.height;
			if (tempW > tempH) {
					if (tempW > MAX_WIDTH) {
						 tempH *= MAX_WIDTH / tempW;
						 tempW = MAX_WIDTH;
					}
			} else {
					if (tempH > MAX_HEIGHT) {
						 tempW *= MAX_HEIGHT / tempH;
						 tempH = MAX_HEIGHT;
					}
			}

			var canvas = document.createElement('canvas');
			canvas.width = tempW;
			canvas.height = tempH;
			var ctx = canvas.getContext("2d");

			ctx.fillStyle = '#fff';
			ctx.fillRect(0, 0, canvas.width, canvas.height);
			
			ctx.drawImage(this, 0, 0, tempW, tempH);
            var dataUrl = canvas.toDataURL('image/jpeg');
            var blob = dataURLToBlob(dataUrl);
			var fd = new FormData();
			fd.append("proxy", proxy2);
			fd.append('file', blob, file.name);
			var xhr = new XMLHttpRequest();
			xhr.open("POST", url2);
			xhr.send(fd);
			xhr.onreadystatechange = function() {
				if (!reload || xhr.readyState != 4) {
					return;
				}
				location.reload();
			}
		}
 
	 }
	 reader.readAsDataURL(file);
}

function dataURLToBlob(dataURL) {
    var marker = ';base64,';
    if (dataURL.indexOf(marker) == -1) {
        var parts = dataURL.split(',');
        var contentType = parts[0].split(':')[1];
        var raw = parts[1];

        return new Blob([raw], {type: contentType});
    }

    var parts = dataURL.split(marker);
    var contentType = parts[0].split(':')[1];
    var raw = window.atob(parts[1]);
    var rawLength = raw.length;

    var blobarray = new Uint8Array(rawLength);

    for (var i = 0; i < rawLength; ++i) {
    	blobarray[i] = raw.charCodeAt(i);
    }

    return new Blob([blobarray], {type: contentType});
}