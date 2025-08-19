# API REST para controle de STREAM via OBS


---

## Base URL STREAM

```
/obs/stream
```

---

## Endpoints

### 1. POST `/connect`

Conecta ao servidor OBS Remote Controller.

- **Request Body**: JSON contendo dados de conexão

```json
{
  "host": "host",
  "port": 0000,
  "password": "password"
}
```

- **Parâmetros**:

| Campo    | Tipo   | Descrição                      |
| -------- | ------ |--------------------------------|
| host     | String | Endereço IP ou hostname do OBS |
| port     | int    | Porta para conexão (ex: 0000)  |
| password | String | Senha do OBS Remote Controller |

- **Resposta**:

```json
200 OK
"Conectado com sucesso" // ou mensagem de erro
```

---

### 2. POST `/start-transmission`

Inicia a transmissão ao vivo no OBS.

- **Request Body**: nenhum

- **Resposta**:

```json
200 OK
  "Transmissão iniciada"
```

- Retorna um `CompletableFuture` com a resposta assíncrona.

---

### 3. POST `/stop-transmission`

Para a transmissão ao vivo no OBS.

- **Request Body**: nenhum

- **Resposta**:

```json
200 OK
  "Transmissão encerrada"
```

- Retorna um `CompletableFuture` com a resposta assíncrona.

---

### 4. POST `/get-transmission-status`

Consulta o status atual da transmissão.

- **Request Body**: nenhum

- **Resposta**:

```json
200 OK
{
  "ativa": true,
  "tempo": "00:15:23.456"
}
```

- Retorna um `CompletableFuture` com a resposta assíncrona contendo se a transmissão está ativa e o tempo decorrido.

---

## Notas

- Os endpoints `/start-transmission`, `/stop-transmission` e `/get-transmission-status` são assíncronos e retornam `CompletableFuture<ResponseEntity<String>>`.
- A conexão deve ser estabelecida previamente via `/connect` para que os outros comandos funcionem.
- Os objetos DTO e resposta devem seguir o padrão JSON indicado.
