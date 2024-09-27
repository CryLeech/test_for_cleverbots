require: slotfilling/slotFilling.sc
  module = sys.zb-common
require: common.js
  module = sys.zb-common
require: conversations.sc
require: functions.js
require: telegram.sc

patterns:
    $phoneNumber = $regexp_i<(\+7|8|7)?[-\s.]?\(?\d{3}\)?[-\s.]?\d{3}[-\s.]?\d{2}[-\s.]?\d{2}>
    $age = $regexp_i<\d{2,3}>

theme: /

    state: Start
        q!: $regex</start>
        a: Подтвердите, есть ли вам 18 лет.
        script:
            log(toPrettyString($response));
        buttons:
            "Подтверждаю" -> ./Agreement
            "Нет 18 лет" -> ./Disagreemrnt

        state: Agreement
            if: testMode()
                go!: /Telegram/TelegramStart
            else:
                if: $request.channelType.toLowerCase() === "whatsapp"
                    random:
                        a: Перевожу наш разговор на оператора.
                        a: Соединяю вас с оператором.
                    script:
                        $response.replies = $response.replies || [];
                        $response.replies.push({
                            "type": "switch", 
                            "firstMessage": "Клиент старше 18 лет, канал WhatsApp.",
                            "ignoreOffline": true
                        });
                elseif: $request.channelType.toLowerCase() === "telegram"
                    go!: /Telegram/TelegramStart
                elseif: $request.channelType.toLowerCase() === "chatapi"
                    go!: /Conversations/Start
                else:
                    random:
                        a: Перевожу наш разговор на оператора.
                        a: Соединяю вас с оператором.
                    script:
                        $response.replies = $response.replies || [];
                        $response.replies.push({
                            "type": "switch", 
                            "firstMessage": "Клиент старше 18 лет, канал " + $request.channelType,
                            "ignoreOffline": true
                        });

        state: Disagreemrnt
            a: Простите, сервис доступен только для совершеннолетних.
            script:
                $analytics.setSessionResult("Клиенту нет 18 лет");
                $jsapi.stopSession();