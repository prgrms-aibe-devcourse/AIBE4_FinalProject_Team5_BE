package kr.java.coditor.domain.grade.langset;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class LanguageStrategyFactory {

	private final Map<String, LanguageStrategy> strategies = new HashMap<>();

	// Key를 모두 소문자로 변환해서 다시 저장 ("java", "javascript", "python")
	public LanguageStrategyFactory(Map<String, LanguageStrategy> injectedStrategies) {
		for (Map.Entry<String, LanguageStrategy> entry : injectedStrategies.entrySet()) {
			strategies.put(entry.getKey().toLowerCase(), entry.getValue());
		}
	}

	public LanguageStrategy findStrategy(String language) {
		// 3. 프론트엔드에서 보낸 언어 문자열("Java", "javascript" 등)을 소문자로 변환해서 바로 맵에서 꺼냄
		String key = language.toLowerCase();
		LanguageStrategy strategy = strategies.get(key);

		if (strategy == null) {
			throw new IllegalArgumentException("지원하지 않는 언어입니다: " + language);
		}

		return strategy;
	}
}
