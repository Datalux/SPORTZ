package it.crisoftdev.sportz.obj;

import java.util.ArrayList;



public class Post {

    private String id;

    private ArrayList<MyLocation> path;
    private long step;
    private double distance;
    private int speed;
    private double maxAltitude;
    private String uid;
    private String authorName;
    private String date;
    private String userPhoto;
    private boolean like;
    private String time;


    public Post(){

    }
    public Post(String id, ArrayList<MyLocation> path, long step, double distance, int speed, double maxAltitude, String time, String uid) {
        this.id = id;
        this.path = path;
        this.step = step;
        this.uid = uid;
        this.distance = distance;
        this.speed = speed;
        this.maxAltitude = maxAltitude;
        this.time = time;
    }

    public Post(ArrayList<MyLocation> path, long step, double distance, int speed, double maxAltitude, String uid) {
        this.path = path;
        this.step = step;
        this.uid = uid;
        this.distance = distance;
        this.speed = speed;
        this.maxAltitude = maxAltitude;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getUserPhoto() {
        return userPhoto;
    }

    public void setUserPhoto(String userPhoto) {
        this.userPhoto = userPhoto;
    }

    public String getId() {
        return id;
    }

    public ArrayList<MyLocation> getPath() {
        return path;
    }

    public void setPath(ArrayList<MyLocation> path) {
        this.path = path;
    }

    public long getStep() {
        return step;
    }

    public String getUid() {
        return uid;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public boolean isLike() {
        return like;
    }

    public void setLike(boolean like) {
        this.like = like;
    }

    public double getDistance() {
        return distance;
    }

    public int getSpeed() {
        return speed;
    }

    public double getMaxAltitude() {
        return maxAltitude;
    }
}
