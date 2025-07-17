# Controle Remoto do Nginx via SSH - API Spring Boot

Este projeto é uma API REST para controlar o servidor Nginx em um servidor Ubuntu remoto via SSH.

## Funcionalidades

- Iniciar o servidor Nginx
- Parar o servidor Nginx
- Reiniciar o servidor Nginx
- Verificar status do servidor Nginx

## Requisitos

- Servidor Ubuntu com Nginx instalado e configurado
- script para instalar o Nginx ➡️ [Script com path configurada](https://github.com/LucasdeMatheus/StreamServer)
- SSH configurado e acessível pelo servidor da API
- Java 21 (ou compatível) para rodar a API Spring Boot

## Configuração

Configure as variáveis no arquivo `application.properties`:

```properties
ssh.host=IP_DO_SERVIDOR_UBUNTU
ssh.user=USUARIO
ssh.password=SENHA
```

## Endpoints

### Controlar Nginx

`POST /ssh?type={tipo}`

Parâmetro `type` pode ser:

- `start` — iniciar Nginx
- `stop` — parar Nginx
- `reload` — recarregar configuração do Nginx
- `status` — obter status do Nginx

Exemplo:

```bash
curl -X POST "http://localhost:8080/ssh?type=reload"
```

Resposta:

- Sucesso: `200 OK` com mensagem de retorno do comando
- Erro: `500 Internal Server Error` com descrição do problema

## Como funciona internamente

A API conecta via SSH ao servidor Ubuntu, executa o comando correspondente e retorna o resultado da execução.
