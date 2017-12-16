package it.unibz.inf.ontop.spec.ontology;

/*
 * #%L
 * ontop-obdalib-core
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import it.unibz.inf.ontop.model.term.Constant;
import it.unibz.inf.ontop.model.term.ObjectConstant;
import it.unibz.inf.ontop.model.term.ValueConstant;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface Ontology  {

	OntologyVocabularyCategory<OClass> classes();

    OntologyVocabularyCategory<ObjectPropertyExpression> objectProperties();

    OntologyVocabularyCategory<DataPropertyExpression> dataProperties();

    OntologyVocabularyCategory<AnnotationProperty> annotationProperties();

	Datatype getDatatype(String uri);


	// SUBCLASS/PROPERTY

	void addSubClassOfAxiom(ClassExpression concept1, ClassExpression concept2) throws InconsistentOntologyException;

	void addDataPropertyRangeAxiom(DataPropertyRangeExpression range, Datatype datatype) throws InconsistentOntologyException;
	
	void addSubPropertyOfAxiom(ObjectPropertyExpression included, ObjectPropertyExpression including) throws InconsistentOntologyException;

	void addSubPropertyOfAxiom(DataPropertyExpression included, DataPropertyExpression including) throws InconsistentOntologyException;

	void addSubPropertyOfAxiom(AnnotationProperty included, AnnotationProperty including);

	Collection<BinaryAxiom<ClassExpression>> getSubClassAxioms();

	Collection<BinaryAxiom<DataRangeExpression>> getSubDataRangeAxioms();
	
	Collection<BinaryAxiom<ObjectPropertyExpression>> getSubObjectPropertyAxioms();

	Collection<BinaryAxiom<DataPropertyExpression>> getSubDataPropertyAxioms();


	// DISJOINTNESS
	
	void addDisjointClassesAxiom(ClassExpression... classes) throws InconsistentOntologyException;

	void addDisjointObjectPropertiesAxiom(ObjectPropertyExpression... properties) throws InconsistentOntologyException;
	
	void addDisjointDataPropertiesAxiom(DataPropertyExpression... properties) throws InconsistentOntologyException;
	
	
	Collection<NaryAxiom<ClassExpression>> getDisjointClassesAxioms();
	
	Collection<NaryAxiom<ObjectPropertyExpression>> getDisjointObjectPropertiesAxioms();

	Collection<NaryAxiom<DataPropertyExpression>> getDisjointDataPropertiesAxioms();
	
	
	// REFLEXIVITY / IRREFLEXIVITY
	
	void addReflexiveObjectPropertyAxiom(ObjectPropertyExpression ope) throws InconsistentOntologyException;

	void addIrreflexiveObjectPropertyAxiom(ObjectPropertyExpression ope) throws InconsistentOntologyException;
	
	Collection<ObjectPropertyExpression> getReflexiveObjectPropertyAxioms();
	
	Collection<ObjectPropertyExpression> getIrreflexiveObjectPropertyAxioms();
	
	// FUNCTIONALITY 
	
	
	void addFunctionalObjectPropertyAxiom(ObjectPropertyExpression prop);

	void addFunctionalDataPropertyAxiom(DataPropertyExpression prop);
	
	Set<ObjectPropertyExpression> getFunctionalObjectProperties();

	Set<DataPropertyExpression> getFunctionalDataProperties();
	
	
	// ASSERTIONS
	
	void addClassAssertion(ClassAssertion assertion);

	void addObjectPropertyAssertion(ObjectPropertyAssertion assertion);

	void addDataPropertyAssertion(DataPropertyAssertion assertion);

	void addAnnotationAssertion(AnnotationAssertion assertion);

	
	List<ClassAssertion> getClassAssertions();

	List<ObjectPropertyAssertion> getObjectPropertyAssertions();
	
	List<DataPropertyAssertion> getDataPropertyAssertions();

	List<AnnotationAssertion> getAnnotationAssertions();





	/**
	 * create an auxiliary object property 
	 * (auxiliary properties result from ontology normalization)
	 * 
	 *
	 */

	ObjectPropertyExpression createAuxiliaryObjectProperty();
	
	
	/**
	 * return all auxiliary object properties
	 * (auxiliary properties result from ontology normalization)
	 * 
	 * @return
	 */
	
	Collection<ObjectPropertyExpression> getAuxiliaryObjectProperties();


	/**
	 * Creates a class assertion
	 *    (implements rule [C4])
	 *
	 * @param ce
	 * @param o
	 * @return
	 * @return null if ce is the top class ([C4])
	 * @throws InconsistentOntologyException if ce is the bottom class ([C4])
	 */

	ClassAssertion createClassAssertion(OClass ce, ObjectConstant o) throws InconsistentOntologyException;


	/**
	 * Creates an object property assertion
	 * (ensures that the property is not inverse by swapping arguments if necessary)
	 *    (implements rule [O4])
	 *
	 * @param ope
	 * @param o1
	 * @param o2
	 * @return null if ope is the top property ([O4])
	 * @throws InconsistentOntologyException if ope is the bottom property ([O4])
	 */

	ObjectPropertyAssertion createObjectPropertyAssertion(ObjectPropertyExpression ope, ObjectConstant o1, ObjectConstant o2) throws InconsistentOntologyException;

	/**
	 * Creates a data property assertion
	 *    (implements rule [D4])
	 *
	 * @param dpe
	 * @param o
	 * @param v
	 * @return null if dpe is the top property ([D4])
	 * @throws InconsistentOntologyException if dpe is the bottom property ([D4])
	 */

	DataPropertyAssertion createDataPropertyAssertion(DataPropertyExpression dpe, ObjectConstant o, ValueConstant v) throws InconsistentOntologyException;

	/**
	 * Creates an annotation property assertion
	 *
	 */

	AnnotationAssertion createAnnotationAssertion(AnnotationProperty ap, ObjectConstant o, Constant c);

}
