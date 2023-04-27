package hcmute.musicapp;

import com.google.firebase.database.Exclude;

import java.io.Serializable;

public class Song implements Serializable {
    private int id;
    private String title;
    private String single;
    private String image;
    private String resource;
    private String duration;
    @Exclude
    public String getKey() {
        return key;
    }
    @Exclude
    public void setKey(String key) {
        this.key = key;
    }

    private String key;

    public Song() {}

    public Song(String title, String single, String image, String resource) {
        this.title = title;
        this.single = single;
        this.image = image;
        this.resource = resource;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSingle() {
        return single;
    }

    public void setSingle(String single) {
        this.single = single;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }
}

