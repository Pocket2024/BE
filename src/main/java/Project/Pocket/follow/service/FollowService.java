package Project.Pocket.follow.service;

import Project.Pocket.follow.entity.Follow;
import Project.Pocket.follow.entity.FollowRepository;
import Project.Pocket.follow.exception.FollowException;
import Project.Pocket.user.entity.User;
import Project.Pocket.user.entity.UserRepository;
import Project.Pocket.user.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FollowService {

    @Autowired
    private FollowRepository followRepository;
    @Autowired
    private UserRepository userRepository;

    public void follow(User follower, User following){
        // 중복 팔로우 방지
        if(followRepository.existsByFollowerAndFollowing(follower, following)){
            throw new FollowException(("이미 팔로우한 사용자입니다."));
        }
        Follow follow = new Follow();
        follow.setFollower(follower);
        follow.setFollowing(following);
        followRepository.save(follow);
    }


    public List<User> getFollowers(User user) {
        List<Follow> follows = followRepository.findByFollowingId(user.getId());
        List<User> followers = new ArrayList<>();
        for (Follow follow : follows) {
            followers.add(follow.getFollower());
        }
        return followers;
    }

    public List<User> getFollowing(User user){
        List<Follow> followingList = followRepository.findByFollower(user);
        return followingList.stream().map(Follow::getFollowing).collect(Collectors.toList());
    }

    public User getUserByID(Long userId){
        return userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));


    }
    public int getFollowersCount(User user){
        //존나 헷갈린다 추후에 확인 다시해야함 
        return followRepository.countByFollowingId(user.getId());
    }
    public int getFollowingsCount(User user){
        return followRepository.countByFollowerId(user.getId());
        
    }

    }



