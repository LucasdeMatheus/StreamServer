# Instalador Nginx RTMP + HLS

Este script automatiza a instalação do **Nginx** com o módulo **RTMP** habilitado, permitindo streaming via protocolo RTMP e reprodução via HLS (HTTP Live Streaming).

---

## O que o script faz

1. Atualiza o sistema (`apt update` e `apt upgrade`)
2. Instala dependências necessárias para compilar o Nginx e módulos (build-essential, libpcre3, libssl, etc)
3. Baixa o código fonte do Nginx (versão 1.25.3) e o módulo RTMP do GitHub
4. Compila o Nginx com suporte a SSL e módulo RTMP
5. Cria a estrutura de pastas para armazenar os arquivos HLS em `/tmp/hls`
6. Gera um arquivo de configuração `nginx.conf` com as configurações básicas de HTTP e RTMP
7. Inicia o serviço Nginx compilado manualmente

---

## Como usar

1. Salve o script como, por exemplo, `install-nginx-rtmp.sh`

2. Dê permissão de execução:

```bash
chmod +x install-nginx-rtmp.sh
```

3. Execute o script com permissão de sudo (requer conexão com a internet):

```bash
sudo ./install-nginx-rtmp.sh
```

---
## nginx.conf

```
worker_processes  auto;
events {
    worker_connections  1024;
}
http {
    include       mime.types;
    default_type  application/octet-stream;
    sendfile        on;

    server {
        listen 8080; < -- PORTA ESCUTADA PARA VER O STRAMING

        location / {
            root   html;
            index  index.html index.htm;
        }

        location /hls {
            types {
                application/vnd.apple.mpegurl m3u8;
                video/mp2t ts;
            }
            root /tmp;
            add_header Cache-Control no-cache;
            add_header Access-Control-Allow-Origin *;
        }
    }
}

rtmp {
    server {
        listen 1935; < -- PORTA DO SERVIDOR DE ENVIO DO STREAMING
        chunk_size 4096;

        application live {
            live on;
            record off;

            hls on;
            hls_path /tmp/hls;
            hls_fragment 3;
            hls_playlist_length 60;
            allow publish all;
            allow play all;
        }
    }
}
```
---

## Configuração do streaming

- **Enviar stream RTMP para:**

```
rtmp://<IP_DO_SERVIDOR>/live
```

- **Assistir via HLS no navegador:**

```
http://<IP_DO_SERVIDOR>:8080/hls/stream.m3u8
```

---

## Observações importantes

- O diretório `/tmp/hls` é usado para armazenar os fragmentos HLS e a playlist. Ele tem permissão `777` para garantir acesso ao Nginx.
- O script compila o Nginx manualmente na pasta `/usr/local/nginx`.
- A configuração do Nginx está em `/usr/local/nginx/conf/nginx.conf`.
- Para reiniciar o Nginx após alterações na configuração, use:

```bash
sudo /usr/local/nginx/sbin/nginx -s reload
```

- Para parar o Nginx:

```bash
sudo /usr/local/nginx/sbin/nginx -s stop
```


---

## Referências

- [Nginx Official Downloads](https://nginx.org/en/download.html)
- [nginx-rtmp-module GitHub](https://github.com/arut/nginx-rtmp-module)

---

**Desenvolvido por:** Lucas Matheus  
**Data:** Julho 2025