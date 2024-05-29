package Project.Pocket.user.entity;

import lombok.Getter;

@Getter
public enum UserRoleEnum {
    ADMIN(Authority.ADMIN),
    MEMBER(Authority.MEMBER),
    GUEST(Authority.GUEST);


    private final String authority;

    UserRoleEnum(String authority) {
        this.authority = authority;
    }

    public static class Authority {
        public static final String ADMIN = "ROLE_ADMIN";
        public static final String GUEST = "ROLE_GUEST";
        public static final String MEMBER = "ROLE_MEMBER";


    }
}