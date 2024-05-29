package Project.Pocket.security.service;

import Project.Pocket.security.exception.CustomException;
import Project.Pocket.security.exception.ExceptionStatus;
import Project.Pocket.user.entity.User;
import Project.Pocket.user.entity.UserRepository;
import Project.Pocket.user.entity.UserRoleEnum;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException{
        User user = findByEmail(email);
        return new UserDetailsImpl(user, user.getEmail());
    }

    private User findByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(
                ()->new CustomException(ExceptionStatus.WRONG_EMAIL)
        );
    }





}
