package kr.java.coditor.domain.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_profiles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserProfile {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", unique = true, nullable = false)
	private User user;

	@Column(name = "phone_number", length = 20)
	private String phoneNumber;

	@Column(name = "introduce", columnDefinition = "TEXT")
	private String introduce;

	@Column(name = "image_url")
	private String imageUrl;

	@Builder
	public UserProfile(User user, String phoneNumber, String introduce, String imageUrl) {
		this.user = user;
		this.phoneNumber = phoneNumber;
		this.introduce = introduce;
		this.imageUrl = imageUrl;
	}

	public void update(String phoneNumber, String introduce) {
		this.phoneNumber = phoneNumber;
		this.introduce = introduce;
	}
}
