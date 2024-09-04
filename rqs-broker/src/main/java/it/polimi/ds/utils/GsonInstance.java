package it.polimi.ds.utils;

import com.google.gson.Gson;

import java.util.Objects;

public class GsonInstance {
    private final Gson gson;
    private static GsonInstance instance;

    private GsonInstance(){
        this.gson = new Gson();
    }

    public static GsonInstance getInstance(){
        if(Objects.isNull(instance))
            instance = new GsonInstance();
        return instance;
    }

    public Gson getGson() {
        return gson;
    }
}
