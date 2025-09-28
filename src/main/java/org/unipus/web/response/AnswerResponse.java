package org.unipus.web.response;

/* (っ*´Д`)っ 小代码要被看光啦 */

public class AnswerResponse extends Response {
    private int code;
    private String message;
    private String data;
    private long publish_version;
    private String k;

    public String getK() {
        return k;
    }

    public String getData() {
        return data;
    }

    public int getCode() {
        return code;
    }
}
