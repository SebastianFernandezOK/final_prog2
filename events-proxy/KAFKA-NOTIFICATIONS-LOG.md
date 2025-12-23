# Sistema de Registro de Notificaciones Kafka

## Ubicación del archivo de log

Las notificaciones Kafka se registran automáticamente en:

```
logs/kafka-notifications.log
```

Este archivo se crea automáticamente cuando la aplicación recibe la primera notificación de Kafka.

## Información registrada

Cada notificación incluye:

- **Timestamp**: Fecha y hora exacta de recepción (formato: yyyy-MM-dd HH:mm:ss.SSS)
- **Topic**: Tópico de Kafka (event-updates)
- **Contenido del mensaje**: Payload completo recibido
- **URL del backend**: Endpoint al que se envía la notificación
- **Respuesta del backend**: Respuesta recibida del backend
- **Tiempo de procesamiento**: Duración en milisegundos
- **Estado**: SUCCESS o FAILED
- **Stack trace**: En caso de error, se incluye el stack trace completo

## Formato del log

```
2025-12-16 13:53:45.123 [kafka-consumer-thread] INFO  EventKafkaConsumerService - ╔════════════════════════════════════════════════════════════════
2025-12-16 13:53:45.123 [kafka-consumer-thread] INFO  EventKafkaConsumerService - ║ KAFKA NOTIFICATION RECEIVED
2025-12-16 13:53:45.123 [kafka-consumer-thread] INFO  EventKafkaConsumerService - ╠════════════════════════════════════════════════════════════════
2025-12-16 13:53:45.123 [kafka-consumer-thread] INFO  EventKafkaConsumerService - ║ Timestamp: 2025-12-16 13:53:45.123
2025-12-16 13:53:45.123 [kafka-consumer-thread] INFO  EventKafkaConsumerService - ║ Topic: event-updates
2025-12-16 13:53:45.123 [kafka-consumer-thread] INFO  EventKafkaConsumerService - ║ Message Content: {...}
2025-12-16 13:53:45.123 [kafka-consumer-thread] INFO  EventKafkaConsumerService - ╚════════════════════════════════════════════════════════════════
```

## Configuración

La configuración del logging se encuentra en `src/main/resources/application.yaml`:

```yaml
logging:
  file:
    name: logs/kafka-notifications.log
  pattern:
    file: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
  level:
    ar.edu.um.events_proxy.service.EventKafkaConsumerService: INFO
```

## Rotación de logs

Spring Boot maneja automáticamente la rotación de logs. Por defecto:
- Tamaño máximo por archivo: 10MB
- Archivos históricos: Se mantienen hasta 7 días

Para modificar estos valores, agregar en `application.yaml`:

```yaml
logging:
  file:
    max-size: 10MB
    max-history: 30
```

## Visualización en tiempo real

Para ver los logs en tiempo real:

```bash
tail -f logs/kafka-notifications.log
```

Para filtrar solo las notificaciones recibidas:

```bash
grep "KAFKA NOTIFICATION RECEIVED" logs/kafka-notifications.log
```

Para ver solo los errores:

```bash
grep "NOTIFICATION FAILED" logs/kafka-notifications.log
```
