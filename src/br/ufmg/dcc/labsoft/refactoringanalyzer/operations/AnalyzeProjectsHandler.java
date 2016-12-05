package br.ufmg.dcc.labsoft.refactoringanalyzer.operations;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jgit.revwalk.RevCommit;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringHandler;

import br.ufmg.dcc.labsoft.refactoringanalyzer.dao.Database;
import br.ufmg.dcc.labsoft.refactoringanalyzer.dao.ProjectGit;
import br.ufmg.dcc.labsoft.refactoringanalyzer.dao.RefactoringGit;
import br.ufmg.dcc.labsoft.refactoringanalyzer.dao.RevisionGit;

class AnalyzeProjectsHandler extends RefactoringHandler {
	protected final Database db;
	protected final ProjectGit project;

	AnalyzeProjectsHandler(Database db, ProjectGit project) {
		this.db = db;
		this.project = project;
	}

	@Override
	public boolean skipCommit(String curRevision) {
		return db.getRevisionById(project, curRevision) != null;
	}

	@Override
	public void handle(RevCommit curRevision, List<Refactoring> refactorings) {
		RevisionGit revision = RevisionGit.getFromRevCommit(curRevision, db.getProjectById(project.getId()));

		Set<RefactoringGit> refactoringSet = new HashSet<RefactoringGit>();
		for (Refactoring refactoring : refactorings) {
			RefactoringGit refact = new RefactoringGit();
			refact.setRefactoringType(refactoring.getName());
			refact.setDescription(refactoring.toString());
			refact.setRevision(revision);
			refactoringSet.add(refact);
		}
		revision.setRefactorings(refactoringSet);
		db.insert(revision);
	}

	@Override
	public void onFinish(int refactoringsCount, int commitsCount, int errorCommitsCount) {
		project.setAnalyzed(true);
		project.setStatus("analyzed");
		project.setMachine(AnalyzeProjects.pid.getMachine());
		if (project.getCommits_count() <= 0) {
			project.setCommits_count(commitsCount);
		}
		project.setError_commits_count(errorCommitsCount);
		db.update(project);
	}
}