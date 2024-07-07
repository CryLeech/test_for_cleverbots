function testMode() {
    return $.request.channelType === "chatwidget";
}

function checkPhoneNumber() {
    var reply = {
        type: "raw",
        method: "sendMessage",
        body: {
            chat_id: $.request.data.chatId,
            text: text,
            reply_markup: {
                keyboard: [
                    [{text: "Поделиться номером", request_contact: true}],
                ],
                resize_keyboard: true,
            },
            disable_web_page_preview: true,
            parse_mode: "html",
        }
    }
    $response.replies = $response.replies || [];
    $response.replies.push(reply);
}

function authCheck(phoneNumber) {
    var url = $.injector.authUrl;
    var options = {
        dataType: "json",
        headers: {
            "Content-Type": "application/json"
        },
        body: {
            "phoneNumber": phoneNumber
        }
    };
    var response = $http.post(url, options);
    return response.isOk ? response.data : false;
}

function registration() {
    var url = $.injector.registrationUrl;
    var options = {
        dataType: "json",
        headers: {
            "Content-Type": "application/json"
        },
        body: {
            "timestamp": $jsapi.timeForZone("Europe/Moscow"),
            "client": $.client.client,
            "age" : $.client.age,
            "channel": $.request.channelType
        }
    };
    var response = $http.post(url, options);
    return response.isOk ? response.data : false;
}

function checkFullName(fullName) {
    var surName = fullName.split(" ")[0];
    var name = fullName.split(" ")[1];
    var fName = fullName.split(" ").length > 2 ? fullName.split(" ")[2] : undefined;
    return fName ? {"surName": surName, "name": name, "fName": fName} : {"surName": surName, "name": name, "fName": null};
}