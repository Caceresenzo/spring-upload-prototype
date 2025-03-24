package com.example.demo.domain.submission;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

import com.example.demo.domain.user.User;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

@Entity
@Table(name = "submissions")
@Data
@Accessors(chain = true)
@FieldNameConstants
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class Submission {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@ManyToOne(optional = false)
	private User user;

	@Column(nullable = false)
	private String message;

	@CreationTimestamp
	@Column(nullable = false)
	private LocalDateTime createdAt;

	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@OneToMany(mappedBy = SubmissionFile.Fields.submission, cascade = CascadeType.ALL)
	private List<SubmissionFile> files;

	public Submission(
		User user,
		String message
	) {
		this.user = user;
		this.message = message;

		this.files = new ArrayList<>();
	}

	public void addFile(String name, long size, String mime) {
		this.files.add(new SubmissionFile(this, name, size, mime));
	}

}