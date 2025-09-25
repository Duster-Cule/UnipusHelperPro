package org.unipus.web.response;

import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class LoginResponse extends Response{
    private String code;
    private String msg;
    private Rs rs;

    public class Rs {
        private String grantingTicket;
        private String serviceTicket;
        private long tgtExpiredTime;
//        @Nullable private String role;
        @SerializedName("openid")
        private String openId;
        private String nickname;
        @Nullable private String fullname;
        private String username;
        private String mobile;
        @Nullable private String email;
        private String perms;
        private String isSsoLogin;
//        @Nullable private String isCompleted;
        @Nullable private String openidHash;
        private String jwt;
        private String rt;
//        private long createTime;
        private int status;
//        @Nullable private String source;
        private List<Link> links;

        public String getOpenId() {
            return openId;
        }

        public String getUsername() {
            return username;
        }

        public String getJwt() {
            return jwt;
        }
    }

    private class Link {
        private String rel;
        private String href;
    }

    public String getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public Rs getRs() {
        return rs;
    }
}
