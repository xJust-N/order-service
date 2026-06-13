# Order Service

Небольшой сервис обработки заказов. Делался как тестовое задание: REST-ручки,
заказы лежат в PostgreSQL, после создания заказа улетает событие в RabbitMQ,
а отдельный слушатель его подхватывает и двигает заказ по статусам.
Документация по API отдаётся через Swagger.

## Стек

- Java 21, Spring Boot 3.5
- PostgreSQL, миграции на Liquibase
- RabbitMQ
- MapStruct + Lombok
- springdoc-openapi (Swagger UI)
- тесты: JUnit 5, Mockito, Testcontainers

## Запуск

Из инструментов нужен только Docker с Compose.

Создайте .env по образцу .env.example:

```
POSTGRES_DB=order_db
POSTGRES_USER=postgres
POSTGRES_PASSWORD=1234
RABBITMQ_USER=rabbit
RABBITMQ_PASSWORD=1234
```

а затем поднять через compose:

```bash
docker compose up --build
```

Поднимутся три контейнера: само приложение, postgres и rabbitmq. Приложение
стартует только после того, как postgres и rabbitmq пройдут healthcheck.

URL:

- API — http://localhost:8080
- Swagger UI — http://localhost:8080/swagger-ui.html
- RabbitMQ — http://localhost:15672 (логин/пароль из `.env`)
- Health — http://localhost:8080/actuator/health

## Архитектура

Структура пакетов:

```
controller   REST и обработка ошибок
service      бизнес-логика, транзакции, отправка событий
repository   Spring Data JPA
messaging    событие order.created и слушатель
model        сущности Order и OrderItem
dto / mapper DTO и маппинг через MapStruct
config       RabbitMQ и OpenAPI
```

`Order` и `OrderItem` связаны как one to many. Статус заказа — enum
(`CREATED`, `PROCESSING`, `COMPLETED`, `CANCELED`).


Дефолты в `application.yaml` настроены на `localhost:5432` и `localhost:5672`;
в контейнере они переопределяются переменными окружения из `docker-compose.yml`.

## Тесты

```bash
./mvnw test
```

- `OrderServiceTest` — юнит на сервис, репозиторий и `RabbitTemplate` замоканы.
- `OrderIntegrationTest` — поднимает настоящие Postgres и RabbitMQ через
  Testcontainers, шлёт POST и проверяет, что заказ сохранился, а слушатель
  перевёл его в `PROCESSING`.

Для интеграционного теста нужен запущенный Docker.

## Коды ответов

- `201` — заказ создан
- `200` — успешные GET и смена статуса
- `400` — ошибки валидации, неизвестный статус или кривое тело запроса
- `404` — заказ не найден

## curl запросы

Создать заказ:

```bash
curl -i -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
        "customerName": "Alice",
        "items": [
          {"productName": "Book", "quantity": 2, "price": 10.00},
          {"productName": "Pen",  "quantity": 5, "price": 1.50}
        ]
      }'
```

Список с фильтром по статусу, пагинацией и сортировкой:

```bash
curl "http://localhost:8080/api/orders?status=PROCESSING&page=0&size=10&sort=orderDate,desc"
```

Один заказ со всеми позициями:

```bash
curl http://localhost:8080/api/orders/{id}
```

Сменить статус:

```bash
curl -i -X PUT http://localhost:8080/api/orders/{id}/status \
  -H "Content-Type: application/json" \
  -d '{"status": "COMPLETED"}'
```

Сумма всех заказов клиента (кастомный запрос с `SUM(price * quantity)` и JOIN):

```bash
curl "http://localhost:8080/api/orders/stats/total-amount?customerName=Alice"
```

