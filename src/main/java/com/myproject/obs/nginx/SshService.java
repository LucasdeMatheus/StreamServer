package com.myproject.obs.nginx;

import com.jcraft.jsch.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Service
public class SshService {
    @Value("${ssh.host}")
    private String host;

    @Value("${ssh.user}")
    private String user;

    @Value("${ssh.password}")
    private String password;

    public ResponseEntity<String> runCommand(SshType sshType) {
        StringBuilder output = new StringBuilder();

        try {
            JSch jsch = new JSch();
            Session session = jsch.getSession(user, host, 22);
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();

            ChannelExec channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(sshType.getCommand());
            channel.setPty(true);
            InputStream in = channel.getInputStream();
            channel.connect();

            byte[] tmp = new byte[1024];
            while (true) {
                while (in.available() > 0) {
                    int i = in.read(tmp, 0, 1024);
                    if (i < 0) break;
                    output.append(new String(tmp, 0, i));
                }
                if (channel.isClosed()) break;
                Thread.sleep(100);
            }

            channel.disconnect();
            session.disconnect();
        } catch (Exception e) {
            output.append("Erro: ").append(e.getMessage());
        }

        return ResponseEntity.ok("Sucess " + output.toString());
    }
}
