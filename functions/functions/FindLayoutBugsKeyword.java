package plugin.com.fisglobal.ktt.business.keywords.utility.functions;

import static com.fisglobal.ktt.business.keywords.ErrorMessages.ERROR_PARAMETERS_LIST;
import static com.fisglobal.ktt.view.config.KTTGuiConstants.DELIMITER;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.galenframework.api.Galen;
import com.galenframework.config.GalenConfig;
import com.galenframework.reports.GalenTestAggregatedInfo;
import com.galenframework.reports.GalenTestInfo;
import com.galenframework.reports.HtmlReportBuilder;
import com.galenframework.reports.json.JsonReportBuilder;
import com.galenframework.reports.json.ReportOverview;
import com.galenframework.reports.model.LayoutReport;
import com.galenframework.reports.model.LayoutSection;
import com.galenframework.speclang2.pagespec.SectionFilter;
import com.googlecode.fightinglayoutbugs.FightingLayoutBugs;
import com.googlecode.fightinglayoutbugs.LayoutBug;
import com.googlecode.fightinglayoutbugs.WebPage;
import com.fisglobal.ktt.business.keywords.AbstractKeyword;
import com.fisglobal.ktt.business.keywords.KeywordUtilities;
import com.fisglobal.ktt.business.keywords.MyWait;
import com.fisglobal.ktt.model.config.InitSAFALProperties;
import com.fisglobal.ktt.model.valueobjects.TestcaseExecutionResultVO;

//import net.mindengine.galen.reports.TestStatistic;

public class FindLayoutBugsKeyword extends AbstractKeyword {

	TestcaseExecutionResultVO testCaseExecutionResult = new TestcaseExecutionResultVO();
	/**
	 * This is logger object used to log keyword actions into a log file
	 */
	private static Logger logger = LogManager.getLogger(FindLayoutBugsKeyword.class.getName());

	/**
	 * This is the excel fileName
	 */
	private String fileName = null;
	private String galenSpecFile = null;
	private String extraParameters = null;
	private String testName = null;
	// private String generateConsolidatedReport;
	// private static List<GalenTestInfo> reportList = new ArrayList<>();
	private static List<String> browserDetailsList = Collections.synchronizedList(new ArrayList<String>());
	private static List<GalenTestInfo> reportList = Collections.synchronizedList(new LinkedList<GalenTestInfo>());
	private HashMap<String, String> extraParamsMap = new HashMap<String, String>();
	/**
	 * This is the excel file stored path
	 */

	private String filePath = null;

	/**
	 * This method validates the keyword
	 * 
	 * @param params
	 *            contains list of parameters - fileName -filePath
	 * 
	 * @return ExecutionResults containing step execution status(pass/fail),
	 *         exact error message according to failure
	 */

	@Override
	public TestcaseExecutionResultVO validateKeyword(String... params) {
		logger.info("Inside FindLayoutBugsKeyword validateKeyword ");

		if (params != null) {
			filePath = params[0];
			fileName = params[1];
			// generateConsolidatedReport = params[2];
			// generateConsolidatedReport="Y";
			extraParameters = params[3];
			if (extraParameters != null && !extraParameters.isEmpty()) {
				extraParameters = extraParameters.replaceAll("\"", "");
				for (String param : extraParameters.split(";")) {
					extraParamsMap.put(param.split("=")[0].toLowerCase(), param.split("=")[1]);
				}
			}

			if (extraParamsMap.get("size") == null) {
				extraParamsMap.put("size", "1600x900");
			}

			testName = params[4];

			if (fileName != null && !fileName.isEmpty() && fileName.endsWith(".spec")) {
				galenSpecFile = fileName;

				if (!new File(galenSpecFile).exists()) {
					logger.error("Galan Spec file " + galenSpecFile + " not found!");
					testCaseExecutionResult.setMessage("Galan Spec file " + galenSpecFile + " not found!");
					return testCaseExecutionResult;
				}
			}

		} else {
			logger.error("Insufficient Parameters!");
			testCaseExecutionResult.setMessage(ERROR_PARAMETERS_LIST);
			return testCaseExecutionResult;
		}

		if (KeywordUtilities.isEmptyString(fileName)) {
			fileName = "Layout_Bugs";
		}

		if (KeywordUtilities.isEmptyString(filePath)) {
			InitSAFALProperties initSAFALProperties = InitSAFALProperties.getInstance();
			String resultStorePath = initSAFALProperties.resultStorePath;

			filePath = resultStorePath;
		}

		extraParamsMap.put("htmlreport", filePath + "/temp");

		testCaseExecutionResult.setTestData(filePath + DELIMITER + fileName + DELIMITER + extraParameters);

		testCaseExecutionResult.setValid(true);
		return testCaseExecutionResult;
	}

	/**
	 * This method validates the object on the browser
	 * 
	 * @return ExecutionResults containing step execution status(pass/fail),
	 *         exact error message according to failure
	 */

	@Override
	public TestcaseExecutionResultVO validateObject(String... params) {
		logger.info("Inside FindLayoutBugsKeyword validateObject ");
		return KeywordUtilities.noValidationRequired(params[0]);
	}

	/**
	 * This method runs after all the validation has been successful
	 * 
	 * @return ExecutionResults containing step execution status(pass/fail),
	 *         exact error message according to failure
	 */

	@Override
	public TestcaseExecutionResultVO executeScript(String... params) {
		logger.info("Inside FindLayoutBugsKeyword executeScript :");

		try {
			System.setProperty("resultsDir", filePath);

			if (galenSpecFile == null) {
				return executeScript();
			} else {
				return executeGalanScript();
			}

		} catch (Exception e) {
			logger.error(e.getMessage());
			testCaseExecutionResult.setMessage(e.getMessage());
			return testCaseExecutionResult;

		}
	}

	public TestcaseExecutionResultVO executeScript() {
		logger.info("Inside executeScript");

		WebPage webPage = new WebPage(webDriver);
		FightingLayoutBugs flb = new FightingLayoutBugs();

		final Collection<LayoutBug> layoutBugs = flb.findLayoutBugsIn(webPage);

		if (layoutBugs.size() == 0) {
			testCaseExecutionResult.setStatus(1);
			return testCaseExecutionResult;

		} else {
			int colCounter = -1;

			XSSFWorkbook writableWorkbook = new XSSFWorkbook();

			XSSFSheet writableSheet = writableWorkbook.createSheet("LayoutBugs");

			XSSFRow headerRow = writableSheet.createRow((short) 0);

			headerRow.setHeightInPoints((float) (1.3 * writableSheet.getDefaultRowHeightInPoints()));

			XSSFCellStyle cellStyleHeader = createCellStyleHeader(writableWorkbook);

			XSSFCellStyle cellStyle = createCellStyle(writableWorkbook);

			XSSFCell sr_No = headerRow.createCell(++colCounter);
			sr_No.setCellValue(new XSSFRichTextString("Sr.No."));
			sr_No.setCellStyle(cellStyleHeader);

			XSSFCell bug_Desc = headerRow.createCell(++colCounter);
			bug_Desc.setCellValue(new XSSFRichTextString("Bug Description"));
			bug_Desc.setCellStyle(cellStyleHeader);

			colCounter = -1;

			writableSheet.setColumnWidth(++colCounter, 2000);
			writableSheet.setColumnWidth(++colCounter, 30000);

			int i = 1;

			for (LayoutBug bug : layoutBugs) {
				colCounter = -1;

				XSSFRow row = writableSheet.createRow((short) i);

				XSSFCell sr_no = row.createCell(++colCounter);
				sr_no.setCellValue(i);
				sr_no.setCellStyle(cellStyle);

				XSSFCell bug_desc = row.createCell(++colCounter);
				bug_desc.setCellValue(bug + "");
				bug_desc.setCellStyle(cellStyle);

				i++;
			}

			String location = getFileStoredLoaction(filePath, fileName);
			scriptFileWrite(writableWorkbook, location);

			testCaseExecutionResult.setMessage("Refer " + location + " location to view the report");
			return testCaseExecutionResult;

		}
	}

	public TestcaseExecutionResultVO executeGalanScript() throws ParseException, Exception {
		logger.info("Inside executeGalanScript ");

		boolean isFailed = false;

		String isClearLayoutResult = configurationMap.get("ClearLayoutResultCache");

		String reportFileName = filePath + "\\Report_Consolidated.html";

		if (isClearLayoutResult == null || (isClearLayoutResult.toUpperCase().equals("Y")
				|| isClearLayoutResult.toUpperCase().equals("TRUE"))) {
			reportList.clear();
			browserDetailsList.clear();
			configurationMap.put("ClearLayoutResultCache", "N");

			File consolidatedReportFile = new File(reportFileName);
			Path consolidatedReportFilePath = consolidatedReportFile.toPath();

			synchronized (this) {
				if (consolidatedReportFile.exists()) {
					BasicFileAttributes attr = Files.readAttributes(consolidatedReportFilePath,
							BasicFileAttributes.class);

					File destDir = new File(
							filePath + "\\Report_Consolidated" + "_" + new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss")
									.format(attr.lastModifiedTime().toMillis()).toString());
					destDir.mkdir();
					File srcDir = new File(filePath + "\\temp");

					FileUtility.move(srcDir, new File(destDir.getAbsoluteFile() + "\\temp"));

					File destFile = new File(destDir.getAbsolutePath() + "\\Report_Consolidated.html");

					FileUtility.move(consolidatedReportFile, destFile);

				}
			}

		}

		List<String> includedTags = new ArrayList<>();
		if (extraParamsMap.get("include") != null) {
			for (String tags : extraParamsMap.get("include").split(",")) {
				includedTags.add(tags);
			}
		}

		List<String> excludedTags = new ArrayList<>();
		if (extraParamsMap.get("exclude") != null) {
			for (String tags : extraParamsMap.get("exclude").split(",")) {
				excludedTags.add(tags);
			}
		}

		SectionFilter sectionFilter = new SectionFilter(includedTags, excludedTags);

		try {

			Properties properties = new Properties();

			if (extraParamsMap.get("config") != null) {
				GalenConfig.reloadConfigFromPath(extraParamsMap.get("config"));
			}

			try {
				if (extraParamsMap.get("size") != null) {
					int width = Integer.parseInt(extraParamsMap.get("size").split("x")[0]);
					int height = Integer.parseInt(extraParamsMap.get("size").split("x")[1]);
					webDriver.manage().window().setSize(new Dimension(width, height));
				} else {
					webDriver.manage().window().setSize(new Dimension(1600, 900));
				}
			} catch (Exception e) {
				webDriver.manage().window().setSize(new Dimension(1600, 900));
			}

			LayoutReport layoutReport = Galen.checkLayout(webDriver, galenSpecFile, sectionFilter, properties);

			// FOllowing section is added as per user requirement to show extra
			// parameters passed in galen report
			LayoutSection paramLayoutSection = new LayoutSection();
			paramLayoutSection.setName("Command line parameters passed:" + extraParameters);
			List<LayoutSection> tmpLstLayoutSection = new ArrayList<LayoutSection>();
			tmpLstLayoutSection.add(paramLayoutSection);
			tmpLstLayoutSection.addAll(layoutReport.getSections());
			layoutReport.setSections(tmpLstLayoutSection);

			if (testName == null || testName.isEmpty()) {
				testName = galenSpecFile;
			}

			String browserName = null;
			String browserVersion = null;
			try {
				browserName = (String) ((RemoteWebDriver) webDriver).getCapabilities().getBrowserName();
				browserVersion = (String) ((RemoteWebDriver) webDriver).getCapabilities().getVersion();
			} catch (Exception e) {
				if (browserName == null) {
					browserName = "";
				}
				if (browserVersion == null) {
					browserVersion = "";
				}

			}

			browserName = browserName.toUpperCase();

			GalenTestInfo test = GalenTestInfo.fromString(testName);

			test.getReport().layout(layoutReport, "Test executed on " + browserName);
			isFailed = test.isFailed();
			reportList.add(test);
			logger.info("Adding " + browserName + "::" + browserVersion);
			browserDetailsList.add(new String(browserName + "::" + browserVersion));

			new HtmlReportBuilder().build(reportList, extraParamsMap.get("htmlreport"));
		} catch (IOException e) {
			logger.error("IOException occured:" + e);
			testCaseExecutionResult.setMessage("IO error occured:" + e.getMessage());
			return testCaseExecutionResult;
		}

		// ================================================================================================================

		
		logger.info("Inside executeGalanScript: generating consolidated report "+browserDetailsList);
		// reportList.addAll(testInfos);
		String reportHTML = null;
		
		synchronized (this) {
			reportHTML = generateStringReportHTML(reportList);
		}
		 

		FileWriter writeReport = new FileWriter(reportFileName);
		writeReport.write(reportHTML);
		writeReport.close();

		logger.info("Inside executeGalanScript: End ");

		if (isFailed) {
			testCaseExecutionResult.setStatus(0);
			testCaseExecutionResult.setMessage("Refer " + reportFileName + " location to view the report");
			return testCaseExecutionResult;
		} else {
			testCaseExecutionResult.setStatus(1);
			return testCaseExecutionResult;
		}
	}

	/**
	 * This method create file stored path
	 * 
	 * @param filePath
	 *            - file stored path
	 * @param fileName
	 * @return file stored loaction
	 */

	private String getFileStoredLoaction(String filePath, String fileName) {
		if (!filePath.endsWith("/")) {
			filePath += "/";
		}

		if (!new File(filePath).exists()) {
			new File(filePath).mkdir();
		}

		Date date = new Date(System.currentTimeMillis());

		String[] datArr = date.toString().split(" ");
		String[] time = datArr[3].split(":");
		String timeStamp = datArr[0] + "_" + datArr[1] + "_" + datArr[2] + "_" + datArr[5] + "_ " + time[0] + "_"
				+ time[1] + "_" + time[2];

		if (fileName.endsWith(".xlsx")) {
			fileName = fileName.split(".xlsx")[0];

		}

		return filePath + fileName + "_" + timeStamp + ".xlsx";
	}

	/**
	 * This method writes workbook to excel
	 * 
	 * @param writableWorkbook
	 *            excel workbook instance
	 * @param location
	 *            - file stored loaction
	 */

	private void scriptFileWrite(XSSFWorkbook writableWorkbook, String location) {
		try {
			FileOutputStream stream = new FileOutputStream(location);
			writableWorkbook.write(stream);
			stream.close();
			MyWait myWait = new MyWait();
			myWait.waitFor(1200, userName);

			// System.out.println("file created sucessfully : "+location);

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * This method is create header style.
	 * 
	 * @param writableWorkbook
	 *            excel workbook instance
	 * @return cellStyleHeader
	 */

	private XSSFCellStyle createCellStyleHeader(XSSFWorkbook writableWorkbook) {
		XSSFCellStyle cellStyleHeader = writableWorkbook.createCellStyle();
		cellStyleHeader.setAlignment(HorizontalAlignment.CENTER);
		cellStyleHeader.setVerticalAlignment(VerticalAlignment.BOTTOM);
		cellStyleHeader.setFillForegroundColor(IndexedColors.AQUA.index);
		cellStyleHeader.setFillPattern(FillPatternType.SOLID_FOREGROUND);

		XSSFFont font = writableWorkbook.createFont();
		font.setFontHeightInPoints((short) 10);
		font.setFontName(HSSFFont.FONT_ARIAL);
		font.setBold(true);
		
		cellStyleHeader.setFont(font);
		cellStyleHeader.setBorderTop(BorderStyle.THICK);
		cellStyleHeader.setBorderLeft(BorderStyle.THIN);
		cellStyleHeader.setBorderBottom(BorderStyle.THIN);
		cellStyleHeader.setBorderRight(BorderStyle.THIN);
		cellStyleHeader.setWrapText(true);

		return cellStyleHeader;

	}

	/**
	 * This method is create cell style.
	 * 
	 * @param writableWorkbook
	 *            excel workbook instance
	 * @return cellStyle
	 */

	private XSSFCellStyle createCellStyle(XSSFWorkbook writableWorkbook) {

		XSSFCellStyle cellStyle = writableWorkbook.createCellStyle();

		cellStyle.setAlignment(HorizontalAlignment.LEFT);
		cellStyle.setVerticalAlignment(VerticalAlignment.BOTTOM);
		cellStyle.setBorderTop(BorderStyle.THIN);
		cellStyle.setBorderLeft(BorderStyle.THIN);
		cellStyle.setBorderBottom(BorderStyle.THIN);
		cellStyle.setBorderRight(BorderStyle.THIN);
		cellStyle.setWrapText(true);

		return cellStyle;
	}

	private synchronized static String generateStringReportHTML(List<GalenTestInfo> testInfos) {

		JsonReportBuilder jsonBuilder = new JsonReportBuilder();
		ReportOverview reportOverview = jsonBuilder.createReportOverview(testInfos);

		String browserName = "";
		String browserVersion = "";
		String reportHTML = "<html>\n" + "    <head>\n" + "        <title>Galen Reports</title>\n"
				+ "        <link rel=\"stylesheet\" type=\"text/css\" href=\"temp\\report.css\"></link>\n"
				+ "        <link rel=\"stylesheet\" type=\"text/css\" href=\"temp\\tablesorter.css\"></link>\n"
				+ "        <script src=\"temp\\jquery-1.11.2.min.js\"></script>\n"
				+ "        <script src=\"temp\\handlebars-v2.0.0.js\"></script>\n"
				+ "        <script src=\"temp\\tablesorter.js\"></script>\n"
				+ "        <script src=\"temp\\galen-report.js\"></script>\n" + "        <script>\n"
				+ "var reportData = {\n" + "  \"tests\" : [ ";
		int i = 0;
		for (GalenTestInfo galenTestInfo : testInfos) {
			// System.out.println(galenTestInfo.getName() + " " +
			// galenTestInfo.isFailed() + " " + galenTestInfo.getStartedAt() + "
			// " + galenTestInfo.getReport().toString());
			browserName = browserDetailsList.get(i).split("::")[0];
			browserVersion = browserDetailsList.get(i).split("::")[1];

			GalenTestAggregatedInfo aggregatedInfo = reportOverview.getTests().get(i);
			i++;
			com.galenframework.reports.TestStatistic statistic = galenTestInfo.getReport().fetchStatistic();

			reportHTML += "{\n" + "    \"name\" : \"" + galenTestInfo.getName().replaceAll("\\\\", "\\\\\\\\") + "\",\n"
					+ "    \"browserName\" : \"" + browserName + "\",\n"+ "    \"browserVersion\" : \"" + browserVersion + "\",\n"							
					+ "    \"startedAt\" : " + galenTestInfo.getStartedAt().getTime() + ",\n" + "    \"endedAt\" : "
					+ galenTestInfo.getEndedAt().getTime() + ",\n" + "    \"failed\" : " + galenTestInfo.isFailed()
					+ ",\n" + "    \"statistic\" : {\n" + "      \"passed\" : " + statistic.getPassed() + ",\n"
					+ "      \"errors\" : " + statistic.getErrors() + ",\n" + "      \"warnings\" : "
					+ statistic.getWarnings() + ",\n" + "      \"total\" : " + statistic.getTotal() + "\n" + "    },\n"
					+ "    \"testId\" : \"" + aggregatedInfo.getTestId() + "\",\n" + "    \"failed\" : "
					+ galenTestInfo.isFailed() + ",\n" + "    \"duration\" : "
					+ (galenTestInfo.getEndedAt().getTime() - galenTestInfo.getStartedAt().getTime()) + "\n" + "  },\n";
		}

		reportHTML += "]\n" + "};\n" + "\n" + "        </script>\n" + "        <script>\n"
				+ "            $(function () {\n" + "                var galenReport = createGalenTestOverview();\n"
				+ "                galenReport.renderTestsTable(\"tests-table\", reportData);\n"
				+ "                galenReport.renderGroupsTable(\"groups-table\", reportData);\n" + "\n"
				+ "                window.onhashchange = function () {\n"
				+ "                    galenReport.handleHash(window.location.hash.substr(1));\n"
				+ "                };\n" + "\n"
				+ "                galenReport.handleHash(window.location.hash.substr(1));\n" + "            });\n"
				+ "        </script>\n" + "    </head>\n" + "    <body>\n" + "\n" + "\n"
				+ "        <div class=\"tests-overview\">\n" + "            <h2>Layout Test Report</h2>\n"
				+ "            <div class=\"tabs\">\n"
				+ "                <a class=\"tab tab-tests\" href=\"#tests\">Tests</a>\n"
				+ "                <a class=\"tab tab-groups\" href=\"#groups\">Groups</a>\n" + "            </div>\n"
				+ "            <div id=\"tests-table\">\n" + "            </div>\n"
				+ "            <div id=\"groups-table\">\n" + "            </div>\n" + "\n"
				+ "            <script id=\"tests-table-tpl\" type=\"text/x-handlebars-template\">\n"
				+ "                <table class=\"tests tablesorter\">\n" + "                    <thead>\n"
				+ "                        <tr>\n" + "                            <th>Test</th>\n"
				+ "							 <th>Browser Name</th>\n"
				+ "							 <th>Browser Version</th>\n"
				+ "                            <th>Passed</th>\n" + "                            <th>Failed</th>\n"
				+ "                            <th>Warnings</th>\n" + "                            <th>Total</th>\n"
				+ "                            <th>Groups</th>\n" + "                            <th>Started</th>\n"
				+ "                            <th>Duration</th>\n" + "                            <th></th>\n"
				+ "                        </tr>\n" + "                    </thead>\n" + "                    <tbody>\n"
				+ "                        {{#each tests}}\n"
				+ "                        <tr data-groups=\"{{groups}}\">\n"
				+ "                            <td class=\"suite-link\">\n"
				+ "                                <a href=\"temp/{{testId}}.html\">{{name}}</a>\n"
				+ "                            </td>\n"
				+ "                            <td class=\"browser name\">{{browserName}}</td>\n"
				+ "          				   <td class=\"browser version\">{{browserVersion}}</td>\n"
				+ "                            <td class=\"status passed\">{{statistic.passed}}</td>\n"
				+ "                            <td class=\"status failed\">{{statistic.errors}}</td>\n"
				+ "                            <td class=\"status warnings\">{{statistic.warnings}}</td>\n"
				+ "                            <td class=\"status total\">{{statistic.total}}</td>\n"
				+ "                            <td class=\"tags\">{{formatGroupsPretty groups}}</td>\n"
				+ "                            <td class=\"time\">{{formatDateTime startedAt}}</td>\n"
				+ "                            <td class=\"time\">{{formatDurationHumanReadable duration}}</td>\n"
				+ "                            <td class=\"progressbar\">\n"
				+ "                                {{renderProgressBar statistic}}\n"
				+ "                            </td>\n" + "                        </tr>\n"
				+ "                        {{/each}}\n" + "                    </tbody>\n"
				+ "                </table>\n" + "            </script>\n"
				+ "            <script id=\"groups-table-tpl\" type=\"text/x-handlebars-template\">\n"
				+ "                <table class=\"groups tablesorter\">\n" + "                    <thead>\n"
				+ "                        <tr>\n" + "                            <th>Group</th>\n"
				+ "                            <th>Passed</th>\n" + "                            <th>Failed</th>\n"
				+ "                            <th>Tests</th>\n" + "                            <th></th>\n"
				+ "                        </tr>\n" + "                    </thead>\n" + "                    <tbody>\n"
				+ "                        {{#each this}}\n" + "                        <tr>\n"
				+ "                            <td class=\"group-link\">\n"
				+ "                                <a href=\"#tests|grouped|{{name}}\">{{name}}</a>\n"
				+ "                            </td>\n"
				+ "                            <td class=\"status passed\">{{passed}}</td>\n"
				+ "                            <td class=\"status failed\">{{failed}}</td>\n"
				+ "                            <td class=\"status total\">{{tests}}</td>\n"
				+ "                            <td class=\"progressbar\">\n"
				+ "                                {{renderGroupsProgressBar this}}\n"
				+ "                            </td>\n" + "                        </tr>\n"
				+ "                        {{/each}}\n" + "                    </tbody>\n"
				+ "                </table>\n" + "            </script>\n" + "        </div>\n" + "    </body>\n"
				+ "</html>\n";

		return reportHTML;
	}
}
