package com.egc.keys;

public class keyGrabber {
    public String get(String name){
        return System.getenv(name);
    }
}
