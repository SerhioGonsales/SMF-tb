package com.lukushin.enums;

public enum LinkType {
    GET_DOC("/file/get-doc"),
    GET_PHOTO("/file/get-photo");

    private String link;

    LinkType(String link){
        this.link = link;
    }

    public String toString(){
        return this.link;
    }

}
