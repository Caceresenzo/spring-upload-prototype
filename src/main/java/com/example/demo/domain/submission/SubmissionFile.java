package com.example.demo.domain.submission;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

@Entity
@Table(name = "submission_files")
@Data
@Accessors(chain = true)
@FieldNameConstants
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class SubmissionFile {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@ManyToOne(optional = false)
	@ToString.Exclude
	private Submission submission;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	private long size;

	@Column(nullable = false)
	private String mime;

	SubmissionFile(Submission submission, String name, long size, String mime) {
		this.submission = submission;
		this.name = name;
		this.size = size;
		this.mime = mime;
	}

}