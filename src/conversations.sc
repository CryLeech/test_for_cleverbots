theme: /Conversations
    
    state: Start
        if: $request.data.isAuth
            go!: /Conversations/Hello
        else:
            a: Вы не авторизованы на сайте. Сначала пройдите авторизацию на сайте.
            script:
                $analytics.setSessionResult("Клиенту не авторизован на сайте");
                $jsapi.stopSession();

    state: Hello
        if: !$injector.accident
            a: Добро пожаловать! Что вас интересует?
            go!: /Conversations/MainMenu
        else:
            a: Простите, сервис временно не работает
            script:
                $analytics.setSessionResult("Авария");
                $jsapi.stopSession();
                
    state: MainMenu
        a: Где-то тут должен быть основной бот.
        script:
            $jsapi.stopSession();

    state: ToOperator
        random:
            a: Перевожу наш разговор на оператора.
            a: Соединяю вас с оператором.
        script:
            $response.replies = $response.replies || [];
            $response.replies.push({
                "type": "switch", 
                "firstMessage": "Клиенту старше 18 лет, авторизация пройдена",
                "ignoreOffline": true
            });