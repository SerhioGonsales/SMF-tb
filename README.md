## Save My File (телеграм-бот)

> ⏱ Скоро бот будет доступен для тестирования

Телеграм-бот позволяет сохранять файлы и делиться ссылками на них для скачивания. Основная задача создания этого бота заключалась в разработке рабочего инструмента и практическом использовании современных технологий, включая RabbitMQ и микросервисную архитектуру.

### Основные функции приложения:

- регистрация в приложении с подтверждением через email
- загрузка файлов (фото/документа) в чат
- получение сгенерированной ссылки на загруженный файл

### Технологии:

- Spring Boot
- Spring WEB
- JPA
- PostgreSQL (JSONB)
- RabbitMQ
- Java Mail API

### Основные этапу работы с приложением: 

**Главное меню до авторизации**
![Главное меню до авторизации](/images/main-screen-not-auth.png)

**Главное меню после авторизации**
![Главное меню после авторизации](/images/main-screen-auth.png)

**Всплывающий блок для добавления сообщения**
![Всплывающий блок для добавления сообщения](/images/add-message.png)

**Страница пользователя с его сообщенииям и подписками**
![Страница пользователя с его сообщенииям и подписками](/images/user-page.png)



