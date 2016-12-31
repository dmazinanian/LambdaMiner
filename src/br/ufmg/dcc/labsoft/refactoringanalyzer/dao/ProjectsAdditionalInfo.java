package br.ufmg.dcc.labsoft.refactoringanalyzer.dao;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Index;

@Entity
@Table(name = "projectsadditionalinfo")
public class ProjectsAdditionalInfo extends AbstractEntity {

	private static final long serialVersionUID = -5254386622041377955L;
	
	@ManyToOne(cascade = CascadeType.PERSIST) 
	@JoinColumn(name = "project")
	@Index(name="index_project_additional_info")
	private ProjectGit project;
	
	private double lambdaDensityPerClass;
	private double lambdaDensityPerMethod;
	private double numberOfLambdasInHead;
	
	public ProjectGit getProject() {
		return project;
	}
	public double getLambdaDensityPerClass() {
		return lambdaDensityPerClass;
	}
	public double getLambdaDensityPerMethod() {
		return lambdaDensityPerMethod;
	}
	public double getNumberOfLambdasInHead() {
		return numberOfLambdasInHead;
	}
	
}
