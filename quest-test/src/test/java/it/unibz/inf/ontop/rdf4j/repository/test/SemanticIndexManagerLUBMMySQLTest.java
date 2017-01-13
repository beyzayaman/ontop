package it.unibz.inf.ontop.rdf4j.repository.test;

/*
 * #%L
 * ontop-test
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

import java.io.File;
import java.sql.Connection;
import java.util.Properties;

import com.google.inject.Injector;
import it.unibz.inf.ontop.injection.*;
import it.unibz.inf.ontop.owlrefplatform.owlapi.*;
import junit.framework.TestCase;

import it.unibz.inf.ontop.io.QueryIOManager;
import it.unibz.inf.ontop.owlrefplatform.core.QuestConstants;
import it.unibz.inf.ontop.querymanager.QueryController;
import it.unibz.inf.ontop.querymanager.QueryControllerEntity;
import it.unibz.inf.ontop.querymanager.QueryControllerQuery;
import it.unibz.inf.ontop.rdf4j.repository.SemanticIndexManager;
import it.unibz.inf.ontop.sql.JDBCConnectionManager;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests if QuestOWL can be initialized on top of an existing semantic index
 * created by the SemanticIndexManager.
 */
public class SemanticIndexManagerLUBMMySQLTest extends TestCase {

	private final NativeQueryLanguageComponentFactory nativeQLFactory;
	String driver = "com.mysql.jdbc.Driver";
	String url = "jdbc:mysql://10.7.20.39/si_test?sessionVariables=sql_mode='ANSI'";
	String username = "fish";
	String password = "fish";
	
	String owlfile = "../quest-owlapi3/src/test/resources/test/lubm-ex-20-uni1/University0_0.owl";

	private OWLOntology ontology;
	private OWLOntologyManager manager;
	private final OntopSQLSettings settings;
	
	Logger log = LoggerFactory.getLogger(this.getClass());

	public SemanticIndexManagerLUBMMySQLTest() throws Exception {
		manager = OWLManager.createOWLOntologyManager();
		ontology = manager.loadOntologyFromOntologyDocument(new File(owlfile));

		OBDACoreConfiguration configuration = OBDACoreConfiguration.defaultBuilder()
				.jdbcUrl(url)
				.dbUser(username)
				.dbPassword(password)
				.jdbcDriver(driver)
				.build();

		// TODO: re-enable them
		// source.setParameter(RDBMSourceParameterConstants.IS_IN_MEMORY, "false");
		// source.setParameter(RDBMSourceParameterConstants.USE_DATASOURCE_FOR_ABOXDUMP, "true");

		settings = configuration.getSettings();
		Injector injector = configuration.getInjector();
		nativeQLFactory = injector.getInstance(NativeQueryLanguageComponentFactory.class);
	}

	@Override
	public void setUp() throws Exception {
		Connection conn = null;
		try {
			conn = JDBCConnectionManager.getJDBCConnectionManager().createConnection(settings);
			SemanticIndexManager simanager = new SemanticIndexManager(ontology, conn, nativeQLFactory);
			simanager.setupRepository(true);
		} catch (Exception e) {
			throw e;
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
	}
	
	@Override
	public void tearDown() {
		Connection conn = null;
		try {
			conn = JDBCConnectionManager.getJDBCConnectionManager().createConnection(settings);

			SemanticIndexManager simanager = new SemanticIndexManager(ontology, conn, nativeQLFactory);
			simanager.dropRepository();
		} catch (Exception e) {
			
		} finally {
			if (conn != null) {
				try {
				conn.close();
				} catch (Exception e) {
					
				}
			} 
		}
	}

	public void test2RestoringAndLoading() throws Exception {
		
		Connection conn = null;
		try {
			conn = JDBCConnectionManager.getJDBCConnectionManager().createConnection(settings);
			SemanticIndexManager simanager = new SemanticIndexManager(ontology, conn, nativeQLFactory);
			//simanager.restoreRepository();
			int inserts = simanager.insertData(ontology, 20000, 5000);
			simanager.updateMetadata();
			log.debug("Inserts: {}", inserts);
//			assertEquals(30033, inserts);
		} catch (Exception e) {
			throw e;
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
	}

	public void test3InitializingQuest() throws Exception {
		Properties p = new Properties();
		p.setProperty(QuestCoreSettings.DBTYPE, QuestConstants.SEMANTIC_INDEX);
		p.setProperty(QuestCoreSettings.ABOX_MODE, QuestConstants.CLASSIC);
		p.setProperty(QuestCoreSettings.STORAGE_LOCATION, QuestConstants.JDBC);
		p.setProperty(QuestCoreSettings.OBTAIN_FROM_ONTOLOGY, "false");
		p.setProperty(QuestCoreSettings.JDBC_DRIVER, driver);
		p.setProperty(QuestCoreSettings.JDBC_URL, url);
		p.setProperty(QuestCoreSettings.DB_USER, username);
		p.setProperty(QuestCoreSettings.DB_PASSWORD, password);


		QuestOWLFactory factory = new QuestOWLFactory();
        QuestConfiguration config = QuestConfiguration.defaultBuilder()
				.ontology(ontology)
				.properties(p)
				.build();
        QuestOWL quest = factory.createReasoner(config);


		QuestOWLConnection qconn = (QuestOWLConnection) quest.getConnection();
		QuestOWLStatement st = (QuestOWLStatement) qconn.createStatement();

		QueryController qc = new QueryController();
		QueryIOManager qman = new QueryIOManager(qc);
		qman.load("../quest-owlapi3/src/test/resources/test/treewitness/LUBM-ex-20.q");

		for (QueryControllerEntity e : qc.getElements()) {
			if (!(e instanceof QueryControllerQuery)) {
				continue;
			}
			QueryControllerQuery query = (QueryControllerQuery) e;
			log.debug("Executing query: {}", query.getID() );
			log.debug("Query: \n{}", query.getQuery());
			
			long start = System.nanoTime();
			QuestOWLResultSet res = (QuestOWLResultSet)st.executeTuple(query.getQuery());
			long end = System.nanoTime();
			
			double time = (end - start) / 1000; 
			
			int count = 0;
			while (res.nextRow()) {
				count += 1;
			}
			log.debug("Total result: {}", count );
			log.debug("Elapsed time: {} ms", time);
		}
	}

}
