# Big Data test case

Данный проект выполнялся в ходе отбора на стажировку на вакансию Java Big Data developer.


## Pre-require

+ [Apache Spark 2.12 for Hadoop 2.7](https://spark.apache.org/downloads.html)
+ [Docker](https://docs.docker.com/install/)
+ [Docker Compose](https://docs.docker.com/compose/install/)
+ Java 8.0+
+ libpcap
+ Maven

## Задание
Было необходимо:

1. Развернуть Apache Kafka и PostgreSQL в Docker-контейнерах 
2. Создать в БД схему traffic_limits с таблицей limits_per_hour. Таблица должна содержать 3 колонки: limit_name, limit_value, effective_date. Задать 2 лимита: min=1024, max=1073741824. В колонку effective_date внести дату, начиная с которой эти лимиты вступают в силу. 
3. Написать приложение на Spark Streaming, которое, используя любую общедоступную библиотеку для обработки трафика (Pcap4J, jpcap, etc), будет считать объем захваченного трафика за 5 минут и в случае выхода за пределы минимального и максимального значения будет посылать сообщение в Kafka в топик alerts. Сообщение должно посылаться всякий раз, когда объем трафика за 5 минут пересекает любое из пороговых значений. 
4. Приложение должно обновлять пороговые значения каждые 20 минут (следует брать значения с максимальной effective_date).
5. Написать unit тесты.
6. Предусмотреть возможность считать только тот трафик, который отправляется/принимается на/с определенного IP-адреса, который задается в качестве аргумента при сабмите. По умолчанию (если IP не указан) должен учитываться весь трафик.
7. Предусмотреть возможность обновления пороговых значений сразу после их обновления в базе данных.

## How to run

Для того, чтобы запустить приложение, необходимо клонировать репозиторий.

Затем в перейти в корневую папку проекта и собрать контейнеры:

```
docker-compose build
```

Запустить контейнеры можно с помощью команды **up**:

```
docker-compose up -d
```

Затем необходимо собрать uber-jar, содержащий все зависимости:
```
mvn clean package
```

После этого можно запустить проект с помощью команды spark-submit:
```
cd /path/to/your/spark
bin/spark-submit --class Application /path/to/your/project/target/uber-DINS-test-case.jar
```

Проект запущен. Для доступа к терминалу контейнеров можно использовать команду:

``` 
docker exec -it [container-name] bash
```

Для доступа к базе данных в PostgreSQL:
```
docker exec -it [postgres-container-name] bash
psql -U postgres dins
```

## Итоги выполнения
Были выполнены все основные пункты за исключением возможности считывания траффика, 
который отправляется/принимается на/с определенного IP-адреса. 
Также не были разработаны Unit-тесты в силу нехватки времени из-за большого количества материала для изучения.

Захват трафика на/с определенного IP-адреса возможно реализовать с помощью встроенных возможностей библиотеки pcap4J
(с использованием BPF синтаксиса фильтрами *dst host, src host* или *host* )
