$(function () {
    let lastTimestamp = 0;
    let waitingMessageId = 0;
    let receiveMessageId = 0;
    let prompt = "ASK";
    let userId = 0;

    $.createMessageElement = function createMessageElement(content, isSent) {
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

    addChatOnOpenListener(function (evt) {
        $($.createMessageElement('后端服务连接成功!', false)).appendTo($("#messages"));
        addWaitingMessage();
    });

    addOnFileUploadListener(function (formData, model) {
        addWaitingMessage();
        $('#message-input').val('');
        receiveMessageId = `receive-${Date.now()}`;
        if (model === "wanx2.1-i2v-turbo" || model === "wanx2.1-i2v-plus") {
            $($.createMessageElement('我要上传图片', true)).appendTo($("#messages"));
            $.ajax({
                url: '/prepareImage2video?model=' + model + '&userId=' + userId, // 替换为实际的上传接口地址
                type: 'POST',
                data: formData,
                processData: false, // 不处理数据
                contentType: false, // 不设置内容类型，让浏览器自动设置
                success: function (response) {
                    $(`#${waitingMessageId}`).remove();
                    $('#message-input').val(response);
                },
                error: function (xhr, status, error) {
                    $(`#${waitingMessageId}`).remove();
                    $.createMessageElement("上传图片失败了，请稍后重试。", false);
                }
            });
        } else if (model === "qwen-vl-max" || model === "qwen-vl-plus") {
            $.ajax({
                url: '/analyzeImage?model=' + model + '&userId=' + userId, // 替换为实际的上传接口地址
                type: 'POST',
                data: formData,
                processData: false,
                contentType: false, // 不设置内容类型，让浏览器自动设置
                success: function (response) {
                    $(`#${waitingMessageId}`).remove();
                    $($.createMessageElement(response, true)).appendTo($("#messages"));
                    let fileName = formData.getAll("file")[0].name;
                    sendMessage("理解图片内容（" + model + "）：" + fileName);
                },
                error: function (xhr, status, error) {
                    $(`#${waitingMessageId}`).remove();
                    $.createMessageElement("上传图片失败了，请稍后重试。", false);
                }
            });
        } else {
            $($.createMessageElement('我要上传新的知识库文档', true)).appendTo($("#messages"));
            $.ajax({
                url: '/knowledge?model=' + model + '&userId=' + userId, // 替换为实际的上传接口地址
                type: 'POST',
                data: formData,
                processData: false,
                contentType: false,
                error: function (xhr, status, error) {
                    $(`#${waitingMessageId}`).remove();
                    $.createMessageElement("上传文件失败了，请稍后重试。", false);
                }
            });
        }
        $('#messages').scrollTop($('#messages')[0].scrollHeight);
    })

    addOnFileContentListener(function (base64, model) {
        if (model === "qwen-vl-max" || model === "qwen-vl-plus") {
            $('#messages').append($.createMessageElement("<img alt='' class='image' src='" + base64 + "'><span class='token splitter'></span>帮我理解一下这张图片的内容", true));
            sendMessage("理解图片内容（" + model + "）：" + base64, false)
        }
    });

    addChatOnMessageListener(function (evt) {
        const data = JSON.parse(evt.data);
        userId = data.user.userId;
        $(`#${waitingMessageId}`).remove();
        let messageElement = $(`#${receiveMessageId}`);
        if (!messageElement.length) {
            messageElement = $($.createMessageElement('', false));
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
        if ("video" === data.type) {
            const videoData = JSON.parse(data.content);
            data.content = "<video class='token video' controls><source src='" + videoData.videoUrl + "'></video>";
            if (videoData.actual_prompt) {
                data.content += "<span class='token splitter'></span>";
                data.content += "<span class='token media-desc'>" + imageData.actual_prompt + "</span>";
            }
        }
        $("<span>").addClass("token").addClass(data.type).html(data.content).appendTo(messageElement.children(".content"));
        if (data.usage && data.usage.cost) {
            $("<span>").addClass("token splitter").appendTo(messageElement.children(".content"));
            $("<span>").addClass("token usage").text(data.usage.cost).appendTo(messageElement.children(".content"));
        }
        $('#messages').scrollTop($('#messages')[0].scrollHeight);
        switch (data.user.model.chat) {
            case "qwen2.5-1.5b-instruct":
                document.title = "小欧 Fancy AI";
                break;
            case "qwen-plus":
                document.title = "小千 Fancy AI";
                break;
            case "deepseek-r1":
                document.title = "小迪 Fancy AI";
                break;
            case "qwen-coder-plus":
                document.title = "小程 Fancy AI";
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

    function sendMessage(text, show) {
        const content = text || $('#message-input').val().trim();
        if (!content) return;
        if (show) $('#messages').append($.createMessageElement(content, true));
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

    $.queryKnowledge = function () {
        $($.createMessageElement('我要查询知识库文档', true)).appendTo($("#messages"));
        addWaitingMessage();
        $('#message-input').val('');
        receiveMessageId = `receive-${Date.now()}`;
        $('#messages').scrollTop($('#messages')[0].scrollHeight);
        $.ajax({
            url: '/knowledge?userId=' + userId, // 替换为实际的上传接口地址
            type: 'GET',
            success: function (response) {
                // 处理成功响应
                console.log(response);
            },
            error: function (xhr, status, error) {
                // 处理错误响应
                console.log(error);
            }
        });
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
        if (!isMobile() && e.which === 13) {
            sendMessage(null, true);
            return false;
        }
    });

    function isMobile() {
        const userAgent = navigator.userAgent || navigator.vendor || window.opera;
        return /android|iphone|ipad|tablet|mobile/i.test(userAgent);
    }
})
;
