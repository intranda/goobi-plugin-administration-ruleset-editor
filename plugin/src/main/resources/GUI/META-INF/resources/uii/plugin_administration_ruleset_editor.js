var rulesetEditor;
function initRulesetEditor() {
	var rulesetTextArea = document.getElementById("rulesetEditor");
	if (rulesetTextArea) {
		rulesetEditor = CodeMirror.fromTextArea(rulesetTextArea, {
			lineNumbers: true,
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
		setHeightOfTextEditor();
	});
	setHeightOfTextEditor();
}
function loadEditorContent() {
	var rulesetTextAreaBase64 = document.getElementById("rulesetEditorBase64");
	rulesetTextAreaBase64.value = window.btoa(rulesetEditor.getValue());
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