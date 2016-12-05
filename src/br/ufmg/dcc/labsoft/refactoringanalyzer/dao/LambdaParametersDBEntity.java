package br.ufmg.dcc.labsoft.refactoringanalyzer.dao;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name = "lambdaparameterstable", uniqueConstraints = {
		@UniqueConstraint(columnNames = { "lambda", "type", "name" }) })
public class LambdaParametersDBEntity extends AbstractEntity {

	@Transient
	private static final long serialVersionUID = -2872793149751149075L;

	@ManyToOne
	@JoinColumn(name = "lambda")
	private LambdaDBEntity lambda;

	private String type;
	private String name;
	
	public LambdaParametersDBEntity() {
	
	}
	
	public LambdaParametersDBEntity(String type, String name, LambdaDBEntity lambda) {
		this.type = type;
		this.name = name;
		this.lambda = lambda;
	}

	public String getType() {
		return type;
	}

	public String getName() {
		return name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		LambdaParametersDBEntity other = (LambdaParametersDBEntity) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}
	
}
