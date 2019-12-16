<script src="scripts/tinymce/tinymce.min.js"></script>
<script type="text/javascript">
	tinymce.init({
		selector: "textarea",
		entity_encoding : "raw",
		plugins: [
			"advlist autolink lists link image charmap print preview anchor",
			"searchreplace visualblocks code fullscreen",
			"insertdatetime media table contextmenu paste textcolor hr"
		],
		browser_spellcheck : true,
		menu : {
			edit   : {title : 'Edit'  , items : 'undo redo | selectall | searchreplace'},
			insert : {title : 'Insert', items : 'link image media | anchor charmap template hr'},
			view   : {title : 'View'  , items : 'visualblocks visualaid | preview fullscreen'},
			format : {title : 'Format', items : 'bold italic underline strikethrough superscript subscript | formats | removeformat '},
			table  : {title : 'Table' , items : 'inserttable tableprops deletetable | cell row column'},
			tools  : {title : 'Tools' , items : 'code'}
		},
		toolbar: "undo redo | bold italic | forecolor backcolor | alignleft aligncenter alignright alignjustify | bullist numlist outdent indent | link image media insertimage insertfile | styleselect | fontselect fontsizeselect",
		setup : function(ed) {
			ed.addButton('insertimage', {
				title : 'Insert Image',
				image : 'images/image.svg',
				onclick : function() {
					if (typeof forum !== 'undefined' && forum != null) {
						psdk.uploadForumAttachment(forum, true, function(link) {
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
					if (typeof forum !== 'undefined' && forum != null) {
						psdk.uploadForumAttachment(forum, false, function(link, name) {
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
