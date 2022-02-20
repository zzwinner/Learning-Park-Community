$(function(){
	$(".follow-btn").click(follow);
});

function follow() {
	var btn = this;
	// 关注TA/取消关注TA
	$.post(
		CONTEXT_PATH + "/follow",
		{"entityType":3,"entityId":$(btn).prev().val()},
		function(data) {
			data = $.parseJSON(data);
			if(data.code == 0) {
				window.location.reload();
			} else {
				alert(data.msg);
			}
		}
	);
}