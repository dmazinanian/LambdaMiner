package ca.concordia.lambdas;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jgit.revwalk.RevCommit;
import org.refactoringminer.api.Churn;

import br.ufmg.dcc.labsoft.refactoringanalyzer.dao.Database;
import br.ufmg.dcc.labsoft.refactoringanalyzer.dao.LambdaDBEntity;
import br.ufmg.dcc.labsoft.refactoringanalyzer.dao.ProjectGit;
import br.ufmg.dcc.labsoft.refactoringanalyzer.dao.RevisionGit;
import gr.uom.java.xmi.decomposition.Lambda;

public class LambdaCommitsHandler {

	protected final Database db;
	protected final ProjectGit project;

	public LambdaCommitsHandler(Database db, ProjectGit project) {
		this.db = db;
		this.project = project;
	}

	public void onFinish(int refactoringsCount, int commitsCount, int errorCommitsCount) {
		project.setAnalyzed(true);
		project.setStatus("analyzed");
		db.update(project);
	}

	public boolean skipCommit(String sha1) {
		// TODO Auto-generated method stub
		return false;
	}

	public void handleException(String commitId, Exception e) {
		// TODO Auto-generated method stub
		
	}

	public void handle(RevCommit currentCommit, List<Lambda> lambdasAtRevision, List<String> filesCurrent, Churn churn) {
		RevisionGit revisionGit = RevisionGit.getFromRevCommit(currentCommit, db.getProjectById(project.getId()));
		Set<LambdaDBEntity> lambdas = 
				lambdasAtRevision.stream()
				.map(lambda -> LambdaDBEntity.getFromLambda(lambda, revisionGit, getRealFileContainingLambda(filesCurrent, lambda)))
				.collect(Collectors.toSet());
		revisionGit.setLambdas(lambdas);
		revisionGit.setLinesAdded(churn.getLinesAdded());
		revisionGit.setLinesRemoved(churn.getLinesRemoved());
		db.insert(revisionGit);
	}

	/*
	 * Lambda keeps track of the file it is defined in.
	 * However, the file path in Lambda#getContainingFile()
	 * only includes the package name, in addition to the name of the
	 * highest-level type, appended with ".java".
	 * In case of nested src folders, etc, we need to map the file path
	 * given from Git to the file path in Lambda.
	 * The below approach is risky though, but should work in most of the cases
	 */
	protected String getRealFileContainingLambda(List<String> filesCurrent, Lambda lambda) {
		for (String path : filesCurrent) {
			if (path.endsWith(lambda.getContainingFile())) {
				return path;
			}
		}
		return lambda.getContainingFile();
	}

}
