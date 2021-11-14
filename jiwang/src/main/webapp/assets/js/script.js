const Toast = Swal.mixin({
    toast: true,
    position: 'top-end',
    showConfirmButton: false,
    timer: 3000,
    timerProgressBar: true,
    didOpen: (toast) => {
        toast.addEventListener('mouseenter', Swal.stopTimer)
        toast.addEventListener('mouseleave', Swal.resumeTimer)
    }
});

if (!$.cookie('name')) {
    $.cookie('avatarid', '0');
    changeUserName();
} else {
    $("#userName").text($.cookie('name'));
    startChat();
}

function startChat() {
    $.cookie('order',0)
    setInterval(function(){
        getUserList();
        refreshAvatar();
    },5000);
    setInterval(function(){
        receiver();
    },1000);
}


function getAvatar(id = 0) {
    return "assets/avatar/" + id + ".png";
}

function refreshAvatar() {
    if ($.cookie('avatarid')) {
        $(".localavatar").attr('src', getAvatar($.cookie('avatarid')));
    } else {
        $.cookie('avatarid', '0');
        refreshAvatar();
    }
}

function changeAvatar() {
    var avatarid = $.cookie('avatarid');
    if (avatarid <= 9) {
        avatarid++;
    } else {
        avatarid = 0;
    }
    $.cookie('avatarid', avatarid);
    refreshAvatar();
}

function changeUserName() {
    Swal.fire({
        title: '欢迎进入聊天室',
        html: '<div class="avatar avatar-xl m-1" onclick="changeAvatar();"><img src="assets/avatar/0.png" alt="avatar" class= "localavatar" ></div><p class="text-muted text-sm-center ">点击切换头像</p>',
        input: 'text',
        inputLabel: '请在下方输入您的用户名',
        inputValue: userName,
        allowEscapeKey: false,
        allowOutsideClick: false,
        showCancelButton: false,
        inputValidator: (value) => {
            if (value) {
                userName = value;
                $.cookie('name', userName);
                $("#userName").text(userName);
                startChat();
            } else {
                changeUserName();
            }
        }
    })
}

function playAudio(obj) {
    var audioid = obj.getAttribute('value');
    var audio = document.getElementById(audioid);
    audio.play();
}

let recorder = new Recorder({
    sampleBits: 16, // 采样位数，支持 8 或 16，默认是16
    sampleRate: 24000, // 采样率，支持 11025、16000、22050、24000、44100、48000，根据浏览器默认值，我的chrome是48000
    numChannels: 1, // 声道，支持 1 或 2， 默认是1
});

function startRecording() {
    recorder.start();
    timer = window.setInterval(function () {
        $("#timer").text(timeFormat(Math.ceil(recorder.duration)));
    }, 1000);
}

function timeFormat(during) {
    var s = Math.floor(during / 1) % 60;
    if (s < 10) s = "0" + s;
    during = Math.floor(during / 60);
    var i = during % 60;
    if (i < 10) i = "0" + i;
    return i + ':' + s;
}

function stopRecording() {
    recorder.stop();
    //clearIntervalclearInterval(timer);
    $("#recording").toggle();
    $("#playing").toggle();
    $("#finishRecording").text("提交");
    $("#finishRecording").attr("onclick", "submitRecording();");
    $("#finishRecording").attr("data-bs-dismiss", "modal");
}

function submitRecording() {
    $("#recording").toggle();
    $("#playing").toggle();
    var filename = $.cookie('name') + '_' + Date.now() + '.wav';
    var fileblob = recorder.getWAVBlob();
    var file = new File([fileblob], filename, {
        type: 'audio/wav'
    });
    console.log(file);
    var formData = new FormData();
    formData.append('name', encodeURI($.cookie('name')));
    formData.append('avatarid', $.cookie('avatarid'));
    formData.append('type', 'audio');
    formData.append('content', file);
    if (sender(formData)) {
        swal.fire({
            title: '语音发送成功！',
            icon: 'success'
        });
    } else {
        swal.fire({
            title: '语音发送失败！',
            icon: 'error'
        });
    }

    //recorder.downloadWAV(Date.now());
    recorder.destroy();
}

function selectPhoto() {
    $("#uploadFile").attr("onchange", "uploadPhoto();");
    $("#uploadFile").attr("accept", ".jpg, .png, .webp, .gif");
    $("#uploadFile").click();
}

function uploadPhoto() {
    $("#uploadFile").attr("onchange", "");
    $("#uploadFile").attr("accept", "");
    if ($("#uploadFile").val() == '') {
        Swal.fire('Oooops!', '图片上传失败！', 'error');
        return;
    }
    var formData = new FormData();
    formData.append('name', encodeURI($.cookie('name')));
    formData.append('avatarid', $.cookie('avatarid'));
    formData.append('type', 'image');
    formData.append('content', document.getElementById('uploadFile').files[0]);
    if (sender(formData)) {
        swal.fire({
            title: '图片发送成功！',
            icon: 'success'
        });
    } else {
        swal.fire({
            title: '图片发送失败！',
            icon: 'error'
        });
    }
    $("#uploadFile").val("");
}

function selectAudio() {
    $("#uploadFile").attr("onchange", "uploadAudio();");
    $("#uploadFile").attr("accept", ".mp3, .wav, .ogg");
    $("#uploadFile").click();
}

function uploadAudio() {
    $("#uploadFile").attr("onchange", "");
    $("#uploadFile").attr("accept", "");
    if ($("#uploadFile").val() == '') {
        Swal.fire('Oooops!', '音频上传失败！', 'error');
        return;
    }
    var formData = new FormData();
    formData.append('name', encodeURI($.cookie('name')));
    formData.append('avatarid', $.cookie('avatarid'));
    formData.append('type', 'audio');
    formData.append('content', document.getElementById('uploadFile').files[0]);
    if (sender(formData)) {
        swal.fire({
            title: '音频发送成功！',
            icon: 'success'
        });
    } else {
        swal.fire({
            title: '音频发送失败！',
            icon: 'error'
        });
    }
    $("#uploadFile").val("");
}

function selectVideo() {
    $("#uploadFile").attr("onchange", "uploadVideo();");
    $("#uploadFile").attr("accept", ".mp4, .webm, .ogg");
    $("#uploadFile").click();
}

function uploadVideo() {
    $("#uploadFile").attr("onchange", "");
    $("#uploadFile").attr("accept", "");
    if ($("#uploadFile").val() == '') {
        Swal.fire('Oooops!', '视频上传失败！', 'error');
        return;
    }
    var formData = new FormData();
    formData.append('name', encodeURI($.cookie('name')));
    formData.append('avatarid', $.cookie('avatarid'));
    formData.append('type', 'video');
    formData.append('content', document.getElementById('uploadFile').files[0]);
    if (sender(formData)) {
        swal.fire({
            title: '视频发送成功！',
            icon: 'success'
        });
    } else {
        swal.fire({
            title: '视频发送失败！',
            icon: 'error'
        });
    }
    $("#uploadFile").val("");
}

function selectDocument() {
    $("#uploadFile").attr("onchange", "uploadDocument();");
    $("#uploadFile").attr("accept", "");
    $("#uploadFile").click();
}

function uploadDocument() {
    $("#uploadFile").attr("onchange", "");
    if ($("#uploadFile").val() == '') {
        Swal.fire('Oooops!', '视频上传失败！', 'error');
        return;
    }
    var formData = new FormData();
    formData.append('name', encodeURI($.cookie('name')));
    formData.append('avatarid', $.cookie('avatarid'));
    formData.append('type', 'file');
    formData.append('content', document.getElementById('uploadFile').files[0]);
    if (sender(formData)) {
        swal.fire({
            title: '文件发送成功！',
            icon: 'success'
        });
    } else {
        swal.fire({
            title: '文件发送失败！',
            icon: 'error'
        });
    }
    $("#uploadFile").val("");
}

function sender(formData) {
    let flag = true;
    $.ajax({
        url: "./send",
        type: "post",
        data: formData,
        contentType: false,
        processData: false,
        success: function () {
            flag = true;
        },
        error: function (data) {
            console.log("无法连接至后端！");
            flag = false;
        }
    });
    return flag;
}

function getUserList() {
    $.ajax({
        url: "./heart",
        type: "post",
        dataType: "json",
        data: {
            "name": encodeURI($.cookie('name')),
            "avatarId": $.cookie('avatarid')
        },
        success: function (data) {
            $(".userCount").text(data.count);
            $("#userListPanel").empty();
            data.list.forEach(function (user) {
                var html = "<li onclick=\"addAtMsg(this);\" name=\"" + decodeURI(user.name) + "\"><span class=\"dropdown-item m-0 onlineUser\"><span class=\"avatar avatar-sm \"><img src=\"" + getAvatar(user.avatarId) + "\" alt=\"avatar\" /></span><span class=\"text-left ms-1\">" + decodeURI(user.name) + "</span></span><li>";
                $("#userListPanel").append(html);
            });

        },
        error: function (xhr, textStatus, errorThrown) {

        }
    });
}

function addText(text) {
    $(".message").val($(".message").val() + text);
}

function addAtMsg(obj) {
    var name = obj.getAttribute("name");
    addText(" @" + name);
}

function atNotice(text) {
    var preg = RegExp("@"+$.cookie("name"));
    if (preg.test(text)) {
        Toast.fire({
            icon: 'info',
            title: '您有一条新的@提醒：\n' + text
        })
    }
}

function tapNotice(text) {
    var preg = RegExp(" "+$.cookie("name"));
    if (preg.test(text)) {
        Toast.fire({
            icon: 'info',
            title: '您有一条新的拍一拍提醒：\n' + text
        })
    }
}

function sendTap(obj) {
    var name = obj.getAttribute("name");
    var text = $.cookie("name") + " Tapped " + name;
    var formData = new FormData();
    formData.append('name', encodeURI($.cookie('name')));
    formData.append('avatarid', $.cookie('avatarid'));
    formData.append('type', 'tap');
    formData.append('content', encodeURI(text));
    if (sender(formData)) {
        /*swal.fire({
            title: '文件发送成功！',
            icon: 'success'
        });*/
    } else {
        swal.fire({
            title: '拍一拍发送失败！',
            icon: 'error'
        });
    }
}

function receiver() {
    $.ajax({
        url: "./receive",
        type: "post",
        dataType: "json",
        data: {
            order: $.cookie('order')
        },
        success: function (data) {
            messageHandler(data);
        },
        error: function () {
            Toast.fire({
                icon: 'error',
                title: '消息接收失败！'
            })
        }
    });
}

function messageHandler(data) {
    data.forEach(function (msg) {
        $.cookie('order',msg.order);
        var type = msg.type;
        var name = decodeURI(msg.name);
        var content = decodeURI(msg.content);
        var avatarId = msg.avatarId;

        var html = "";
        if (type == "TAP") {
            tapNotice(content);
            html += "<div class=\"divider\"><div class=\"divider-text\">" + content + "</div></div>";
        } else {
            if (name == $.cookie('name')) {
                html += "<div class=\"chat\"> \
                <div class=\"chat-avatar\"  onclick=\"sendTap(this);\" name=\"" + name + "\"> \
                    <span class=\"avatar avatar-lg box-shadow-1 cursor-pointer\"> \
                        <img src=\"" + getAvatar(avatarId) + "\" alt=\"avatar\" height=\"42\" width=\"42\" class=\"localavatar\" /> \
                    </span>\
                </div>\
                <div class=\"chat-body\">\
                    <h6 class=\"chat-name cursor-pointer\" onclick=\"addText(' @'+$(this).text());\">" + name + "</h6>";
            } else {
                html += "<div class=\"chat chat-left\"> \
            <div class=\"chat-avatar\"  onclick=\"sendTap(this);\" name=\"" + name + "\"> \
                <span class=\"avatar avatar-lg box-shadow-1 cursor-pointer\"> \
                    <img src=\"" + getAvatar(avatarId) + "\" alt=\"avatar\" height=\"42\" width=\"42\" /> \
                </span>\
            </div>\
            <div class=\"chat-body\">\
                <h6 class=\"chat-name cursor-pointer\" onclick=\"addText(' @'+$(this).text());\">" + name + "</h6>";
            }
            switch (type) {
                case "TEXT": {
                    atNotice(content);
                    html += "<div class=\"chat-content\"><p>" + content + "</p></div></div></div>";
                    break;
                }
                case "IMAGE": {
                    html += "<div class=\"chat-content\"> <img src=\"." + content + "\" class=\"chat-img img-responsive\"></img></div></div></div>";
                    break;
                }
                case "AUDIO": {
                    var audioid = src2name(content) + generateStr(4);
                    html += "<audio src=\"." + content + "\" id=\"" + audioid + "\" style=\"display:none\"></audio>\
                    <button class=\"btn btn-info btn-play chat-content\" value=\"" + audioid + "\"onclick=\"playAudio(this);\"><i data-feather=\"mic\"></i> Play Audio Here</button></div></div>";
                    break;
                }
                case "VIDEO": {
                    html += "<div class=\"chat-content\"><video src=\"." + content + "\" controls=\"controls\" class=\"ratio\"></video></div></div></div>";
                    break;
                }
                case "FILE": {
                    html += "<a href=\"" + content + "\" download=\"" + src2name(content) + "\" class=\"btn btn-play chat-content\"><i data-feather=\"file\"></i> 点击下载文件</a></div></div>";
                    break;
                }
                case "VIDEOCHAT": {
                    html += "<a href=\"" + content + "\" target=\"_blank\" class=\"btn btn-play chat-content\"><i data-feather=\"video\"></i> 加入视频通话</a></div></div>";
                    break;
                }
                default: {
                    html += "<div class=\"chat-content\"><p>暂不支持的消息类型</p></div></div></div>";
                    break;
                }
            }
        }
        $(".chats").append(html);
        feather.replace({width: 14,height: 14});
        $('.user-chats').scrollTop($('.user-chats > .chats').height());
    });
}

function src2name(src, flag = 0) {
    console.log(src)
    var name = src.substring(src.lastIndexOf("/") + 1); //去除路径
    if (flag) {
        name = src.substring(0, src.lastIndexOf(".")); //去除拓展名
    }
    return name;
}

function generateStr(len) {
    var res = Math.random().toString(24).slice(2);
    var resLen = res.length;
    while (resLen < len) {
        res += Math.random().toString(24).slice(2);
        resLen = res.length;
    }
    return res.substr(0, len);
}

function startVideoChat(){
    var room="stream.html#"+generateStr(6);
    console.log(room);
    var formData = new FormData();
    formData.append('name', encodeURI($.cookie('name')));
    formData.append('avatarid', $.cookie('avatarid'));
    formData.append('type', 'videochat');
    formData.append('content', room);
    if (sender(formData)) {
        window.open(room);
    } else {
        swal.fire({
            title: '视频聊天创建失败！',
            icon: 'error'
        });
    }
}