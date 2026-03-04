package kr.java.coditor.domain.grade.langset;

public interface LanguageStrategy {

	String getFileName();
	String getDockerImage();
	String getRunCommand();
}
