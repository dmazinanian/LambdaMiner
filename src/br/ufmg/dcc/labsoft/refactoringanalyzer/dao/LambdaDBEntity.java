package br.ufmg.dcc.labsoft.refactoringanalyzer.dao;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Index;

import gr.uom.java.xmi.decomposition.Lambda;
import gr.uom.java.xmi.decomposition.Lambda.LambdaLocationStatus;

@Entity
@Table(name = "lambdastable", uniqueConstraints = {
		@UniqueConstraint(columnNames = { "revision", "offset", "length" }) })
public class LambdaDBEntity extends AbstractEntity {

	public enum LambdaStatus {
		NEW,
		SKIPPED,
		MAIL_SENT,
		DONE
	}

	@Transient
	private static final long serialVersionUID = 4524068566569180688L;

	@ManyToOne
	@JoinColumn(name = "revision")
	@Index(name = "index_lambdastable_revision")
	private RevisionGit revision;
	
	@OneToMany(mappedBy = "lambda", targetEntity = LambdaTagsDBEntity.class, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	private Set<LambdaTagsDBEntity> usertags;
	
	@OneToMany(mappedBy = "lambda", targetEntity = LambdaParametersDBEntity.class, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	private Set<LambdaParametersDBEntity> lambdaParameters;
	
	@OneToMany(targetEntity = SurveyMail.class, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	private Set<SurveyMail> surveyEmails;

	private int offset;
	private int length;
	private int startLine;
	private int startColumn;
	private int endLine;
	private int endColumn;
	private int numberOfParameters;
	
	@Column(columnDefinition="text")
	private String body;
	
	@Column(columnDefinition="text")
	private String filePath;

	@Column(columnDefinition="text")
	private String functionalInterfaceType;
	
	@Enumerated(EnumType.STRING)
	private LambdaStatus status;
	
	@Enumerated(EnumType.STRING)
	private LambdaLocationStatus lambdaLocationStatus;

	@Override
	public Long getId() {
		return this.id;
	}
	
	public RevisionGit getRevision() {
		return revision;
	}

	public int getOffset() {
		return offset;
	}

	public int getLength() {
		return length;
	}

	public int getStartLine() {
		return startLine;
	}

	public int getStartColumn() {
		return startColumn;
	}

	public int getEndLine() {
		return endLine;
	}

	public int getEndColumn() {
		return endColumn;
	}

	public int getNumberOfParameters() {
		return numberOfParameters;
	}

	public String getBody() {
		return body;
	}
	
	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public Set<LambdaParametersDBEntity> getLambdaParameters() {
		return lambdaParameters;
	}
	
	private void setParameters(Set<LambdaParametersDBEntity> lambdaParameters) {
		this.lambdaParameters = lambdaParameters;
	}
	
	public String getFunctionalInterfaceType() {
		return this.functionalInterfaceType;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((body == null) ? 0 : body.hashCode());
		result = prime * result + ((filePath == null) ? 0 : filePath.hashCode());
		result = prime * result + ((functionalInterfaceType == null) ? 0 : functionalInterfaceType.hashCode());
		result = prime * result + ((lambdaParameters == null) ? 0 : lambdaParameters.hashCode());
		result = prime * result + length;
		result = prime * result + offset;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LambdaDBEntity other = (LambdaDBEntity) obj;
		if (body == null) {
			if (other.body != null)
				return false;
		} else if (!body.equals(other.body))
			return false;
		if (filePath == null) {
			if (other.filePath != null)
				return false;
		} else if (!filePath.equals(other.filePath))
			return false;
		if (functionalInterfaceType == null) {
			if (other.functionalInterfaceType != null)
				return false;
		} else if (!functionalInterfaceType.equals(other.functionalInterfaceType))
			return false;
		if (lambdaParameters == null) {
			if (other.lambdaParameters != null)
				return false;
		} else if (!lambdaParameters.equals(other.lambdaParameters))
			return false;
		if (length != other.length)
			return false;
		if (offset != other.offset)
			return false;
		return true;
	}

	public static LambdaDBEntity getFromLambda(Lambda lambda, RevisionGit revisionGit, String fileContainingLambda) {
		LambdaDBEntity lambdaDBEntity = new LambdaDBEntity();
		lambdaDBEntity.revision = revisionGit;
		lambdaDBEntity.offset = lambda.getOffset();
		lambdaDBEntity.length = lambda.getLength();
		lambdaDBEntity.startLine = lambda.getLineStart();
		lambdaDBEntity.startColumn = lambda.getColumnStart();
		lambdaDBEntity.endLine = lambda.getLineEnd();
		lambdaDBEntity.endColumn = lambda.getColumnEnd();
		lambdaDBEntity.numberOfParameters = lambda.getParameterNames().size();
		lambdaDBEntity.body = lambda.getBody();
		lambdaDBEntity.filePath = lambda.getContainingFile();
		lambdaDBEntity.filePath = fileContainingLambda;
		lambdaDBEntity.functionalInterfaceType = lambda.getFunctionalInterfaceType();
		lambdaDBEntity.lambdaLocationStatus = lambda.getLambdaLocationStatus();
		Set<LambdaParametersDBEntity> lambdaParameters = new HashSet<>();
		for (int i = 0; i < lambda.getParameterNames().size(); i++) {
			String type = lambda.getParameterTypes().get(i);
			String name = lambda.getParameterNames().get(i);
			LambdaParametersDBEntity parameterDBEntity = new LambdaParametersDBEntity(type, name, lambdaDBEntity);
			lambdaParameters.add(parameterDBEntity);
		}
		lambdaDBEntity.setParameters(lambdaParameters);
		return lambdaDBEntity;
	}

	public void setStatus(LambdaStatus status) {
		this.status = status;
	}

	public LambdaLocationStatus getLambdaLocationStatus() {
		return lambdaLocationStatus;
	}

	public void setLambdaLocationStatus(LambdaLocationStatus lambdaLocationStatus) {
		this.lambdaLocationStatus = lambdaLocationStatus;
	}

}
