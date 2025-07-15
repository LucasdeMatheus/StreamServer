package com.myproject.obs.stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/obs/stream")
public class ObsStreamController {
    @Autowired
    private final ObsService obsService;

    @Autowired
    public ObsStreamController(ObsService obsService) {
        this.obsService = obsService;
    }

    @PostMapping("/connect")
    public ResponseEntity<String> connect(@RequestBody ObsConnectionDTO dto) {
        return obsService.connect(dto.host(), dto.port(), dto.password());
    }

    @PostMapping("/start-transmission")
    public CompletableFuture<ResponseEntity<String>> start(){
        return obsService.startStreaming();
    }
    @PostMapping("/stop-transmission")
    public CompletableFuture<ResponseEntity<String>> stop(){
        return obsService.stopStreaming();
    }
    @PostMapping("/get-transmission-status")
    public CompletableFuture<ResponseEntity<String>> getStatus(){
        return obsService.getStreamingStatus();
    }
}
