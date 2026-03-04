package kr.java.coditor.domain.grade.langset;

import org.springframework.stereotype.Component;

@Component("python")
public class Python implements LanguageStrategy {

	@Override
	public String getFileName() {
		return "main.py";
	}

	@Override
	public String getDockerImage() {
		return "python:3.10-alpine";
	}

	@Override
	public String getRunCommand() {
		return "python main.py < input.txt";
	}
}
