package com.example.whatsapp;

public class Contacts {

    public String name,status,profilepic;

    public Contacts()
    {

    }

    public Contacts(String name, String status, String profilepic) {
        this.name = name;
        this.status = status;
        this.profilepic = profilepic;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getProfilepic() {
        return profilepic;
    }

    public void setProfilepic(String profilepic) {
        this.profilepic = profilepic;
    }
}

