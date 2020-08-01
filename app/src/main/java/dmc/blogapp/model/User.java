package dmc.blogapp.model;

public class User {
    private String id;
    private String displayName;
    private String profileImg;

    public User() {

    }

    public User(String id, String displayName, String profileImg) {
        this.id = id;
        this.displayName = displayName;
        this.profileImg = profileImg;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getProfileImg() {
        return profileImg;
    }

    public void setProfileImg(String profileImg) {
        this.profileImg = profileImg;
    }
}
