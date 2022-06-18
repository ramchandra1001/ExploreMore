package plugin.com.fisglobal.ktt.business.keywords.utility.functions;

import static com.fisglobal.ktt.business.keywords.ErrorMessages.ERROR_BROWSER_NOT_INSTANTIATED;
import static com.fisglobal.ktt.business.keywords.ErrorMessages.ERROR_PARAMETERS_LIST;
import static com.fisglobal.ktt.view.config.KTTGuiConstants.DELIMITER;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;

import com.fisglobal.ktt.business.keywords.AbstractKeyword;
import com.fisglobal.ktt.business.keywords.KeywordUtilities;
import com.fisglobal.ktt.model.valueobjects.TestcaseExecutionResultVO;

/**
 * 
 * @author Sameer.Soundankar
 * 
 *         This keyword creates Galen framework compatible specification file
 *         Param1: Specify folder path where specification file is required to
 *         save Param2: name of specification file. This creates said file under
 *         folder specified in Param1 topLocator = listOfParameters[2];
 *         htmlControlToCheck = listOfParameters[3]; uniqueLocatorSequence =
 *         listOfParameters[4]; attributesToCheck = listOfParameters[5]; Param3:
 *         Optional. Specify top parent element locator from which other
 *         elements to capture. Param4: Optional. Specify comma separated name
 *         of HTML tags to capture Param5: Optional. Specify comma separated
 *         list of unique attributes to identify elements Param6: Specify comma
 *         separated list of properties to check. Such as height, width and
 *         text.
 * 
 *
 */
public class CreateLayoutSpecKeyword extends AbstractKeyword {
	/**
	 * This is logger object used to log keyword actions into a log file
	 */
	private static Logger logger = LogManager.getLogger(CreateLayoutSpecKeyword.class.getName());

	private String topLocator = null;

	private WebElement topPageElement = null;

	private String specFilePath = null;

	private String specFileName = null;

	private String frameName = null;

	private String htmlControlToCheck = null;

	private String attributesToCheck = null;

	private String uniqueLocatorSequence = null;

	HashMap<String, Integer> mapOfDuplicateElements = new HashMap<>();

	HashMap<String, String> mapOfLogicalNameAndLocator = new HashMap<>();

	final ArrayList<String> elementsToSkipHeightWidthTextAttributes = new ArrayList<>(
			Arrays.asList("div", "table", "tr"));

	TestcaseExecutionResultVO testCaseExecutionResult = new TestcaseExecutionResultVO();

	@Override
	public TestcaseExecutionResultVO validateKeyword(String... listOfParameters) {
		specFilePath = listOfParameters[0];
		specFileName = listOfParameters[1];
		topLocator = listOfParameters[2];
		htmlControlToCheck = listOfParameters[3];
		uniqueLocatorSequence = listOfParameters[4];
		attributesToCheck = listOfParameters[5];

		if (topLocator == null || topLocator.isEmpty()) {
			topLocator = "@xpath=//body";
		}

		if (specFilePath == null || specFilePath.isEmpty()) {
			logger.error(ERROR_PARAMETERS_LIST);
			testCaseExecutionResult.setMessage(ERROR_PARAMETERS_LIST);
			return testCaseExecutionResult;
		}
		if (specFileName == null || specFileName.isEmpty()) {
			logger.error(ERROR_PARAMETERS_LIST);
			testCaseExecutionResult.setMessage(ERROR_PARAMETERS_LIST);
			return testCaseExecutionResult;
		}
		if (htmlControlToCheck == null || htmlControlToCheck.isEmpty()) {
			htmlControlToCheck = "label,input,select";
		}
		if (attributesToCheck == null || attributesToCheck.isEmpty()) {
			attributesToCheck = "height,width,text";
		}
		if (uniqueLocatorSequence == null || uniqueLocatorSequence.isEmpty()) {
			uniqueLocatorSequence = "name,id,title,fisid,sgid,fis-id,fis-unique-id,ng-model,tabIndex";
		}

		testCaseExecutionResult.setTestData(specFilePath + DELIMITER + specFileName + DELIMITER + topLocator + DELIMITER
				+ htmlControlToCheck + DELIMITER + uniqueLocatorSequence + DELIMITER + attributesToCheck);
		testCaseExecutionResult.setValid(true);
		return testCaseExecutionResult;
	}

	@Override
	public TestcaseExecutionResultVO validateObject(String... listOfParameters) {
		if (webDriver == null) {
			logger.error("ERROR_BROWSER_NOT_INSTANTIATED");
			testCaseExecutionResult.setMessage(ERROR_BROWSER_NOT_INSTANTIATED);
			testCaseExecutionResult.setValid(false);
			return testCaseExecutionResult;
		}

		topPageElement = KeywordUtilities.waitForElementPresentAndEnabledInstance(configurationMap, webDriver,
				topLocator, "1", userName);
		if (topPageElement == null) {

			logger.error("ERROR_REQUIRED_ELEMENT_NOT_FOUND");
			testCaseExecutionResult.setMessage("ERROR_REQUIRED_ELEMENT_NOT_FOUND");
			testCaseExecutionResult.setValid(false);
			return testCaseExecutionResult;
		}

		frameName = KeywordUtilities.getAllSelectedFramesDriver(webDriver);
		testCaseExecutionResult.setValid(true);
		return testCaseExecutionResult;
	}

	@Override
	public TestcaseExecutionResultVO executeScript(String... params) {

		/*
		 * SKIP_IDENTICAL_ELEMENT is used to skip those elements having no
		 * unique attributes. If this is set to "N" , then elements having same
		 * attributes or same innerText will be identified using index, like
		 * 
		 * @xpath=(//tagName)[index]
		 */

		String skipIdenticalElement = configurationMap.get("SKIP_IDENTICAL_ELEMENT");

		if (skipIdenticalElement == null || skipIdenticalElement.equalsIgnoreCase("Y")
				|| skipIdenticalElement.equalsIgnoreCase("TRUE") || skipIdenticalElement.equalsIgnoreCase("YES")) {
			skipIdenticalElement = "Y";
		} else {
			skipIdenticalElement = "N";
		}

		String matchExactTextFilter = configurationMap.get("MATCH_EXACT_TEXT");
		if (matchExactTextFilter != null && (matchExactTextFilter.equalsIgnoreCase("Y")
				|| matchExactTextFilter.equalsIgnoreCase("TRUE") || matchExactTextFilter.equalsIgnoreCase("YES"))) {
			matchExactTextFilter = "Y";
		} else {
			matchExactTextFilter = "N";
		}
		
		
		String positionTolerance = configurationMap.get("POSITION_TOLERANCE");
		if (positionTolerance != null && !positionTolerance.isEmpty()) {
			try{
				Integer.parseInt(positionTolerance);
			}
			catch(Exception e){
				positionTolerance="GALEN_CONFIG_RANGE";
			}
			
		} else {
			positionTolerance="GALEN_CONFIG_RANGE";
		}

		String elementText = "";

		WebElement element = null;

		List<String> tmpAttributeList = null;

		String updatedAttributes = null;

		Map<String, List<String>> mapOfAllValidObjects = new HashMap<String, List<String>>();
		Map<String, List<String>> allObjects = null;

		// Load Javascript into String variable to execute. Javascript returns
		// all elements with object locator and properties.
		StringBuilder sb = null;
		InputStream in = null;
		BufferedReader br = null;
		try {
			in = getClass().getResourceAsStream(
					"/plugin/com/fisglobal/ktt/business/keywords/utility/functions/CreateLayoutSpec.js");
			br = new BufferedReader(new InputStreamReader(in));

			sb = new StringBuilder();
			String line = null;

			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
		} catch (Exception e) {
			logger.error("Failed to load CreateLayoutSpec.js file", e);
			testCaseExecutionResult.setStatus(0);
			testCaseExecutionResult.setMessage("Failed to load CreateLayoutSpec.js file");
			return testCaseExecutionResult;
		} finally {
			try {
				in.close();
				br.close();
			} catch (IOException e) {
				logger.error("Failed to close file handle", e);
			}
		}

		try {
			
			allObjects = (Map<String, List<String>>) ((JavascriptExecutor) webDriver).executeScript(
					"return " + sb.toString(), topPageElement, htmlControlToCheck, uniqueLocatorSequence, topLocator,
					skipIdenticalElement, attributesToCheck, matchExactTextFilter,positionTolerance);
		} catch (Exception e) {
			logger.error(
					"An Error has occured while fetching elements. This may occur because of huge number of objects",
					e);
			testCaseExecutionResult.setStatus(0);
			testCaseExecutionResult.setMessage(
					"An Error has occured while fetching elements. This may occur because of huge number of objects");
			return testCaseExecutionResult;
		}

		
		//System.out.println(allObjects);
		if (allObjects == null) {
			testCaseExecutionResult.setStatus(0);
			testCaseExecutionResult.setMessage("Error occured while fetching elements");
			return testCaseExecutionResult;
		}

		// Iterate through all objects and check if it is displayed and also get
		// text if required.
		for (String locator : allObjects.keySet()) {
			try {
				element = topPageElement.findElement(By.xpath(allObjects.get(locator).get(0)));
				if (topPageElement != null && element.isDisplayed()) {
					updatedAttributes = "";
					if (allObjects.get(locator).get(1).contains("text contains ")
							|| allObjects.get(locator).get(1).contains("text is")) {

						elementText = element.getText();
						if(element.getTagName().toLowerCase().equals("input") && elementText!=null && elementText.trim().isEmpty()){
							elementText = element.getAttribute("value");
						}
						if(elementText.contains("\n")){
							elementText=elementText.replaceAll("\n", "\\\\\\\\n");
						}						
						
						Pattern patternContains = Pattern.compile("(text contains  \")(.*)(\")");
						Pattern patternIs = Pattern.compile("(text is \")(.*)(\")");
						String input = allObjects.get(locator).get(1);
						Matcher m = patternContains.matcher(input);

						if (m.find()) {
							updatedAttributes = m.replaceFirst("$1" + elementText + "$3");
							tmpAttributeList = new ArrayList<String>();
							tmpAttributeList.add(allObjects.get(locator).get(0));
							tmpAttributeList.add(updatedAttributes);

						} else {
							m = patternIs.matcher(input);
							if (m.find()) {
								updatedAttributes = m.replaceFirst("$1" + elementText + "$3");
								tmpAttributeList = new ArrayList<String>();
								tmpAttributeList.add(allObjects.get(locator).get(0));
								tmpAttributeList.add(updatedAttributes);
							}
						}

					}
					if (updatedAttributes.isEmpty()) {
						mapOfAllValidObjects.put(locator, allObjects.get(locator));
					} else {
						mapOfAllValidObjects.put(locator, tmpAttributeList);
					}
				}
			} catch (Exception e) {
				logger.info("Exception occured while finding element", e);
			}
		}

		allObjects.clear();

		// Generate spec file which compatible to galen framework
		StringBuilder fileHeader = new StringBuilder();
		fileHeader.append("#================HEADER STARTS ======================\r\n");
		if (frameName == null || frameName.isEmpty()) {
			fileHeader.append("#THIS IS SAFAL GENERATED SPEC FILE. DO NOT MODIFY HEADER SECTION::::\r\n");
		} else {
			fileHeader.append("#THIS IS SAFAL GENERATED MASTER SPEC FILE. DO NOT MODIFY HEADER SECTION::::\r\n");
		}

		fileHeader.append("#HTMLCONTROLS::::" + htmlControlToCheck + "::::\r\n");
		fileHeader.append("#Attributes::::" + attributesToCheck + "::::\r\n");
		fileHeader.append("#LocatorSequence::::" + uniqueLocatorSequence + "::::\r\n");
		fileHeader.append("#topLocator::::" + topLocator + "::::\r\n");
		fileHeader.append("#================HEADER ENDS======================\r\n\r\n");

		try {
			FileWriter fw = new FileWriter(specFilePath + File.separator + specFileName + ".spec");

			if (frameName == null || frameName.trim().isEmpty()) {
				fw.write(fileHeader.toString());
				writeSpecsToFile(fw, mapOfAllValidObjects);
			} else {

				FileWriter fwMaster = null;
				String[] frameArr = frameName.split(";");
				for (int fcnt = 0; fcnt < frameArr.length; fcnt++) {
					if (frameArr[fcnt].trim().length() > 0) {

						String master = "@objects\r\n" +

								"		" + frameArr[fcnt] + "-frame     css	   iframe#" + frameArr[fcnt] + "\r\n" +

								"==============================================================\r\n" +

								"		" + frameArr[fcnt] + "-frame:\r\n";
						if (fcnt + 1 != frameArr.length) {
							master = master + "    		component frame " + specFileName + "_Master_" + (fcnt + 1)
									+ ".spec";
						} else {
							master = master + "    		component frame " + specFileName + "_details.spec";
						}

						if (fcnt == 0) {
							fwMaster = new FileWriter(specFilePath + File.separator + specFileName + ".spec");
							fwMaster.write(fileHeader.toString());

						} else {
							fwMaster = new FileWriter(
									specFilePath + File.separator + specFileName + "_Master_" + fcnt + ".spec");
						}

						fwMaster.write(master);
						fwMaster.flush();
						fwMaster.close();
					}
				}
				FileWriter fwDetail = new FileWriter(specFilePath + File.separator + specFileName + "_details.spec");
				writeSpecsToFile(fwDetail, mapOfAllValidObjects);

			}

		} catch (IOException e) {
			logger.error("In final catch:An IO eror has occured", e);
			testCaseExecutionResult.setMessage("An IO error has occured:" + e.getMessage());
			testCaseExecutionResult.setStatus(0);
		} finally {
			mapOfAllValidObjects.clear();
		}

		testCaseExecutionResult.setStatus(1);
		return testCaseExecutionResult;
	}

	/*
	 * Writes galen compatible spec file
	 */
	private void writeSpecsToFile(FileWriter fw, Map<String, List<String>> mapOfAllValidObjects) {
		try {
			fw.write("@objects\n");

			for (String name : mapOfAllValidObjects.keySet()) {
				fw.write("		" + name + "		xpath		" + mapOfAllValidObjects.get(name).get(0) + "\n");
			}
			fw.write("\n\n=====================Attribute Section=========================================\n\n");
			for (String name : mapOfAllValidObjects.keySet()) {
				fw.write("\n		" + name + ":\n");
				fw.write(mapOfAllValidObjects.get(name).get(1));
			}
		} catch (IOException e) {
			logger.error("An error has occured while writing to spec file", e);

		} finally {
			try {
				fw.flush();
				fw.close();
			} catch (IOException e) {
				logger.error("An error has occured while closing spec file", e);
			}
		}
	}

}
