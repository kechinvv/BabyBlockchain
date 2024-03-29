# language: ru
@starting
Функция: Запуск узла

  @fail
  Сценарий: Запуск без указания соседних узлов
    Когда пользователь не указал переменную NEIGHBORS
    Тогда пользователь получит ошибку NEIGHBORS must not be null

  @fail
  Сценарий: Запуск без указания порта
    Когда пользователь не указал переменную PORT
    Тогда пользователь получит ошибку PORT must not be null

  @success
  Сценарий: Запуск вторичного узла без переменной MASTER
    Когда пользователь не указал переменную MASTER
    Тогда узел будет ждать блок из сети

  @success
  Сценарий: Запуск вторичного узла с MASTER != true
    Когда пользователь не указал переменную MASTER=true
    Тогда узел будет ждать блок из сети

  @success
  Сценарий: Запуск главного узла
    Когда пользователь указал переменную MASTER=true
    Тогда узел начнет создавать блоки