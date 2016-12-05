package br.ufmg.dcc.labsoft.refactoringanalyzer.dao;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Index;

import gr.uom.java.xmi.decomposition.Lambda;

@Entity
@Table(name = "lambdastable", uniqueConstraints = {
		@UniqueConstraint(columnNames = { "revision", "offset", "length" }) })
public class LambdaDBEntity extends AbstractEntity {

	@Transient
	private static final long serialVersionUID = 4524068566569180688L;

	@ManyToOne
	@JoinColumn(name = "revision")
	@Index(name = "index_lambdastable_revision")
	private RevisionGit revision;
	
	@OneToMany(mappedBy = "lambda", targetEntity = LambdaParametersDBEntity.class, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	private Set<LambdaParametersDBEntity> lambdaParameters;

	private int offset;
	private int length;
	private int startLine;
	private int startColumn;
	private int endLine;
	private int endColumn;
	private int numberOfParameters;
	
	@Column(columnDefinition="text")
	private String body;

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
	
	public Set<LambdaParametersDBEntity> getLambdaParameters() {
		return lambdaParameters;
	}
	
	private void setParameters(Set<LambdaParametersDBEntity> lambdaParameters) {
		this.lambdaParameters = lambdaParameters;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((body == null) ? 0 : body.hashCode());
		result = prime * result + endColumn;
		result = prime * result + endLine;
		result = prime * result + ((lambdaParameters == null) ? 0 : lambdaParameters.hashCode());
		result = prime * result + length;
		result = prime * result + numberOfParameters;
		result = prime * result + offset;
		result = prime * result + ((revision == null) ? 0 : revision.hashCode());
		result = prime * result + startColumn;
		result = prime * result + startLine;
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
		if (endColumn != other.endColumn)
			return false;
		if (endLine != other.endLine)
			return false;
		if (lambdaParameters == null) {
			if (other.lambdaParameters != null)
				return false;
		} else if (!lambdaParameters.equals(other.lambdaParameters))
			return false;
		if (length != other.length)
			return false;
		if (numberOfParameters != other.numberOfParameters)
			return false;
		if (offset != other.offset)
			return false;
		if (revision == null) {
			if (other.revision != null)
				return false;
		} else if (!revision.equals(other.revision))
			return false;
		if (startColumn != other.startColumn)
			return false;
		if (startLine != other.startLine)
			return false;
		return true;
	}

	public static LambdaDBEntity getFromLambda(Lambda lambda, RevisionGit revisionGit) {
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

}
