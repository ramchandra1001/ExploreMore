package plugin.com.fisglobal.ktt.business.keywords.utility.functions;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fisglobal.ktt.business.keywords.AbstractKeyword;
import com.fisglobal.ktt.business.keywords.Keyword;
import com.fisglobal.ktt.business.keywords.KeywordFactoryImpl;
import com.fisglobal.ktt.model.valueobjects.TestcaseExecutionResultVO;

/**
 * 
 * @author Sameer.Soundankar
 * This keyword is creates galen layout specification file and compares it with existing base specification and generates galen report
 *  
*/


public class CompareLayoutKeyword extends AbstractKeyword {
	private static Logger logger = LogManager.getLogger(CompareLayoutKeyword.class.getName());
	TestcaseExecutionResultVO testCaseExecutionResult = new TestcaseExecutionResultVO();

	private String reportPath = null;
	private String baseSpecPath = null;
	private String extraParameters = null;

	@Override
	public TestcaseExecutionResultVO validateKeyword(String... listOfParameters) {
		reportPath = listOfParameters[0];
		baseSpecPath = listOfParameters[1];
		extraParameters = listOfParameters[3];
		if (reportPath == null || reportPath.isEmpty()) {
			logger.error("Report path not provided");
			testCaseExecutionResult.setMessage("Report path not provided");
			return testCaseExecutionResult;
		}

		if (baseSpecPath == null || baseSpecPath.isEmpty()) {
			logger.error("Base file specs not provided");
			testCaseExecutionResult.setMessage("Base file specs not provided");
			return testCaseExecutionResult;
		}

		if (!baseSpecPath.toUpperCase().endsWith(".SPEC") || !new File(baseSpecPath).exists()) {
			logger.error("Invalid specification file");
			testCaseExecutionResult.setMessage("Invalid specification file");
			return testCaseExecutionResult;
		}

		testCaseExecutionResult.setTestData(
				"Report path:" + reportPath + "Base Spec Path:" + baseSpecPath + "ExtraParams" + extraParameters);
		testCaseExecutionResult.setValid(true);
		return testCaseExecutionResult;
	}

	@Override
	public TestcaseExecutionResultVO validateObject(String... listOfParameters) {
		testCaseExecutionResult.setValid(true);
		return testCaseExecutionResult;
	}

	@Override
	public TestcaseExecutionResultVO executeScript(String... listOfParameters) {
		String htmlControls = "";
		String elementAttributes = "";
		int totalAttributes = 0;
		String uniqueLocators = "";
		String topLocator = "";
		String tmpFileName = "";
		String baseMainFileName = "";
		boolean isTextAttribPresent = false;
		BufferedWriter bwBaseFile = null;

		String frameName = null;
		File tmpFile = null;
		File baseMainFile = null;
		File currentMainFile = null;
		try {
			boolean isMasterFilePresent = false;
			File baseFile = new File(baseSpecPath);
			String fileName = baseFile.getName();
			String folderPath = baseFile.getParent();
			fileName = fileName.split("\\.")[0];

			File tmpFolder = new File(folderPath + File.separator + "tmp");
			if (!tmpFolder.exists()) {
				tmpFolder.mkdir();
			}

			
			//Take backup of existing specification file and get which elements and attributes to verify for current layout
			BufferedReader brMaster = new BufferedReader(new FileReader(baseFile));			
			try {

				brMaster.readLine();
				if (brMaster.readLine().contains("MASTER")) {
					isMasterFilePresent = true;
				} else {
					isMasterFilePresent = false;
				}

				if (isMasterFilePresent) {
					tmpFileName = folderPath + File.separator + "tmp" + File.separator + "SAFAL_BKUP_" + fileName
							+ "_details.spec";
					baseMainFileName = folderPath + File.separator + fileName + "_details.spec";
				} else {
					tmpFileName = folderPath + File.separator + "tmp" + File.separator + "SAFAL_BKUP_" + fileName
							+ ".spec";
					baseMainFileName = folderPath + File.separator + fileName + ".spec";
				}

				htmlControls = brMaster.readLine().split("::::")[1];
				elementAttributes = brMaster.readLine().split("::::")[1];
				totalAttributes = elementAttributes.split(",").length;
				uniqueLocators = brMaster.readLine().split("::::")[1];
				topLocator = brMaster.readLine().split("::::")[1];
			} catch (Exception e) {
				logger.error("Invalid format of spec file provided", e);
				testCaseExecutionResult.setMessage("Invalid format of spec file provided");
				return testCaseExecutionResult;
			} finally {
				brMaster.close();
			}

			
			
			//Using base element and attribute list capture current layout specifications
			Keyword createLayoutSpecs = KeywordFactoryImpl.getInstance().get("CreateLayoutSpec", "false");
			String[] parameters = new String[6];

			parameters[0] = folderPath + File.separator + "tmp";
			parameters[1] = "SAFAL_CURRENT_" + fileName;
			parameters[2] = topLocator;
			parameters[3] = htmlControls;
			parameters[4] = uniqueLocators;
			parameters[5] = elementAttributes;

			TestcaseExecutionResultVO cgsVo = createLayoutSpecs.execute(null, webDriver, configurationMap, null,
					userName, parameters);

			
			
			//Get captured elements from base file and store in List
			if (cgsVo.getStatus() == 1) {
				tmpFile = new File(tmpFileName);
				baseMainFile = new File(baseMainFileName);
				FileUtils.copyFile(baseMainFile, tmpFile);

				List<String> baseFileContents = readLines(baseMainFile);

				List<String> baseFileElements = new ArrayList<String>();
				boolean isObjectRegionStarts = false;
				int elementRegion = 0;
				for (String line : baseFileContents) {
					if (line.contains("@objects")) {
						isObjectRegionStarts = true;
						elementRegion++;
						baseFileElements.add("@objects");
					}
					if (isObjectRegionStarts && line.contains("=========")) {
						elementRegion++;
					}
					if (elementRegion == 1 && !line.contains("@objects") && !line.isEmpty()) {
						baseFileElements.add(line.split("\\s+")[1]);
					} else if (elementRegion > 1) {
						break;
					}
				}

				
				
				
				
				// compare base and current file and store extra elements in current page, in List					
				List<String> extraElements = new ArrayList<String>();
				List<String> extraElementsSpec = new ArrayList<String>();
				List<String> extraAtrributes = new ArrayList<String>();
				
				if (isMasterFilePresent) {
					currentMainFile = new File(folderPath + File.separator + "tmp" + File.separator + "SAFAL_CURRENT_"
							+ fileName + "_details.spec");
				} else {
					currentMainFile = new File(folderPath + File.separator + "tmp" + File.separator + "SAFAL_CURRENT_"
							+ fileName + ".spec");
				}
				
				List<String> currentFileContents = readLines(currentMainFile);

				bwBaseFile = new BufferedWriter(new FileWriter(baseMainFile));				

				String currentFileLineText = "";
				String attributes = "";
				isObjectRegionStarts = false;
				elementRegion = 0;
				int nextLineCount=0;
				String setTextToExtraElement = "";
				
				for (int currentLineCount = 0; currentLineCount < currentFileContents.size(); currentLineCount++) {
					currentFileLineText = currentFileContents.get(currentLineCount);
					if (currentFileLineText.contains("@objects")) {
						isObjectRegionStarts = true;
						elementRegion++;
					}
					if (isObjectRegionStarts && currentFileLineText.contains("=========")) {
						elementRegion++;
					}

					if (elementRegion == 1 && !currentFileLineText.contains("@objects")
							&& !currentFileLineText.isEmpty()
							&& !baseFileElements.contains(currentFileLineText.split("\\s+")[1])) {
						extraElements.add(currentFileLineText.split("\\s+")[1]);
						extraElementsSpec.add(currentFileLineText);
					}

					attributes = "";
					if (elementRegion > 1 && !currentFileLineText.isEmpty() && extraElements.contains(
							currentFileLineText.trim().substring(0, currentFileLineText.trim().length() - 1))) {
						attributes = currentFileContents.get(currentLineCount) + "\n";						
						nextLineCount = currentLineCount + 1;
						setTextToExtraElement = "";
						isTextAttribPresent = false;

						for (int cnt = 0; cnt < totalAttributes; cnt++) {
							if (currentFileContents.get(nextLineCount).contains("text is")
									|| currentFileContents.get(nextLineCount).contains("text matches")) {
								setTextToExtraElement = "    		text is \"NEW ELEMENT FOUND\"\n";
								isTextAttribPresent = true;
							}
							if (currentFileContents.get(nextLineCount).trim().isEmpty()) {
								break;
							}
							if (setTextToExtraElement.isEmpty()) {
								attributes += currentFileContents.get(nextLineCount) + "\n";
							} else {
								attributes += setTextToExtraElement + "\n";
								isTextAttribPresent = true;
								setTextToExtraElement = "";
							}
							nextLineCount++;
						}
						if (!isTextAttribPresent) {
							attributes += "    		text is \"NEW ELEMENT FOUND\"\n";
						}

						extraAtrributes.add(attributes);
						currentLineCount = nextLineCount;

					}
				}

				// Create new specification file comprising of base and extra current elements in current layout 
				isObjectRegionStarts = false;
				elementRegion = 0;

				boolean isAddedExtraObjects = false;
				for (String line : baseFileContents) {
					bwBaseFile.write(line + "\r\n");
					if (line.contains("@objects")) {
						isObjectRegionStarts = true;
						elementRegion++;
					}
					if (isObjectRegionStarts && line.contains("=========")) {
						elementRegion++;
					}

					if (elementRegion == 1 && !isAddedExtraObjects) {
						isAddedExtraObjects = true;
						for (String extraEle : extraElementsSpec) {
							bwBaseFile.write(extraEle + "\r\n");
						}
					}

				}

				for (String extraAttrib : extraAtrributes) {
					bwBaseFile.write(extraAttrib + "\r\n");
				}
				bwBaseFile.close();
			} else {
				logger.error("Unable to captured current layout specification");
				testCaseExecutionResult.setMessage("An error has occured while capturing current layout spec");
				testCaseExecutionResult.setStatus(0);
				return testCaseExecutionResult;
			}

			
			//Pass newly created specification to FindLayoutBugs keyword to generate Galen report
			frameName = configurationMap.get("FrameName");

			Keyword selectFrame = KeywordFactoryImpl.getInstance().get("SelectFrame", "false");
			String[] sfParameters = new String[3];

			sfParameters[0] = "NULL";
			sfParameters[1] = "1";
			sfParameters[2] = "False";
			selectFrame.execute(null, webDriver, configurationMap, null, userName, sfParameters);

			Keyword FindLayoutBugs = KeywordFactoryImpl.getInstance().get("FindLayoutBugs", "false");
			String[] layOutparameters = new String[6];
			layOutparameters[0] = reportPath;
			layOutparameters[1] = baseSpecPath;
			layOutparameters[3] = extraParameters;

			TestcaseExecutionResultVO flbVo = FindLayoutBugs.execute(null, webDriver, configurationMap, null, userName,
					layOutparameters);
			testCaseExecutionResult.setMessage(flbVo.getMessage());
			testCaseExecutionResult.setStatus(flbVo.getStatus());

			//Copy spec file used for comparison in tmp folder and restore original base file.
			try {
				File diffFile = null;
				if (isMasterFilePresent) {
					diffFile = new File(folderPath + File.separator + "tmp" + File.separator + "SAFAL_Diff_" + fileName
							+ "_details.spec");
				} else {
					diffFile = new File(
							folderPath + File.separator + "tmp" + File.separator + "SAFAL_Diff_" + fileName + ".spec");
				}

				FileUtils.copyFile(baseMainFile, diffFile);
				FileUtils.copyFile(tmpFile, baseMainFile);
				tmpFile.delete();

			} catch (Exception e) {
				logger.error("Unable to delete files", e);
			}

		} catch (Exception e) {
			logger.error("An error has occured in CompareLayout", e);
		} finally {
			if (bwBaseFile != null) {
				try {
					bwBaseFile.close();
				} catch (IOException e1) {
					logger.error("Unable to close buffered writer", e1);
				}
			}
			
			configurationMap.put("FrameName", frameName);
			testCaseExecutionResult.setConfigUpdate(true);
		}
		return testCaseExecutionResult;
	}

	private List<String> readLines(File fileName) throws IOException {
		List<String> fileLines = new ArrayList<String>();
		BufferedReader brSpecFile = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
		
		String lineText = "";
		while ((lineText = brSpecFile.readLine()) != null) {
			fileLines.add(lineText);
		}
		brSpecFile.close();
		return fileLines;
	}
}
