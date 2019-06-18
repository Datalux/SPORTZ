package it.crisoftdev.sportz.obj;

public class User {

    private String uid;
    private String email;
    private String name;
    private String photoUrl;

    public User(String uid, String email, String name, String photoUrl) {
        this.uid = uid;
        this.email = email;
        this.name = name;
        this.photoUrl = photoUrl;
    }

    public String getUid() {
        return uid;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }
}
