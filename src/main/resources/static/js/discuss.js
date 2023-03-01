function like(btn, entityType, entityId, entityUserId) {
    $.post(
        CONTEXT_PATH + "/like",
        // 传给服务端的参数以及对应的值
        {"entityType": entityType, "entityId": entityId, "entityUserId": entityUserId},
        function (data) {
            data = $.parseJSON(data);
            if (data.code == 0) {
                // js方法只是在你点赞，也就是说单机事件发生时才会执行, 第一次进入页面时不会正确显示赞的数量和状态
                $(btn).children("i").text(data.likeCount);
                $(btn).children("b").text(data.likeStatus == 1 ? '已赞' : '赞');
            } else {
                alert(data.msg);
            }
        }
    )
    ;
}