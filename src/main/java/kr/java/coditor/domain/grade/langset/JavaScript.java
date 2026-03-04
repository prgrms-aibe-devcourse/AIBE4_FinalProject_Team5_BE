package kr.java.coditor.domain.grade.langset;

import org.springframework.stereotype.Component;

@Component("javascript")
public class JavaScript implements LanguageStrategy {

	@Override
	public String getFileName() {
		return "main.js";
	}

	@Override
	public String getDockerImage() {
		return "node:18-alpine";
	}

	@Override
	public String getRunCommand() {
		return "node main.js < input.txt";
	}
}
