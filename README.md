# udemy-kafka

https://www.udemy.com/course/apache-kafka

## instalace a spusteni

- stahnout https://kafka.apache.org/downloads
- rozbalit treba do C:\work\kafka_2.12-2.8.0
- vytvorit adresare: C:\work\kafka_2.12-2.8.0\data\zookeeper a C:\work\kafka_2.12-2.8.0\data\kafka
- v C:\work\kafka_2.12-2.8.0\config\zookeeper.properties zmenit dataDir=C:\work\kafka_2.12-2.8.0\data\zookeeper
- v C:\work\kafka_2.12-2.8.0\config\server.properties zmenit log.dirs=C:\work\kafka_2.12-2.8.0\data\kafka
- spustit zookeeper: C:\work\kafka_2.12-2.8.0\bin\windows>zookeeper-server-start.bat C:\work\kafka_2.12-2.8.0\config\zookeeper.properties
- zkontrolovat vystup zookeeperu, na kterem bezi portu, default 2181
- spustit kafku: C:\work\kafka_2.12-2.8.0\bin\windows>kafka-server-start.bat C:\work\kafka_2.12-2.8.0\config\server.properties
- zkontrolovat vystup kafky, na kterem bezi portu, default 9092

### chyby

- pridat do C:\work\kafka_2.12-2.8.0\config\server.properties dva radky: port=9092 a advertised.host.name=localhost