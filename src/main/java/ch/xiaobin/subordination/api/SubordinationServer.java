/**
 * 
 */
package ch.xiaobin.subordination.api;


import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;

/**
 * Runs a server to provide api for extracting subordinate clauses from texts.
 * @author xiaobin
 *
 */
public class SubordinationServer {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		Server server = new Server(8080);
		ServletHandler servletHandler = new ServletHandler();
		server.setHandler(servletHandler);
		servletHandler.addServletWithMapping(SubordinationServlet.class, "/*");
		server.start();
		server.join();

	}

}
