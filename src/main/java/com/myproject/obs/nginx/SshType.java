package com.myproject.obs.nginx;

public enum SshType {
    START("sudo /usr/local/nginx/sbin/nginx"),
    STOP("sudo /usr/local/nginx/sbin/nginx -s stop"),
    RESTART("sudo /usr/local/nginx/sbin/nginx -s reload"),
    STATUS("ps aux | grep nginx");

    private final String command;

    SshType(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }
}

