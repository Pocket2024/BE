package Project.Pocket.user.entity;


import Project.Pocket.follow.entity.Follow;
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
//@Setter
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
//   //팔로우
//    @OneToMany(mappedBy = "follower", fetch = FetchType.LAZY)
//    private List<Follow> followers;
//    @OneToMany(mappedBy = "following", fetch = FetchType.LAZY)
//    private List<Follow> following;





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


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

//    public String getAddress() {
//        return address;
//    }
//
//    public void setAddress(String address) {
//        this.address = address;
//    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public UserRoleEnum getRole() {
        return role;
    }

    public void setRole(UserRoleEnum role) {
        this.role = role;
    }


}
