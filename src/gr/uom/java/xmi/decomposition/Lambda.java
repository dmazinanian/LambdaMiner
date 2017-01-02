package gr.uom.java.xmi.decomposition;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

public class Lambda {

	public enum LambdaLocationStatus {
		IN_NEW_CLASS,
		IN_NEW_METHOD,
		IN_EXISTING_METHOD;
	}

	private final String containingFile;
	private final String body;
	private final int offset;
	private final int length;
	private final int lineStart;
	private final int columnStart;
	private final int lineEnd;
	private final int columnEnd;
	private final String functionalInterfaceType;
	private final List<String> parameterTypes;
	private final List<String> parameterNames;
	private LambdaLocationStatus lambdaLocationStatus;

	public Lambda(LambdaExpression node) {
		CompilationUnit compilationUnit = (CompilationUnit)node.getRoot();
		body = node.getBody().toString();
		offset = node.getStartPosition();
		length = node.getLength();
		containingFile = getContainingFile(node);
		lineStart = compilationUnit.getLineNumber(offset);
		columnStart = compilationUnit.getColumnNumber(offset);
		int endOffset = offset + length - 1;
		lineEnd = compilationUnit.getLineNumber(endOffset);
		columnEnd = compilationUnit.getColumnNumber(endOffset);
		ITypeBinding lambdaExpressionBinding = node.resolveTypeBinding();
		if (lambdaExpressionBinding != null) {
			functionalInterfaceType = lambdaExpressionBinding.getErasure().getQualifiedName();
		} else {
			functionalInterfaceType = "";
		}
		parameterTypes = new ArrayList<>();
		parameterNames = new ArrayList<>();
		for (Object parameter : node.parameters()) {
			String parameterTypeBindingKey = "";
			String parameterName = "";
			if (parameter instanceof SingleVariableDeclaration) {
				SingleVariableDeclaration singleVariableDeclaration = (SingleVariableDeclaration) parameter;
				IVariableBinding singleVariableBinding = singleVariableDeclaration.resolveBinding();
				if (singleVariableBinding != null) {
					parameterTypeBindingKey = singleVariableBinding.getType().getErasure().getQualifiedName();
				}
				parameterName = singleVariableDeclaration.getName().getIdentifier();
			} else if (parameter instanceof VariableDeclarationFragment) {
				VariableDeclarationFragment variableDeclarationFragment = (VariableDeclarationFragment) parameter;
				
				IVariableBinding variableDeclarationFragmentBinding = variableDeclarationFragment.resolveBinding();
				if (variableDeclarationFragmentBinding != null) {
					parameterTypeBindingKey = variableDeclarationFragmentBinding.getType().getErasure().getQualifiedName();
				}
				parameterName = variableDeclarationFragment.getName().getIdentifier();
			} else {
				throw new RuntimeException("We shouldn't get to this normally, parameter type is " + parameter.getClass().getName());
			}
			parameterNames.add(parameterName);
			parameterTypes.add(parameterTypeBindingKey);
		}
	}
	
	private String getContainingFile(LambdaExpression node) {
		String containginFilePath = "";
		CompilationUnit compilationUnit = (CompilationUnit)node.getRoot();
		String packageName = compilationUnit.getPackage().getName().getFullyQualifiedName();
		containginFilePath = packageName.replace(".", "/") + "/";
		ASTNode parent = node.getParent();
		AbstractTypeDeclaration candidateParent = null;
		while (parent != null) {
			if (parent instanceof AbstractTypeDeclaration) {
				candidateParent = (AbstractTypeDeclaration)parent;
			}
			parent = parent.getParent();
		}
		if (candidateParent != null) {
			containginFilePath += candidateParent.getName().getFullyQualifiedName() + ".java";
		} else {
			// We couldn't get the enclosing class, the lambda is out of a class? (e.g., in static context?!)
		}
		return containginFilePath;
	}

	public String getContainingFile() {
		return containingFile;
	}

	public String getBody() {
		return body;
	}

	public int getOffset() {
		return offset;
	}

	public int getLength() {
		return length;
	}
	
	public int getLineStart() {
		return lineStart;
	}

	public int getColumnStart() {
		return columnStart;
	}

	public int getLineEnd() {
		return lineEnd;
	}

	public int getColumnEnd() {
		return columnEnd;
	}

	public String getFunctionalInterfaceType() {
		return functionalInterfaceType;
	}
	
	public List<String> getParameterNames() {
		return parameterNames;
	}
	
	public List<String> getParameterTypes() {
		return parameterTypes;
	}
	
	public void setLambdaLocationStatus(LambdaLocationStatus lambdaLocationStatus) {
		this.lambdaLocationStatus = lambdaLocationStatus;
	}
	
	public LambdaLocationStatus getLambdaLocationStatus() {
		return this.lambdaLocationStatus;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("(");
		for (int i = 0; i < parameterNames.size(); i++) {
			String parameterName = parameterNames.get(i);
			String parameterType = parameterTypes.get(i);
			if (!"".equals(parameterType)) {
				builder.append(parameterType).append(" ");
			}
			builder.append(parameterName);
			if (i < parameterNames.size() - 1) {
				builder.append(", ");
			}
		}
		builder.append(") -> ");
		builder.append(body);
		builder.append(" <").append(offset).append(", ").append(length).append(">");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((body == null) ? 0 : body.hashCode());
		result = prime * result + ((functionalInterfaceType == null) ? 0 : functionalInterfaceType.hashCode());
		result = prime * result + length;
		/*result = prime * result + offset;*/
		result = prime * result + ((parameterNames == null) ? 0 : parameterNames.hashCode());
		result = prime * result + ((parameterTypes == null) ? 0 : parameterTypes.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Lambda other = (Lambda) obj;
		if (body == null) {
			if (other.body != null)
				return false;
		} else if (!body.equals(other.body))
			return false;
		if (functionalInterfaceType == null) {
			if (other.functionalInterfaceType != null)
				return false;
		} else if (!functionalInterfaceType.equals(other.functionalInterfaceType))
			return false;
		if (length != other.length)
			return false;
		/*if (offset != other.offset)
			return false;*/
		if (parameterNames == null) {
			if (other.parameterNames != null)
				return false;
		} else if (!parameterNames.equals(other.parameterNames))
			return false;
		if (parameterTypes == null) {
			if (other.parameterTypes != null)
				return false;
		} else if (!parameterTypes.equals(other.parameterTypes))
			return false;
		return true;
	}

}
