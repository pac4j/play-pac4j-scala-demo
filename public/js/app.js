function ajaxClick() {
  $.getJSON("form/index.html.json",function(result){
	alert("Received valid JSON from the server");
  });
}

var $doc = $(document);
$doc.ajaxError(function (event, xhr) {
	if (xhr.status === 401) {
		alert('AJAX request received 401 status: Unauthorized');
	} else {
		alert('Unexpected AJAX Error');
	}
});
