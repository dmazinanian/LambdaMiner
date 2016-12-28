package br.ufmg.dcc.labsoft.refactoringanalyzer.dao;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Index;

@Entity
@Table(name = "lambda_tags", uniqueConstraints = {
		@UniqueConstraint(columnNames = { "lambda", "user", "tag" }) })
public class LambdaTagsDBEntity extends AbstractEntity {

	private static final long serialVersionUID = 2789968607998931526L;
	
	@ManyToOne(cascade = CascadeType.PERSIST) 
	@JoinColumn(name = "lambda")
	@Index(name="index_lambdatag_lambda")
	private LambdaDBEntity lambda;

	@ManyToOne(cascade = CascadeType.PERSIST) 
	@JoinColumn(name="user")
	@Index(name="index_lambdatag_user")
	private User user;

	@ManyToOne(cascade = CascadeType.PERSIST) 
	@Index(name="index_lambdatag_tag")
	@JoinColumn(name="tag")
	private Tag tag;
	
	public LambdaDBEntity getLambda() {
		return lambda;
	}
	
	public User getUser() {
		return user;
	}
	
	public Tag getTag() {
		return tag;
	}

}
