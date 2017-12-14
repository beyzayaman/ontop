package it.unibz.inf.ontop.si.dag;

/*
 * #%L
 * ontop-reformulation-core
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


import it.unibz.inf.ontop.spec.ontology.*;
import it.unibz.inf.ontop.spec.ontology.impl.TBoxReasonerImpl;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Representation of the named part of the property and class DAGs  
 *     based on the DAGs provided by a TBoxReasonerImpl
 * 
 * WARNING: THIS CLASS IS FOR TESTING ONLY 
 */
@Deprecated
public class TestTBoxReasonerImpl_Named implements TBoxReasoner {

	private final EquivalencesDAG<ObjectPropertyExpression> objectPropertyDAG;
	private final EquivalencesDAG<DataPropertyExpression> dataPropertyDAG;
	private final EquivalencesDAG<ClassExpression> classDAG;
	private final EquivalencesDAG<DataRangeExpression> dataRangeDAG;
	private final TBoxReasoner reasoner;

	public TestTBoxReasonerImpl_Named(TBoxReasoner reasoner) {
		this.objectPropertyDAG = new EquivalencesDAGImpl<>(reasoner.getObjectPropertyDAG());
		this.dataPropertyDAG = new EquivalencesDAGImpl<>(reasoner.getDataPropertyDAG());
		this.classDAG = new EquivalencesDAGImpl<>(reasoner.getClassDAG());
		this.dataRangeDAG = new EquivalencesDAGImpl<>(reasoner.getDataRangeDAG());
		this.reasoner = reasoner;
	}


	/**
	 * Return the DAG of properties
	 * 
	 * @return DAG 
	 */

	@Override
	public EquivalencesDAG<ObjectPropertyExpression> getObjectPropertyDAG() {
		return objectPropertyDAG;
	}
	/**
	 * Return the DAG of properties
	 * 
	 * @return DAG 
	 */

	@Override
	public EquivalencesDAG<DataPropertyExpression> getDataPropertyDAG() {
		return dataPropertyDAG;
	}
	
	/**
	 * Return the DAG of classes
	 * 
	 * @return DAG 
	 */

	@Override
	public EquivalencesDAG<ClassExpression> getClassDAG() {
		return classDAG;
	}
	
	@Override
	public EquivalencesDAG<DataRangeExpression> getDataRangeDAG() {
		return dataRangeDAG;
	}


	// DUMMMY

	@Override
	public Collection<OClass> getClasses() {
		return null;
	}

	@Override
	public Collection<ObjectPropertyExpression> getObjectProperties() {
		return null;
	}

	@Override
	public Collection<DataPropertyExpression> getDataProperties() {
		return null;
	}

	@Override
	public Collection<AnnotationProperty> getAnnotationProperties() {
		return null;
	}

	@Override
	public OClass getClass(String uri) {
		return null;
	}

	@Override
	public ObjectPropertyExpression getObjectProperty(String uri) {
		return null;
	}

	@Override
	public DataPropertyExpression getDataProperty(String uri) {
		return null;
	}

	@Override
	public AnnotationProperty getAnnotationProperty(String uri) {
		return null;
	}

	@Override
	public Datatype getDatatype(String uri) {
		return null;
	}

	@Override
	public boolean containsClass(String uri) {
		return false;
	}

	@Override
	public boolean containsObjectProperty(String uri) {
		return false;
	}

	@Override
	public boolean containsDataProperty(String uri) {
		return false;
	}

	@Override
	public boolean containsAnnotationProperty(String uri) {
		return false;
	}


	/**
	 * Reconstruction of the Named DAG (as EquivalencesDAG) from a DAG
	 *
	 * @param <T> Property or BasicClassDescription
	 */
	
	public static final class EquivalencesDAGImpl<T> implements EquivalencesDAG<T> {

		private final EquivalencesDAG<T> reasonerDAG;
		
		EquivalencesDAGImpl(EquivalencesDAG<T> reasonerDAG) {
			this.reasonerDAG = reasonerDAG;
		}
		
		@Override
		public Iterator<Equivalences<T>> iterator() {
			LinkedHashSet<Equivalences<T>> result = new LinkedHashSet<Equivalences<T>>();
			
			for (Equivalences<T> e : reasonerDAG) {
				Equivalences<T> nodes = getVertex(e.getRepresentative());
				if (nodes != null)
					result.add(nodes);			
			}
			return result.iterator();
		}

		@Override
		public Equivalences<T> getVertex(T desc) {

			// either all elements of the equivalence set are there or none!
			Equivalences<T> vertex = reasonerDAG.getVertex(desc);
			if (vertex.isIndexed())
				return vertex;
			else
				return null;
		}

		
		@Override
		public Set<Equivalences<T>> getDirectSub(Equivalences<T> v) {
			LinkedHashSet<Equivalences<T>> result = new LinkedHashSet<Equivalences<T>>();

			for (Equivalences<T> e : reasonerDAG.getDirectSub(v)) {
				T child = e.getRepresentative();
				
				// get the child node and its equivalent nodes
				Equivalences<T> namedEquivalences = getVertex(child);
				if (namedEquivalences != null)
					result.add(namedEquivalences);
				else 
					result.addAll(getDirectSub(e)); // recursive call if the child is not empty
			}
			return result;
		}

		@Override
		public Set<Equivalences<T>> getSub(Equivalences<T> v) {
			LinkedHashSet<Equivalences<T>> result = new LinkedHashSet<Equivalences<T>>();
			
			for (Equivalences<T> e : reasonerDAG.getSub(v)) {
				Equivalences<T> nodes = getVertex(e.getRepresentative());
				if (nodes != null)
					result.add(nodes);			
			}
			return result;
		}

		@Override
		public Set<T> getSubRepresentatives(T v) {
			Equivalences<T> eq = reasonerDAG.getVertex(v);
			LinkedHashSet<T> result = new LinkedHashSet<T>();
			
			for (Equivalences<T> e : reasonerDAG.getSub(eq)) {
				Equivalences<T> nodes = getVertex(e.getRepresentative());
				if (nodes != null)
					result.add(nodes.getRepresentative());			
			}
			return result;
		}		

		@Override
		public Set<Equivalences<T>> getDirectSuper(Equivalences<T> v) {
			LinkedHashSet<Equivalences<T>> result = new LinkedHashSet<Equivalences<T>>();
			
			for (Equivalences<T> e : reasonerDAG.getDirectSuper(v)) {
				T parent = e.getRepresentative();
				
				// get the child node and its equivalent nodes
				Equivalences<T> namedEquivalences = getVertex(parent);
				if (namedEquivalences != null)
					result.add(namedEquivalences);
				else 
					result.addAll(getDirectSuper(e)); // recursive call if the parent is not named
			}
			return result;
		}
		
		@Override
		public Set<Equivalences<T>> getSuper(Equivalences<T> v) {
			LinkedHashSet<Equivalences<T>> result = new LinkedHashSet<Equivalences<T>>();

			for (Equivalences<T> e : reasonerDAG.getSuper(v)) {
				Equivalences<T> nodes = getVertex(e.getRepresentative());
				if (nodes != null)
					result.add(nodes);			
			}
			
			return result;
		}

		@Override
		public T getCanonicalForm(T v) {
			// TODO Auto-generated method stub
			return null;
		}
	}


}
