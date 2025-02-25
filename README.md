# run the kafka consumer
docker exec -it qtra-kafka sh
kafka-console-consumer --bootstrap-server localhost:9092 --topic tls-scan-results --from-beginning

# api call
curl -X GET "http://localhost:8080/tls/scan?domain=example.com"