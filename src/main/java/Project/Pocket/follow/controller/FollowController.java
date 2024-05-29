package Project.Pocket.follow.controller;

import Project.Pocket.follow.dto.FollowRequest;
import Project.Pocket.follow.service.FollowService;
import Project.Pocket.user.entity.User;
import Project.Pocket.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/follow")
public class FollowController {


    @Autowired
    private FollowService followService;
    @Autowired
    private UserService userService;


    @PostMapping("/follow")
    public ResponseEntity<String> followUser(@RequestBody @Validated FollowRequest followRequest, Authentication authentication){
        // 사용자 인증 확인
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        User follower = followService.getUserByID(followRequest.getFollowerId());
        User following = followService.getUserByID(followRequest.getFollowingId());

        followService.follow(follower, following);

        return ResponseEntity.ok("팔로우 성공");
    }
    @GetMapping("/followers/{userId}")
    public ResponseEntity<List<User>> getFollowers(@PathVariable Long userId) {
        // userId에 해당하는 사용자를 데이터베이스에서 가져온다고 가정
        User user = userService.getUserById(userId);

        // 이후에 FollowService를 사용하여 해당 사용자의 팔로워 리스트를 조회하고 반환
        List<User> followers = followService.getFollowers(user);
        return ResponseEntity.ok(followers);
    }

    @GetMapping("/following/{userId}")
    public ResponseEntity<List<User>> getFollowing(@PathVariable Long userId) {
        // userId에 해당하는 사용자를 데이터베이스에서 가져온다고 가정
        User user = userService.getUserById(userId);

        // 이후에 FollowService를 사용하여 해당 사용자의 팔로잉 리스트를 조회하고 반환
        List<User> following = followService.getFollowing(user);
        return ResponseEntity.ok(following);
    }
    @GetMapping("/followersCount/{userId}")
    public ResponseEntity<Integer> getFollowersCount(@PathVariable Long userId){
        User user = userService.getUserById(userId);
        int followersCount = followService.getFollowersCount(user);
        return ResponseEntity.ok(followersCount);
    }
    @GetMapping("/followingsCount/{userId}")
    public ResponseEntity<Integer> getFollowingsCount(@PathVariable Long userId){
        User user = userService.getUserById(userId);
        int followingsCount = followService.getFollowingsCount(user);
        return ResponseEntity.ok(followingsCount);


    }

}
