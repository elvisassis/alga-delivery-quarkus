# 🚚 Courier Service (Quarkus)

## 📖 Descrição
O **Courier Service** é responsável por receber solicitações de cálculo de frete a partir dos eventos publicados pelo **Delivery Service** e retornar o resultado via Kafka.  
Ele implementa o papel de **Consumer** para os eventos do Delivery e de **Producer** para eventos de resposta.

---

## ✅ Funcionalidades Implementadas

- Consome eventos do **Delivery Service** via Kafka:
    - `DELIVERY_PLACED_EVENT`
    - `DELIVERY_PICKUP_EVENT`
    - `DELIVERY_FULFILLED_EVENT`
- Calcula o **valor do frete** e tempo estimado de entrega.
- Integração com **PostgreSQL** para persistência de dados.
- Criação automática de tópicos Kafka (um por canal) nos perfis `dev` e `test`.
- Configuração via **application.yaml**.

---

## ⚡ Integração com Kafka

### 📥 Consome eventos (Delivery → Courier):
- **Tópico:** `delivery-placed-events` → Evento `DELIVERY_PLACED_EVENT`
- **Tópico:** `delivery-pick-up-events` → Evento `DELIVERY_PICKUP_EVENT`
- **Tópico:** `delivery-fulfilled-events` → Evento `DELIVERY_FULFILLED_EVENT`



---

## 📂 Estrutura Principal

- `CourierEventConsumer` → Consome mensagens dos tópicos `delivery-placed-events`, `delivery-pick-up-events` e `delivery-fulfilled-events`.
- `CourierEventPublisher` → Publica mensagens no tópico `courier-quote-calculated-events`.
- `CourierService` → Contém a lógica de cálculo do frete.
- `KafkaTopicCreator` → Criação automática de tópicos Kafka em dev/test.

---

## ⚙️ Configuração `application.yaml`

```yaml
quarkus:
  http:
    port: 8082

mp:
  messaging:
    incoming:
      delivery-placed-events:
        connector: smallrye-kafka
        topic: delivery-placed-events
        value:
          deserializer: br.com.elvisassis.infrastructure.kafka.JsonObjectDeserializer
        key:
          deserializer: org.apache.kafka.common.serialization.StringDeserializer

      delivery-pick-up-events:
        connector: smallrye-kafka
        topic: delivery-pick-up-events
        value:
          deserializer: br.com.elvisassis.infrastructure.kafka.JsonObjectDeserializer
        key:
          deserializer: org.apache.kafka.common.serialization.StringDeserializer

      delivery-fulfilled-events:
        connector: smallrye-kafka
        topic: delivery-fulfilled-events
        value:
          deserializer: br.com.elvisassis.infrastructure.kafka.JsonObjectDeserializer
        key:
          deserializer: org.apache.kafka.common.serialization.StringDeserializer

    outgoing:
      courier-quote-calculated-events:
        connector: smallrye-kafka
        topic: courier-quote-calculated-events
        value:
          serializer: br.com.elvisassis.infrastructure.kafka.JsonObjectSerializer
        key:
          serializer: org.apache.kafka.common.serialization.StringSerializer
```

---

## Instruções de Execução

3. Suba o Courier localmente:
   ```bash
   ./mvnw quarkus:dev
   ```

---

## 📦 Tópicos Kafka

- Entrada (Delivery → Courier)
    - `delivery-placed-events`
    - `delivery-pick-up-events`
    - `delivery-fulfilled-events`  
