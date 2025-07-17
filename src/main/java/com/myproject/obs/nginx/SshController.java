package com.myproject.obs.nginx;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ssh")
public class SshController {

    @Autowired
    private SshService sshService;

    @PostMapping
    public ResponseEntity<String> runCommand(@RequestParam("type") SshType type){
        return sshService.runCommand(type);
    }
}
