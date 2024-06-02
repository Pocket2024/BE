package Project.Pocket.security.service;

import Project.Pocket.user.entity.User;
import Project.Pocket.user.entity.UserRoleEnum;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;

@Getter
@NoArgsConstructor
public class UserDetailsImpl  implements UserDetails {
    private User user;
    private String email; //id로 이메일 사용

    public UserDetailsImpl(User user, String email){
        this.user = user;
        this.email = email;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        UserRoleEnum role = user.getRole();
        String authority = role.getAuthority();
        SimpleGrantedAuthority simpleGrantedAuthority = new SimpleGrantedAuthority(authority);
        Collection<GrantedAuthority>authorities = new ArrayList<>();
        authorities.add(simpleGrantedAuthority); // 권한을 simpleGrantedAuthority로 추상화하여 관리함
        return authorities;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public User getUser(){

        return user;
    }
    public void setUser(User user){
        this.user = user;
    }
}
