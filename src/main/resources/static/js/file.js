// 创建隐藏的文件输入框
const $fileInput = $('<input type="file" accept="*/*" style="display: none;">');
const $onFileContentListeners = []
const $onFileUploadListeners = []
const $fileTypeImage = ['image/jpeg', 'image/png', 'image/gif'];
const $fileTypeKnowledge = [
    'application/pdf', "pplication/vnd.openxmlformats-officedocument.wordprocessingml.document",
    "application/msword", " text/plain", "text/markdown",
    "application/vnd.openxmlformats-officedocument.presentationml.presentation",
    "application/vnd.ms-powerpoint", "application/vnd.ms-excel", "image/png",
    "image/jpeg", "image/bmp", "image/gif"
]

function addOnFileContentListener(handler) {
    $onFileContentListeners.push(handler);
}

function addOnFileUploadListener(handler) {
    $onFileUploadListeners.push(handler);
}

$.openFileSelect = function openFIleUpload(modelName) {
    $fileInput.attr("model", modelName).trigger('click');
}

// 文件选择事件
$fileInput.on('change', function (e) {
    const file = e.target.files[0];
    if (!file) return;

    const model = $fileInput.attr("model");
    let validTypes = $fileTypeKnowledge;
    if (model === "wanx2.1-i2v-turbo" || model === "wanx2.1-i2v-plus" || model === "qwen-vl-max") {
        validTypes = $fileTypeImage;
    }

    // 校验文件类型‌:
    if (!validTypes.includes(file.type)) {
        $($.createMessageElement('仅支持' + validTypes.join("/") + '格式的文件', false)).appendTo($("#messages"));
        return;
    }

    if (file.size > 10 * 1024 * 1024) {
        $($.createMessageElement('文件大小不能超过10M。', false)).appendTo($("#messages"));
        return;
    }

    //文件上传
    if ($onFileUploadListeners.length) {
        const formData = new FormData();
        formData.append('file', new File([file], Date.now() + "_" + file.name, {type: file.type}));
        $onFileUploadListeners.forEach(handler => {
            handler(formData, model)
        })
    }
});
