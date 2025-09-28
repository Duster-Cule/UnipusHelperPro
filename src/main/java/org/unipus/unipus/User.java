package org.unipus.unipus;

/* (っ*´Д`)っ 小代码要被看光啦 */

public class User {
    private String username;
    private String jwt;
    private String openId;
    private String name;
    private String schoolName;
    private String appUserId;
    private String ssoId;


    public User(String username, String jwt, String openId) {
        this.username = username;
        this.jwt = jwt;
        this.openId = openId;
    }

    public String getUsername() {
        return username;
    }

    public String getJwt() {
        return jwt;
    }

    public String getOpenId() {
        return openId;
    }

    public String getName() {
        return name;
    }

    public String getSchoolName() {
        return schoolName;
    }

    public String getAppUserId() {
        return appUserId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSchoolName(String schoolName) {
        this.schoolName = schoolName;
    }

    public void setAppUserId(String appUserId) {
        this.appUserId = appUserId;
    }

    public String getSsoId() {
        return ssoId;
    }

    public void setSsoId(String ssoId) {
        this.ssoId = ssoId;
    }
}
