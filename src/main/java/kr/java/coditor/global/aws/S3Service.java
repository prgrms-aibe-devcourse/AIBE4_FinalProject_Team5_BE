package kr.java.coditor.global.aws;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import kr.java.coditor.global.exception.S3ErrorCode;
import kr.java.coditor.global.exception.S3Exception;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

	private final S3Client s3Client;

	@Value("${spring.cloud.aws.s3.bucket}")
	private String bucket;

	public String uploadFile(MultipartFile file, String dirName) {
		if (file == null || file.isEmpty()) {
			throw new S3Exception(S3ErrorCode.EMPTY_FILE);
		}

		String originalFileName = file.getOriginalFilename();
		String uniqueFileName = dirName + "/" + UUID.randomUUID() + "_" + originalFileName;

		try {
			PutObjectRequest putObjectRequest = PutObjectRequest.builder()
				.bucket(bucket)
				.key(uniqueFileName)
				.contentType(file.getContentType())
				.build();

			s3Client.putObject(putObjectRequest,
				RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

			String uploadUrl = s3Client.utilities().getUrl(b -> b.bucket(bucket).key(uniqueFileName)).toExternalForm();
			log.info("S3 파일 업로드 성공: {}", uploadUrl);

			return uploadUrl;

		} catch (Exception e) {
			log.error("S3 파일 업로드 중 에러 발생: {}", e.getMessage());
			throw new S3Exception(S3ErrorCode.FILE_UPLOAD_ERROR);
		}
	}

	public void deleteFile(String fileUrl) {
		try {
			String splitStr = ".amazonaws.com/";
			String key = fileUrl.substring(fileUrl.indexOf(splitStr) + splitStr.length());

			DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
				.bucket(bucket)
				.key(key)
				.build();

			s3Client.deleteObject(deleteObjectRequest);
			log.info("S3 파일 삭제 성공: {}", key);

		} catch (Exception e) {
			log.error("S3 파일 삭제 중 에러 발생: {}", e.getMessage());
			throw new S3Exception(S3ErrorCode.FILE_DELETE_ERROR);
		}
	}
}
