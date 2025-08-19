package com.myproject.obs.service.live;

import com.google.gson.JsonObject;
import com.myproject.obs.domain.Live;
import com.myproject.obs.domain.LiveRepository;
import com.myproject.obs.service.obs.ObsService;
import com.myproject.obs.service.server.FileService;
import io.obswebsocket.community.client.message.request.stream.GetStreamStatusRequest;
import io.obswebsocket.community.client.message.response.stream.GetStreamStatusResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class LiveService {
    @Autowired
    private LiveRepository liveRepository;

    @Autowired
    @Lazy
    private ObsService obsService;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private Live live = new Live();
    @Autowired
    private FileService fileService;



    public void saveLiveInfo(Live live) {
        liveRepository.save(live);
    }

    // crud
    public ResponseEntity<Live> createLive(LiveDTO liveDTO){
        live = new Live();

        if (liveDTO.description().isBlank()){
            live.setDescription("");
        }else{
            live.setDescription(liveDTO.description());
        }
        live.setStartTime(LocalDateTime.now());
        live.setPublicUrl(liveDTO.publicUrl());
        live.setStreamKey(liveDTO.streamKey());
        live.setTitle(liveDTO.title());

        liveRepository.save(live);

        return ResponseEntity.ok(live);
    }
    // by id
    public ResponseEntity<Live> getLive(Long id) {
        return liveRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // to list by live active
    public ResponseEntity<?> getLiveList(boolean isLive) {
        try {
            List<Live> lives = liveRepository.findByLive(isLive);
            return ResponseEntity.ok(lives); // estava retornando só "live"
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("erro: " + e);
        }
    }


    public ResponseEntity<?> updateLive(Long id, LiveUpdateDTO liveUpdateDTO){
        try {
            Live upLive = liveRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Live não encontrada"));


            if (!liveUpdateDTO.description().isBlank()){
                upLive.setDescription(liveUpdateDTO.description());
            }
            if (!liveUpdateDTO.publicUrl().isBlank()){
                upLive.setPublicUrl(liveUpdateDTO.publicUrl());
            }
            if (!liveUpdateDTO.title().isBlank()){
                upLive.setTitle(liveUpdateDTO.title());
            }
            liveRepository.save(upLive);
            return ResponseEntity.ok(upLive);
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("erro: " + e);
        }
    }

    public ResponseEntity<?> deleteLive(Long id){
        try{
            if (!liveRepository.existsById(id)){
                return ResponseEntity.notFound().build();
            }
            if (liveRepository.getReferenceById(id).isLive()){
                return ResponseEntity.badRequest().body("live ativa");
            }
            liveRepository.deleteById(id);
            return ResponseEntity.ok("live deletada");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao deletar live: " + e.getMessage());
        }
    }

    public CompletableFuture<Boolean> updateLiveStream() {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        getStreamingStatus().thenAccept(streamingStatus -> {
            JsonObject body = streamingStatus.getBody();

            if (body != null) {
                if (live.getStoragePath().isBlank()){
                    live.setStoragePath(fileService.getLastFile().getPath());
                }
                boolean isLive = body.get("ativa").getAsBoolean();
                live.setLive(isLive);

                String timecode = body.get("tempo").getAsString();
                live.setDuration(parseTimecodeToDuration(timecode));

                long bytes = body.get("bytes").getAsLong();
                live.setBitrateKbps(bytes / 1000.0);

                saveLiveInfo(live);

                future.complete(isLive);
            } else {
                System.out.println("Resposta vazia.");
                future.complete(false);
            }
        }).exceptionally(ex -> {
            System.out.println("Erro ao obter status da live: " + ex.getMessage());
            future.complete(false);
            return null;
        });

        return future;
    }

    // Método para iniciar o monitoramento periódico
    public void startLiveMonitoring() {
        scheduler.schedule(this::monitorLive, 2, TimeUnit.SECONDS);
    }

    private void monitorLive() {
        updateLiveStream().thenAccept(isLive -> {
            if (isLive) {
                scheduler.schedule(this::monitorLive, 5, TimeUnit.SECONDS);
            } else {
                System.out.println("Live finalizada, monitoramento parado.");
            }
        });
    }




    public CompletableFuture<ResponseEntity<JsonObject>> getStreamingStatus() {
        CompletableFuture<ResponseEntity<JsonObject>> future = new CompletableFuture<>();

        try {
            obsService.getObsRemoteController().sendRequest(
                    GetStreamStatusRequest.builder().build(),
                    (GetStreamStatusResponse response) -> {
                        try {
                            if (!response.isSuccessful()) {
                                throw new RuntimeException("Erro inesperado: " + response.getMessageData());
                            }

                            JsonObject result = new JsonObject();
                            result.addProperty("ativa", response.getOutputActive());
                            result.addProperty("tempo", response.getOutputTimecode());
                            result.addProperty("bytes", response.getOutputBytes());
                            result.addProperty("congestao", response.getOutputCongestion());

                            future.complete(ResponseEntity.ok(result));
                        } catch (Exception e) {
                            JsonObject error = new JsonObject();
                            error.addProperty("erro", e.getMessage());
                            future.complete(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error));
                        }
                    }
            );
        } catch (Exception e) {
            JsonObject error = new JsonObject();
            error.addProperty("erro", e.getMessage());
            future.complete(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error));
        }

        return future;
    }

    // Método auxiliar para converter timecode em Duration
    private Duration parseTimecodeToDuration(String timecode) {
        // timecode no formato "HH:mm:ss.SSS" (exemplo: "00:00:09.666")
        // Remover o ponto e transformar os milissegundos para nanos

        String[] parts = timecode.split(":");
        if (parts.length != 3) {
            return Duration.ZERO; // fallback caso formato inesperado
        }

        try {
            int hours = Integer.parseInt(parts[0]);
            int minutes = Integer.parseInt(parts[1]);

            // Separar segundos e milissegundos (ex: "09.666")
            String[] secParts = parts[2].split("\\.");
            int seconds = Integer.parseInt(secParts[0]);
            int millis = secParts.length > 1 ? Integer.parseInt(secParts[1]) : 0;

            return Duration.ofHours(hours)
                    .plusMinutes(minutes)
                    .plusSeconds(seconds)
                    .plusMillis(millis);
        } catch (NumberFormatException e) {
            return Duration.ZERO;
        }
    }
}
