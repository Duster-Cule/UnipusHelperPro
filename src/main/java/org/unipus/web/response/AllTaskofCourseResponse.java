package org.unipus.web.response;

public class AllTaskofCourseResponse extends Response {
    private int code;
    private String course;
    private int version;
    private int publish_version;
    private String k;

    public int getCode() {
        return code;
    }

    public String getCourse() {
        return course;
    }

    public int getVersion() {
        return version;
    }

    public int getPublish_version() {
        return publish_version;
    }

    public String getK() {
        return k;
    }
}
