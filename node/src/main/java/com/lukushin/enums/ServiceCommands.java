package com.lukushin.enums;

public enum ServiceCommands {
    START ("/start"),
    CANCEL ("/cancel"),
    HELP ("/help"),
    REGISTRATION ("/registration");

    private final String cmd;

    ServiceCommands(String cmd){
        this.cmd = cmd;
    }

    @Override
    public String toString(){
        return cmd;
    }

    public boolean equals(String cmd){
        return this.cmd.toString().equals(cmd);
    }
}
