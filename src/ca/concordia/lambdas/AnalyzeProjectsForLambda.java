package ca.concordia.lambdas;

import java.io.File;

import org.eclipse.jgit.lib.Repository;
import org.refactoringminer.api.GitService;
import org.refactoringminer.util.GitServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.ufmg.dcc.labsoft.refactoringanalyzer.dao.Database;
import br.ufmg.dcc.labsoft.refactoringanalyzer.dao.ProjectGit;
import br.ufmg.dcc.labsoft.refactoringanalyzer.operations.AnalyzeProjects;
import br.ufmg.dcc.labsoft.refactoringanalyzer.operations.Pid;
import br.ufmg.dcc.labsoft.refactoringanalyzer.operations.TaskWithProjectLock;

public class AnalyzeProjectsForLambda extends TaskWithProjectLock {

	private static Logger logger = LoggerFactory.getLogger(AnalyzeProjects.class);
	static Pid pid = new Pid();
	
	public static void main(String[] args) {
		try {
			AnalyzeProjectsForLambda task = new AnalyzeProjectsForLambda(args);
			task.doTask(pid);
		} catch (Exception e) {
			logger.error("Fatal error", e);
		}
	}

	public AnalyzeProjectsForLambda(String[] args) throws Exception {
		super(new Database());
		initWorkingDir(args, pid);
	}

	@Override
	protected void doTask(Database db, Pid pid, ProjectGit project) throws Exception {
		GitService gitService = new GitServiceImpl();
		File projectFile = new File(workingDir, project.getName());
		Repository repo = gitService.cloneIfNotExists(projectFile.getPath(), project.getCloneUrl()/*, project.getDefault_branch()*/);

		LambdaMiner detector = new LambdaMiner();
		detector.detectAll(repo, project.getDefault_branch(), new LambdaCommitsHandler(db, project));
		repo.close();
	}

	@Override
	protected ProjectGit findNextProject(Database db, Pid pid) throws Exception {
		return db.findNonAnalyzedProjectAndLock(pid.toString());
	}

}
