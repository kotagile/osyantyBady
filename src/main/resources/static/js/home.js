// ホーム画面専用JavaScript
function startWorkout() {
    window.location.href = '/workout/start';
}

// ページ読み込み時の処理
document.addEventListener('DOMContentLoaded', function() {
    // 必要に応じて初期化処理を追加
    console.log('ホーム画面が読み込まれました');
});

document.addEventListener("DOMContentLoaded", function() {
	const goalToggle = document.getElementById("goalToggle");
	const goalContent = document.getElementById("goalContent");
	
	goalToggle.addEventListener("click", function() {
		goalContent.classList.toggle("open");
		
		if(goalContent.classList.contains("open")){
			goalToggle.setAttribute("src", "/img/pulldown-after.png");
		} else{
			goalToggle.setAttribute("src", "/img/pulldown-before.png");
		}
	});

});