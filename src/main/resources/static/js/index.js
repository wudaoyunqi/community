$(function () {
    $("#publishBtn").click(publish);
});

function publish() {
    $("#publishModal").modal("hide");
    // $("#hintModal").modal("show");

    // 发送AJAX请求之前，将CSRF令牌设置到请求的消息头中
    // var token = $("meta[name='_csrf']").attr("content");
    // var header = $("meta[name='_csrf_header']").attr("content");
    // $(document).ajaxSend(function (e, xhr, options) {
    //     xhr.setRequestHeader(header, token);
    // });

    // 获取标题、内容
    var title = $("#recipient-name").val();
    var content = $("#message-text").val();
    // 发送异步请求
    // $.post(
    //     CONTEXT_PATH + "/discuss/add",
    //     // 向服务器发送的数据
    //     {"title": title, "content": content},
    //
    //     // 匿名的回调函数，当浏览器得到响应之后会调用该方法，并将服务器返回的数据(即data)传入该方法
    //     function (data) {
    //         // 服务器传给前端也是json格式的字符串
    //         data = $.parseJSON(data);
    //         // 在提示框中显示服务器返回的消息
    //         $("#hintBody").text(data.msg);
    //         // 显示提示框
    //         $("#hintModal").modal("show");
    //         // 2秒后自动隐藏提示框
    //         setTimeout(function () {
    //             $("#hintModal").modal("hide");
    //             // 发布成功时刷新页面
    //             if (data.code === 0) {
    //                 window.location.reload();
    //             }
    //         }, 2000);
    //     }
    // );
    // 使用ajax请求，默认会带上x-requested-with请求头，该请求头的值为"XMLHttpRequest"
    $.ajax({
            url: CONTEXT_PATH + "/discuss/add",
            type: "POST",
            data: JSON.stringify({"title": title, "content": content}),
            contentType: "application/json;charset=utf-8",
            // dataType: "json",
            success: function (data) {
                // 服务器传给前端也是json格式的字符串
                data = $.parseJSON(data);
                console.log(data)
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
            },
            error: function (data) {
                alert("请求失败！");
            }
        }
    )

}

function profile_link(userId) {
    location.href = CONTEXT_PATH + "/user/profile/" + userId;
}
