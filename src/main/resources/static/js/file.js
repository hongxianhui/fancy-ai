// 创建隐藏的文件输入框
const $fileInput = $('<input type="file" accept="*/*" style="display: none;">');
const $onFileContentListeners = []
const $onFileUploadListeners = []
const $fileTypeImage = ['image/jpeg', 'image/png', 'image/gif'];
const $fileTypeKnowledge = ['text/plain', "application/pdf", "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"]

function addOnFileContentListener(handler) {
    $onFileContentListeners.push(handler);
}

function addOnFileUploadListener(handler) {
    $onFileUploadListeners.push(handler);
}

$.openFileGetImage = function openFIleUpload(modelName) {
    $fileInput.attr("action", "getImage").attr("model", modelName).attr("fileType", $fileTypeImage).trigger('click');
}

$.openFileUpload = function openFIleUpload() {
    $fileInput.attr("action", "upload").attr("fileType", $fileTypeKnowledge).trigger('click');
}

// 文件选择事件
$fileInput.on('change', function (e) {
    const file = e.target.files;
    if (!file) return;

    const validTypes = $fileInput.attr("fileType");
    const action = $fileInput.attr("action");

    // 校验文件类型‌:
    if (!validTypes.includes(file[0].type)) {
        $($.createMessageElement('仅支持' + validTypes.join("/") + '格式的文件', false)).appendTo($("#messages"));
        return;
    }

    if (file[0].size > 512 * 1024) {
        $($.createMessageElement('文件大小不能超过512K。', false)).appendTo($("#messages"));
        return;
    }

    // 读取文件内容‌
    if (action === "getImage" && $onFileContentListeners.length) {
        const reader = new FileReader();
        reader.onload = function (e) {
            const base64 = e.target.result;
            $onFileContentListeners.forEach(handler => {
                handler(base64, $fileInput.attr("model"))
            })
        };
        reader.readAsDataURL(file[0]);
    }

    //文件上传
    if (action === "upload" && $onFileUploadListeners.length) {
        // 创建FormData对象并添加文件
        const formData = new FormData();
        formData.append('file', file[0]);
        $onFileUploadListeners.forEach(handler => {
            handler(formData)
        })
    }
});
