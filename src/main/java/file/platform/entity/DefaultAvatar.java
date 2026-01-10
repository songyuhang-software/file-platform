package file.platform.entity;

import java.time.LocalDateTime;

public class DefaultAvatar {
    private Long id;
    private String avatarUrl;
    private Integer recommendedGender;

    public DefaultAvatar() {
    }

    public DefaultAvatar(String avatarUrl, Integer recommendedGender) {
        this.avatarUrl = avatarUrl;
        this.recommendedGender = recommendedGender;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public Integer getRecommendedGender() {
        return recommendedGender;
    }

    public void setRecommendedGender(Integer recommendedGender) {
        this.recommendedGender = recommendedGender;
    }
}