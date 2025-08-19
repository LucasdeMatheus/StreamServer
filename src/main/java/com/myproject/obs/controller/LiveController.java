package com.myproject.obs.controller;

import com.myproject.obs.domain.Live;
import com.myproject.obs.service.live.LiveDTO;
import com.myproject.obs.service.live.LiveService;
import com.myproject.obs.service.live.LiveUpdateDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/live")
public class LiveController {
    @Autowired
    private LiveService liveService;

    // crud
    @PostMapping("/create")
    public ResponseEntity<Live> createLive(@RequestBody LiveDTO liveDTO){
        return liveService.createLive(liveDTO);
    }

    @GetMapping("/get")
    public ResponseEntity<?> getLive(
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) Boolean live
    ) {
        if (id != null && live != null) {
            return ResponseEntity.badRequest().body("Escolha apenas um parâmetro: 'id' OU 'live'");
        }

        if (id != null) {
            return liveService.getLive(id);
        } else if (live != null) {
            return liveService.getLiveList(live);
        } else {
            return ResponseEntity.badRequest().body("Informe 'id' ou 'live'");
        }
    }




    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateLive(@PathVariable Long id, @RequestBody LiveUpdateDTO liveUpdateDTO){
        return liveService.updateLive(id, liveUpdateDTO);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteLive(@PathVariable Long id){
        return liveService.deleteLive(id);
    }

}
