package integration;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;

import main.Files2Facts;
import util.ConfigManager;

/**
 * 
 * Integrates Two AML files based on Prolog Rules.
 */
public class Integration {

	private XmlParser xml;

	/**
	 * This method integrates two AML files.
	 * 
	 * @throws Throwable
	 */

	public Integration() {

		xml = new XmlParser();

	}

	public void integrate() throws Throwable {

		Files2Facts filesAMLInRDF = new Files2Facts();

		// gets heterogeneity files in array.
		ArrayList<File> file = filesAMLInRDF.readFiles(ConfigManager.getFilePath(), ".aml");

		String contents = FileUtils.readFileToString(new File(file.get(1).getPath()), "UTF-8");

		new File(ConfigManager.getFilePath() + "integration/").mkdir();

		// One of the AML file will have its contents copied as it is.
		PrintWriter prologWriter = new PrintWriter(
				new File(ConfigManager.getFilePath() + "integration/integration.aml"));
		prologWriter.println(contents);
		prologWriter.close();

		// initializing documents.

		Document seed = xml.initInput(file.get(0).getPath());
		Document integration = xml.initInput(ConfigManager.getFilePath() + "integration/integration.aml");

		processNodesArributes(seed, integration);
		processNodesValues(seed, integration);

	}

	/**
	 * Algorithm for integrating data for nodes with attributes
	 * 
	 * compareConflicts(skips compared one),compareNonConflicts,addNonConflicts
	 * 
	 * @param seed
	 * @param integration
	 * @throws XPathExpressionException
	 * @throws DOMException
	 * @throws TransformerFactoryConfigurationError
	 * @throws TransformerException
	 * @throws IOException
	 */
	public void processNodesArributes(Document seed, Document integration) throws XPathExpressionException,
			DOMException, TransformerFactoryConfigurationError, TransformerException, IOException {

		xml.getAllNodes(seed, integration);

		// looping through the seedNode which will be compared to matching
		// elements in output.txt
		for (int i = 0; i < xml.getSeedNodes().size(); i++) {

			// not in the conflicting Element of output.txt
			if (xml.compareConflicts(i, seed) == 0) {

				// we run our noConflicting comparision algorithm
				if (xml.compareNonConflicts(i, seed, integration) != 1) {
					// if its identified its not in integration.aml
					// We need to add non match elements to the integration
					// file.
					xml.addNonConflicts(i, seed, integration);

				}

			}
		}
		// update the integration.aml file
		xml.finalizeIntegration(integration);

	}

	/**
	 * Algorithm for data integration for nodes with values
	 * 
	 * compareConflicts(skips compared one),compareNonConflictsNodes with
	 * value,addNonConflicts
	 * 
	 * @param seed
	 * @param integration
	 * @throws XPathExpressionException
	 * @throws TransformerFactoryConfigurationError
	 * @throws TransformerException
	 * @throws IOException
	 * @throws DOMException
	 */
	void processNodesValues(Document seed, Document integration) throws XPathExpressionException,
			TransformerFactoryConfigurationError, TransformerException, DOMException, IOException {

		// update for node values, array's updated.
		xml.setNodeValues(seed, integration);

		// looping through the seedNode which will be compared to matching
		// elements in output.txt
		for (int i = 0; i < xml.getSeedNodes().size(); i++) {

			// not in the conflicting Element of output.txt
			if (xml.compareConflicts(i, seed) == 0) {

				// we run our noConflicting comparision algorithm
				if (xml.compareNonConflictsValues(i, seed, integration) != 1) {

					// if its identified its not in integration.aml
					// We need to add only non matched elements to the
					// integration file.

					xml.addNonConflictsValues(i, seed, integration);

				}

			}
		}

		xml.finalizeIntegration(integration);

	}

}