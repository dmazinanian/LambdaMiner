package ca.concordia.lambdas;

import java.io.File;
import java.util.Date;

import org.eclipse.jgit.lib.Repository;
import org.refactoringminer.api.GitService;
import org.refactoringminer.util.GitServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.ufmg.dcc.labsoft.refactoringanalyzer.dao.Database;
import br.ufmg.dcc.labsoft.refactoringanalyzer.dao.ProjectGit;
import br.ufmg.dcc.labsoft.refactoringanalyzer.operations.Pid;
import br.ufmg.dcc.labsoft.refactoringanalyzer.operations.TaskWithProjectLock;

public class AnalyzeNewCommitsForLambdas extends TaskWithProjectLock {

	private static Logger logger = LoggerFactory.getLogger(AnalyzeNewCommitsForLambdas.class);
	static Pid pid = new Pid();
	private Date startTime = new Date();

	public static void main(String[] args) {
		try {
			AnalyzeNewCommitsForLambdas task = new AnalyzeNewCommitsForLambdas(args);
			task.doTask(pid);
		} catch (Exception e) {
			logger.error("Fatal error", e);
		}
	}

	public AnalyzeNewCommitsForLambdas(String[] args) throws Exception {
		super(new Database());
		initWorkingDir(args, pid);
	}

	@Override
	protected ProjectGit findNextProject(Database db, Pid pid) throws Exception {
		return db.findProjectToMonitorAndLock(pid.toString(), startTime);
	}

	@Override
	protected void doTask(Database db, Pid pid, ProjectGit project) throws Exception {
		final Database db1 = db;
		GitService gitService = new GitServiceImpl();
		File projectFile = new File(this.workingDir, project.getName());
		Repository repo = gitService.cloneIfNotExists(projectFile.getPath(), project.getCloneUrl());

		LambdaMiner detector = new LambdaMiner();
		detector.fetchAndDetectNew(repo, new LambdaCommitsHandler(db1, project));
		repo.close();
	}
}
