<script src="scripts/tinymce/tinymce.min.js"></script>
<script type="text/javascript">
	var customInitInstanceForTinyMce = function customInitInstanceForTinyMce() {
		setTimeout(function () {
			tinyMCE.get('details').focus();
		}, 500);
	}
	tinymce.init({
		selector: "#details",
		entity_encoding : "raw",
		/*selector: "textarea",*/
		plugins: [
			"advlist autolink lists link image charmap print preview anchor",
			"searchreplace visualblocks code fullscreen",
			"insertdatetime media table contextmenu paste textcolor emoticons hr"
		],
		browser_spellcheck : true,
		auto_focus : "details",
		init_instance_callback : "customInitInstanceForTinyMce",
		menu : {
			edit   : {title : 'Edit'  , items : 'undo redo | selectall | searchreplace'},
			insert : {title : 'Insert', items : 'link image media | anchor charmap template hr'},
			view   : {title : 'View'  , items : 'visualblocks visualaid | preview fullscreen'},
			format : {title : 'Format', items : 'bold italic underline strikethrough superscript subscript | formats | removeformat '},
			table  : {title : 'Table' , items : 'inserttable tableprops deletetable | cell row column'},
			tools  : {title : 'Tools' , items : 'code'}
		},
		toolbar: "undo redo | bold italic | forecolor backcolor | alignleft aligncenter alignright alignjustify | bullist numlist outdent indent | insertbutton link image media insertimage insertfile | styleselect | fontselect fontsizeselect",
		setup : function(ed) {
			ed.addButton('insertimage', {
				title : 'Insert Image',
				image : 'images/image.svg',
				onclick : function() {
					if (typeof botinstance !== 'undefined' && botinstance != null) {
						psdk.uploadBotAttachment(botinstance, true, function(link) {
							ed.focus();
							ed.selection.setContent("<img src='" + link + "'/>");
						});
					} else if (typeof issueTracker !== 'undefined' && issueTracker != null) {
						psdk.uploadIssueTrackerAttachment(issueTracker, true, function(link) {
							ed.focus();
							ed.selection.setContent("<img src='" + link + "'/>");
						});
					}
				}
			});
			ed.addButton('insertfile', {
				title : 'Insert File',
				image : 'images/attach.svg',
				onclick : function() {
					if (typeof botinstance !== 'undefined' && botinstance != null) {
						psdk.uploadBotAttachment(botinstance, false, function(link, name) {
							ed.focus();
							ed.selection.setContent("<a href='" + link + "'>" + name + "</a>");
						});
					} else if (typeof issueTracker !== 'undefined' && issueTracker != null) {
						psdk.uploadIssueTrackerAttachment(issueTracker, true, function(link) {
							ed.focus();
							ed.selection.setContent("<a href='" + link + "'>" + name + "</a>");
						});
					}
				}
			});
			ed.addButton('insertbutton', {
				title : 'Insert Button',
				image : 'images/okbutton.svg',
				onclick : function() {
					if (typeof botinstance !== 'undefined' && botinstance != null) {
						$( "#dialog-button" ).dialog( "open" );
						document.getElementById("button-name").value = "";
						$( "#insert-button" ).unbind("click");
						$( "#insert-button" ).click(function() {
							if (this.id == 'insert-button') {
								var buttonName = $( '#button-name' ).val();
								ed.focus();
								ed.selection.setContent("<button>" + buttonName + "</button>&nbsp;");
							}
							$( "#dialog-button" ).dialog( "close" );
						});
					}
				}
			});
		}
	});
</script>
<div id="dialog-error" title="Error" class="dialog">
	<p id="error-message"></p>
</div>
<script>
	$(function() {
		$( "#dialog-error" ).dialog({
			autoOpen: false,
			modal: true,
			buttons: {
				Ok: function() {
					$( this ).dialog( "close" );
				}
			}
		});
	});
</script>
