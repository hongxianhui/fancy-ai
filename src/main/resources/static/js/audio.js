$(function () {
    const audio = document.getElementById('audio');
    const buffer = [];
    let playing = false;

    audio.addEventListener("ended", function (event) {
        if (buffer.length > 0) {
            const blob = buffer.shift();
            audio.src = URL.createObjectURL(blob);
            audio.play();
            playing = true
            return;
        }
        playing = false;
    })

    addSpeechOnMessageListener(function (evt) {
        if (playing) {
            buffer.push(evt.data);
            return;
        }
        audio.src = URL.createObjectURL(evt.data);
        audio.play();
        playing = true
    })

});