package ch.xiaobin.subordination.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;

import ch.xiaobin.subordination.annotator.AnnotationPipelineManager;
import ch.xiaobin.subordination.dao.AdjunctClause;
import ch.xiaobin.subordination.dao.ClauseType;
import ch.xiaobin.subordination.dao.ComplementClause;
import ch.xiaobin.subordination.dao.RelativeClause;
import ch.xiaobin.subordination.dao.SubordinateClause;
import ch.xiaobin.subordination.extractor.DocumentSubClauseExtractor;
import edu.stanford.nlp.pipeline.AnnotationPipeline;

/**
 * Servlet implementation class SubordinationServlet
 */
public class SubordinationServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
	AnnotationPipelineManager pipelineManager;
	AnnotationPipeline pipeline;
	
	public static final String PARAM_KEY_TEXT = "text";
	private Logger logger = LogManager.getLogger();

    /**
     * @see HttpServlet#HttpServlet()
     */
    public SubordinationServlet() {
        super();
    }

    @Override
    public void init() throws ServletException {
    	super.init();
		try {
			pipelineManager = new AnnotationPipelineManager();
			pipeline = pipelineManager.getLocalPipeLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.getWriter().append("subordination servlet");
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//		response.getWriter().append("posted to servlet.");

		//get the document passed in
		String document = request.getParameter(PARAM_KEY_TEXT);
		String documentId = "WebDoc";
		
		logger.trace("Analyzing text: {}...", document.length() > 50 ? 
				document.substring(0, 50) : document);

		DocumentSubClauseExtractor documentSubClauseExtractor = 
				new DocumentSubClauseExtractor(documentId, document, pipeline);
		List<SubordinateClause> subordinateClauses = 
				documentSubClauseExtractor.extractClauses();
		
		//construct response json
		StringBuilder strBuilder = new StringBuilder("[");
		for(SubordinateClause sc: subordinateClauses) {
			switch(sc.getClauseType()) {
			case RELATIVE:
				strBuilder.append(((RelativeClause) sc).toJSONString()).append(", ");
				break;
			case COMPLEMENT:
				strBuilder.append(((ComplementClause) sc).toJSONString()).append(", ");
				break;
			case ADJUNCT:
				strBuilder.append(((AdjunctClause) sc).toJSONString()).append(", ");
				break;
			}
		}

		//remove last comma ","
		strBuilder.deleteCharAt(strBuilder.lastIndexOf(","));
		strBuilder.append("]\n");
		
		//write results 
		response.setHeader("Content-Type", "application/json; charset=utf-8");
		response.getWriter().append(strBuilder.toString());
	}

}
