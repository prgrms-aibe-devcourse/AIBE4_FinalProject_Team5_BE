package kr.java.coditor;

import io.awspring.cloud.autoconfigure.s3.S3AutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication(exclude = {S3AutoConfiguration.class})
public class CoditorApplication {

    public static void main(String[] args) {
        SpringApplication.run(CoditorApplication.class, args);
    }

}
