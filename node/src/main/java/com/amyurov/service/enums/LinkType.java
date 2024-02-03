package com.amyurov.service.enums;

public enum LinkType {
    GET_DOC("file/get-doc"),
    GET_PHOTO("file/get-photo");
private final String link;
    LinkType(String s) {
        this.link = s;
    }

    @Override
    public String toString() {
        return link;
    }
}
