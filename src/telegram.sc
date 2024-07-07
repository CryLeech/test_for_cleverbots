theme: /Telegram

    state: TelegramStart
        script: $session.checkPhoneNumber = $session.checkPhoneNumber || 0
        if: !testMode()
            script:
                checkPhoneNumber();
        else:
            a: Введите Ваш номер телефона.

        state: TelegramAuth
            event: telegramSendContact
            q: * $phoneNumber * || fromState = /Telegram/TelegramStart/TelegramAuthNoMatch
            q: * $phoneNumber *
            script:
                $temp.response = authCheck($request.query);
            if: $temp.response
                go!: /Conversations/Hello
            else:
                script:
                    $client.phone = $request.query
                a: Уточните ваше ФИО в формате: "Фамилия Имя Отчество (при наличии)".

            state: ClientName
                event: noMatch || fromState = .., onlyThisState = true
                script:
                    $client.client = checkFullName($request.query);
                    $session.checkAge = 0;
                a: Уточните Ваш возраст.

                state: Age
                    q: * $age * || fromState = /Telegram/TelegramStart/TelegramAuth/ClientName/AgeNoMatch
                    q: * $age *
                    script:
                        $client.age = $parseTree._age
                        $session.smsCheck = randomInteger(1000, 9999)
                    if: testMode()
                        a: Ваш код: {{$session.smsCheck}}
                        go!: /Telegram/SmsSuccess
                    else:
                        Sms:
                            text = "Ваш код: " + $session.smsCheck
                            destination = $client.phone
                            okState = /Telegram/SmsSuccess

                state: AgeNoMatch
                    event: noMatch || fromState = .., onlyThisState = true
                    event: noMatch || fromState = /Telegram/TelegramStart/TelegramAuth/ClientName/AgeNoMatch
                    if: $session.checkAge < 3
                        a: Вы указали неверный возраст.
                        script: $session.checkAge++
                    else:
                        a: Вы не прошли авторизацию, попробуйте позже.
                        script:
                            $analytics.setSessionResult("Клиент не прошел авторизацию");
                            $jsapi.stopSession();

        state: TelegramAuthNoMatch
            event: noMatch || fromState = .., onlyThisState = true
            event: noMatch || fromState = /Telegram/TelegramStart/TelegramAuthNoMatch
            script:
                log($session.checkPhoneNumber);
            if: $session.checkPhoneNumber < 3
                a: Неподходящий формат номера телефона.
                script: $session.checkPhoneNumber++
            else:
                a: Вы не прошли авторизацию, попробуйте позже.
                script:
                    $analytics.setSessionResult("Клиент не прошел авторизацию");
                    $jsapi.stopSession();

    state: SmsSuccess
        a: Введите код из смс.

        state: GetCode
            event: noMatch || fromState = .., onlyThisState = true
            script:
                $session.checkGetCode = $session.checkGetCode || 0
            if: $request.query == $session.smsCheck
                script: 
                    registration();
                a: Поздравляю, вы успешно авторизовались.
                go!: /Conversations/Hello
            else:
                if: $session.checkGetCode < 3
                    a: Код не совпадает, повторите попытку
                    script: $session.checkGetCode++
                    go!: /Telegram/SmsSuccess
                else:
                    a: Вы не прошли авторизацию, попробуйте позже
                    script:
                        $analytics.setSessionResult("Клиент не прошел авторизацию");
                        $jsapi.stopSession();