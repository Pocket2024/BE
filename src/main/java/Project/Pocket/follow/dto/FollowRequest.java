package Project.Pocket.follow.dto;

import Project.Pocket.follow.entity.Follow;
import lombok.Data;

@Data
public class FollowRequest {
    private Long followerId;
    private Long followingId;

    public FollowRequest(){
        //기본 생성자 : jackson이 json을  객체로 변환할떄 기본생성자 사용 ?
    }

    public FollowRequest(Long followerId, Long followingId) {
        this.followerId = followerId;
        this.followingId = followingId;
    }

    public Long getFollowerId() {
        return followerId;
    }

    public void setFollowerId(Long followerId) {
        this.followerId = followerId;
    }

    public Long getFollowingId() {
        return followingId;
    }

    public void setFollowingId(Long followingId) {
        this.followingId = followingId;
    }
}
