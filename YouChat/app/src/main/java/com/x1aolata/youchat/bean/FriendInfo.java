package com.x1aolata.youchat.bean;

/**
 * @author x1aolata
 * @date 2020/11/24 11:09
 * @script ...
 */
public class FriendInfo {
    private int image;
    private String name;
    private String ip;
    private String time;

    public FriendInfo(int image, String name, String ip, String time) {
        this.image = image;
        this.name = name;
        this.ip = ip;
        this.time = time;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "FriendInfo{" +
                "image=" + image +
                ", name='" + name + '\'' +
                ", ip='" + ip + '\'' +
                ", time='" + time + '\'' +
                '}';
    }
}
