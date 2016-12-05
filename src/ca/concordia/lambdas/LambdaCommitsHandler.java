package ca.concordia.lambdas;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jgit.revwalk.RevCommit;

import br.ufmg.dcc.labsoft.refactoringanalyzer.dao.Database;
import br.ufmg.dcc.labsoft.refactoringanalyzer.dao.LambdaDBEntity;
import br.ufmg.dcc.labsoft.refactoringanalyzer.dao.ProjectGit;
import br.ufmg.dcc.labsoft.refactoringanalyzer.dao.RevisionGit;
import gr.uom.java.xmi.decomposition.Lambda;

public class LambdaCommitsHandler {

	private final Database db;
	private final ProjectGit project;

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

	public void handle(RevCommit currentCommit, List<Lambda> lambdasAtRevision) {
		RevisionGit revisionGit = RevisionGit.getFromRevCommit(currentCommit, db.getProjectById(project.getId()));
		Set<LambdaDBEntity> lambdas = 
				lambdasAtRevision.stream()
				.map(lambda -> LambdaDBEntity.getFromLambda(lambda, revisionGit)).collect(Collectors.toSet());
		revisionGit.setLambdas(lambdas);
		db.insert(revisionGit);
	}

}