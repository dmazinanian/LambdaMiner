# LambdaMiner

LambdaMiner is a fork for [RefactoringMiner](https://github.com/tsantalis/RefactoringMiner).
While RefactoringMiner is used to detect refactorings in the history of Java projects,
LambdaMiner is used for identifying newly-introduced lambda expressions in Java code.
We used this tool for conducting a study on how Java developers take advantage of 
lambda expressions in their code. 

## Installation

1. Clone or download [**LambdaMiner**](https://github.com/dmazinanian/LambdaMiner){:target='_blank'}.

2. Import **LambdMiner** to Eclipse (it is easily possible to create a standalone
configuration as well, we will provide it later).
For that, first you will need to run the command `gradlew eclipse` in the 
project's folder (`./gradlew eclipse` on posix-compliant systems).
After the downloading of the necessary dependencies is finished,
open Eclipse, and use the `File > Import > General > Existing Projects into Workspace`.
Select the project's folder and click Finish to import **LambdaMiner** into eclipse.

3. **LambdaMiner** needs a database engine to write the extracted data to.
We used MySQL, while you are free to use whatever engine that you like. 
You will need to configure the `src/META_INF/persistence.xml` file accordingly.
You can download a sample `persistence.xml` file from [here](https://www.dropbox.com/s/wqp0gx0j4v0j43m/persistence.xml){:target='_blank'}.
Make sure to provide the correct server address/username/password for the 
database in this file.

4. Make a database named `lambda-study` in your database server
(this name can be changed in the `persistence.xml` if necessary).

5. If you want to automatically fetch the top 1000 projects from GitHub
(ranked by stargazers count) into the database, follow this step.

    **Note**: *You can manually enter the information about the projects of interest
to the database and skip this step.
To this aim, go to step 6 and run the tool once so that the necessary tables
are created into the database.
Then enter the necessary data to the `projectsgit` table*.

    Make a run configuration in Eclipse for running **LambdaMiner**
with the main class `br.ufmg.dcc.labsoft.refactoringanalyzer.operations.GitProjectFinder`.
You should provide your GitHub user name and password as the program input arguments 
(space separated).
Not that, your user name and password will not be sent,
it's just your run configuration for Eclipse saved on your local Eclipse workspace
(you can check the class `GitProjectFinder` to see what we are doing with your
credentials).

    In the database, a table named `projectsgit` will be created after this step. 

6. Make a run configuration in Eclipse for running **LambdaMiner**
and detecting lambdas.
There are two modes for the tool:

    A. Detecting **all the lambda expressions** in the history
    of software systems. 
    To do this, run the tool with the with the main class
    `ca.concordia.lambdas.AnalyzeProjectsForLambda`.
    You should mark the projects that you want to be analyzed in the database
    by setting the value of the `analyzed` column to 0
    and the `status` column to `pending` for those projects.

    B. Detecting **only new lambda expressions** from the last time that the tool
    is run.
    Run the tool with the main class `ca.concordia.lambdas.AnalyzeNewCommitsForLambdas` 
    for this mode.
    You should select which projects you want to be monitored for lambdas,
    by setting the value of the `monitoring_enabled` column to 1 for those projects.

    In both cases, the program should be run with one argument, 
    which is the path to which the repositories will be cloned.

**ACKNOWLEDGEMENT**: *We used the existing data-access logic in **RefactoringMiner**
which was developed by [Danilo Silva](https://github.com/danilofes).*

### LambdaMiner Output

The following tables are created in the database when running **LambdaMiner**:

Table Name | Description
---|---
 `projectgit` | Info about the projects under analysis
 `revisiongit` | Info about each revision of each analyzed project
 `lambdastable` | Info about each lambda expression found in each revision
 `lambdaparameterstable` | Info about each parameter for each lambda expression
 {: .table .table-striped }

We used a dump of the database created after running the tool 
to generate CSV files and fulfill the analysis in R.