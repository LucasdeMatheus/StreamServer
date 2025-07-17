#!/bin/bash

set -e

echo "==> Atualizando o sistema..."
sudo apt update && sudo apt upgrade -y

echo "==> Instalando dependências..."
sudo apt install -y build-essential libpcre3 libpcre3-dev libssl-dev zlib1g-dev git wget curl ffmpeg

echo "==> Baixando o nginx e o módulo RTMP..."
cd /usr/local/src
sudo rm -rf nginx nginx-rtmp-module

sudo wget http://nginx.org/download/nginx-1.25.3.tar.gz
sudo tar -zxvf nginx-1.25.3.tar.gz
sudo git clone https://github.com/arut/nginx-rtmp-module.git

echo "==> Compilando o nginx com suporte a RTMP..."
cd nginx-1.25.3
sudo ./configure --with-http_ssl_module --add-module=../nginx-rtmp-module
sudo make
sudo make install

echo "==> Criando estrutura de pastas para HLS..."
sudo mkdir -p /tmp/hls
sudo chmod -R 777 /tmp/hls

echo "==> Criando arquivo de configuração nginx.conf..."
sudo tee /usr/local/nginx/conf/nginx.conf > /dev/null <<EOF
worker_processes  auto;
events {
    worker_connections  1024;
}
http {
    include       mime.types;
    default_type  application/octet-stream;
    sendfile        on;

    server {
        listen 8080;

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
        listen 1935;
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
EOF

echo "==> Iniciando o nginx..."
sudo /usr/local/nginx/sbin/nginx

echo "✅ Streaming RTMP + HLS instalado com sucesso!"
echo "▶️ Envie seu stream para: rtmp://<IP_DO_SERVIDOR>/live"
echo "📺 Assista em: http://<IP_DO_SERVIDOR>:8080/hls/stream.m3u8"
==> Baixando o nginx e o módulo RTMP...