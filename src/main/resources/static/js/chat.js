$(function () {
    let lastTimestamp = 0;
    let waitingMessageId = 0;
    let receiveMessageId = 0;
    let prompt = "ASK";
    let userId = 0;

    addChatOnOpenListener(function (evt) {
        $(createMessageElement('后端服务连接成功!', false)).appendTo($("#messages"));
        addWaitingMessage();
    });
    addChatOnMessageListener(function (evt) {
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
        switch (data.user.model.chat) {
            case "qwen2.5:0.5b":
                document.title = "小欧 Fancy AI";
                break;
            case "qwen-plus":
                document.title = "小千 Fancy AI";
                break;
            case "deepseek-r1":
                document.title = "小迪 Fancy AI";
        }
    });

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

    function sendMessage(text, show) {
        const content = text || $('#message-input').val().trim();
        if (!content) return;
        if (show) $('#messages').append(createMessageElement(content, true));
        addWaitingMessage();
        wsChat.send(JSON.stringify({"content": content}));
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
            sendMessage(null, true);
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
            sendMessage(null, true);
            return false;
        }
    });

    // 创建隐藏的文件输入框
    const $fileInput = $('<input type="file" accept="image/*" style="display: none;">');

    $.openFIleUpload = function openFIleUpload(modelName) {
        $fileInput.attr("model", modelName).trigger('click');
    }

    // 文件选择事件
    $fileInput.on('change', function (e) {
        const file = e.target.files;
        if (!file) return;

        // 校验文件类型‌:ml-citation{ref="2,5" data="citationList"}
        const validTypes = ['image/jpeg', 'image/png', 'image/gif'];
        if (!validTypes.includes(file[0].type)) {
            $(createMessageElement('仅支持JPG/PNG/GIF格式的图片', false)).appendTo($("#messages"));
            return;
        }

        if (file[0].size > 512 * 1024) {
            $(createMessageElement('文件大小不能超过512K。', false)).appendTo($("#messages"));
            return;
        }

        // 读取文件内容‌:ml-citation{ref="1,4" data="citationList"}
        const reader = new FileReader();
        reader.onload = function (e) {
            const base64 = e.target.result;
            $('#messages').append(createMessageElement("<img alt='' class='image' src='" + base64 + "'><span class='token splitter'></span>帮我理解一下这张图片的内容", true));
            sendMessage("理解图片内容（" + $fileInput.attr("model") + "）：" + base64, false)
        };
        reader.readAsDataURL(file[0]);
    });
});
