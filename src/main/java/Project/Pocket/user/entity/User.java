package Project.Pocket.user.entity;


import Project.Pocket.follow.entity.Follow;
import Project.Pocket.user.dto.UserDto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import javax.persistence.*;
import javax.persistence.criteria.Order;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@JsonIgnoreProperties
@Table(name = "USERS", indexes = @Index(name = "idx_users_id", columnList ="id" ))
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User implements Serializable {

    private static final long serialVersionUID = -7073518456700893970L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true)
    private String nickname;
//    @Column(nullable = false)
//    private String address;
    @Column(nullable = false, unique = true)
    private String phoneNumber;
    @Column(nullable = false)
    private String password;
    @Column(nullable = false)
    private String bio;
    @Setter
    private String profileImage;
    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private UserRoleEnum role;






   @Builder
    public User(String email, String nickname, String password, String bio, String profileImage,String phoneNumber, String address, UserRoleEnum role){
        this.email = email;
        this.nickname = nickname;
        this.password = password;
        this.bio = bio;
        this.profileImage = profileImage;
        this.role = role;
        this.phoneNumber = phoneNumber;
//        this.address = address;

    }




    public UserDto toDto() {
        UserDto userDto = new UserDto();
        userDto.setId(this.id);
        userDto.setNickName(this.nickname);
        //프로필 이미지 url 설정
        String profileImageUrl = "/profile-images/" + this.profileImage;
        userDto.setProfileImageUrl(profileImageUrl);
        userDto.setBio(this.bio);
        return userDto;
    }

}
