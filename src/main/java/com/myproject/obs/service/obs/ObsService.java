package com.myproject.obs.service.obs;

import com.google.gson.JsonObject;
import com.myproject.obs.service.live.LiveService;
import com.myproject.obs.service.server.FileService;
import io.obswebsocket.community.client.*;
import io.obswebsocket.community.client.message.request.config.SetStreamServiceSettingsRequest;
import io.obswebsocket.community.client.message.request.record.StartRecordRequest;
import io.obswebsocket.community.client.message.request.record.StopRecordRequest;
import io.obswebsocket.community.client.message.request.scenes.*;
import io.obswebsocket.community.client.message.request.stream.*;

import io.obswebsocket.community.client.message.request.stream.StopStreamRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class ObsService {

    private OBSRemoteController obsRemoteController;

    @Autowired
    @Lazy
    private LiveService liveService;

    @Autowired
    private FileService fileService;

    public OBSRemoteController getObsRemoteController() {
        return obsRemoteController;
    }

    // ---------- CONNECT ----------
    public ResponseEntity<String> connect(String host, int port, String password) {
        try {
            this.obsRemoteController = OBSRemoteController.builder()
                    .host(host)
                    .port(port)
                    .password(password)
                    .autoConnect(true)
                    .build();
            return ResponseEntity.ok("Conexão realizada com sucesso" + obsRemoteController);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " +  e );
            // Se quiser, pode lançar RuntimeException ou outro tratamento
            // throw new RuntimeException("Falha na conexão OBS", e);
        }
    }

    // CONFIG
    public CompletableFuture<ResponseEntity<String>> configServerStreaming(JsonObject config) {
        CompletableFuture<ResponseEntity<String>> future = new CompletableFuture<>();

        try {
            String streamServiceType = config.get("streamServiceType").getAsString();
            JsonObject streamServiceSettings = config.getAsJsonObject("streamServiceSettings");

            SetStreamServiceSettingsRequest request = SetStreamServiceSettingsRequest.builder()
                    .streamServiceType(streamServiceType)
                    .streamServiceSettings(streamServiceSettings)
                    .build();

            obsRemoteController.sendRequest(request, response -> {
                if (response.isSuccessful()) {
                    future.complete(ResponseEntity.ok("Configurações realizadas com sucesso"));
                } else {
                    future.complete(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("Erro inesperado: " + response.getMessageData()));
                }
            });

        } catch (Exception e) {
            future.complete(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Erro ao processar configuração: " + e.getMessage()));
        }

        return future;
    }




    // ---------- START STREAM ----------
    public CompletableFuture<ResponseEntity<String>> startStreaming() {
        CompletableFuture<ResponseEntity<String>> future = new CompletableFuture<>();

        obsRemoteController.sendRequest(
                StartStreamRequest.builder().build(),
                response -> {
                    if (response.isSuccessful()) {
                        try {
                            // Tudo certo
                            startRecording();
                            liveService.startLiveMonitoring();
                            future.complete(ResponseEntity.ok("Transmissão iniciada"));

                        } catch (Exception e) {
                            future.complete(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                    .body("Erro ao iniciar a gravação: " + e.getMessage()));
                        }

                    } else {
                        future.complete(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body("Erro inesperado: " + response.getMessageData()));
                    }
                }
        );

        return future;
    }


    // ------------- START RECORDING ----------------
    public CompletableFuture<Boolean> startRecording() {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        obsRemoteController.sendRequest(
                StartRecordRequest.builder().build(),
                response -> {
                    if (response.isSuccessful()) {
                        future.complete(true);
                    } else {
                        future.complete(false);
                    }
                }
        );
        return future;

    }

    // ---------------- STOP STREAMING ----------------------
    public CompletableFuture<ResponseEntity<String>> stopStreaming() {
        CompletableFuture<ResponseEntity<String>> future = new CompletableFuture<>();

        obsRemoteController.sendRequest(
                StopStreamRequest.builder().build(),
                response -> {
                    if (response.isSuccessful()) {
                        stopRecording();
                        future.complete(ResponseEntity.ok("Transmissão finalizada"));
                    } else {
                        future.complete(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body("Erro inesperado: " + response.getMessageData()));
                    }
                }
        );

        return future;
    }

    // -------------- STOP RECORDING -------------
    public CompletableFuture<Boolean> stopRecording() {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        obsRemoteController.sendRequest(
                StopRecordRequest.builder().build(),
                response -> {
                    if (response.isSuccessful()) {
                        future.complete(true);
                    } else {
                        future.complete(false);
                    }
                }
        );
        return future;

    }

    // ---------- SCENE ----------
    public CompletableFuture<ResponseEntity<String>> createSceneRequest(String sceneName) {
        CompletableFuture<ResponseEntity<String>> future = new CompletableFuture<>();

        obsRemoteController.sendRequest(
                CreateSceneRequest.builder().sceneName(sceneName).build(),
                response -> {
                    if (response.isSuccessful()) {
                        future.complete(ResponseEntity.ok("Cena " + sceneName + " criada!"));
                    } else {
                        future.complete(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body("Erro inesperado: " + response.getMessageData()));
                    }
                }
        );
        return  future;
    }

    public CompletableFuture<ResponseEntity<String>> removeSceneRequest(String sceneName) {
        CompletableFuture<ResponseEntity<String>> future = new CompletableFuture<>();

        obsRemoteController.sendRequest(
                RemoveSceneRequest.builder().sceneName(sceneName).build(),
                response -> {
                    if (response.isSuccessful()) {
                        future.complete(ResponseEntity.ok("Cena " + sceneName + " excluida"));
                    } else {
                        future.complete(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body("Erro inesperado: " + response.getMessageData()));
                    }
                }
        );
        return  future;
    }


}
