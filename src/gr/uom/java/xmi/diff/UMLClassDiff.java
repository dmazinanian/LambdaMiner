package gr.uom.java.xmi.diff;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.refactoringminer.api.Refactoring;

import gr.uom.java.xmi.UMLAttribute;
import gr.uom.java.xmi.UMLClass;
import gr.uom.java.xmi.UMLOperation;
import gr.uom.java.xmi.UMLParameter;
import gr.uom.java.xmi.UMLType;
import gr.uom.java.xmi.decomposition.Lambda;
import gr.uom.java.xmi.decomposition.Lambda.LambdaLocationStatus;
import gr.uom.java.xmi.decomposition.OperationInvocation;
import gr.uom.java.xmi.decomposition.UMLOperationBodyMapper;

public class UMLClassDiff implements Comparable<UMLClassDiff> {
	private UMLClass originalClass;
	private String className;
	private List<UMLOperation> addedOperations;
	private List<UMLOperation> removedOperations;
	private List<UMLAttribute> addedAttributes;
	private List<UMLAttribute> removedAttributes;
	private List<UMLAttributeDiff> attributeDiffList;
	private List<UMLOperationDiff> operationDiffList;
	private List<UMLOperationBodyMapper> operationBodyMapperList;
	private Map<UMLOperation, OperationInvocation> extractedDelegateOperations;
	private List<Refactoring> refactorings;
	private boolean visibilityChanged;
	private String oldVisibility;
	private String newVisibility;
	private boolean abstractionChanged;
	private boolean oldAbstraction;
	private boolean newAbstraction;
	private boolean superclassChanged;
	private UMLType oldSuperclass;
	private UMLType newSuperclass;
	private List<Lambda> addedLambdas;
	
	public UMLClassDiff(UMLClass originalClass) {
		this.originalClass = originalClass;
		this.className = originalClass.getName();
		this.addedOperations = new ArrayList<UMLOperation>();
		this.removedOperations = new ArrayList<UMLOperation>();
		this.addedAttributes = new ArrayList<UMLAttribute>();
		this.removedAttributes = new ArrayList<UMLAttribute>();
		this.attributeDiffList = new ArrayList<UMLAttributeDiff>();
		this.operationDiffList = new ArrayList<UMLOperationDiff>();
		this.operationBodyMapperList = new ArrayList<UMLOperationBodyMapper>();
		this.extractedDelegateOperations = new LinkedHashMap<UMLOperation, OperationInvocation>();
		this.refactorings = new ArrayList<Refactoring>();
		this.visibilityChanged = false;
		this.abstractionChanged = false;
		this.superclassChanged = false;
		this.addedLambdas = new ArrayList<>();
	}

	public String getClassName() {
		return className;
	}

	public void reportAddedOperation(UMLOperation umlOperation) {
		this.addedOperations.add(umlOperation);
	}

	public void reportRemovedOperation(UMLOperation umlOperation) {
		this.removedOperations.add(umlOperation);
	}

	public void reportAddedAttribute(UMLAttribute umlAttribute) {
		this.addedAttributes.add(umlAttribute);
	}

	public void reportRemovedAttribute(UMLAttribute umlAttribute) {
		this.removedAttributes.add(umlAttribute);
	}

	public void addOperationBodyMapper(UMLOperationBodyMapper operationBodyMapper) {
		this.operationBodyMapperList.add(operationBodyMapper);
	}

	public void setVisibilityChanged(boolean visibilityChanged) {
		this.visibilityChanged = visibilityChanged;
	}

	public void setOldVisibility(String oldVisibility) {
		this.oldVisibility = oldVisibility;
	}

	public void setNewVisibility(String newVisibility) {
		this.newVisibility = newVisibility;
	}

	public void setAbstractionChanged(boolean abstractionChanged) {
		this.abstractionChanged = abstractionChanged;
	}

	public void setOldAbstraction(boolean oldAbstraction) {
		this.oldAbstraction = oldAbstraction;
	}

	public void setNewAbstraction(boolean newAbstraction) {
		this.newAbstraction = newAbstraction;
	}

	public void setSuperclassChanged(boolean superclassChanged) {
		this.superclassChanged = superclassChanged;
	}

	public void setOldSuperclass(UMLType oldSuperclass) {
		this.oldSuperclass = oldSuperclass;
	}

	public void setNewSuperclass(UMLType newSuperclass) {
		this.newSuperclass = newSuperclass;
	}

	public UMLType getSuperclass() {
		if(!superclassChanged && oldSuperclass != null && newSuperclass != null)
			return oldSuperclass;
		return null;
	}

	public boolean containsOperationWithTheSameSignature(UMLOperation operation) {
		for(UMLOperation originalOperation : originalClass.getOperations()) {
			if(originalOperation.equalSignature(operation))
				return true;
		}
		return false;
	}

	public boolean isEmpty() {
		return addedOperations.isEmpty() && removedOperations.isEmpty() &&
			addedAttributes.isEmpty() && removedAttributes.isEmpty() &&
			operationDiffList.isEmpty() && attributeDiffList.isEmpty() &&
			operationBodyMapperList.isEmpty() &&
			!visibilityChanged && !abstractionChanged;
	}

	public List<UMLOperation> getAddedOperations() {
		return addedOperations;
	}

	public List<UMLOperation> getRemovedOperations() {
		return removedOperations;
	}

	public List<UMLAttribute> getAddedAttributes() {
		return addedAttributes;
	}

	public List<UMLAttribute> getRemovedAttributes() {
		return removedAttributes;
	}

	public List<UMLOperationBodyMapper> getOperationBodyMapperList() {
		return operationBodyMapperList;
	}

	public Map<UMLOperation, OperationInvocation> getExtractedDelegateOperations() {
		return extractedDelegateOperations;
	}

	public List<Refactoring> getRefactorings() {
		return refactorings;
	}

	public void checkForAttributeChanges() {
		for(Iterator<UMLAttribute> removedAttributeIterator = removedAttributes.iterator(); removedAttributeIterator.hasNext();) {
			UMLAttribute removedAttribute = removedAttributeIterator.next();
			for(Iterator<UMLAttribute> addedAttributeIterator = addedAttributes.iterator(); addedAttributeIterator.hasNext();) {
				UMLAttribute addedAttribute = addedAttributeIterator.next();
				if(removedAttribute.getName().equals(addedAttribute.getName())) {
					UMLAttributeDiff attributeDiff = new UMLAttributeDiff(removedAttribute, addedAttribute);
					addedAttributeIterator.remove();
					removedAttributeIterator.remove();
					attributeDiffList.add(attributeDiff);
					break;
				}
			}
		}
	}
	
	public void checkForOperationSignatureChanges() {
		if(removedOperations.size() <= addedOperations.size()) {
			for(Iterator<UMLOperation> removedOperationIterator = removedOperations.iterator(); removedOperationIterator.hasNext();) {
				UMLOperation removedOperation = removedOperationIterator.next();
				TreeSet<UMLOperationBodyMapper> mapperSet = new TreeSet<UMLOperationBodyMapper>();
				for(Iterator<UMLOperation> addedOperationIterator = addedOperations.iterator(); addedOperationIterator.hasNext();) {
					UMLOperation addedOperation = addedOperationIterator.next();
					UMLOperationBodyMapper operationBodyMapper = new UMLOperationBodyMapper(removedOperation, addedOperation);
					if(!operationBodyMapper.getMappings().isEmpty() &&
							operationBodyMapper.getMappings().size() > operationBodyMapper.nonMappedElementsT1()) {
						mapperSet.add(operationBodyMapper);
					}
				}
				if(!mapperSet.isEmpty()) {
					UMLOperationBodyMapper firstMapper = mapperSet.first();
					UMLOperation addedOperation = firstMapper.getOperation2();
					addedOperations.remove(addedOperation);
					removedOperationIterator.remove();

					UMLOperationDiff operationDiff = new UMLOperationDiff(removedOperation, addedOperation);
					operationDiffList.add(operationDiff);
					if(!removedOperation.getName().equals(addedOperation.getName()) && addedOperation.equalParameters(removedOperation)) {
						RenameOperationRefactoring rename = new RenameOperationRefactoring(removedOperation, addedOperation);
						refactorings.add(rename);
					}
					this.addOperationBodyMapper(firstMapper);
				}
			}
		}
		else {
			for(Iterator<UMLOperation> addedOperationIterator = addedOperations.iterator(); addedOperationIterator.hasNext();) {
				UMLOperation addedOperation = addedOperationIterator.next();
				TreeSet<UMLOperationBodyMapper> mapperSet = new TreeSet<UMLOperationBodyMapper>();
				for(Iterator<UMLOperation> removedOperationIterator = removedOperations.iterator(); removedOperationIterator.hasNext();) {
					UMLOperation removedOperation = removedOperationIterator.next();
					UMLOperationBodyMapper operationBodyMapper = new UMLOperationBodyMapper(removedOperation, addedOperation);
					if(!operationBodyMapper.getMappings().isEmpty() &&
							operationBodyMapper.getMappings().size() > operationBodyMapper.nonMappedElementsT1()) {
						mapperSet.add(operationBodyMapper);
					}
				}
				if(!mapperSet.isEmpty()) {
					UMLOperationBodyMapper firstMapper = mapperSet.first();
					UMLOperation removedOperation = firstMapper.getOperation1();
					removedOperations.remove(removedOperation);
					addedOperationIterator.remove();

					UMLOperationDiff operationDiff = new UMLOperationDiff(removedOperation, addedOperation);
					operationDiffList.add(operationDiff);
					if(!removedOperation.getName().equals(addedOperation.getName()) && addedOperation.equalParameters(removedOperation)) {
						RenameOperationRefactoring rename = new RenameOperationRefactoring(removedOperation, addedOperation);
						refactorings.add(rename);
					}
					this.addOperationBodyMapper(firstMapper);
				}
			}
		}
	}
	
	public void checkForInlinedOperations() {
		List<UMLOperation> operationsToBeRemoved = new ArrayList<UMLOperation>();
		List<UMLOperationBodyMapper> mappersToBeAdded = new ArrayList<UMLOperationBodyMapper>();
		for(Iterator<UMLOperation> removedOperationIterator = removedOperations.iterator(); removedOperationIterator.hasNext();) {
			UMLOperation removedOperation = removedOperationIterator.next();
			for(UMLOperationBodyMapper mapper : getOperationBodyMapperList()) {
				if(!mapper.getNonMappedLeavesT2().isEmpty() || !mapper.getNonMappedInnerNodesT2().isEmpty() ||
					!mapper.getVariableReplacementsWithMethodInvocation().isEmpty() || !mapper.getMethodInvocationReplacements().isEmpty()) {
					Set<OperationInvocation> operationInvocations = mapper.getOperation1().getBody().getAllOperationInvocations();
					OperationInvocation removedOperationInvocation = null;
					for(OperationInvocation invocation : operationInvocations) {
						if(invocation.matchesOperation(removedOperation)) {
							removedOperationInvocation = invocation;
							break;
						}
					}
					if(removedOperationInvocation != null) {
						List<String> arguments = removedOperationInvocation.getArguments();
						List<String> parameters = removedOperation.getParameterNameList();
						Map<String, String> parameterToArgumentMap = new LinkedHashMap<String, String>();
						for(int i=0; i<parameters.size(); i++) {
							parameterToArgumentMap.put(parameters.get(i), arguments.get(i));
						}
						UMLOperationBodyMapper operationBodyMapper = new UMLOperationBodyMapper(removedOperation, mapper, parameterToArgumentMap);
						if(!operationBodyMapper.getMappings().isEmpty() &&
								(operationBodyMapper.getMappings().size() > operationBodyMapper.nonMappedElementsT1()
										|| operationBodyMapper.exactMatches() > 0) ) {
							if(operationBodyMapper.nonMappedElementsT1() > 0) {
								mappersToBeAdded.add(operationBodyMapper);
							}
							InlineOperationRefactoring inlineOperationRefactoring =
									new InlineOperationRefactoring(removedOperation, operationBodyMapper.getOperation2(), operationBodyMapper.getOperation2().getClassName());
							refactorings.add(inlineOperationRefactoring);
							operationsToBeRemoved.add(removedOperation);
						}
					}
				}
			}
		}
		removedOperations.removeAll(operationsToBeRemoved);
	}
	
	public void checkForExtractedOperations() {
		List<UMLOperation> operationsToBeRemoved = new ArrayList<UMLOperation>();
		for(Iterator<UMLOperation> addedOperationIterator = addedOperations.iterator(); addedOperationIterator.hasNext();) {
			UMLOperation addedOperation = addedOperationIterator.next();
			for(UMLOperationBodyMapper mapper : getOperationBodyMapperList()) {
				if(!mapper.getNonMappedLeavesT1().isEmpty() || !mapper.getNonMappedInnerNodesT1().isEmpty() ||
					!mapper.getVariableReplacementsWithMethodInvocation().isEmpty() || !mapper.getMethodInvocationReplacements().isEmpty()) {
					Set<OperationInvocation> operationInvocations = mapper.getOperation2().getBody().getAllOperationInvocations();
					OperationInvocation addedOperationInvocation = null;
					for(OperationInvocation invocation : operationInvocations) {
						if(invocation.matchesOperation(addedOperation)) {
							addedOperationInvocation = invocation;
							break;
						}
					}
					if(addedOperationInvocation != null) {
						List<UMLParameter> originalMethodParameters = mapper.getOperation1().getParametersWithoutReturnType();
						Map<UMLParameter, UMLParameter> originalMethodParametersPassedAsArgumentsMappedToCalledMethodParameters = new LinkedHashMap<UMLParameter, UMLParameter>();
						List<String> arguments = addedOperationInvocation.getArguments();
						List<UMLParameter> parameters = addedOperation.getParametersWithoutReturnType();
						Map<String, String> parameterToArgumentMap = new LinkedHashMap<String, String>();
						for(int i=0; i<parameters.size(); i++) {
							String argumentName = arguments.get(i);
							String parameterName = parameters.get(i).getName();
							parameterToArgumentMap.put(parameterName, argumentName);
							for(UMLParameter originalMethodParameter : originalMethodParameters) {
								if(originalMethodParameter.getName().equals(argumentName)) {
									originalMethodParametersPassedAsArgumentsMappedToCalledMethodParameters.put(originalMethodParameter, parameters.get(i));
								}
							}
						}
						if(parameterTypesMatch(originalMethodParametersPassedAsArgumentsMappedToCalledMethodParameters)) {
							UMLOperationBodyMapper operationBodyMapper = new UMLOperationBodyMapper(mapper, addedOperation, parameterToArgumentMap);
							if(!operationBodyMapper.getMappings().isEmpty() &&
									(operationBodyMapper.getMappings().size() > operationBodyMapper.nonMappedElementsT2()
											|| operationBodyMapper.exactMatches() > 0) ) {
								ExtractOperationRefactoring extractOperationRefactoring =
										new ExtractOperationRefactoring(addedOperation, operationBodyMapper.getOperation1(), operationBodyMapper.getOperation1().getClassName());
								refactorings.add(extractOperationRefactoring);
								operationsToBeRemoved.add(addedOperation);
							}
							else if(addedOperation.isDelegate() != null) {
								extractedDelegateOperations.put(addedOperation, addedOperation.isDelegate());
								operationsToBeRemoved.add(addedOperation);
							}
						}
					}
				}
			}
		}
		addedOperations.removeAll(operationsToBeRemoved);
	}

	private boolean parameterTypesMatch(Map<UMLParameter, UMLParameter> originalMethodParametersPassedAsArgumentsMappedToCalledMethodParameters) {
		for(UMLParameter key : originalMethodParametersPassedAsArgumentsMappedToCalledMethodParameters.keySet()) {
			UMLParameter value = originalMethodParametersPassedAsArgumentsMappedToCalledMethodParameters.get(key);
			if(!key.getType().equals(value.getType()) && !key.getType().equalsWithSubType(value.getType())) {
				return false;
			}
		}
		return true;
	}
/*
	public void checkForOperationSignatureChanges() {
		for(Iterator<UMLOperation> removedOperationIterator = removedOperations.iterator(); removedOperationIterator.hasNext();) {
			UMLOperation removedOperation = removedOperationIterator.next();
			List<UMLOperation> matchingAddedOperations = new ArrayList<UMLOperation>();
			for(Iterator<UMLOperation> addedOperationIterator = addedOperations.iterator(); addedOperationIterator.hasNext();) {
				UMLOperation addedOperation = addedOperationIterator.next();
				if(removedOperation.getName().equals(addedOperation.getName())) {
					matchingAddedOperations.add(addedOperation);
				}
			}
			if(matchingAddedOperations.size() == 1) {
				UMLOperation addedOperation = matchingAddedOperations.get(0);
				UMLOperationDiff operationDiff = new UMLOperationDiff(removedOperation, addedOperation);
				addedOperations.remove(addedOperation);
				removedOperationIterator.remove();
				operationDiffList.add(operationDiff);
				UMLOperationBodyMapper operationBodyMapper = new UMLOperationBodyMapper(removedOperation, addedOperation);
    			this.addOperationBodyMapper(operationBodyMapper);
			}
			else if(matchingAddedOperations.size() > 1) {
				UMLOperation addedOperation = null;
				for(UMLOperation operation : matchingAddedOperations) {
					if(operation.getParameters().size() == removedOperation.getParameters().size()) {
						addedOperation = operation;
						break;
					}
				}
				if(addedOperation != null) {
					UMLOperationDiff operationDiff = new UMLOperationDiff(removedOperation, addedOperation);
					addedOperations.remove(addedOperation);
					removedOperationIterator.remove();
					operationDiffList.add(operationDiff);
					UMLOperationBodyMapper operationBodyMapper = new UMLOperationBodyMapper(removedOperation, addedOperation);
	    			this.addOperationBodyMapper(operationBodyMapper);
				}
			}
		}
	}

	public void checkForOperationRenames() {
		for(Iterator<UMLOperation> removedOperationIterator = removedOperations.iterator(); removedOperationIterator.hasNext();) {
			UMLOperation removedOperation = removedOperationIterator.next();
			Map<UMLOperationDiff, Double> diffMap = new HashMap<UMLOperationDiff, Double>();
			for(Iterator<UMLOperation> addedOperationIterator = addedOperations.iterator(); addedOperationIterator.hasNext();) {
				UMLOperation addedOperation = addedOperationIterator.next();
				if(removedOperation.equalSignatureIgnoringOperationName(addedOperation)) {
					int bothMethodCallsFound = 0;
					for(UMLOperationBodyDiff operationBodyDiff : operationBodyDiffList) {
						boolean removedMethodCallFound = false;
						boolean addedMethodCallFound = false;
						for(MethodCall methodCall : operationBodyDiff.getRemovedMethodCalls()) {
							if(methodCall.matchesOperation(removedOperation)) {
								removedMethodCallFound = true;
								break;
							}
						}
						for(MethodCall methodCall : operationBodyDiff.getAddedMethodCalls()) {
							if(methodCall.matchesOperation(addedOperation)) {
								addedMethodCallFound = true;
								break;
							}
						}
						if(removedMethodCallFound && addedMethodCallFound) {
							bothMethodCallsFound++;
						}
					}
					if(bothMethodCallsFound > 0) {
						UMLOperationDiff operationDiff = new UMLOperationDiff(removedOperation, addedOperation);
						double normalized = removedOperation.normalizedNameDistance(addedOperation);
						diffMap.put(operationDiff, normalized);
					}
				}
			}
			double min = 1.0;
			UMLOperationDiff minOperationDiff = null;
			for(UMLOperationDiff operationDiff : diffMap.keySet()) {
				double value = diffMap.get(operationDiff);
				if(value < min) {
					minOperationDiff = operationDiff;
					min = value;
				}
			}
			if(minOperationDiff != null) {
				operationDiffList.add(minOperationDiff);
				addedOperations.remove(minOperationDiff.getAddedOperation());
				removedOperationIterator.remove();
				UMLOperationBodyMapper operationBodyMapper = new UMLOperationBodyMapper(minOperationDiff.getRemovedOperation(), minOperationDiff.getAddedOperation());
    			this.addOperationBodyMapper(operationBodyMapper);
			}
		}
	}

	public void checkForAttributeRenames() {
		for(Iterator<UMLAttribute> removedAttributeIterator = removedAttributes.iterator(); removedAttributeIterator.hasNext();) {
			UMLAttribute removedAttribute = removedAttributeIterator.next();
			Map<UMLAttributeDiff, Double> diffMap = new HashMap<UMLAttributeDiff, Double>();
			for(Iterator<UMLAttribute> addedAttributeIterator = addedAttributes.iterator(); addedAttributeIterator.hasNext();) {
				UMLAttribute addedAttribute = addedAttributeIterator.next();
				if(removedAttribute.getType().equals(addedAttribute.getType())) {
					int bothFieldAccessesFound = 0;
					for(UMLOperationBodyDiff operationBodyDiff : operationBodyDiffList) {
						boolean removedFieldAccessFound = false;
						boolean addedFieldAccessFound = false;
						for(FieldAccess fieldAccess : operationBodyDiff.getRemovedFieldAccesses()) {
							if(fieldAccess.matchesAttribute(removedAttribute)) {
								removedFieldAccessFound = true;
								break;
							}
						}
						for(FieldAccess fieldAccess : operationBodyDiff.getAddedFieldAccesses()) {
							if(fieldAccess.matchesAttribute(addedAttribute)) {
								addedFieldAccessFound = true;
								break;
							}
						}
						if(removedFieldAccessFound && addedFieldAccessFound) {
							bothFieldAccessesFound++;
						}
					}
					if(bothFieldAccessesFound > 0) {
						UMLAttributeDiff attributeDiff = new UMLAttributeDiff(removedAttribute, addedAttribute);
						double normalized = removedAttribute.normalizedNameDistance(addedAttribute);
						diffMap.put(attributeDiff, normalized);
					}
				}
			}
			double min = 1.0;
			UMLAttributeDiff minAttributeDiff = null;
			for(UMLAttributeDiff attributeDiff : diffMap.keySet()) {
				double value = diffMap.get(attributeDiff);
				if(value < min) {
					minAttributeDiff = attributeDiff;
					min = value;
				}
			}
			if(minAttributeDiff != null) {
				attributeDiffList.add(minAttributeDiff);
				addedAttributes.remove(minAttributeDiff.getAddedAttribute());
				removedAttributeIterator.remove();
			}
		}
	}
*/
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if(!isEmpty())
			sb.append(className).append(":").append("\n");
		if(visibilityChanged) {
			sb.append("\t").append("visibility changed from " + oldVisibility + " to " + newVisibility).append("\n");
		}
		if(abstractionChanged) {
			sb.append("\t").append("abstraction changed from " + (oldAbstraction ? "abstract" : "concrete") + " to " +
					(newAbstraction ? "abstract" : "concrete")).append("\n");
		}
		Collections.sort(removedOperations);
		for(UMLOperation umlOperation : removedOperations) {
			sb.append("operation " + umlOperation + " removed").append("\n");
		}
		Collections.sort(addedOperations);
		for(UMLOperation umlOperation : addedOperations) {
			sb.append("operation " + umlOperation + " added").append("\n");
		}
		Collections.sort(removedAttributes);
		for(UMLAttribute umlAttribute : removedAttributes) {
			sb.append("attribute " + umlAttribute + " removed").append("\n");
		}
		Collections.sort(addedAttributes);
		for(UMLAttribute umlAttribute : addedAttributes) {
			sb.append("attribute " + umlAttribute + " added").append("\n");
		}
		for(UMLOperationDiff operationDiff : operationDiffList) {
			sb.append(operationDiff);
		}
		for(UMLAttributeDiff attributeDiff : attributeDiffList) {
			sb.append(attributeDiff);
		}
		Collections.sort(operationBodyMapperList);
		for(UMLOperationBodyMapper operationBodyMapper : operationBodyMapperList) {
			sb.append(operationBodyMapper);
		}
		return sb.toString();
	}

	public int compareTo(UMLClassDiff classDiff) {
		return this.className.compareTo(classDiff.className);
	}

	public void checkForLambdas() {
		for (UMLOperationBodyMapper umlOperationBodyMapper : operationBodyMapperList) {
			List<Lambda> lambdasBefore = umlOperationBodyMapper.getOperation1().getLambdas();
			List<Lambda> lambdasAfter = umlOperationBodyMapper.getOperation2().getLambdas();
			Collection<? extends Lambda> newLambdas = compareLambdasToDetectAddedLambdas(lambdasBefore, lambdasAfter);
			newLambdas.forEach(lambda -> {
				lambda.setLambdaLocationStatus(LambdaLocationStatus.IN_EXISTING_METHOD);
				addedLambdas.add(lambda);
			});
			
		}
		for (UMLOperationDiff umlOperationDiff : operationDiffList) {
			List<Lambda> lambdasBefore = umlOperationDiff.getRemovedOperation().getLambdas();
			List<Lambda> lambdasAfter = umlOperationDiff.getAddedOperation().getLambdas();
			Collection<? extends Lambda> newLambdas = compareLambdasToDetectAddedLambdas(lambdasBefore, lambdasAfter);
			newLambdas.forEach(lambda -> {
				lambda.setLambdaLocationStatus(LambdaLocationStatus.IN_EXISTING_METHOD);
				addedLambdas.add(lambda);
			});
		}
		for (UMLOperation umlOperation : addedOperations) {
			umlOperation.getLambdas().forEach(lambda -> {
				lambda.setLambdaLocationStatus(LambdaLocationStatus.IN_NEW_METHOD);
				addedLambdas.add(lambda);
			});
			
		}
	}

	private Collection<? extends Lambda> compareLambdasToDetectAddedLambdas(List<Lambda> lambdasBefore, List<Lambda> lambdasAfter) {
		Set<Lambda> matchedLambdasAfter = new LinkedHashSet<>(lambdasAfter);
		Set<Lambda> matchedLambdasBefore = new LinkedHashSet<>(lambdasBefore);
		matchedLambdasAfter.retainAll(lambdasBefore); // Remove everything which is not matched
		matchedLambdasBefore.retainAll(lambdasAfter);
		//final double SIMILARITY_THRESHOLD = 0.5;
		for (Lambda lambdaAfter : lambdasAfter) {
			if (!matchedLambdasAfter.contains(lambdaAfter)) {
				for (Lambda lambdaBefore : lambdasBefore) {
					if (!matchedLambdasBefore.contains(lambdaBefore)) {
						
						/*if (lambdaAfter.getParameterNames().size() == lambdaBefore.getParameterNames().size() &&
								textuallySimilar(lambdaAfter.getBody(), lambdaBefore.getBody(), SIMILARITY_THRESHOLD)) {*/
						if (lambdaAfter.getBody().equals(lambdaAfter.getBody())) {
							matchedLambdasAfter.add(lambdaAfter);
							matchedLambdasBefore.add(lambdaBefore);
							break;
						}
					}
				}
			}
		}
		List<Lambda> detectedAddedLambdas = new ArrayList<>(lambdasAfter);
		detectedAddedLambdas.removeAll(matchedLambdasAfter);
		return detectedAddedLambdas;
	}

	public List<Lambda> getAddedLambdas() {
		return addedLambdas;
	}
}
