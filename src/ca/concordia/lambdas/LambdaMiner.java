package ca.concordia.lambdas;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.refactoringminer.api.GitService;
import org.refactoringminer.rm1.GitHistoryRefactoringMinerImpl;
import org.refactoringminer.util.GitServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gr.uom.java.xmi.UMLModel;
import gr.uom.java.xmi.UMLModelASTReader;
import gr.uom.java.xmi.decomposition.Lambda;


public class LambdaMiner {

	Logger logger = LoggerFactory.getLogger(GitHistoryRefactoringMinerImpl.class);

//	private static final OutputStream NULL_OUTPUT_STREAM = new OutputStream() {
//		@Override
//		public void write(int b) throws IOException {
//		}
//	};
	
	private void detect(GitService gitService, Repository repository, final LambdaCommitsHandler handler, Iterator<RevCommit> i) {
		int commitsCount = 0;
		int errorCommitsCount = 0;
		int refactoringsCount = 0;

		File metadataFolder = repository.getDirectory();
		File projectFolder = metadataFolder.getParentFile();
		String projectName = projectFolder.getName();

		long time = System.currentTimeMillis();
		while (i.hasNext()) {
			RevCommit currentCommit = i.next();
			try {
				detectLambdas(gitService, repository, handler, projectFolder, currentCommit);
			} catch (Exception e) {
				logger.warn(String.format("Ignored revision %s due to error", currentCommit.getId().getName()), e);
				errorCommitsCount++;
			}

			commitsCount++;
			long time2 = System.currentTimeMillis();
			if ((time2 - time) > 20000) {
				time = time2;
				logger.info(String.format("Processing %s [Commits: %d, Errors: %d, Refactorings: %d]", projectName, commitsCount, errorCommitsCount, refactoringsCount));
			}
		}

		handler.onFinish(refactoringsCount, commitsCount, errorCommitsCount);
		logger.info(String.format("Analyzed %s [Commits: %d, Errors: %d, Refactorings: %d]", projectName, commitsCount, errorCommitsCount, refactoringsCount));
	}

	protected List<Lambda> detectLambdas(GitService gitService, Repository repository, final LambdaCommitsHandler handler, File projectFolder, RevCommit currentCommit) throws Exception {
		List<Lambda> lambdasAtRevision;
		String commitId = currentCommit.getId().getName();
		List<String> filesBefore = new ArrayList<>();
		List<String> filesCurrent = new ArrayList<>();
		Map<String, String> renamedFilesHint = new HashMap<>();
		gitService.fileTreeDiff(repository, currentCommit, filesBefore, filesCurrent, renamedFilesHint, true);
		// If no java files changed, there is no Lambdas added
		if (!filesCurrent.isEmpty()) {
			// Checkout and build model for current commit
			gitService.checkout(repository, commitId);
			UMLModel currentUMLModel = createModel(projectFolder, filesCurrent);

			// Checkout and build model for parent commit
			String parentCommit = currentCommit.getParent(0).getName();
			gitService.checkout(repository, parentCommit);
			UMLModel parentUMLModel = createModel(projectFolder, filesBefore);

			// Diff between currentModel e parentModel
			lambdasAtRevision = parentUMLModel.diff(currentUMLModel, renamedFilesHint).getAddedLambdas();
			/*if (lambdasAtRevision.size() > 0) {
				System.out.println(commitId);
				System.out.println(lambdasAtRevision.toString());
				System.out.println("_________");
			}*/
			
			//lambdasAtRevision = filter(lambdasAtRevision);

		} else {
			logger.info(String.format("Ignored revision %s with no changes in java files", commitId));
			lambdasAtRevision = Collections.emptyList();
		}
		handler.handle(currentCommit, lambdasAtRevision, filesCurrent);
		return lambdasAtRevision;
	}

//	protected List<Refactoring> filter(List<Refactoring> refactoringsAtRevision) {
//		if (this.refactoringTypesToConsider == null) {
//			return refactoringsAtRevision;
//		}
//		List<Refactoring> filteredList = new ArrayList<Refactoring>();
//		for (Refactoring ref : refactoringsAtRevision) {
//			if (this.refactoringTypesToConsider.contains(ref.getRefactoringType())) {
//				filteredList.add(ref);
//			}
//		}
//		return filteredList;
//	}

	public void detectAll(Repository repository, String branch, final LambdaCommitsHandler handler) throws Exception {
		GitService gitService = new GitServiceImpl() {
			@Override
			public boolean isCommitAnalyzed(String sha1) {
				return handler.skipCommit(sha1);
			}
		};
		RevWalk walk = gitService.createAllRevsWalk(repository, branch);
		try {
			detect(gitService, repository, handler, walk.iterator());
		} finally {
			walk.dispose();
		}
	}

	public void fetchAndDetectNew(Repository repository, final LambdaCommitsHandler handler) throws Exception {
		GitService gitService = new GitServiceImpl() {
			@Override
			public boolean isCommitAnalyzed(String sha1) {
				return handler.skipCommit(sha1);
			}
		};
		RevWalk walk = gitService.fetchAndCreateNewRevsWalk(repository);
		try {
			detect(gitService, repository, handler, walk.iterator());
		} finally {
			walk.dispose();
		}
	}

	protected UMLModel createModel(File projectFolder, List<String> files) throws Exception {
		return new UMLModelASTReader(projectFolder, files).getUmlModel();
	}

	public void detectAtCommit(Repository repository, String commitId, LambdaCommitsHandler handler) {
		File metadataFolder = repository.getDirectory();
		File projectFolder = metadataFolder.getParentFile();
		GitService gitService = new GitServiceImpl();
		RevWalk walk = new RevWalk(repository);
		try {
			RevCommit commit = walk.parseCommit(repository.resolve(commitId));
			walk.parseCommit(commit.getParent(0));
			this.detectLambdas(gitService, repository, handler, projectFolder, commit);
		} catch (Exception e) {
			logger.warn(String.format("Ignored revision %s due to error", commitId), e);
			handler.handleException(commitId, e);
		} finally {
			walk.close();
			walk.dispose();
		}
	}

}



