package ca.concordia.lambdas;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jgit.revwalk.RevCommit;

import br.ufmg.dcc.labsoft.refactoringanalyzer.dao.Database;
import br.ufmg.dcc.labsoft.refactoringanalyzer.dao.LambdaDBEntity;
import br.ufmg.dcc.labsoft.refactoringanalyzer.dao.LambdaDBEntity.LambdaStatus;
import br.ufmg.dcc.labsoft.refactoringanalyzer.dao.ProjectGit;
import br.ufmg.dcc.labsoft.refactoringanalyzer.dao.RevisionGit;
import gr.uom.java.xmi.decomposition.Lambda;

public class LambdaNewCommitsHandler extends LambdaCommitsHandler {

	public LambdaNewCommitsHandler(Database db, ProjectGit project) {
		super(db, project);
		project.setStatus("analyzing");
		db.update(project);
	}
	
	@Override
	public void handle(RevCommit currentCommit, List<Lambda> lambdasAtRevision, List<String> filesCurrent) {
		RevisionGit revisionGit = RevisionGit.getFromRevCommit(currentCommit, db.getProjectById(project.getId()));
		Set<LambdaDBEntity> lambdas = 
				lambdasAtRevision.stream()
				.map(lambda -> LambdaDBEntity.getFromLambda(lambda, revisionGit, getRealFileContainingLambda(filesCurrent, lambda)))
				.map(lambdaDBEntity -> { 
					lambdaDBEntity.setStatus(LambdaStatus.NEW);
					return lambdaDBEntity;
				})
				.collect(Collectors.toSet());
		revisionGit.setLambdas(lambdas);
		db.insert(revisionGit);
	}
	
	@Override
	public void onFinish(int refactoringsCount, int commitsCount, int errorCommitsCount) {
		project.setLast_update(new Date());
		project.setStatus("analyzed");
		db.update(project);
	}

}
