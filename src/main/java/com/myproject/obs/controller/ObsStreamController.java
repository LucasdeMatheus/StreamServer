package com.myproject.obs.controller;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.myproject.obs.service.obs.ObsService;
import com.myproject.obs.service.obs.ObsConnectionDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
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
    @PutMapping("/config")
    public CompletableFuture<ResponseEntity<String>> config(@RequestBody String jsonString) {
        JsonObject config = JsonParser.parseString(jsonString).getAsJsonObject();
        return obsService.configServerStreaming(config);
    }

    @PostMapping("/start-transmission")
    public CompletableFuture<ResponseEntity<String>> start(){
        return obsService.startStreaming();
    }
    @PostMapping("/stop-transmission")
    public CompletableFuture<ResponseEntity<String>> stop(){
        return obsService.stopStreaming();
    }

}
