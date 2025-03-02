$(function () {
    const audio = document.getElementById('audio');
    let lastTimestamp = 0;
    let waitingMessageId = 0;
    let receiveMessageId = 0;
    let prompt = "ASK";
    let userId = 0;
    let websocket = new WebSocket("/ws?" + window.location.search);

    websocket.onopen = function (evt) {
        $(createMessageElement('后端服务连接成功!', false)).appendTo($("#messages"));
        addWaitingMessage();
    };
    websocket.onclose = function (evt) {
        $(createMessageElement('服务器连接已断开，刷新页面重新连接。', false)).appendTo($("#messages"));
    };
    websocket.onerror = function (evt) {
        $(createMessageElement('服务器报错了，赶快喊大神起床查问题。', false)).appendTo($("#messages"));
    };
    websocket.onmessage = function (evt) {
        const data = JSON.parse(evt.data);
        userId = data.user.userId;
        $(`#${waitingMessageId}`).remove();
        let messageElement = $(`#${receiveMessageId}`);
        if (!messageElement.length) {
            messageElement = $(createMessageElement('', false));
            messageElement.attr('id', receiveMessageId).appendTo($("#messages"));
        }
        data.content = data.content.replace(/\n\n/g, "<span class='token splitter'></span>");
        if ("image" === data.type) {
            const imageData = JSON.parse(data.content)[0];
            data.content = "<img class='token image' alt='" + (imageData.actual_prompt ? imageData.actual_prompt : "") + "' src='" + imageData.url + "'>";
            if (imageData.actual_prompt) {
                data.content += "<span class='token splitter'></span>";
                data.content += "<span class='token image-desc'>" + imageData.actual_prompt + "</span>";
            }
        }
        $("<span>").addClass("token").addClass(data.type).html(data.content).appendTo(messageElement.children(".content"));
        if (data.usage) {
            $("<span>").addClass("token splitter").appendTo(messageElement.children(".content"));
            $("<span>").addClass("token usage").text(data.usage.cost).appendTo(messageElement.children(".content"));
        }
        $('#messages').scrollTop($('#messages')[0].scrollHeight);
        switch (data.user.model) {
            case "qwen2.5:0.5b":
                document.title = "小欧 Fancy AI";
                break;
            case "qwen-plus":
                document.title = "小千 Fancy AI";
                break;
            case "deepseek-r1":
                document.title = "小迪 Fancy AI";
                break;
            case "wanx2.1-t2i-turbo":
                document.title = "小威 Fancy AI";
                break;
        }
    };

    function formatTime(timestamp) {
        return new Date(timestamp).toLocaleTimeString('zh-CN', {
            hour: '2-digit',
            minute: '2-digit'
        });
    }

    function checkShowTime(currentTime) {
        const FIVE_MINUTES = 5 * 60 * 1000;
        if (!lastTimestamp || currentTime - lastTimestamp > FIVE_MINUTES) {
            lastTimestamp = currentTime;
            return true;
        }
        return false;
    }

    function createMessageElement(content, isSent) {
        const currentTime = Date.now();
        const timeHtml = checkShowTime(currentTime) ?
            `<div class="center-time">${formatTime(currentTime)}</div>` : '';
        return `
                    ${timeHtml}
                    <div class="message ${isSent ? 'sent' : 'received'}">
                        <div class="content">${content}</div>
                    </div>
                `;
    }

    function sendMessage(text) {
        debugger;
        $(audio).attr("src", "/tts/" + userId);
        audio.play();
        const content = text || $('#message-input').val().trim();
        if (!content) return;
        // 添加用户消息
        $('#messages').append(createMessageElement(content, true));
        addWaitingMessage();
        websocket.send(JSON.stringify({"content": content}));
        $('#message-input').val('');
        receiveMessageId = `receive-${Date.now()}`;
        $('#messages').scrollTop($('#messages')[0].scrollHeight);
    }

    // 添加等待消息
    function addWaitingMessage() {
        waitingMessageId = `waiting-${Date.now()}`;
        $('#messages').append(`
            <div class="message received waiting" id="${waitingMessageId}">
                <div class="loading-icon"></div>
                <div class="loading-icon"></div>
                <div class="loading-icon"></div>
            </div>
        `);
    }

    //渲染提示词列表
    $.get("/prompts", function (result) {
        $.each(result, function (key, value) {
            $("<div>").addClass("preset-msg").text(value).appendTo($(".preset-menu")).click(function (e) {
                $("#message-input").val(value);
                setTimeout(function () {
                    prompt = value.id;
                    $('.preset-menu').removeClass('show-menu');
                }, 200);
            });
        })
    })

    $('#send-btn').click(function (e) {
        if (!$(e.target).closest('.dropdown-arrow').length) {
            sendMessage();
        }
    });

    $('.dropdown-arrow').click(function () {
        $('.preset-menu').toggleClass('show-menu');
    });

    $('.preset-msg').click(function () {
        $('.preset-menu').removeClass('show-menu');
    });

    $(document).click(function (e) {
        if (!$(e.target).closest('.send-wrapper').length) {
            $('.preset-menu').removeClass('show-menu');
        }
    });

    $('#message-input').keypress(function (e) {
        if (e.which === 13) {
            sendMessage();
            return false;
        }
    });
});
