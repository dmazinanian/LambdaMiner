package gr.uom.java.xmi.decomposition;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.Statement;

public class CompositeStatementObject extends AbstractStatement {

	private List<AbstractStatement> statementList;
	private List<AbstractExpression> expressionList;
	private String type;

	public CompositeStatementObject(Statement statement, int depth, String type) {
		super();
		this.type = type;
		this.setDepth(depth);
		this.statementList = new ArrayList<AbstractStatement>();
		this.expressionList = new ArrayList<AbstractExpression>();
	}

	public void addStatement(AbstractStatement statement) {
		statement.setIndex(statementList.size());
		statementList.add(statement);
		statement.setParent(this);
	}

	public List<AbstractStatement> getStatements() {
		return statementList;
	}

	public void addExpression(AbstractExpression expression) {
		//an expression has the same index and depth as the composite statement it belong to
		expression.setDepth(this.getDepth());
		expression.setIndex(this.getIndex());
		expressionList.add(expression);
		expression.setOwner(this);
	}

	public List<AbstractExpression> getExpressions() {
		return expressionList;
	}

	@Override
	public List<StatementObject> getLeaves() {
		List<StatementObject> leaves = new ArrayList<StatementObject>();
		for(AbstractStatement statement : statementList) {
			leaves.addAll(statement.getLeaves());
		}
		return leaves;
	}

	public List<CompositeStatementObject> getInnerNodes() {
		List<CompositeStatementObject> innerNodes = new ArrayList<CompositeStatementObject>();
		for(AbstractStatement statement : statementList) {
			if(statement instanceof CompositeStatementObject) {
				CompositeStatementObject composite = (CompositeStatementObject)statement;
				innerNodes.addAll(composite.getInnerNodes());
			}
		}
		innerNodes.add(this);
		return innerNodes;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(type);
		if(expressionList.size() > 0) {
			sb.append("(");
			for(AbstractExpression expression : expressionList)
				sb.append(expression.toString());
			sb.append(")");
		}
		return sb.toString();
	}

	@Override
	public List<String> getVariables() {
		List<String> variables = new ArrayList<String>();
		for(AbstractExpression expression : expressionList) {
			variables.addAll(expression.getVariables());
		}
		return variables;
	}

	@Override
	public List<VariableDeclaration> getVariableDeclarations() {
		List<VariableDeclaration> variableDeclarations = new ArrayList<VariableDeclaration>();
		for(AbstractExpression expression : expressionList) {
			variableDeclarations.addAll(expression.getVariableDeclarations());
		}
		return variableDeclarations;
	}

	@Override
	public Map<String, OperationInvocation> getMethodInvocationMap() {
		Map<String, OperationInvocation> map = new LinkedHashMap<String, OperationInvocation>();
		for(AbstractExpression expression : expressionList) {
			map.putAll(expression.getMethodInvocationMap());
		}
		return map;
	}

	public Map<String, OperationInvocation> getAllMethodInvocations() {
		Map<String, OperationInvocation> map = new LinkedHashMap<String, OperationInvocation>();
		map.putAll(getMethodInvocationMap());
		for(AbstractStatement statement : statementList) {
			if(statement instanceof CompositeStatementObject) {
				CompositeStatementObject composite = (CompositeStatementObject)statement;
				map.putAll(composite.getAllMethodInvocations());
			}
			else if(statement instanceof StatementObject) {
				StatementObject statementObject = (StatementObject)statement;
				map.putAll(statementObject.getMethodInvocationMap());
			}
		}
		return map;
	}

	public List<Lambda> getLambdas() {
		List<Lambda> lambdas = new ArrayList<>();
		for (AbstractExpression abstractExpression : expressionList) {
			lambdas.addAll(abstractExpression.getLambdas());
		}
		for (AbstractStatement abstractStatement : statementList) {
			lambdas.addAll(abstractStatement.getLambdas());
		}
		return lambdas;
	}
	
	
}
