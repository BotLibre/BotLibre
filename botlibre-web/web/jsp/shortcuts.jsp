<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<a id="shortcuts" class="menu" href="#">shortcuts</a>
<script>
	$(function() {
		$( "#dialog-shortcuts" ).dialog({
			autoOpen: false,
			modal: true,
		    height:400,
		    width:700,
			buttons: {
				Ok: function() {
					$( this ).dialog( "close" );
				}
			}
		});
	});
	
	$( "#shortcuts" ).click(function() {
		$( "#dialog-shortcuts" ).dialog( "open" );
		return false;
	});
</script>
<div id="dialog-shortcuts" title="<%= loginBean.translate("Keyboard Shortcuts") %>" class="dialog">
	<table>
	<thead><tr>
	<th align="left">PC</th>
	<th align="left">Mac</th>
	<th align="left"><%= loginBean.translate("action") %></th>
	</tr></thead>
	<tbody>
	<tr>
	<td align="left">Ctrl-,</td>
	<td align="left">Command-,</td>
	<td align="left"><%= loginBean.translate("Show the settings menu") %></td>
	</tr>
	<tr>
	<td align="left">Ctrl-Alt-Up</td>
	<td align="left">Ctrl-Option-Up</td>
	<td align="left"><%= loginBean.translate("add multi-cursor above") %></td>
	</tr>
	<tr>
	<td align="left">Ctrl-Alt-Down</td>
	<td align="left">Ctrl-Option-Down</td>
	<td align="left"><%= loginBean.translate("add multi-cursor below") %></td>
	</tr>
	<tr>
	<td align="left">Ctrl-Alt-Right</td>
	<td align="left">Ctrl-Option-Right</td>
	<td align="left"><%= loginBean.translate("add next occurrence to multi-selection") %></td>
	</tr>
	<tr>
	<td align="left">Ctrl-Alt-Left</td>
	<td align="left">Ctrl-Option-Left</td>
	<td align="left"><%= loginBean.translate("add previous occurrence to multi-selection") %></td>
	</tr>
	<tr>
	<td align="left"></td>
	<td align="left">Ctrl-L</td>
	<td align="left"><%= loginBean.translate("center selection") %></td>
	</tr>
	<tr>
	<td align="left">Ctrl-Shift-U</td>
	<td align="left">Ctrl-Shift-U</td>
	<td align="left"><%= loginBean.translate("change to lower case") %></td>
	</tr>
	<tr>
	<td align="left">Ctrl-U</td>
	<td align="left">Ctrl-U</td>
	<td align="left"><%= loginBean.translate("change to upper case") %></td>
	</tr>
	<tr>
	<td align="left">Alt-Shift-Down</td>
	<td align="left">Command-Option-Down</td>
	<td align="left"><%= loginBean.translate("copy lines down") %></td>
	</tr>
	<tr>
	<td align="left">Alt-Shift-Up</td>
	<td align="left">Command-Option-Up</td>
	<td align="left"><%= loginBean.translate("copy lines up") %></td>
	</tr>
	<tr>
	<td align="left">Delete</td>
	<td align="left"></td>
	<td align="left"><%= loginBean.translate("delete") %></td>
	</tr>
	<tr>
	<td align="left">Ctrl-Shift-D</td>
	<td align="left">Command-Shift-D</td>
	<td align="left"><%= loginBean.translate("duplicate selection") %></td>
	</tr>
	<tr>
	<td align="left">Ctrl-F</td>
	<td align="left">Command-F</td>
	<td align="left"><%= loginBean.translate("find") %></td>
	</tr>
	<tr>
	<td align="left">Ctrl-K</td>
	<td align="left">Command-G</td>
	<td align="left"><%= loginBean.translate("find next") %></td>
	</tr>
	<tr>
	<td align="left">Ctrl-Shift-K</td>
	<td align="left">Command-Shift-G</td>
	<td align="left"><%= loginBean.translate("find previous") %></td>
	</tr>
	<tr>
	<td align="left">Alt-0</td>
	<td align="left">Command-Option-0</td>
	<td align="left"><%= loginBean.translate("fold all") %></td>
	</tr>
	<tr>
	<td align="left">Alt-L, Ctrl-F1</td>
	<td align="left">Command-Option-L, Command-F1</td>
	<td align="left"><%= loginBean.translate("fold selection") %></td>
	</tr>
	<tr>
	<td align="left">Down</td>
	<td align="left">Down, Ctrl-N</td>
	<td align="left"><%= loginBean.translate("go line down") %></td>
	</tr>
	<tr>
	<td align="left">Up</td>
	<td align="left">Up, Ctrl-P</td>
	<td align="left"><%= loginBean.translate("go line up") %></td>
	</tr>
	<tr>
	<td align="left">Ctrl-End</td>
	<td align="left">Command-End, Command-Down</td>
	<td align="left"><%= loginBean.translate("go to end") %></td>
	</tr>
	<tr>
	<td align="left">Left</td>
	<td align="left">Left, Ctrl-B</td>
	<td align="left"><%= loginBean.translate("go to left") %></td>
	</tr>
	<tr>
	<td align="left">Ctrl-L</td>
	<td align="left">Command-L</td>
	<td align="left"><%= loginBean.translate("go to line") %></td>
	</tr>
	<tr>
	<td align="left">Alt-Right, End</td>
	<td align="left">Command-Right, End, Ctrl-E</td>
	<td align="left"><%= loginBean.translate("go to line end") %></td>
	</tr>
	<tr>
	<td align="left">Alt-Left, Home</td>
	<td align="left">Command-Left, Home, Ctrl-A</td>
	<td align="left"><%= loginBean.translate("go to line start") %></td>
	</tr>
	<tr>
	<td align="left">Ctrl-P</td>
	<td align="left"></td>
	<td align="left"><%= loginBean.translate("go to matching bracket") %></td>
	</tr>
	<tr>
	<td align="left">PageDown</td>
	<td align="left">Option-PageDown, Ctrl-V</td>
	<td align="left"><%= loginBean.translate("go to page down") %></td>
	</tr>
	<tr>
	<td align="left">PageUp</td>
	<td align="left">Option-PageUp</td>
	<td align="left"><%= loginBean.translate("go to page up") %></td>
	</tr>
	<tr>
	<td align="left">Right</td>
	<td align="left">Right, Ctrl-F</td>
	<td align="left"><%= loginBean.translate("go to right") %></td>
	</tr>
	<tr>
	<td align="left">Ctrl-Home</td>
	<td align="left">Command-Home, Command-Up</td>
	<td align="left"><%= loginBean.translate("go to start") %></td>
	</tr>
	<tr>
	<td align="left">Ctrl-Left</td>
	<td align="left">Option-Left</td>
	<td align="left"><%= loginBean.translate("go to word left") %></td>
	</tr>
	<tr>
	<td align="left">Ctrl-Right</td>
	<td align="left">Option-Right</td>
	<td align="left"><%= loginBean.translate("go to word right") %></td>
	</tr>
	<tr>
	<td align="left">Tab</td>
	<td align="left">Tab</td>
	<td align="left"><%= loginBean.translate("indent") %></td>
	</tr>
	<tr>
	<td align="left">Ctrl-Alt-E</td>
	<td align="left"></td>
	<td align="left"><%= loginBean.translate("macros recording") %></td>
	</tr>
	<tr>
	<td align="left">Ctrl-Shift-E</td>
	<td align="left">Command-Shift-E</td>
	<td align="left"><%= loginBean.translate("macros replay") %></td>
	</tr>
	<tr>
	<td align="left">Alt-Down</td>
	<td align="left">Option-Down</td>
	<td align="left"><%= loginBean.translate("move lines down") %></td>
	</tr>
	<tr>
	<td align="left">Alt-Up</td>
	<td align="left">Option-Up</td>
	<td align="left"><%= loginBean.translate("move lines up") %></td>
	</tr>
	<tr>
	<td align="left">Ctrl-Alt-Shift-Up</td>
	<td align="left">Ctrl-Option-Shift-Up</td>
	<td align="left"><%= loginBean.translate("move multicursor from current line to the line above") %></td>
	</tr>
	<tr>
	<td align="left">Ctrl-Alt-Shift-Down</td>
	<td align="left">Ctrl-Option-Shift-Down</td>
	<td align="left"><%= loginBean.translate("move multicursor from current line to the line below") %></td>
	</tr>
	<tr>
	<td align="left">Shift-Tab</td>
	<td align="left">Shift-Tab</td>
	<td align="left"><%= loginBean.translate("outdent") %></td>
	</tr>
	<tr>
	<td align="left">Insert</td>
	<td align="left">Insert</td>
	<td align="left"><%= loginBean.translate("overwrite") %></td>
	</tr>
	<tr>
	<td align="left">Ctrl-Shift-Z, Ctrl-Y</td>
	<td align="left">Command-Shift-Z, Command-Y</td>
	<td align="left"><%= loginBean.translate("redo") %></td>
	</tr>
	<tr>
	<td align="left">Ctrl-Alt-Shift-Right</td>
	<td align="left">Ctrl-Option-Shift-Right</td>
	<td align="left"><%= loginBean.translate("remove current occurrence from multi-selection and move to next") %></td>
	</tr>
	<tr>
	<td align="left">Ctrl-Alt-Shift-Left</td>
	<td align="left">Ctrl-Option-Shift-Left</td>
	<td align="left"><%= loginBean.translate("remove current occurrence from multi-selection and move to previous") %></td>
	</tr>
	<tr>
	<td align="left">Ctrl-D</td>
	<td align="left">Command-D</td>
	<td align="left"><%= loginBean.translate("remove line") %></td>
	</tr>
	<tr>
	<td align="left">Alt-Delete</td>
	<td align="left">Ctrl-K</td>
	<td align="left"><%= loginBean.translate("remove to line end") %></td>
	</tr>
	<tr>
	<td align="left">Alt-Backspace</td>
	<td align="left">Command-Backspace</td>
	<td align="left"><%= loginBean.translate("remove to linestart") %></td>
	</tr>
	<tr>
	<td align="left">Ctrl-Backspace</td>
	<td align="left">Option-Backspace, Ctrl-Option-Backspace</td>
	<td align="left"><%= loginBean.translate("remove word left") %></td>
	</tr>
	<tr>
	<td align="left">Ctrl-Delete</td>
	<td align="left">Option-Delete</td>
	<td align="left"><%= loginBean.translate("remove word right") %></td>
	</tr>
	<tr>
	<td align="left">Ctrl-R</td>
	<td align="left">Command-Option-F</td>
	<td align="left"><%= loginBean.translate("replace") %></td>
	</tr>
	<tr>
	<td align="left">Ctrl-Shift-R</td>
	<td align="left">Command-Shift-Option-F</td>
	<td align="left"><%= loginBean.translate("replace all") %></td>
	</tr>
	<tr>
	<td align="left">Ctrl-Down</td>
	<td align="left">Command-Down</td>
	<td align="left"><%= loginBean.translate("scroll line down") %></td>
	</tr>
	<tr>
	<td align="left">Ctrl-Up</td>
	<td align="left"></td>
	<td align="left"><%= loginBean.translate("scroll line up") %></td>
	</tr>
	<tr>
	<td align="left"></td>
	<td align="left">Option-PageDown</td>
	<td align="left"><%= loginBean.translate("scroll page down") %></td>
	</tr>
	<tr>
	<td align="left"></td>
	<td align="left">Option-PageUp</td>
	<td align="left"><%= loginBean.translate("scroll page up") %></td>
	</tr>
	<tr>
	<td align="left">Ctrl-A</td>
	<td align="left">Command-A</td>
	<td align="left"><%= loginBean.translate("select all") %></td>
	</tr>
	<tr>
	<td align="left">Ctrl-Shift-L</td>
	<td align="left">Ctrl-Shift-L</td>
	<td align="left"><%= loginBean.translate("select all from multi-selection") %></td>
	</tr>
	<tr>
	<td align="left">Shift-Down</td>
	<td align="left">Shift-Down</td>
	<td align="left"><%= loginBean.translate("select down") %></td>
	</tr>
	<tr>
	<td align="left">Shift-Left</td>
	<td align="left">Shift-Left</td>
	<td align="left"><%= loginBean.translate("select left") %></td>
	</tr>
	<tr>
	<td align="left">Shift-End</td>
	<td align="left">Shift-End</td>
	<td align="left"><%= loginBean.translate("select line end") %></td>
	</tr>
	<tr>
	<td align="left">Shift-Home</td>
	<td align="left">Shift-Home</td>
	<td align="left"><%= loginBean.translate("select line start") %></td>
	</tr>
	<tr>
	<td align="left">Shift-PageDown</td>
	<td align="left">Shift-PageDown</td>
	<td align="left"><%= loginBean.translate("select page down") %></td>
	</tr>
	<tr>
	<td align="left">Shift-PageUp</td>
	<td align="left">Shift-PageUp</td>
	<td align="left"><%= loginBean.translate("select page up") %></td>
	</tr>
	<tr>
	<td align="left">Shift-Right</td>
	<td align="left">Shift-Right</td>
	<td align="left"><%= loginBean.translate("select right") %></td>
	</tr>
	<tr>
	<td align="left">Ctrl-Shift-End</td>
	<td align="left">Command-Shift-Down</td>
	<td align="left"><%= loginBean.translate("select to end") %></td>
	</tr>
	<tr>
	<td align="left">Alt-Shift-Right</td>
	<td align="left">Command-Shift-Right</td>
	<td align="left"><%= loginBean.translate("select to line end") %></td>
	</tr>
	<tr>
	<td align="left">Alt-Shift-Left</td>
	<td align="left">Command-Shift-Left</td>
	<td align="left"><%= loginBean.translate("select to line start") %></td>
	</tr>
	<tr>
	<td align="left">Ctrl-Shift-P</td>
	<td align="left"></td>
	<td align="left"><%= loginBean.translate("select to matching bracket") %></td>
	</tr>
	<tr>
	<td align="left">Ctrl-Shift-Home</td>
	<td align="left">Command-Shift-Up</td>
	<td align="left"><%= loginBean.translate("select to start") %></td>
	</tr>
	<tr>
	<td align="left">Shift-Up</td>
	<td align="left">Shift-Up</td>
	<td align="left"><%= loginBean.translate("select up") %></td>
	</tr>
	<tr>
	<td align="left">Ctrl-Shift-Left</td>
	<td align="left">Option-Shift-Left</td>
	<td align="left"><%= loginBean.translate("select word left") %></td>
	</tr>
	<tr>
	<td align="left">Ctrl-Shift-Right</td>
	<td align="left">Option-Shift-Right</td>
	<td align="left"><%= loginBean.translate("select word right") %></td>
	</tr>
	<tr>
	<td align="left"></td>
	<td align="left">Ctrl-O</td>
	<td align="left"><%= loginBean.translate("split line") %></td>
	</tr>
	<tr>
	<td align="left">Ctrl-/</td>
	<td align="left">Command-/</td>
	<td align="left"><%= loginBean.translate("toggle comment") %></td>
	</tr>
	<tr>
	<td align="left">Ctrl-T</td>
	<td align="left">Ctrl-T</td>
	<td align="left"><%= loginBean.translate("transpose letters") %></td>
	</tr>
	<tr>
	<td align="left">Ctrl-Z</td>
	<td align="left">Command-Z</td>
	<td align="left"><%= loginBean.translate("undo") %></td>
	</tr>
	<tr>
	<td align="left">Alt-Shift-L, Ctrl-Shift-F1</td>
	<td align="left">Command-Option-Shift-L, Command-Shift-F1</td>
	<td align="left"><%= loginBean.translate("unfold") %></td>
	</tr>
	<tr>
	<td align="left">Alt-Shift-0</td>
	<td align="left">Command-Option-Shift-0</td>
	<td align="left"><%= loginBean.translate("unfold all") %></td>
	</tr>
	<tr>
	<td align="left">Ctrl-Enter</td>
	<td align="left">Command-Enter</td>
	<td align="left"><%= loginBean.translate("enter full screen") %></td>
	</tr>
	</tbody>
	</table>
</div>