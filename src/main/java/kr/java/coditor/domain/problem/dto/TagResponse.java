package kr.java.coditor.domain.problem.dto;

import kr.java.coditor.domain.problem.entity.Tag;

public record TagResponse(
	Long id,
	String name
) {
	public static TagResponse from(Tag tag) {
		return new TagResponse(tag.getId(), tag.getName());
	}
}
