const userId = generateUUID();
const params = new URLSearchParams(window.location.search);
const apiKey = params.get("apiKey");
let wsSpeech;
const chatOnOpenListeners = [];
const speechOnOpenListeners = [];
const chatOnMessageListeners = [];
const speechOnMessageListeners = [];
const wsChat = new WebSocket("/chat?id=" + userId + "&apiKey=" + (apiKey === null ? "" : apiKey));

function generateUUID() {
    const array = new Uint8Array(16);
    window.crypto.getRandomValues(array);
    return Array.from(array, byte => byte.toString(16).padStart(2, '0')).join('');
}

function addChatOnMessageListener(handler) {
    chatOnMessageListeners.push(handler);
}

function addSpeechOnMessageListener(handler) {
    speechOnMessageListeners.push(handler);
}

function addChatOnOpenListener(handler) {
    chatOnOpenListeners.push(handler);
}

function addSpeechOnOpenListener(handler) {
    speechOnOpenListeners.push(handler);
}

wsChat.onopen = function handleMessage(event) {
    chatOnOpenListeners.forEach(handler => handler(event));
}

wsChat.onclose = function handleMessage(event) {
    $($.createMessageElement('后端服务已断开，请刷新页面重新连接!', false)).appendTo($("#messages"));
}

wsChat.onmessage = function handleMessage(event) {
    if (!wsSpeech) {
        wsSpeech = new WebSocket("/speech?id=" + userId + "&apiKey=" + (apiKey === null ? "" : apiKey));
        wsSpeech.onopen = function handleMessage(event) {
            speechOnOpenListeners.forEach(handler => handler(event));
        }
        wsSpeech.onmessage = function handleMessage(event) {
            speechOnMessageListeners.forEach(handler => handler(event));
        }
    }
    chatOnMessageListeners.forEach(handler => handler(event));
}
