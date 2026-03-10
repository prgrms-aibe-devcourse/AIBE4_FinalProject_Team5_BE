package kr.java.coditor.domain.user.dto;

import kr.java.coditor.domain.user.entity.Provider;
import kr.java.coditor.domain.user.entity.User;
import kr.java.coditor.domain.user.entity.UserProfile;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserResponseDto {
    private Long id;
    private String email;
    private String nickname;
    private String phoneNumber;
    private String introduce;
    private String imageUrl;
    private Provider provider;

    public UserResponseDto(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.nickname = user.getNickname();
        this.provider = user.getProvider();

        UserProfile userProfile = user.getUserProfile();
        if (userProfile != null) {
            this.phoneNumber = userProfile.getPhoneNumber();
            this.introduce = userProfile.getIntroduce();
            this.imageUrl = userProfile.getImageUrl();
        }
    }
}
