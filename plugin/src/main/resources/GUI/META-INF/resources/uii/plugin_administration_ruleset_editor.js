var rulesetEditor;

function initRulesetEditor() {
	var rulesetTextArea = document.getElementById("rulesetEditor");
	if (rulesetTextArea) {
		rulesetEditor = CodeMirror.fromTextArea(rulesetTextArea, {
			lineNumbers: true,
			viewportMargin: Infinity,
			mode: 'xml'
		});
		setTimeout(function() {
			rulesetEditor.refresh();
		}, 100);
		rulesetEditor.on('change', editor => {
			document.getElementById("rulesetEditor").innerHTML = editor.getValue();
		});
	}
	window.addEventListener('resize', function() {
		//setHeightOfTextEditor();
	});
	//setHeightOfTextEditor();
}

function loadEditorContent() {
	var rulesetTextAreaBase64 = document.getElementById("rulesetEditorForm:contentbox:rulesetEditorBase64");
	let string = rulesetEditor.getValue();
	// console.log("Load: " + string);
	rulesetTextAreaBase64.value = base64EncodeUnicode(string);
}

function base64EncodeUnicode(str) {
	// Firstly, escape the string using encodeURIComponent to get the UTF-8 encoding of the characters, 
	// Secondly, we convert the percent encodings into raw bytes, and add it to btoa() function.
	utf8Bytes = encodeURIComponent(str).replace(/%([0-9A-F]{2})/g, function (match, p1) {
		return String.fromCharCode('0x' + p1);
	});
	return btoa(utf8Bytes);
}

function loadEditorContentAndInit() {
	loadEditorContent();
	initRulesetEditor();
}

function setHeightOfTextEditor() {
	var documentHeight = document.body.clientHeight;
	var offset = $("#boxUntilBottom").offset();
	var xPosition = offset.left - $(window).scrollLeft();
	var yPosition = offset.top - $(window).scrollTop();
	var border = 20;
	var resultHeight = documentHeight - yPosition - border;
	var box = document.getElementById("boxUntilBottom");
	box.style.height = resultHeight + "px";
	var codeMirror = document.getElementsByClassName("CodeMirror")[0];
	codeMirror.style.height = "100%";
	var borderBox = document.getElementById("rulesetEditorBorder");
	// 100 is an estimated height of buttons and spaces
	borderBox.style.height = (resultHeight - 100) + "px";
}