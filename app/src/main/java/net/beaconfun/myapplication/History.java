package net.beaconfun.myapplication;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by akiya on 2017/10/09.
 */

public class History extends RealmObject {
    @PrimaryKey
    private long id;
    private Date createdAt;
    private String visitor;
    private String location;
    private byte[] thumbnail;
    private String imageURL;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public byte[] getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(byte[] thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getVisitor() {
        return visitor;
    }

    public void setVisitor(String visitor) {
        this.visitor = visitor;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }
}
