package com.myproject.obs.stream;


import com.google.gson.JsonObject;
import io.obswebsocket.community.client.*;
import io.obswebsocket.community.client.message.request.stream.GetStreamStatusRequest;
import io.obswebsocket.community.client.message.request.stream.StartStreamRequest;

import io.obswebsocket.community.client.message.request.stream.StopStreamRequest;
import io.obswebsocket.community.client.message.response.stream.GetStreamStatusResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class ObsService {

    private OBSRemoteController obsRemoteController;

    public CompletableFuture<ResponseEntity<String>> startStreaming() {
        CompletableFuture<ResponseEntity<String>> future = new CompletableFuture<>();

        obsRemoteController.sendRequest(
                StartStreamRequest.builder().build(),
                response -> {
                    if (response.isSuccessful()) {
                        future.complete(ResponseEntity.ok("Transmissão iniciada"));
                    } else {
                        future.complete(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body("Erro inesperado: " + response.getMessageData()));
                    }
                }
        );

        return future;
    }

    public CompletableFuture<ResponseEntity<String>> stopStreaming() {
        CompletableFuture<ResponseEntity<String>> future = new CompletableFuture<>();

        obsRemoteController.sendRequest(
                StopStreamRequest.builder().build(),
                response -> {
                    if (response.isSuccessful()) {
                        future.complete(ResponseEntity.ok("Transmissão pausada"));
                    } else {
                        future.complete(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body("Erro inesperado: " + response.getMessageData()));
                    }
                }
        );

        return future;
    }

    public CompletableFuture<ResponseEntity<String>> getStreamingStatus() {
        CompletableFuture<ResponseEntity<String>> future = new CompletableFuture<>();

        obsRemoteController.sendRequest(
                GetStreamStatusRequest.builder().build(),
                (GetStreamStatusResponse response) -> {
                    if (response.isSuccessful()) {
                        Boolean outputActive = response.getOutputActive();
                        String outputTimecode = response.getOutputTimecode();

                        // Pode ter null, trate se quiser:
                        if (outputActive == null) outputActive = false;
                        if (outputTimecode == null) outputTimecode = "00:00:00.000";

                        JsonObject result = new JsonObject();
                        result.addProperty("ativa", outputActive);
                        result.addProperty("tempo", outputTimecode);

                        future.complete(ResponseEntity.ok(result.toString()));
                    } else {
                        future.complete(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body("Erro inesperado: " + response.getMessageData()));
                    }
                }
        );


        return future;
    }



    public ResponseEntity<String> connect(String host, int port, String password) {
        try {
            this.obsRemoteController = OBSRemoteController.builder()
                    .host(host)
                    .port(port)
                    .password(password)
                    .autoConnect(true)
                    .build();
            return ResponseEntity.ok("Conexão realizada com sucesso");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " +  e );
            // Se quiser, pode lançar RuntimeException ou outro tratamento
            // throw new RuntimeException("Falha na conexão OBS", e);
        }
    }

}
