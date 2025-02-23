(function ($) {
    let websocket = null;
    let chatId = null;

    $(document).ready(function () {
        $(".container").css("top", (window.innerHeight - 800) / 2 + "px");
        $(".chat-box img").bind("click", sendQuestion);
        let websocket_ = new WebSocket("/ws");
        websocket_.onopen = function (evt) {
            websocket = websocket_;
            chatId = Date.now();
            newChat({type: "system", done: true, content: "代理层服务连接成功，尝试连接大模型适配层服务..."})
            $("#message-waiting").appendTo($(".message-container")).show();
        };
        websocket_.onclose = function (evt) {
            newChat({type: "answer", done: true, content: "服务器连接已断开，刷新页面重新连接。"})
        };
        websocket_.onerror = function (evt) {
            newChat({type: "answer", done: true, content: "服务器报错了，赶快喊大神。"})
        };
        websocket_.onmessage = function (evt) {
            const data = JSON.parse(evt.data);
            const textBox = $("#answer-" + chatId + " .text-box");
            if (!textBox.length && (data.content === "\n" || data.content === "\n\n")) {
                return;
            }
            $("#message-waiting").hide();
            if (textBox.length) {
                $("<span>").addClass("token").addClass(data.type).html(data.content).appendTo(textBox);
            } else {
                newChat(data);
            }
            scrollDown();
            if (data.done) {
                $("#prompt-chat").prop("selected", true);
                $("#chat-text").val("");
            }
        };
        setupChatPrompt();
        $(".chat-text").focus();
    });

    function setupChatPrompt() {
        $.get("/prompts", function (result) {
            $.each(result, function (key, value) {
                $("<option></option>").val(value.key).text(value.prompt).appendTo($(".combobox"));
            })
        })
    }

    function scrollDown() {
        const container = $(".message-container");
        container.prop("scrollTop", container.prop("scrollHeight"));
    }

    function sendQuestion() {
        if (websocket.readyState !== WebSocket.OPEN) {
            newChat({type: "answer", done: true, content: "服务器连接已断开，刷新页面重新连接。"})
            return;
        }
        const question = $("#chat-text");
        if (question.val().trim() === "") {
            return;
        }
        chatId = Date.now();
        newChat({type: "question", content: question.val()});
        $("#message-waiting").appendTo($(".message-container")).show();
        websocket.send(JSON.stringify({"prompt": $(".combobox").val(), "content": question.val()}));
        scrollDown();
    }

    function newChat(data) {
        const template = $($("#chat").html());
        template.children(".icon").attr("src", "images/" + (data.type === "question" ? "q" : "a") + ".jpg");
        template.children(".text-box").append($("<span>").addClass("token").addClass(data.type).html(data.content));
        return template.attr("id", (data.type === "think" ? "answer" : data.type) + "-" + chatId).appendTo($(".message-container"));
    }

})
(jQuery);