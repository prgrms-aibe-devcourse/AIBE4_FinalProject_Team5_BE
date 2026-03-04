package kr.java.coditor.domain.grade.langset;

import org.springframework.stereotype.Component;

@Component("java")
public class Java implements LanguageStrategy {

	@Override
	public String getFileName() {
		return "Main.java";
	}

	@Override
	public String getDockerImage() {
		return "eclipse-temurin:17-jdk-alpine";
	}

	@Override
	public String getRunCommand() {
		return "javac Main.java && java Main < input.txt";
	}
}
