package Project.Pocket.user.entity;


import Project.Pocket.Review.entity.Review;
import Project.Pocket.follow.entity.Follow;
import Project.Pocket.user.dto.UserDto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import javax.persistence.*;
import javax.persistence.criteria.Order;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    @Column(nullable = false, unique = true)
    private String phoneNumber;
    @Column(nullable = false)
    private String password;
    @Column(nullable = false)
    private String bio;
    @Column
    private String profileImage;
    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private UserRoleEnum role;
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Review> reviews = new HashSet<>();
    @Column(nullable = false)
    private boolean isPrivate;


    @Builder
    public User(String email, String nickname, String password, String bio, String profileImage, String phoneNumber, UserRoleEnum role, boolean isPrivate) {
        this.email = email;
        this.nickname = nickname;
        this.password = password;
        this.bio = bio;
        this.profileImage = profileImage;
        this.role = role;
        this.phoneNumber = phoneNumber;
        this.isPrivate = isPrivate;


    }


    public UserDto toDto() {
        UserDto userDto = new UserDto();
        userDto.setId(this.id);
        userDto.setNickName(this.nickname);
        userDto.setProfileImageUrl(this.profileImage);
        userDto.setBio(this.bio);
        userDto.setEmail(this.email);
        userDto.setPhoneNumber(this.phoneNumber);
        userDto.setPrivate(isPrivate);
        return userDto;

    }
}
