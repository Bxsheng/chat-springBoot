package cn.kdream.pojo.bo;

import org.springframework.web.multipart.MultipartFile;

/**
 * @author 笨小昇
 */
public class UsersBo {
    private String userId;
    private MultipartFile file;

    private String nickname;

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    private String username;
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }

    public UsersBo(String userId, MultipartFile file) {
        this.userId = userId;
        this.file = file;
    }
}