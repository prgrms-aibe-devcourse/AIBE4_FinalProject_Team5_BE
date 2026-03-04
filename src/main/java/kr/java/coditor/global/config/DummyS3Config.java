package kr.java.coditor.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class DummyS3Config {

	@Bean
	@Primary
	public S3Client dummyS3Client() {
		return S3Client.builder()
			.region(Region.AP_NORTHEAST_2) // 서울 리전 임시 지정
			.credentialsProvider(AnonymousCredentialsProvider.create()) // 인증 없는 가짜 자격 증명
			.build();
	}
}
