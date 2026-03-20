package kr.java.coditor.global.util.gemini;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GeminiApiService {

	private static final Logger log = LoggerFactory.getLogger(GeminiApiService.class);

	private final WebClient webClient;
	private final ObjectMapper objectMapper;

	@Value("${gemini.api.key}")
	private String apiKey;

	@Value("${gemini.api.url}")
	private String apiUrl;

	public GeminiApiService(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
		this.webClient = webClientBuilder.build();
		this.objectMapper = objectMapper;
	}

	public String getAiReview(String problemTitle, String problemContent, String userCode, String language, String persona) {
		log.info("Gemini API 리뷰 요청 시작... (Problem: {}, Persona: {})", problemTitle, persona);

		// 페르소나 변경을 위해 추가됨. 아무 값도 넘어오지 않았을 경우 기본값 설정
		String actualPersona = (persona != null && !persona.isBlank()) ? persona : "10년 차 시니어 알고리즘 멘토";

		String prompt = String.format(
			"당신은 %s입니다. 사용자가 제출한 코드를 분석해주세요.\n" +
				"이 리뷰는 어두운 터미널(콘솔) 테마의 웹 화면에서 출력되므로 마크다운 가독성이 매우 중요합니다.\n\n" +
				"【문제 제목】: %s\n" +
				"【문제 설명】: %s\n" +
				"【사용자 언어】: %s\n" +
				"【사용자 코드】:\n" +
				"```%s\n%s\n```\n\n" +
				"반드시 아래의 마크다운 템플릿 구조를 엄격하게 지켜서 답변해주세요. 각 섹션 사이에는 반드시 빈 줄을 넣어 여백을 만들어주세요.\n\n" +
				"### ⏱️ 복잡도 분석\n" +
				"- **시간 복잡도**: (예: `O(N)`) 간략한 이유\n" +
				"- **공간 복잡도**: (예: `O(1)`) 간략한 이유\n\n" +
				"### 🔍 코드 품질 및 가독성\n" +
				"- (네이밍, 컨벤션, 구조 등에 대한 피드백을 리스트 형태로 짧게 작성)\n\n" +
				"### 💡 핵심 개선 포인트\n" +
				"- (더 효율적인 접근법이나 버그 발생 가능성을 지적)\n" +
				"- (필요하다면 짧은 개선 예시 코드를 마크다운 코드 블록으로 제공)\n\n" +
				"### 🎯 멘토의 총평\n" +
				"> (핵심 조언을 담은 한 줄 평을 인용구 형식으로 작성. 단, 페르소나의 성격과 말투를 100%% 반영해서 작성할 것!)",
			actualPersona, problemTitle, problemContent, language, language, userCode
		);

		Map<String, Object> body = new HashMap<>();
		body.put("contents", List.of(
			Map.of("parts", List.of(
				Map.of("text", prompt)
			))
		));

		// 기본 thinking기능 때문에 느려질 수 있으므로 비활성화 ( 리뷰 정확도는 검증 필요함 )
		body.put("generationConfig", Map.of(
			"thinkingConfig", Map.of("thinkingBudget", 0),
			"maxOutputTokens", 1024,
			"temperature", 0.3
		));

		try {
			String responseJson = webClient.post()
				.uri(apiUrl + "?key=" + apiKey)
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(body)
				.retrieve()
				.onStatus(HttpStatusCode::isError, response ->
					response.bodyToMono(String.class)
						.flatMap(errorBody -> {
							log.error("Gemini HTTP 에러. status={}, body={}",
								response.statusCode(), errorBody);
							return Mono.error(new RuntimeException("Gemini API error: " + errorBody));
						})
				)
				.bodyToMono(String.class)
				.timeout(Duration.ofSeconds(45)) // 15초 -> 45초
				.block();

			if (responseJson == null || responseJson.isBlank()) {
				log.warn("Gemini 응답이 비어 있습니다. problem={}", problemTitle);
				return "⚠️ AI 리뷰 응답이 비어 있습니다.";
			}

			JsonNode rootNode = objectMapper.readTree(responseJson);
			String text = rootNode.path("candidates")
				.path(0)
				.path("content")
				.path("parts")
				.path(0)
				.path("text")
				.asText();

			if (text == null || text.isBlank()) {
				log.warn("Gemini 응답 파싱 결과가 비어 있습니다. body={}", responseJson);
				return "⚠️ AI 리뷰를 생성했지만 내용이 비어 있습니다.";
			}

			return text;

		} catch (Exception e) {
			log.error("Gemini API 호출 실패! (Problem: {}, url: {})", problemTitle, apiUrl, e);
			return "❌ AI 리뷰 생성 시간이 초과되었거나 호출 중 오류가 발생했습니다.";
		}
	}
}
