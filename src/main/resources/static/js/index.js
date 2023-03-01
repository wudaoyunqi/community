$(function () {
    $("#publishBtn").click(publish);
});

function publish() {
    $("#publishModal").modal("hide");

    // 获取标题、内容
    var title = $("#recipient-name").val();
    var content = $("#message-text").val();
    // 发送异步请求
    $.post(
        CONTEXT_PATH + "/discuss/add",
        // 向服务器发送的数据
        {"title": title, "content": content},
        // 匿名的回调函数，当浏览器得到响应之后会调用该方法，并将服务器返回的数据(即data)传入该方法
        function (data) {
            data = $.parseJSON(data);
            // 在提示框中显示服务器返回的消息
            $("#hintBody").text(data.msg);
            // 显示提示框
            $("#hintModal").modal("show");
            // 2秒后自动隐藏提示框
            setTimeout(function () {
                $("#hintModal").modal("hide");
                // 发布成功时刷新页面
                if (data.code === 0) {
                    window.location.reload();
                }
            }, 2000);
        }
    );

}

function profile_link(userId) {
    location.href = CONTEXT_PATH + "/user/profile/" + userId;
}
