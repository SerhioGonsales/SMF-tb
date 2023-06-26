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

    public static ServiceCommands fromValue(String v){
        for(ServiceCommands c : ServiceCommands.values()){
            if(c.cmd.equals(v)){
                return c;
            }
        }
        return null;
    }
}
