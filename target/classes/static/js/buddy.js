function copyInvitationLink() {
    const invitationLink = window.location.origin + '/buddy/invite/' + 'YOUR_USER_ID';
    navigator.clipboard.writeText(invitationLink).then(function() {
        alert('招待リンクをコピーしました！');
    }, function(err) {
        console.error('コピーに失敗しました: ', err);
        alert('コピーに失敗しました。');
    });
} 