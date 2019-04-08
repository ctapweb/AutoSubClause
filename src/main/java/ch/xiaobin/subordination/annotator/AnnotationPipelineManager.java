/**
 * 
 */
package ch.xiaobin.subordination.annotator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.stanford.nlp.pipeline.AnnotationPipeline;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.pipeline.StanfordCoreNLPClient;

/**
 * For getting the annotation pipeline.
 * @author xiaobin
 *
 */
public class AnnotationPipelineManager {
	private static AnnotationPipeline serverPipeline = null;
	private static AnnotationPipeline localPipeline = null;

	Properties properties; 
	private static final String DEFAULT_PROP_FILENAME = "config.properties";
	private static final String PROP_KEY_HOST = "anno_server.host";
	private static final String PROP_KEY_PORT = "anno_server.port";
	private static final String PROP_KEY_ANNOTATORS = "annotators";

	private String serverHost;
	private int serverPort;
	private String annotators;
	
	private final Logger logger = LogManager.getLogger();
	
	public AnnotationPipelineManager() throws IOException {
		this(DEFAULT_PROP_FILENAME);
	}

	/**
	 * Load annotation pipeline properties from properties file.
	 * @param propFileName
	 * @throws IOException 
	 */
	public AnnotationPipelineManager(String propFileName) throws IOException {
		properties = new Properties();
		logger.trace("Loading annotation server properties from {}...", propFileName);
		InputStream ins = 
				Thread.currentThread().getContextClassLoader().getResourceAsStream(propFileName);
		properties.load(ins);
		ins.close();
		
		//sets server properties
		serverHost = properties.getProperty(PROP_KEY_HOST);
		serverPort = Integer.parseInt(properties.getProperty(PROP_KEY_PORT));
		annotators = properties.getProperty(PROP_KEY_ANNOTATORS);

	}
	
	/**
	 * Gets an annotation pipeline from a coreNLP server. Server properties are
	 * set in the resources/config.properties file.
	 * @return
	 */
	public AnnotationPipeline getServerPipeline() {
		if(serverPipeline == null) {
			logger.trace("Setting up server annotator pipeline with properties: {}, {}, {}", 
					serverHost, serverPort, annotators);
			serverPipeline = new StanfordCoreNLPClient(properties, serverHost, serverPort);
		}
		
		return serverPipeline;
		
	}
	
	/**
	 * Gets a local annotation pipeline.
	 * @return
	 */
	public AnnotationPipeline getLocalPipeLine() {
		if(localPipeline == null) {
			logger.trace("Setting up local annotator pipeline with properties: {}", annotators);
			localPipeline = new StanfordCoreNLP(properties);
		}
		
		return localPipeline;
	}
	
}



