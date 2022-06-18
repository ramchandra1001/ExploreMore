package com.sungard.ktt.business.keywords;

import static com.sungard.ktt.business.keywords.ErrorMessages.ERROR_BROWSER_NOT_INSTANTIATED;
import static com.sungard.ktt.business.keywords.ErrorMessages.ERROR_PARAMETERS_LIST;
import static com.sungard.ktt.business.keywords.ErrorMessages.ERROR_TABLE_NOT_FOUND;
import static com.sungard.ktt.business.keywords.ErrorMessages.ERROR_TABLE_NOT_FOUND_XPATH;
import static com.sungard.ktt.business.keywords.ErrorMessages.NAGATIVE_TEST_SCENARIO;
import static com.sungard.ktt.view.config.KTTGuiConstants.CLASS;
import static com.sungard.ktt.view.config.KTTGuiConstants.COLUMN_VALUE_SEPARATOR;
import static com.sungard.ktt.view.config.KTTGuiConstants.DEFAULT_OBJECT_WAIT_TIME;
import static com.sungard.ktt.view.config.KTTGuiConstants.EMPTY_STRING;
import static com.sungard.ktt.view.config.KTTGuiConstants.ENV_PARA;
import static com.sungard.ktt.view.config.KTTGuiConstants.EXPECTED_STATUS_NOTFOUND;
import static com.sungard.ktt.view.config.KTTGuiConstants.FAIL;
import static com.sungard.ktt.view.config.KTTGuiConstants.FAIL_STEP_STATUS;
import static com.sungard.ktt.view.config.KTTGuiConstants.FRAME_FLAG_ENV;
import static com.sungard.ktt.view.config.KTTGuiConstants.FRAME_NAME_ENV;
import static com.sungard.ktt.view.config.KTTGuiConstants.ID;
import static com.sungard.ktt.view.config.KTTGuiConstants.NAME;
import static com.sungard.ktt.view.config.KTTGuiConstants.OBJECT_RENDERING_ITERATION_WAIT_VARIABLE;
import static com.sungard.ktt.view.config.KTTGuiConstants.OBJECT_SPECIFIER;
import static com.sungard.ktt.view.config.KTTGuiConstants.OBJECT_WAIT_VARIABLE;
import static com.sungard.ktt.view.config.KTTGuiConstants.PASS;
import static com.sungard.ktt.view.config.KTTGuiConstants.PASS_STEP_STATUS;
import static com.sungard.ktt.view.config.KTTGuiConstants.SEMICOLON;
import static com.sungard.ktt.view.config.KTTGuiConstants.WDLINK;
import static com.sungard.ktt.view.config.KTTGuiConstants.XPATH;
import static com.sungard.ktt.view.config.KTTGuiConstants.XPATH_LOCATOR;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.FileWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchWindowException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;

import com.sungard.ktt.business.keywords.hybrid.DataSheetUtility;
import com.sungard.ktt.exception.AutomationException;
import com.sungard.ktt.model.config.InitSAFALProperties;
import com.sungard.ktt.model.util.TimeWatcher;
import com.sungard.ktt.model.valueobjects.TestcaseExecutionResultVO;
import com.sungard.ktt.view.config.KTTGuiConstants;
import com.sungard.ktt.view.config.KTTGuiConstants.ScriptStatus;
import com.sungard.ktt.view.encr_decr.ENC_DEC;
import com.sungard.ktt.web.util.SAFALUserSession;
//import com.thoughtworks.selenium.webdriven.WebDriverBackedSelenium;

public class KeywordUtilities {
	private static ENC_DEC enc_dec = new ENC_DEC();
	Logger logger = Logger.getLogger("Thread" + Thread.currentThread().getName());
	public static boolean CleanUp = true; // For clean up
	public static boolean DoneOneOnce = false; // For Flags Table
	public static boolean DoneTwoOnce = false; // For calculation History Table
	public static int counter = 0;

	private static String ENV_TAG = "<ENV>";
	private static String COL_TAG = "<COL>";
	private static String ENV_TAG_END = "</ENV>";
	private static String COL_TAG_END = "</COL>";
	private final static char[] ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"
			.toCharArray();

	private static int[] toInt = new int[128];

	static {
		for (int i = 0; i < ALPHABET.length; i++) {
			toInt[ALPHABET[i]] = i;
		}
	}

	public static String toHex(int r, int g, int b) {
		return "#" + toBrowserHexValue(r) + toBrowserHexValue(g) + toBrowserHexValue(b);
	}

	private static String toBrowserHexValue(int number) {
		StringBuilder builder = new StringBuilder(Integer.toHexString(number & 0xff));
		while (builder.length() < 2) {
			builder.append("0");
		}
		return builder.toString().toUpperCase();
	}

	public static TestcaseExecutionResultVO setEnvVariableValue(Map<String, String> configurationMap,
			Map<String, Map<String, Map<String, String>>> workBookMap,
			TestcaseExecutionResultVO testCaseExecutionResult, String sWorkSheet, Logger loggerKeyword,
			String sColumnName, String sRow, String sEnvVariable, String sExtractedValue) {
		if (sExtractedValue == null) {
			sExtractedValue = EMPTY_STRING;
		}
		if (sEnvVariable.toUpperCase().startsWith("#ENV#")) {
			sEnvVariable = sEnvVariable.substring(5, sEnvVariable.length());
			if (sEnvVariable.toUpperCase().endsWith("#ENV#")) {
				sEnvVariable = sEnvVariable.substring(0, sEnvVariable.length() - 5);
			}
		}

		if (KeywordUtilities.isEmptyString(sWorkSheet)) {
//			configurationMap.put(sEnvVariable, sExtractedValue);

			KeywordUtilities.updateConfigurationMap(configurationMap, sEnvVariable, sExtractedValue);
			testCaseExecutionResult.setConfigUpdate(true);
		} else {
			if (sEnvVariable.toUpperCase().startsWith("#COL#")) {
				sColumnName = sEnvVariable.substring(5, sEnvVariable.length());
				if (sColumnName.toUpperCase().endsWith("#COL#")) {
					sColumnName = sColumnName.substring(0, sColumnName.length() - 5);
				}
				try {
					DataSheetUtility.setValueFromGlobalMap(workBookMap, sWorkSheet, sColumnName, sRow, sExtractedValue);
					testCaseExecutionResult.setWorkBookMap(workBookMap);
					testCaseExecutionResult.setTestDataUpdate(true);
				} catch (AutomationException e) {
					loggerKeyword.error("Automation exception occured: ", e);
					testCaseExecutionResult.setStatus(FAIL);
					testCaseExecutionResult.setMessage(e.getMessage());
					return testCaseExecutionResult;
				}
			} else {
//				configurationMap.put(sEnvVariable, sExtractedValue);

				KeywordUtilities.updateConfigurationMap(configurationMap, sEnvVariable, sExtractedValue);
				testCaseExecutionResult.setConfigUpdate(true);
			}

		}
		testCaseExecutionResult.setStatus(PASS);
		return testCaseExecutionResult;
	}

	public static void updateConfigurationMap(Map<String, String> configurationMap, String env_Variable,
			String env_Variable_Value) {
		// if(env_Variable.startsWith("ENCR_") || env_Variable.startsWith("<ENCRYPT>")
		// && env_Variable.endsWith("</ENCRYPT>") ||
		// env_Variable_Value.startsWith("<ENCRYPT>") &&
		// env_Variable_Value.endsWith("</ENCRYPT>"))
		if (env_Variable.startsWith("<ENCRYPT>") && env_Variable.endsWith("</ENCRYPT>")) {
			/*
			 * if(env_Variable_Value.startsWith("<ENCRYPT>") &&
			 * env_Variable_Value.endsWith("</ENCRYPT>")) {
			 * env_Variable_Value=StringUtils.substringBetween(env_Variable_Value,
			 * "<ENCRYPT>", "</ENCRYPT>"); }
			 * 
			 * String value="<ENCR>"+enc_dec.encrypt(env_Variable_Value)+"</ENCR>";
			 */

			String value = env_Variable_Value;

			// if(!env_Variable_Value.startsWith("<ENCR>") &&
			// !env_Variable_Value.endsWith("</ENCR>"))
			if (!env_Variable_Value.startsWith("<ENCR>") && !env_Variable_Value.startsWith("</ENCR>")) {
				value = "<ENCR>" + enc_dec.encrypt(env_Variable_Value) + "</ENCR>";
			}

			if (value != null) {
				if (value.toUpperCase().startsWith("SAFAL-FATAL-ERROR") || value.isEmpty()) {
					configurationMap.put(env_Variable, "<ERROR>");
				} else {
					configurationMap.put(env_Variable, value);
				}
			}
		} else {
			configurationMap.put(env_Variable, env_Variable_Value);
		}
	}

	static boolean isSynchroziedLaunchBrowser(String userName) {
		boolean launchBrowserFlag = false;

		InitSAFALProperties initSAFALProperties = InitSAFALProperties.getInstance(userName);
		KTTGuiConstants.launchBrowser = initSAFALProperties.isLaunchBrowser;
		launchBrowserFlag = initSAFALProperties.isLaunchBrowser;

		return launchBrowserFlag;
	}

	private static String replaceNewLineWithSpaceIfStringEndsWithNewLineChar(String msg) {
		try {
			StringBuffer msg2 = new StringBuffer(msg);

			if (msg.endsWith("\n")) {
				msg = msg2.reverse().toString();
				msg = msg.replaceFirst("\n", "");
				StringBuffer msg3 = new StringBuffer(msg);
				msg = msg3.reverse().toString();
			}

			return msg;
		} catch (Exception e) {
			return msg;
		}
	}

	private static String replaceCarriageReturnWithSpaceIfStringEndsWithNewLineChar(String msg) {
		try {
			StringBuffer msg2 = new StringBuffer(msg);

			if (msg.endsWith("\r")) {
				msg = msg2.reverse().toString();
				msg = msg.replaceFirst("\r", "");
				StringBuffer msg3 = new StringBuffer(msg);
				msg = msg3.reverse().toString();
			}

			return msg;
		} catch (Exception e) {
			return msg;
		}
	}

	public static String parseParameterValueForColAndEnvTag(Map<String, Map<String, Map<String, String>>> workBookMap,
			String sWorkSheet, String sColumnNameParam, String sRowNumber, Map<String, String> executionConfiguration,
			TestcaseExecutionResultVO execution, String userName) throws AutomationException {

		String sColumnNameParam1 = sColumnNameParam;

		try {
			String[] sWorkSheetArray = sWorkSheet.split(";");

			List<String> columns = new ArrayList<String>();
			List<String> envs = new ArrayList<String>();

			for (int i = 0; i < sWorkSheetArray.length; i++) {

				Map<String, Map<String, String>> sheetMapSingle = workBookMap.get(sWorkSheetArray[i]);

				if (sheetMapSingle != null) {
					Map<String, String> actualSheetMap = sheetMapSingle.get("1");

					Iterator<Map.Entry<String, String>> itr = actualSheetMap.entrySet().iterator();
					while (itr.hasNext()) {
						Map.Entry<String, String> entry = itr.next();

						columns.add(entry.getKey());
					}
				}
			}

			Iterator<Map.Entry<String, String>> itr = executionConfiguration.entrySet().iterator();
			while (itr.hasNext()) {
				Map.Entry<String, String> entry = itr.next();

				envs.add(entry.getKey());
			}

			int counter = getCounter(sColumnNameParam, COL_TAG);
			counter += getCounter(sColumnNameParam, ENV_TAG);

			sColumnNameParam = replaceNewLineWithSpaceIfStringEndsWithNewLineChar(sColumnNameParam);
			sColumnNameParam = replaceCarriageReturnWithSpaceIfStringEndsWithNewLineChar(sColumnNameParam);

			if (counter == 1) {
				if (sColumnNameParam.contains(ENV_TAG) && !sColumnNameParam.contains(ENV_TAG_END)) {
					sColumnNameParam = sColumnNameParam + ENV_TAG_END;
				}

				if (sColumnNameParam.contains(COL_TAG) && !sColumnNameParam.contains(COL_TAG_END)) {
					sColumnNameParam = sColumnNameParam + COL_TAG_END;
				}
			}

			if (counter == 0) {

				return sColumnNameParam;
			}

			/////////////// code added to take care of param like
			/////////////// <COL>FolderName</COL>_<ENV>date////////////////////////
			int iCOLTAG_counter = getCounter(sColumnNameParam, COL_TAG);
			int iENVTAG_counter = getCounter(sColumnNameParam, ENV_TAG);
			int iCOLENDTAG_counter = getCounter(sColumnNameParam, COL_TAG_END);
			int iENVENDTAG_counter = getCounter(sColumnNameParam, ENV_TAG_END);

			int iCOLTAGDiff = iCOLTAG_counter - iCOLENDTAG_counter;
			int iENVTAGDiff = iENVTAG_counter - iENVENDTAG_counter;

			if (iCOLTAGDiff > 1) {
				throw new AutomationException(
						"Could not evaluate param value, please check <COL> syntax: " + sColumnNameParam1);
			}

			if (iENVTAGDiff > 1) {
				throw new AutomationException(
						"Could not evaluate param value, please check <ENV> syntax: " + sColumnNameParam1);
			}

			if (iCOLTAGDiff < 0) {
				throw new AutomationException(
						"Could not evaluate param value, please check </COL> syntax: " + sColumnNameParam1);
			}

			if (iENVTAGDiff < 0) {
				throw new AutomationException(
						"Could not evaluate param value, please check </ENV> syntax: " + sColumnNameParam1);
			}

			if (iENVTAGDiff > 0 && iCOLTAGDiff > 0) {
				throw new AutomationException(
						"Could not evaluate param value, please check syntax: " + sColumnNameParam1);
			}

			if (iCOLTAG_counter > iCOLENDTAG_counter) {
				sColumnNameParam = sColumnNameParam + COL_TAG_END;
			} else if (iENVTAG_counter > iENVENDTAG_counter) {
				sColumnNameParam = sColumnNameParam + ENV_TAG_END;
			}
			////////////////////////////////////////////////////////////////////

			int lastCounter = counter;

			do {
				lastCounter = counter;
				for (Iterator<String> iterator = columns.iterator(); iterator.hasNext();) {
					String string = iterator.next();

					if (sColumnNameParam.contains(COL_TAG + string + COL_TAG_END)) {
						String sValue = new DataSheetUtility().getValueFromGlobalMap(workBookMap, sWorkSheet, string,
								sRowNumber, executionConfiguration, execution, userName);// SAF-1654
						sColumnNameParam = sColumnNameParam.replace(COL_TAG + string + COL_TAG_END, sValue);
					}
				}

				for (Iterator<String> iterator = envs.iterator(); iterator.hasNext();) {
					String string = iterator.next();

					if (sColumnNameParam.contains(ENV_TAG + string + ENV_TAG_END)) {
						String sValue = null;
						/*
						 * if (string.contains("<ENCRYPT>") && string.contains("</ENCRYPT>")) { sValue =
						 * executionConfiguration.get(string); if (sValue.startsWith("<ENCR>") &&
						 * sValue.endsWith("</ENCR>")) { sValue = sValue.replace("<ENCR>",
						 * "").replace("</ENCR>", ""); sValue = enc_dec.decrypt(sValue); }
						 * 
						 * } else { sValue = executionConfiguration.get(string); }
						 */
						sValue = executionConfiguration.get(string);
						sColumnNameParam = sColumnNameParam.replace(ENV_TAG + string + ENV_TAG_END, sValue);
					}
				}

				counter = getCounter(sColumnNameParam, COL_TAG);
				counter += getCounter(sColumnNameParam, ENV_TAG);

				if (counter == 0) {
					break; // condition one
				}

				if (lastCounter - counter == 0) {
					throw new AutomationException(
							"Could not evaluate param value, please check value or syntax workSheetName : " + sWorkSheet
									+ " columnName : " + sColumnNameParam + " row Number : " + sRowNumber);
				}

			} while (true);

			return sColumnNameParam;

		} catch (Exception e) {

			throw new AutomationException(e.getMessage());
		}

	}

	private static int getCounter(String str, String subStr) {
		int counter = 0;

		int ind = str.indexOf(subStr);

		while (ind != -1) {
			ind = str.indexOf(subStr, ind + 1);
			counter++;
		}
		return counter;

	}

	public static void main(String a[]) {
		// System.out.println(getCounter("abc<COL>aa</COL>cc<COL>aa</COL>", "<COL>"));
	}

	public static char[] insertElementIntoArray(char[] cInputArray, int index, String sValue) {
		int ilength = cInputArray.length;
		char[] cOutputArray = new char[ilength + 1];
		System.arraycopy(cInputArray, 0, cOutputArray, 0, index);
		cOutputArray[index] = sValue.charAt(0);
		System.arraycopy(cInputArray, index, cOutputArray, index + 1, ilength - index);
		return cOutputArray;
	}

	public static String Concatenate(String sManipulate, Map<String, String> configurationMap) {
		String[] sValue = sManipulate.split("&");
		String sReturnedString = KTTGuiConstants.EMPTY_STRING;

		for (int index = 0; index < sValue.length; index++) {
			if (sValue[index].contains("Counter")) {
				int sX = sValue[index].indexOf("(");
				int sXX = sValue[index].indexOf(")");
				String sY = sValue[index].substring(sX + 1, sXX);
				String[] sParam = sY.split(",");

				if (sParam[0].contains(ENV_PARA))
					sParam[0] = parseParamValue(configurationMap, sParam[0]);

				if (sParam.length == 1)
					sReturnedString += Integer.toString(Counter(sParam[0], EMPTY_STRING));
				else
					sReturnedString += Integer.toString(Counter(sParam[0], sParam[1]));
			} else if (sValue[index].contains(ENV_PARA)) {
				sReturnedString += parseParamValue(configurationMap, sValue[index]);
			} else
				sReturnedString += sValue[index];
		}

		return sReturnedString;
	}

	private static int Counter(String sCounterName, String sBase) {
		int iCounter = 0;
		int iBase = 0;
		String sign = null;

		iCounter = convertStringToInt(sCounterName);

		if (!isEmptyString(sBase)) {
			int sLen = sBase.length();
			sign = sBase.substring(0, 1);
			sBase = sBase.substring(1, sLen);

			iBase = Integer.parseInt(sBase);

			if (sign.equalsIgnoreCase("+"))
				iCounter = iCounter + iBase;
			else
				iCounter = iCounter - iBase;
		}

		return iCounter;
	}

	public static char ASCIIToChar(final int ascii) {
		return (char) ascii;
	}

	public static int getKeyCode(String keyEvent) {
		try {
			if (keyEvent.equalsIgnoreCase("A"))
				return KeyEvent.VK_A;
			else if (keyEvent.equalsIgnoreCase("B"))
				return KeyEvent.VK_B;
			else if (keyEvent.equalsIgnoreCase("C"))
				return KeyEvent.VK_C;
			else if (keyEvent.equalsIgnoreCase("D"))
				return KeyEvent.VK_D;
			else if (keyEvent.equalsIgnoreCase("E"))
				return KeyEvent.VK_E;
			else if (keyEvent.equalsIgnoreCase("F"))
				return KeyEvent.VK_F;
			else if (keyEvent.equalsIgnoreCase("G"))
				return KeyEvent.VK_G;
			else if (keyEvent.equalsIgnoreCase("H"))
				return KeyEvent.VK_H;
			else if (keyEvent.equalsIgnoreCase("I"))
				return KeyEvent.VK_I;
			else if (keyEvent.equalsIgnoreCase("J"))
				return KeyEvent.VK_J;
			else if (keyEvent.equalsIgnoreCase("K"))
				return KeyEvent.VK_K;
			else if (keyEvent.equalsIgnoreCase("L"))
				return KeyEvent.VK_L;
			else if (keyEvent.equalsIgnoreCase("M"))
				return KeyEvent.VK_M;
			else if (keyEvent.equalsIgnoreCase("N"))
				return KeyEvent.VK_N;
			else if (keyEvent.equalsIgnoreCase("O"))
				return KeyEvent.VK_O;
			else if (keyEvent.equalsIgnoreCase("P"))
				return KeyEvent.VK_P;
			else if (keyEvent.equalsIgnoreCase("Q"))
				return KeyEvent.VK_Q;
			else if (keyEvent.equalsIgnoreCase("R"))
				return KeyEvent.VK_R;
			else if (keyEvent.equalsIgnoreCase("S"))
				return KeyEvent.VK_S;
			else if (keyEvent.equalsIgnoreCase("T"))
				return KeyEvent.VK_T;
			else if (keyEvent.equalsIgnoreCase("U"))
				return KeyEvent.VK_U;
			else if (keyEvent.equalsIgnoreCase("V"))
				return KeyEvent.VK_V;
			else if (keyEvent.equalsIgnoreCase("W"))
				return KeyEvent.VK_W;
			else if (keyEvent.equalsIgnoreCase("X"))
				return KeyEvent.VK_X;
			else if (keyEvent.equalsIgnoreCase("Y"))
				return KeyEvent.VK_Y;
			else if (keyEvent.equalsIgnoreCase("Z"))
				return KeyEvent.VK_Z;
			else if (keyEvent.equalsIgnoreCase("ALT"))
				return KeyEvent.VK_ALT;
			else if (keyEvent.equalsIgnoreCase("ENTER"))
				return KeyEvent.VK_ENTER;
			else if (keyEvent.equalsIgnoreCase("TAB"))
				return KeyEvent.VK_TAB;
			else if (keyEvent.equalsIgnoreCase("DELETE"))
				return KeyEvent.VK_DELETE;
			else if (keyEvent.equalsIgnoreCase("CONTROL"))
				return KeyEvent.VK_CONTROL;
			else if (keyEvent.equalsIgnoreCase("SHIFT"))
				return KeyEvent.VK_SHIFT;
			else if (keyEvent.equalsIgnoreCase("ESC"))
				return KeyEvent.VK_ESCAPE;
			else if (keyEvent.equalsIgnoreCase("F1"))
				return KeyEvent.VK_F1;
			else if (keyEvent.equalsIgnoreCase("F2"))
				return KeyEvent.VK_F2;
			else if (keyEvent.equalsIgnoreCase("F3"))
				return KeyEvent.VK_F3;
			else if (keyEvent.equalsIgnoreCase("F4"))
				return KeyEvent.VK_F4;
			else if (keyEvent.equalsIgnoreCase("F5"))
				return KeyEvent.VK_F5;
			else if (keyEvent.equalsIgnoreCase("F6"))
				return KeyEvent.VK_F6;
			else if (keyEvent.equalsIgnoreCase("F7"))
				return KeyEvent.VK_F7;
			else if (keyEvent.equalsIgnoreCase("F8"))
				return KeyEvent.VK_F8;
			else if (keyEvent.equalsIgnoreCase("F9"))
				return KeyEvent.VK_F9;
			else if (keyEvent.equalsIgnoreCase("F10"))
				return KeyEvent.VK_F10;
			else if (keyEvent.equalsIgnoreCase("F11"))
				return KeyEvent.VK_F11;
			else if (keyEvent.equalsIgnoreCase("F12"))
				return KeyEvent.VK_F12;
			else if (keyEvent.equalsIgnoreCase("0"))
				return KeyEvent.VK_0;
			else if (keyEvent.equalsIgnoreCase("1"))
				return KeyEvent.VK_1;
			else if (keyEvent.equalsIgnoreCase("2"))
				return KeyEvent.VK_2;
			else if (keyEvent.equalsIgnoreCase("3"))
				return KeyEvent.VK_3;
			else if (keyEvent.equalsIgnoreCase("4"))
				return KeyEvent.VK_4;
			else if (keyEvent.equalsIgnoreCase("5"))
				return KeyEvent.VK_5;
			else if (keyEvent.equalsIgnoreCase("6"))
				return KeyEvent.VK_6;
			else if (keyEvent.equalsIgnoreCase("7"))
				return KeyEvent.VK_7;
			else if (keyEvent.equalsIgnoreCase("8"))
				return KeyEvent.VK_8;
			else if (keyEvent.equalsIgnoreCase("9"))
				return KeyEvent.VK_9;
			else if (keyEvent.equalsIgnoreCase("ARROW_LEFT"))
				return KeyEvent.VK_LEFT;
			else if (keyEvent.equalsIgnoreCase("ARROW_RIGHT"))
				return KeyEvent.VK_RIGHT;
			else if (keyEvent.equalsIgnoreCase("ARROW_UP"))
				return KeyEvent.VK_UP;
			else if (keyEvent.equalsIgnoreCase("ARROW_DOWN"))
				return KeyEvent.VK_DOWN;
			else if (keyEvent.equalsIgnoreCase("INSERT"))
				return KeyEvent.VK_INSERT;
			else if (keyEvent.equalsIgnoreCase("SPACE"))
				return KeyEvent.VK_SPACE;
			else
				return 65535;
		} catch (Exception E) {
			return 65535;
		}
	}

	public static boolean isValidInstance(String sValue) {

		if (sValue != null) {
			sValue = sValue.trim();
		}

		try {
			if (sValue == null || isEmptyString(sValue)) {
				return true;
			}

			int iInstanceValue = Integer.parseInt(sValue);

			if (iInstanceValue < 0) {
				return false;
			}

			return true;
		} catch (NumberFormatException e) {

			return false;
		}
	}
	// New Code for implementing synchronization in the two keywords
	// VerfiyTableCellValue and VerifyMessage

	public static boolean synchronizeMessage(Map<String, String> mapConfigMap, WebDriver webDriver, String userName) {
		String finalStatus = null;
		int iWaitTime = DEFAULT_OBJECT_WAIT_TIME;

		if (mapConfigMap != null && null != mapConfigMap.get(OBJECT_WAIT_VARIABLE)) {
			try {
				iWaitTime = Integer.parseInt(mapConfigMap.get(OBJECT_WAIT_VARIABLE));
			} catch (NumberFormatException e) {
				iWaitTime = DEFAULT_OBJECT_WAIT_TIME;
			}
		}
		String sSynchronizeBrowser =

				"		synchronizeBrowser();	" + "		function synchronizeBrowser()" + "		{"
						+ "		var CurrentWindowDoc = document;							"
						+ "			if (CurrentWindowDoc.readyState == \"complete\")" + "			{	"
						+ "				return \"PASS\";" +

						"			}   " + "		return \"FAIL\"    ;    " + "		}";

		MyWait myWait = new MyWait();

		TimeWatcher timeWatcher = new TimeWatcher(iWaitTime);
		timeWatcher.startTimeWatcher();

		try {
			while (!timeWatcher.isTimeUp(userName)) {
				try {
					if (SAFALUserSession.getSciptStatus(userName) == ScriptStatus.TO_BE_STOPPED) {
						return false;
					}
					// finalStatus = selenium.getEval(sSynchronizeBrowser);
					finalStatus = ((JavascriptExecutor) webDriver).executeScript("return " + sSynchronizeBrowser)
							.toString();

					if (finalStatus.equalsIgnoreCase("PASS")) {
						return true;
					}

					myWait.waitFor(10, null);

				} catch (Exception e) {
					return false;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			timeWatcher.cancel();
		}
		return false;
	}

	public static void RobotTab() {
		Robot robot;
		try {
			robot = new Robot();
			robot.keyPress(KeyEvent.VK_TAB);
			robot.keyRelease(KeyEvent.VK_TAB);

			MyWait myWait = new MyWait();
			myWait.waitFor(4000, null);
		} catch (AWTException e) {
			e.printStackTrace();
		}
	}

	public static String myCounterParsing(String param) {
		String output = "";

		// e.g. ABC Counter(XYZ,+2) XYZ Counter(XYZ,+2)

		int indCounter = param.indexOf("Counter(");
		int lastInd = 0;
		while (indCounter != -1) {
			output = output + param.substring(lastInd, indCounter);

			int commaIndex = param.indexOf(",", indCounter);
			int bracketInd = param.indexOf(")", commaIndex);

			String firstValue = param.substring(indCounter + 8, commaIndex);
			String secondValue = param.substring(commaIndex + 1, bracketInd);

			int f1 = 0;
			int f2 = 0;

			try {
				f1 = Integer.parseInt(firstValue);
			} catch (NumberFormatException e) {

				e.printStackTrace();
			}

			try {
				secondValue = secondValue.replace("+", "");
				f2 = Integer.parseInt(secondValue);
			} catch (NumberFormatException e) {

				e.printStackTrace();
			}

			int result = f1 + f2;
			output = output + String.valueOf(result);

			indCounter = param.indexOf("Counter(", bracketInd);

			if (indCounter == -1) {
				String rem = param.substring(bracketInd + 1);
				output = output + rem;
				lastInd = param.length();
			} else {
				String rem = param.substring(bracketInd + 1, indCounter);
				output = output + rem;
				lastInd = bracketInd;
			}

		}

		return output;

	}

	/**
	 * Allow a parameter which may contain a list of ENV To extract the value(s)
	 * relating to the ENV variable. To return the string with the value(s) of the
	 * ENV to the calling function. Currently only support | and ;
	 * 
	 * @param param
	 * @return string value of the ENV values.
	 * @author Lance.Ingram
	 */
	public static String parseEnvSplitParam2(Map<String, String> mapConfigMap, String param, String userName) {
		String[] splitArray = null;
		String delimiter = null;
		String colValues = KTTGuiConstants.EMPTY_STRING;

		if (param.contains(SEMICOLON))
			delimiter = SEMICOLON;
		if (param.contains(COLUMN_VALUE_SEPARATOR))
			delimiter = COLUMN_VALUE_SEPARATOR;

		if (delimiter != null) {
			if (delimiter.equals(COLUMN_VALUE_SEPARATOR))
				splitArray = param.split("\\" + delimiter);
			else
				splitArray = param.split(delimiter);

			for (int index = 0; index < splitArray.length; index++) {
				if (SAFALUserSession.getSciptStatus(userName) == ScriptStatus.TO_BE_STOPPED) {

					return colValues;
				}

				if (index == 0)
					colValues = getValues(mapConfigMap, splitArray[index]).concat(delimiter);
				else if (index == splitArray.length - 1)
					colValues += getValues(mapConfigMap, splitArray[index]);
				else
					colValues += getValues(mapConfigMap, splitArray[index]).concat(delimiter);
			}
		}
		// if there are only one value which is a <ENV>
		else {
			colValues = KeywordUtilities.parseParamValue(mapConfigMap, param);
		}

		return colValues;
	}

	/**
	 * This method is used to return the value of the key in the hash map
	 * 
	 * @param key value of the hashmap
	 * @return value of the key
	 * @author Lance.Ingram
	 */
	private static String getValues(Map<String, String> mapConfigMap, String value) {
		if (value.contains(ENV_PARA))
			value = KeywordUtilities.parseParamValue(mapConfigMap, value);

		return value;
	}

	public static boolean convertStringToBoolean(String param) {
		boolean bValue = false;

		if (param == null || isEmptyString(param) || param.equals("false"))
			bValue = false;
		else
			bValue = true;

		return bValue;
	}

	public static TestcaseExecutionResultVO validateTable(Map<String, String> mapConfigMap, WebDriver selenium,
			String userName, String... listOfParameters) {
		TestcaseExecutionResultVO TestcaseExecutionResult = new TestcaseExecutionResultVO();

		String sTableHeader = null;

		// check for required parameter count
		if (listOfParameters != null) {
			sTableHeader = listOfParameters[0];

		} else {
			TestcaseExecutionResult.setMessage(ERROR_PARAMETERS_LIST);
			return TestcaseExecutionResult;
		}

		if (selenium == null) {
			// Browser not instantiated
			TestcaseExecutionResult.setMessage(ERROR_BROWSER_NOT_INSTANTIATED);
			return TestcaseExecutionResult;
		}

		if (sTableHeader != null && sTableHeader.trim().startsWith(OBJECT_SPECIFIER)) {
			// remove OBJECT_SPECIFIER sign
			sTableHeader = sTableHeader.substring(OBJECT_SPECIFIER.length(), sTableHeader.length());

			if (sTableHeader.toUpperCase().contains(XPATH_LOCATOR.toUpperCase())) {
				sTableHeader = KeywordUtilities.formXpath(sTableHeader);
				if (!waitForElementPresent(mapConfigMap, selenium, sTableHeader, userName)) {
					TestcaseExecutionResult.setMessage(ERROR_TABLE_NOT_FOUND_XPATH);
					TestcaseExecutionResult.setObjectError(true);
					return TestcaseExecutionResult;
				}
			}
		}

		TestcaseExecutionResult.setObject(sTableHeader);
		TestcaseExecutionResult.setValid(true);
		return TestcaseExecutionResult;

	}

	public static boolean isValidNumbericValue(String sValue) {

		if (sValue != null) {
			sValue = sValue.trim();
		}

		try {
			if (sValue == null || isEmptyString(sValue)) {
				return false;
			}
			Integer.parseInt(sValue);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	public static void calcHMS(long timeInSeconds) {
		long hours, minutes, seconds;
		hours = timeInSeconds / 3600;
		timeInSeconds = timeInSeconds - (hours * 3600);
		minutes = timeInSeconds / 60;
		timeInSeconds = timeInSeconds - (minutes * 60);
		seconds = timeInSeconds;
		// System.out.println(hours + ":" + minutes + ":" + seconds );
	}

	public static void saveReader(String name, Reader body) {
		int c;
		try {

			if (body == null) {

				return;
			}
			Writer f = new FileWriter(name);
			while ((c = body.read()) > -1) {
				// c=body.read();
				f.write(c);
			}
			f.close();
		} catch (Exception e) {
			System.err.println("Exception: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public static TestcaseExecutionResultVO noValidationRequired() {

		TestcaseExecutionResultVO executionResults = new TestcaseExecutionResultVO();
		executionResults.setValid(true);
		return executionResults;
	}

	public static TestcaseExecutionResultVO noValidationRequired(String obj) {

		TestcaseExecutionResultVO executionResults = new TestcaseExecutionResultVO();
		executionResults.setObject(obj);
		executionResults.setValid(true);
		return executionResults;
	}

	public static TestcaseExecutionResultVO prepareFinalResult(TestcaseExecutionResultVO TestcaseExecutionResult,
			String sCurrentStatus, String sExpectedStatus, String sNotFoundError, String sFailError) {
		if (sCurrentStatus.equalsIgnoreCase(PASS_STEP_STATUS)) {

			if (sExpectedStatus != null && EXPECTED_STATUS_NOTFOUND.equalsIgnoreCase(sExpectedStatus)) {
				TestcaseExecutionResult.setMessage(sNotFoundError);
				return TestcaseExecutionResult;
			}

			TestcaseExecutionResult.setStatus(PASS);
			return TestcaseExecutionResult;

		}

		else if (sCurrentStatus.equalsIgnoreCase(FAIL_STEP_STATUS)) {

			if (sExpectedStatus != null && EXPECTED_STATUS_NOTFOUND.equalsIgnoreCase(sExpectedStatus)) {
				// value not found as per expectation, so this case will be considered as passed
				TestcaseExecutionResult.setMessage(NAGATIVE_TEST_SCENARIO);
				TestcaseExecutionResult.setStatus(PASS);
				return TestcaseExecutionResult;
			}

			// "Expected Value not found in TextField
			TestcaseExecutionResult.setMessage(sFailError);
			return TestcaseExecutionResult;

		} else {
			TestcaseExecutionResult.setMessage(sCurrentStatus);
			return TestcaseExecutionResult;
		}
	}

	public static boolean waitForObjectPresentIO(WebDriver webDriver, String objName, String sFrameName,
			final Map<String, String> mapConfigMap, String userName) {

		InitSAFALProperties initSAFALProperties = InitSAFALProperties.getInstance(userName);

		int iObjectRenderingWaitTime = initSAFALProperties.ObjectRenderingWaitTime;
		if (mapConfigMap != null && null != mapConfigMap.get(OBJECT_RENDERING_ITERATION_WAIT_VARIABLE)) {
			try {
				iObjectRenderingWaitTime = Integer.parseInt(mapConfigMap.get(OBJECT_RENDERING_ITERATION_WAIT_VARIABLE));
			} catch (NumberFormatException e) {
				iObjectRenderingWaitTime = initSAFALProperties.ObjectRenderingWaitTime;
			}
		}

		final String checkObject =

				"		fun();																		 	 						"
						+ " 		function fun()																							"
						+ "   	{																										"
						+ "		 	var nameObj = \"" + objName
						+ "\";																		"
						+ KeywordUtilities.getDocumentObject(sFrameName) + "			if(fd.getElementById (\""
						+ objName + "\")==null)													"
						+ "				return '0';																						"
						+ "			else																								"
						+ "				return '1';																						"
						+ "		}																										";

		String Result = "0";
		boolean objectFoundFlag = false;

		TimeWatcher timeWatcher = new TimeWatcher(120);
		timeWatcher.startTimeWatcher();

		try {
			while (!timeWatcher.isTimeUp(userName)) {
				if (SAFALUserSession.getSciptStatus(userName) == ScriptStatus.TO_BE_STOPPED) {

					return true;
				}

				try {
					Result = ((JavascriptExecutor) webDriver).executeScript("return " + checkObject).toString();
				} catch (Exception e) {

					Result = "0";
				}

				if (Result.equals("1")) {
					objectFoundFlag = true;
					break;
				}

				try {
					Thread.sleep(iObjectRenderingWaitTime);
				} catch (InterruptedException e) {

					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			timeWatcher.cancel();
		}

		if (objectFoundFlag == true) {
			return true;
		} else {
			return false;
		}
	}

	public static String getDocumentObject(String sFrameValue) {
		if (sFrameValue == null) {
			sFrameValue = "";
		}

		String sDocumentObject = "	     var CurrentWindowDoc=null;								" + "		 try"
				+ "		 	{" + "				CurrentWindowDoc = document;" + "		 	}" + "		 catch(e)"
				+ "			{" + "				CurrentWindowDoc=document;" + "			}		"
				+ "			if (CurrentWindowDoc==null)" + "			{"
				+ "					CurrentWindowDoc=document;" + "			}		" + "        var sFrameValue = \""
				+ sFrameValue + "\";                                                          		"
				+ "		 if(sFrameValue != null && sFrameValue!=\"\") 																					"
				+ "		 { 																										"
				+ "        var splittedfrms=sFrameValue.split(\";\");                                                          	"
				+ "        for(i=0;i<splittedfrms.length;i++)                                                                     "
				+ "        	{                                                                                                   "
				+ "           	try                                                                                             "
				+ " 	          	{                                                                                               "
				+ "				  var frameObj=CurrentWindowDoc.getElementById(splittedfrms[i]);                                "
				+ "             	  var frameDoc=null;"
				+ "				  try {frameDoc=frameObj.contentWindow.document;}catch(e){frameDoc=null;} "
				+ "					if (frameDoc==null)" + "					{"
				+ "						try {frameDoc=frameObj.contentDocument;}catch(e){frameDoc=null;}"
				+ "							if(frameDoc==null)" + "								{"
				+ "									try {frameDoc=frameObj.document;}catch(e){frameDoc=null;}"
				+ "								}" + "					}" + "					if (frameDoc==null)"
				+ "					{" + "						return \"Fail, unable to get the document object.\";"
				+ "					}                                                                                           "
				+ "              	  CurrentWindowDoc = frameDoc;                                                                  "
				+ "           	}                                                                                               "
				+ "            	catch(error)                                                                                    "
				+ "            	{                                                                                               "
				+ "              	  return  \"FRAME NOT FOUND\";                                                        "
				+ "            	}                                                                                               "
				+ "            }                                                                                                  "
				+ "		}																										"
				+ "        var fd=CurrentWindowDoc;                                                                               ";

		return sDocumentObject;
	}

	public static String getDocumentObjectBkup(String sFrameValue) {
		if (sFrameValue == null) {
			sFrameValue = "";
		}

		String sDocumentObject = "	     var CurrentWindowDoc;								" + "		 try"
				+ "		 	{" + "				CurrentWindowDoc = document;" + "		 	}" + "		 catch(e)"
				+ "			{" + "				CurrentWindowDoc=document;" + "			}		"
				+ "        var sFrameValue = \"" + sFrameValue
				+ "\";                                                          		" +

				"		 if(sFrameValue != null && sFrameValue!=\"\") 																					"
				+ "		 { 																										"
				+ "        var splittedfrms=sFrameValue.split(\";\");                                                          	"
				+ "        for(i=0;i<splittedfrms.length;i++)                                                                     "
				+ "        	{                                                                                                   "
				+ "           	try                                                                                             "
				+ " 	          	{                                                                                               "
				+ "				  var frameObj=CurrentWindowDoc.getElementById(splittedfrms[i]);                                "
				+ "             	  var frameDoc=frameObj.contentWindow.document;                                                 "
				+ "              	  CurrentWindowDoc = frameDoc;                                                                  "
				+ "           	}                                                                                               "
				+ "            	catch(error)                                                                                    "
				+ "            	{                                                                                               "
				+ "              	  return  \"FRAME NOT FOUND\";                                                        "
				+ "            	}                                                                                               "
				+ "            }                                                                                                  "
				+ "		}																										"
				+ "        var fd=CurrentWindowDoc;                                                                               ";

		return sDocumentObject;
	}

	public static String getWindowObject(String sFrameValue) {
		if (sFrameValue == null) {
			sFrameValue = "";
		}

		String sDocumentObject =
				// " var windowObj=selenium.browserbot.getCurrentWindow(); "+
				"	     var CurrentWindowDoc = document;																"
						+ "        var sFrameValue = \"" + sFrameValue
						+ "\";                                                          		" +

						"		 if(sFrameValue != null && sFrameValue!=\"\") 																					"
						+ "		 { 																										"
						+ "        var splittedfrms=sFrameValue.split(\";\");                                                          	"
						+ "        for(i=0;i<splittedfrms.length;i++)                                                                     "
						+ "        	{                                                                                                   "
						+ "           	try                                                                                             "
						+ " 	          	{                                                                                               "
						+ "				  var frameObj=CurrentWindowDoc.getElementById(splittedfrms[i]);                                "
						+ "				  windowObj=frameObj.contentWindow;																"
						+ "             	  var frameDoc=windowObj.document;                 				                                "
						+ "              	  CurrentWindowDoc = frameDoc;                                                                  "
						+ "           	}                                                                                               "
						+ "            	catch(error)                                                                                    "
						+ "            	{                                                                                               "
						+ "              	  return  \"FRAME NOT FOUND\";                                                        "
						+ "            	}                                                                                               "
						+ "            }                                                                                                  "
						+ "		}																										"
						+ "        var fd=windowObj;                                                                               ";

		return sDocumentObject;
	}

	public static boolean isValidPositiveNumbericValue(String sValue) {

		if (sValue != null) {
			sValue = sValue.trim();
		}

		try {
			if (sValue == null || isEmptyString(sValue)) {
				return true;
			}

			int iInstanceValue = Integer.parseInt(sValue);

			if (iInstanceValue < 0) {
				return false;
			}

			return true;
		} catch (NumberFormatException e) {

			return false;
		}
	}

	public static boolean isValidExpectedStatus(String sExpectedStatus) {

		if (null == sExpectedStatus || isEmptyString(sExpectedStatus)
				|| sExpectedStatus.equalsIgnoreCase(EXPECTED_STATUS_NOTFOUND)
				|| sExpectedStatus.equalsIgnoreCase("fail")) {
			return true;
		}

		return false;
	}

	public static String formXpath(String sXpath) {
		return sXpath;
	}

	public static int getObjectWaitTime(String sWaitTime, Map<String, String> mapConfigMap) {
		int iWaitTime = DEFAULT_OBJECT_WAIT_TIME;

		if (EMPTY_STRING.equals(sWaitTime)) {

			if (mapConfigMap != null && null != mapConfigMap.get(OBJECT_WAIT_VARIABLE)) {
				try {
					iWaitTime = Integer.parseInt(mapConfigMap.get(OBJECT_WAIT_VARIABLE));
				} catch (NumberFormatException e) {
					iWaitTime = DEFAULT_OBJECT_WAIT_TIME;
				}
			}
		} else {
			try {

				iWaitTime = Integer.parseInt(sWaitTime);

			} catch (NumberFormatException e) {

				iWaitTime = DEFAULT_OBJECT_WAIT_TIME;
			}
		}
		return iWaitTime;
	}

	public static String getTimeZone(Map<String, String> mapConfigMap) {
		String sTimeZone = KTTGuiConstants.DEFAULT_TIME_ZONE;

		if (mapConfigMap != null && null != mapConfigMap.get(KTTGuiConstants.TIME_ZONE_VARIABLE)) {
			try {
				sTimeZone = mapConfigMap.get(KTTGuiConstants.TIME_ZONE_VARIABLE);
			} catch (NumberFormatException e) {
				sTimeZone = KTTGuiConstants.DEFAULT_TIME_ZONE;
			}
		}
		return sTimeZone;
	}

	public static String getTimeZoneDiff(Map<String, String> mapConfigMap) {
		String iTomeZoneDiff = KTTGuiConstants.DEFAULT_TIME_ZONE_DIFF;

		if (mapConfigMap != null && null != mapConfigMap.get(KTTGuiConstants.TIME_ZONE_DIFF_VARIABLE)) {
			try {
				iTomeZoneDiff = mapConfigMap.get(KTTGuiConstants.TIME_ZONE_DIFF_VARIABLE);
			} catch (NumberFormatException e) {
				iTomeZoneDiff = KTTGuiConstants.DEFAULT_TIME_ZONE_DIFF;
			}
		}
		return iTomeZoneDiff;
	}

	public static boolean isEmptyString(String sStr) {
		if (sStr != null) {
			return sStr.isEmpty();
		} else {
			return false;
		}

	}

	public static WebElement waitForElementPresentInstance(final Map<String, String> mapConfigMap,
			final WebDriver webDriver, final String sTargetObject, final String instance, String userName) {
		Logger logger = Logger.getLogger("Thread" + Thread.currentThread().getName());
		logger.info("in waitForElementPresentInstance");

		boolean isMobile = false;

		if (webDriver.getClass().toString().contains("AndroidDriver")
				|| webDriver.getClass().toString().contains("IOSDriver")) {
			isMobile = true;
			// return waitForMobileElementPresent(mapConfigMap, (AppiumDriver) webDriver,
			// sTargetObject, instance, userName);
		}

		int iWaitTime = DEFAULT_OBJECT_WAIT_TIME;
		if (mapConfigMap != null && null != mapConfigMap.get(OBJECT_WAIT_VARIABLE)) {
			try {
				iWaitTime = Integer.parseInt(mapConfigMap.get(OBJECT_WAIT_VARIABLE));
			} catch (NumberFormatException e) {
				iWaitTime = DEFAULT_OBJECT_WAIT_TIME;
			}
		}

		InitSAFALProperties initSAFALProperties = InitSAFALProperties.getInstance(userName);

		int iObjectRenderingWaitTime = initSAFALProperties.ObjectRenderingWaitTime;
		if (mapConfigMap != null && null != mapConfigMap.get(OBJECT_RENDERING_ITERATION_WAIT_VARIABLE)) {
			try {
				iObjectRenderingWaitTime = Integer.parseInt(mapConfigMap.get(OBJECT_RENDERING_ITERATION_WAIT_VARIABLE));
			} catch (NumberFormatException e) {
				iObjectRenderingWaitTime = initSAFALProperties.ObjectRenderingWaitTime;
			}
		}

		TimeWatcher timeWatcher = new TimeWatcher(iWaitTime);
		timeWatcher.startTimeWatcher();
		WebElement ele = null;

		try {
			while (!timeWatcher.isTimeUp(userName)) {
				logger.info("in while 1");

				ele = null;
				try {

					boolean sf = getSelectedFrames(mapConfigMap, webDriver, userName);

					logger.info("in while 1 : " + sf);

					if (sf == false) {
						return null;
					}

					try {

						if (EMPTY_STRING.equals(instance)) {
							ele = getWebElement(webDriver, sTargetObject);
						} else {
							ele = getWebElementWithInstance(webDriver, sTargetObject, instance);
						}

						if (ele != null && !isMobile) {
							try {
								if (ele.isEnabled() && isElementDisplayed(ele)) {
								}
								;
							} catch (StaleElementReferenceException e) {
								logger.error("in excep 7");

								e.printStackTrace();

								ele = null;
							} catch (Exception e) {
								logger.error("in excep 8");

								e.printStackTrace();
								ele = null;
							}
						}

						if (ele != null) {
							/*
							 * WebDriverWait wait = new WebDriverWait(webDriver,iWaitTime); try {
							 * wait.until(ExpectedConditions.stalenessOf(ele)); } catch (Exception e1) {
							 * 
							 * }
							 */

							try {
								if (!isElementDisplayed(ele)) {
									try {
										scrollToView(webDriver, ele, mapConfigMap);
									} catch (Exception e) {
										e.printStackTrace();
									}
								}
							} catch (Exception e) {
								e.printStackTrace();
								ele = null;
							}
							if (initSAFALProperties.isHighlight) {
								try {
									highlight(webDriver, ele, userName);
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
							return ele;
						}
					} catch (Exception e) {
						e.printStackTrace();
						logger.error("in excep 9");

						ele = null;
					}

					try {
						Thread.sleep(iObjectRenderingWaitTime);
					} catch (InterruptedException e) {

					}
				} catch (Exception e) {
					logger.error("in excep 10");

					e.printStackTrace();
					ele = null;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("in excep11");

		} finally {
			timeWatcher.cancel();
		}

		logger.info("end of waitForElementPresentInstance");

		return null;
	}

	public static ExpectedCondition<Boolean> stalenessOf(final WebElement element) {
		return new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver ignored) {
				try {
					// Calling any method forces a staleness check
					if (element.isEnabled() && isElementDisplayed(element))
						;
					return false;
				} catch (StaleElementReferenceException expected) {
					return true;
				}
			}

			@Override
			public String toString() {
				return String.format("element (%s) to become stale", element);
			}
		};
	}

	public static WebElement waitForElementPresentInstanceWithOutFrame(final Map<String, String> mapConfigMap,
			final WebDriver webDriver, final String sTargetObject, final String instance, String userName) {
		try {
			boolean isMobile = false;
			;

			if (webDriver.getClass().toString().contains("AndroidDriver")
					|| webDriver.getClass().toString().contains("IOSDriver")) {
				isMobile = true;
				// return waitForMobileElementPresent(mapConfigMap, (AppiumDriver) webDriver,
				// sTargetObject, instance, userName);
			}

			int iWaitTime = DEFAULT_OBJECT_WAIT_TIME;
			if (mapConfigMap != null && null != mapConfigMap.get(OBJECT_WAIT_VARIABLE)) {
				try {
					iWaitTime = Integer.parseInt(mapConfigMap.get(OBJECT_WAIT_VARIABLE));
				} catch (NumberFormatException e) {
					iWaitTime = DEFAULT_OBJECT_WAIT_TIME;
				}
			}

			InitSAFALProperties initSAFALProperties = InitSAFALProperties.getInstance(userName);

			int iObjectRenderingWaitTime = initSAFALProperties.ObjectRenderingWaitTime;
			if (mapConfigMap != null && null != mapConfigMap.get(OBJECT_RENDERING_ITERATION_WAIT_VARIABLE)) {
				try {
					iObjectRenderingWaitTime = Integer
							.parseInt(mapConfigMap.get(OBJECT_RENDERING_ITERATION_WAIT_VARIABLE));
				} catch (NumberFormatException e) {
					iObjectRenderingWaitTime = initSAFALProperties.ObjectRenderingWaitTime;
				}
			}

			TimeWatcher timeWatcher = new TimeWatcher(iWaitTime);
			timeWatcher.startTimeWatcher();
			WebElement ele = null;
			try {
				while (!timeWatcher.isTimeUp(userName)) {
					ele = null;
					if (EMPTY_STRING.equals(instance)) {
						ele = getWebElement(webDriver, sTargetObject);
					} else {
						ele = getWebElementWithInstance(webDriver, sTargetObject, instance);
					}

					if (isMobile && ele != null) {
						return ele;
					}

					if (!isMobile && ele != null) {
						try {
							if (ele.isEnabled() && isElementDisplayed(ele)) {
							}
							;
						} catch (StaleElementReferenceException e) {
							ele = null;
						} catch (Exception e) {
							ele = null;
						}
					}

					if (!isMobile && ele != null) {
						try {
							if (!isElementDisplayed(ele)) {
								try {

									scrollToView(webDriver, ele, mapConfigMap);

								} catch (Exception e) {

								}

							}
						} catch (Exception e) {
							ele = null;
						}

						if (initSAFALProperties.isHighlight) {
							try {
								// getWebBackedSelenium(webDriver).highlight(sTargetObject);
								highlight(webDriver, ele, userName);
							} catch (Exception e) {

							}
						}
						return ele;
					} else {
						try {
							Thread.sleep(iObjectRenderingWaitTime);
						} catch (Exception e) {
						}
					}
				}
			} catch (Exception e) {
				ele = null;
			} finally {
				timeWatcher.cancel();
			}

		} catch (Exception e) {

		}

		return null;
	}

	/*
	 * public static WebElement waitForMobileElementPresent(final Map<String,
	 * String> mapConfigMap, AppiumDriver webDriver,String locator, String
	 * instance,String userName){ String sWaitTime=
	 * mapConfigMap.get(OBJECT_WAIT_VARIABLE); int iWaitTime=120; if(sWaitTime==null
	 * && sWaitTime.isEmpty()){ iWaitTime =
	 * KTTGuiConstants.DEFAULT_OBJECT_WAIT_TIME; } else{ try{
	 * iWaitTime=Integer.parseInt(sWaitTime); } catch(Exception e){ iWaitTime =
	 * KTTGuiConstants.DEFAULT_OBJECT_WAIT_TIME; } }
	 * 
	 * TimeWatcher timeWatcher = new TimeWatcher(iWaitTime);
	 * timeWatcher.startTimeWatcher();
	 * 
	 * WebElement ele=null;
	 * 
	 * while(!timeWatcher.isTimeUp(userName)){ if(instance!=null &&
	 * !instance.isEmpty()){ ele =
	 * MobileKeywordUtils.getWebElementWithInstance(webDriver, locator,instance); }
	 * else{ ele = MobileKeywordUtils.getWebElement(webDriver, locator); }
	 * if(ele!=null){ break; } try { Thread.sleep(1000); } catch
	 * (InterruptedException e) { // TODO Auto-generated catch block
	 * e.printStackTrace(); } } timeWatcher.cancel(); return ele; }
	 */

	public static WebElement waitForElementPresentAndEnabledInstance(final Map<String, String> mapConfigMap,
			final WebDriver webDriver, final String sTargetObject, final String instance, String userName) {
		Logger logger = Logger.getLogger("Thread" + Thread.currentThread().getName());
		logger.info("in waitForElementPresentAndEnabledInstance");

		boolean isMobile = false;

		if (webDriver.getClass().toString().contains("AndroidDriver")
				|| webDriver.getClass().toString().contains("IOSDriver")) {
			isMobile = true;
			// return waitForMobileElementPresent(mapConfigMap, (AppiumDriver) webDriver,
			// sTargetObject, instance, userName);
		}

		int iWaitTime = DEFAULT_OBJECT_WAIT_TIME;
		if (mapConfigMap != null && null != mapConfigMap.get(OBJECT_WAIT_VARIABLE)) {
			try {
				iWaitTime = Integer.parseInt(mapConfigMap.get(OBJECT_WAIT_VARIABLE));
			} catch (NumberFormatException e) {
				iWaitTime = DEFAULT_OBJECT_WAIT_TIME;
			}
		}

		InitSAFALProperties initSAFALProperties = InitSAFALProperties.getInstance(userName);

		int iObjectRenderingWaitTime = initSAFALProperties.ObjectRenderingWaitTime;
		if (mapConfigMap != null && null != mapConfigMap.get(OBJECT_RENDERING_ITERATION_WAIT_VARIABLE)) {
			try {
				iObjectRenderingWaitTime = Integer.parseInt(mapConfigMap.get(OBJECT_RENDERING_ITERATION_WAIT_VARIABLE));
			} catch (NumberFormatException e) {
				iObjectRenderingWaitTime = initSAFALProperties.ObjectRenderingWaitTime;
			}
		}

		int toWait_PgLoad = Integer.parseInt(KTTGuiConstants.DEFAULT_PAGE_LOAD_TIME);
		try {
			String val = mapConfigMap.get(KTTGuiConstants.PAGE_LOAD_WAIT_VARIABLE);
			if (val != null) {
				toWait_PgLoad = Integer.parseInt(val);
			}
		} catch (Exception e) {

			logger.error("Exception", e);
		}

		TimeWatcher timeWatcher = new TimeWatcher(iWaitTime);
		timeWatcher.startTimeWatcher();

		//// System.out.println("Test");

		WebElement ele = null;
		try {
			while (!timeWatcher.isTimeUp(userName)) {
				logger.info("in while 2");

				try {

					waitForPageToLoad(webDriver, toWait_PgLoad, userName);
					boolean sf = getSelectedFrames(mapConfigMap, webDriver, userName);
					if (sf == false) {
						return null;
					}

					if (EMPTY_STRING.equals(instance)) {
						ele = getWebElement(webDriver, sTargetObject);
					} else {
						ele = getWebElementWithInstance(webDriver, sTargetObject, instance);
					}
					/*
					 * isMobile= false;
					 * 
					 * if( webDriver.getClass().toString().contains("AndroidDriver")) { isMobile =
					 * true; }
					 */

					if (ele != null && !isMobile) {
						try {
							if (ele.isEnabled() && isElementDisplayed(ele)) {
							}
							;
						} catch (StaleElementReferenceException e) {
							logger.error("in excep 12");

							ele = null;
						} catch (Exception e) {
							e.printStackTrace();
							logger.error("in excep 13");

							ele = null;
						}
					} else if (ele != null && isMobile) {
						return ele;
					}

					if (!isMobile && ele != null && ele.isEnabled()) {
						try {
							if (!isElementDisplayed(ele)) {
								try {
									scrollToView(webDriver, ele, mapConfigMap);
								} catch (Exception e) {

								}
							}
						} catch (Exception e) {
							ele = null;
							e.printStackTrace();
						}

						if (initSAFALProperties.isHighlight) {
							try {
								highlight(webDriver, ele, userName);
							} catch (Exception e) {
							}
						}
						return ele;
					}
				} catch (Exception e) {
					e.printStackTrace();
					logger.error("in excep14");

					ele = null;
				}

				try {
					Thread.sleep(iObjectRenderingWaitTime);
				} catch (InterruptedException e) {
					logger.error("InterruptedException", e);
				}

			}
		} catch (Exception e) {
			logger.error("Exception", e);
			ele = null;
		} finally {
			timeWatcher.cancel();
		}

		logger.info("end of  waitForElementPresentInstanceenables ");

		return null;
	}

	public static boolean getSelectedFrames(final Map<String, String> mapConfigMap, final WebDriver webDriver,
			String userName) {
		String sFrameSelectedEnv = EMPTY_STRING;
		Keyword selectFrame = KeywordFactoryImpl.getInstance().get("SelectFrame", "false");
		String[] listOfParameters = new String[3];

		try {
			sFrameSelectedEnv = getAllSelectedFramesDriver(webDriver);
		} catch (Exception e1) {
		}

		String sFrameFlag = "TRUE";
		try {
			sFrameFlag = mapConfigMap.get(FRAME_FLAG_ENV);
		} catch (Exception e3) {

		}

		if (sFrameFlag == null || sFrameFlag.isEmpty() || sFrameFlag.equalsIgnoreCase("Y")
				|| sFrameFlag.equalsIgnoreCase("TRUE")) {
			sFrameFlag = "TRUE";
		}

		String sFrameValue = mapConfigMap.get(FRAME_NAME_ENV);
		try {
			String FramesToSelect[] = sFrameValue.split(";");
			String SelectedFrames[] = sFrameSelectedEnv.split(";");

			int selectedFramecnt = 0;
			boolean mainpageFlag = false;
			boolean sequenceCheck = false;

			if (sFrameValue.isEmpty()) {
				try {
					listOfParameters[0] = "NULL";
					listOfParameters[1] = "1";
					listOfParameters[2] = "TRUE";
					TestcaseExecutionResultVO sfVo = selectFrame.execute(null, webDriver, mapConfigMap, null, userName,
							listOfParameters);
					if (sfVo.getStatus() == 1) {
						return true;
					} else {
						return false;
					}
				} catch (Exception e) {
					return false;
				}
			}

			if (!sFrameValue.equals(sFrameSelectedEnv)) {
				for (int i = 0; i < FramesToSelect.length; i++) {
					if (i < SelectedFrames.length) {
						if (FramesToSelect[i].equals(SelectedFrames[i])) {
							// System.out.println("Frame is already selected: "+SelectedFrames[i]);

							selectedFramecnt = selectedFramecnt + 1;

							if (selectedFramecnt == SelectedFrames.length) {
								sequenceCheck = true;
							}

							continue;
						} else {
							sequenceCheck = false;
							break;
						}
					}

					if (selectedFramecnt == 0 && !mainpageFlag && !sequenceCheck) {
						//// System.out.println("Selecting the main page..");
						try {
							listOfParameters[0] = "NULL";
							listOfParameters[1] = "1";
							listOfParameters[2] = "FALSE";
							TestcaseExecutionResultVO sfVo = selectFrame.execute(null, webDriver, mapConfigMap, null,
									userName, listOfParameters);
						} catch (Exception e) {
						}
						mainpageFlag = true;
					}
					//// System.out.println("Selecting the frame: "+ FramesToSelect[i]);
					listOfParameters[0] = FramesToSelect[i];
					listOfParameters[1] = "1";
					listOfParameters[2] = "TRUE";
					TestcaseExecutionResultVO sfVo = selectFrame.execute(null, webDriver, mapConfigMap, null, userName,
							listOfParameters);
					if (sfVo.getStatus() == 0) {
						if (sFrameFlag.equalsIgnoreCase("TRUE")) {
							return true;
						} else {
							return false;
						}
					}
				}

				if (!sequenceCheck) {
					//// System.out.println("Selecting the main page..");
					try {
						listOfParameters[0] = "NULL";
						listOfParameters[1] = "1";
						listOfParameters[2] = "FALSE";
						TestcaseExecutionResultVO sfVo = selectFrame.execute(null, webDriver, mapConfigMap, null,
								userName, listOfParameters);
					} catch (Exception e) {
					}

					for (int i = 0; i < FramesToSelect.length; i++) {
						//// System.out.println("Selecting the frame: "+ FramesToSelect[i]);
						listOfParameters[0] = FramesToSelect[i];
						listOfParameters[1] = "1";
						listOfParameters[2] = "TRUE";
						TestcaseExecutionResultVO sfVo = selectFrame.execute(null, webDriver, mapConfigMap, null,
								userName, listOfParameters);
						if (sfVo.getStatus() == 0) {
							if (sFrameFlag.equalsIgnoreCase("TRUE")) {
								return true;
							} else {
								return false;
							}
						}
					}
				}
			}

		} catch (Exception e) {
			if (sFrameFlag.equalsIgnoreCase("TRUE")) {
				return true;
			} else {
				return false;
			}
		}
		return true;
	}

	// SAF-2741 added method to wait for table to appear (Synchronization)
	public static WebElement waitForTableToPresent(final Map<String, String> mapConfigMap, final WebDriver webDriver,
			final String sTargetObject, String instance, String userName) {

		if (sTargetObject.contains("|") || !sTargetObject.startsWith("@")) {
			if (instance.isEmpty() || instance.equals("0")) {
				instance = "0";
			} else {
				try {
					int inst = Integer.parseInt(instance);
					inst--;
					instance = inst + "";
				} catch (Exception e) {
					instance = "0";
				}
			}
			return KeywordUtilities.waitForTablePresentAndEnabledInstance(mapConfigMap, webDriver, sTargetObject,
					instance, userName);

		} else {
			if (instance.equals("0") || instance.isEmpty()) {
				instance = "1";
			}
			return KeywordUtilities.waitForElementPresentInstance(mapConfigMap, webDriver, sTargetObject, instance,
					userName);
		}

	}

	public static WebElement waitForTablePresentAndEnabledInstance(final Map<String, String> mapConfigMap,
			final WebDriver webDriver, final String sTargetObject, final String instance, String userName) {

		int iWaitTime = DEFAULT_OBJECT_WAIT_TIME;
		if (mapConfigMap != null && null != mapConfigMap.get(OBJECT_WAIT_VARIABLE)) {
			try {
				iWaitTime = Integer.parseInt(mapConfigMap.get(OBJECT_WAIT_VARIABLE));
			} catch (NumberFormatException e) {
				iWaitTime = DEFAULT_OBJECT_WAIT_TIME;
			}
		}

		InitSAFALProperties initSAFALProperties = InitSAFALProperties.getInstance(userName);

		int iObjectRenderingWaitTime = initSAFALProperties.ObjectRenderingWaitTime;
		if (mapConfigMap != null && null != mapConfigMap.get(OBJECT_RENDERING_ITERATION_WAIT_VARIABLE)) {
			try {
				iObjectRenderingWaitTime = Integer.parseInt(mapConfigMap.get(OBJECT_RENDERING_ITERATION_WAIT_VARIABLE));
			} catch (NumberFormatException e) {
				iWaitTime = initSAFALProperties.ObjectRenderingWaitTime;
			}
		}

		int inst = 0;
		try {
			inst = Integer.parseInt(instance);
		} catch (NumberFormatException e) {
			inst = 1;
		}
		TimeWatcher timeWatcher = new TimeWatcher(iWaitTime);
		timeWatcher.startTimeWatcher();

		try {
			while (!timeWatcher.isTimeUp(userName)) {
				try {

					boolean sf = getSelectedFrames(mapConfigMap, webDriver, userName);

					if (sf == false) {
						return null;
					}

					try {
						WebElement ele = getTableByColumnHeader(sTargetObject, inst, webDriver, mapConfigMap);
						if (ele != null) {
							try {
								if (ele.isEnabled() && isElementDisplayed(ele)) {
								}
								;
							} catch (StaleElementReferenceException e) {
								ele = null;
							} catch (Exception e) {
								ele = null;
							}
						}

						if (ele != null && ele.isEnabled()) {
							try {
								if (!isElementDisplayed(ele)) {
									try {
										scrollToView(webDriver, ele, mapConfigMap);
									} catch (Exception e) {

									}
								}
							} catch (Exception e) {
								ele = null;
							}

							if (initSAFALProperties.isHighlight) {
								try {
									// getWebBackedSelenium(webDriver).highlight(sTargetObject);
									highlight(webDriver, ele, userName);
								} catch (Exception e) {

								}
							}
							return ele;
						}
					} catch (Exception e) {

					}

					try {
						Thread.sleep(iObjectRenderingWaitTime);
					} catch (InterruptedException e) {

					}
				} catch (Exception e) {

				}
			}
		} catch (Exception e) {
		} finally {
			timeWatcher.cancel();
		}
		return null;
	}

	public static boolean waitForElementPresent(final Map<String, String> mapConfigMap, final WebDriver webDriver,
			final String sTargetObject, final String instance, String userName) {
		InitSAFALProperties initSAFALProperties = InitSAFALProperties.getInstance(userName);

		int iWaitTime = DEFAULT_OBJECT_WAIT_TIME;
		if (mapConfigMap != null && null != mapConfigMap.get(OBJECT_WAIT_VARIABLE)) {
			try {
				iWaitTime = Integer.parseInt(mapConfigMap.get(OBJECT_WAIT_VARIABLE));
			} catch (NumberFormatException e) {
				iWaitTime = DEFAULT_OBJECT_WAIT_TIME;
			}
		}

		int iObjectRenderingWaitTime = initSAFALProperties.ObjectRenderingWaitTime;
		if (mapConfigMap != null && null != mapConfigMap.get(OBJECT_RENDERING_ITERATION_WAIT_VARIABLE)) {
			try {
				iObjectRenderingWaitTime = Integer.parseInt(mapConfigMap.get(OBJECT_RENDERING_ITERATION_WAIT_VARIABLE));
			} catch (NumberFormatException e) {
				iObjectRenderingWaitTime = initSAFALProperties.ObjectRenderingWaitTime;
			}
		}

		int reqInstance = 1;

		try {
			reqInstance = Integer.parseInt(instance);
		} catch (NumberFormatException e) {
			reqInstance = 1;
		}

		int actualInstance = 0;

		TimeWatcher timeWatcher = new TimeWatcher(iWaitTime);
		timeWatcher.startTimeWatcher();

		try {
			while (!timeWatcher.isTimeUp(userName)) {
				try {

					boolean sf = getSelectedFrames(mapConfigMap, webDriver, userName);

					if (sf == false) {
						return false;
					}

					try {
						if (isElementPresent(webDriver, sTargetObject)) {
							actualInstance = actualInstance + 1;

							if (actualInstance == reqInstance) {
								try {
									if (!isVisible(webDriver, sTargetObject)) {
										scrollToView(webDriver, sTargetObject, mapConfigMap);
									}
								} catch (Exception e) {
								}

								if (initSAFALProperties.isHighlight) {
									try {
										highlight(webDriver, sTargetObject, userName);
									} catch (Exception e) {

									}
								}
								return true;
							}

						}
					} catch (Exception e) {

					}
					try {
						Thread.sleep(iObjectRenderingWaitTime);
					} catch (InterruptedException e) {
					}
				} catch (Exception e) {

				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			timeWatcher.cancel();
		}

		return false;

	}

	public static boolean waitForElementPresent(final Map<String, String> mapConfigMap, final WebDriver webDriver,
			final String sTargetObject, String userName) {

		int iWaitTime = DEFAULT_OBJECT_WAIT_TIME;
		if (mapConfigMap != null && null != mapConfigMap.get(OBJECT_WAIT_VARIABLE)) {
			try {
				iWaitTime = Integer.parseInt(mapConfigMap.get(OBJECT_WAIT_VARIABLE));
			} catch (NumberFormatException e) {
				iWaitTime = DEFAULT_OBJECT_WAIT_TIME;
			}
		}

		InitSAFALProperties initSAFALProperties = InitSAFALProperties.getInstance(userName);

		int iObjectRenderingWaitTime = initSAFALProperties.ObjectRenderingWaitTime;
		if (mapConfigMap != null && null != mapConfigMap.get(OBJECT_RENDERING_ITERATION_WAIT_VARIABLE)) {
			try {
				iObjectRenderingWaitTime = Integer.parseInt(mapConfigMap.get(OBJECT_RENDERING_ITERATION_WAIT_VARIABLE));
			} catch (NumberFormatException e) {
				iObjectRenderingWaitTime = initSAFALProperties.ObjectRenderingWaitTime;
			}
		}

		TimeWatcher timeWatcher = new TimeWatcher(iWaitTime);
		timeWatcher.startTimeWatcher();

		try {
			while (!timeWatcher.isTimeUp(userName)) {
				try {

					boolean sf = getSelectedFrames(mapConfigMap, webDriver, userName);

					if (sf == false) {
						return false;
					}

					try {
						if (isElementPresent(webDriver, sTargetObject)) {
							try {
								if (!isVisible(webDriver, sTargetObject)) {
									scrollToView(webDriver, sTargetObject, mapConfigMap);
								}
							} catch (Exception e) {
							}

							if (initSAFALProperties.isHighlight) {
								try {
									highlight(webDriver, sTargetObject, userName);
								} catch (Exception e) {

								}
							}
							return true;
						}
					} catch (Exception e) {

					}

					try {
						Thread.sleep(iObjectRenderingWaitTime);
					} catch (InterruptedException e) {

					}
				} catch (Exception e) {
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			timeWatcher.cancel();
		}

		return false;

	}

	public static void highlight(WebDriver driver, String sObject, String userName) {
		try {
			InitSAFALProperties initSAFALProperties = InitSAFALProperties.getInstance(userName);

			if (initSAFALProperties.isHighlight) {
				String Scolor = initSAFALProperties.getBackground();
				int iBlinkCount = initSAFALProperties.getBlinkCount();

				JavascriptExecutor js = ((JavascriptExecutor) driver);
				WebElement elementtoBlink = getWebElement(driver, sObject);
				if (elementtoBlink != null) {
					String orgBorder = EMPTY_STRING;
					try {
						orgBorder = elementtoBlink.getCssValue("border");
					} catch (Exception e1) {
					}

					for (int i = 0; i < iBlinkCount; i++) {
						try {
							js.executeScript("arguments[0].style.border = '3px solid " + Scolor + "'", elementtoBlink);
							Thread.sleep(50);
							js.executeScript("arguments[0].style.border = '" + orgBorder + "'", elementtoBlink);
						} catch (Exception e) {
						}
					}

				}
			} else {
				return;
			}

		} catch (Exception e) {
		}

	}

	public static void highlight(WebDriver driver, WebElement elementtoBlink, String userName) {
		try {

			InitSAFALProperties initSAFALProperties = InitSAFALProperties.getInstance(userName);

			if (initSAFALProperties.isHighlight) {
				String Scolor = initSAFALProperties.getBackground();
				int iBlinkCount = initSAFALProperties.getBlinkCount();
				JavascriptExecutor js = ((JavascriptExecutor) driver);
				if (elementtoBlink != null) {
					String orgBorder = EMPTY_STRING;
					try {
						orgBorder = elementtoBlink.getCssValue("border");
					} catch (Exception e1) {
					}

					for (int i = 0; i < iBlinkCount; i++) {
						try {
							js.executeScript("arguments[0].style.border = '3px solid " + Scolor + "'", elementtoBlink);
							Thread.sleep(50);
							js.executeScript("arguments[0].style.border = '" + orgBorder + "'", elementtoBlink);
						} catch (Exception e) {
						}
					}

				}
			} else {
				return;
			}

		} catch (Exception e) {
		}

	}

	public static String getRGB(String bgcolorString) {

		String finalRGB = EMPTY_STRING;

		String part1, part4;
		try {
			part1 = "";
			part4 = "";
			String[] part2 = {};

			part1 = bgcolorString.substring(0, 3);
			String requiredString = bgcolorString.substring(bgcolorString.indexOf("(") + 1, bgcolorString.indexOf(")"));
			part2 = requiredString.split(",");
			for (int p = 0; p < part2.length - 1; p++) {
				if (part4.isEmpty()) {
					part4 = part2[p];
				} else {
					part4 = part4 + "," + part2[p];
				}
			}
			finalRGB = part1 + "(" + part4 + ")";
			// finalRGB=part4;
			if (finalRGB.contains(" ")) {
				finalRGB = finalRGB.replaceAll(" ", "");
			}
		} catch (Exception e) {

		}

		return finalRGB;
	}

	public static void changeColor(String color, WebElement elementisGoingToBlink, JavascriptExecutor js) {
		js.executeScript("arguments[0].style.border = '2px solid \"" + color + "\"'", elementisGoingToBlink);
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
		}
	}

	public static void scrollToView(WebDriver driver, WebElement sObject) {
		try {
			JavascriptExecutor js = ((JavascriptExecutor) driver);
			if (sObject != null) {
				try {
					js.executeScript("window.focus();");
				} catch (Exception e1) {
				}

				try {
					js.executeScript("arguments[0].scrollIntoView(true);", sObject);
				} catch (Exception e1) {
				}

				try {
					js.executeScript("arguments[0].parentNode.scrollTop = arguments[0].target.offsetTop;", sObject);
				} catch (Exception e1) {
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void scrollToView(WebDriver driver, WebElement sObject, final Map<String, String> mapConfigMap) {
		try {
			String sScrollToViewElementFlag = "TRUE";
			try {
				sScrollToViewElementFlag = mapConfigMap.get("ScrollElementToView");
			} catch (Exception e3) {
			}

			if (sScrollToViewElementFlag == null || sScrollToViewElementFlag.isEmpty()
					|| sScrollToViewElementFlag.equalsIgnoreCase("Y")
					|| sScrollToViewElementFlag.equalsIgnoreCase("TRUE")) {
				sScrollToViewElementFlag = "TRUE";
			}

			if (sScrollToViewElementFlag.equals("TRUE")) {

				JavascriptExecutor js = ((JavascriptExecutor) driver);
				if (sObject != null) {
					try {
						js.executeScript("window.focus();");
					} catch (Exception e1) {
					}

					try {
						js.executeScript("arguments[0].scrollIntoView(true);", sObject);
					} catch (Exception e1) {
					}

					try {
						js.executeScript("arguments[0].parentNode.scrollTop = arguments[0].target.offsetTop;", sObject);
					} catch (Exception e1) {
					}

				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void scrollToView(WebDriver driver, String sObject, final Map<String, String> mapConfigMap) {
		try {

			String sScrollToViewElementFlag = "TRUE";
			try {
				sScrollToViewElementFlag = mapConfigMap.get("ScrollElementToView");
			} catch (Exception e3) {
			}

			if (sScrollToViewElementFlag == null || sScrollToViewElementFlag.isEmpty()
					|| sScrollToViewElementFlag.equalsIgnoreCase("Y")
					|| sScrollToViewElementFlag.equalsIgnoreCase("TRUE")) {
				sScrollToViewElementFlag = "TRUE";
			}

			if (sScrollToViewElementFlag.equals("TRUE")) {

				JavascriptExecutor js = ((JavascriptExecutor) driver);
				WebElement element = getWebElement(driver, sObject);
				if (element != null) {
					try {
						js.executeScript("window.focus();");
					} catch (Exception e1) {
					}

					try {
						js.executeScript("arguments[0].scrollIntoView(true);", element);
					} catch (Exception e1) {
					}

					try {
						js.executeScript("arguments[0].parentNode.scrollTop = arguments[0].target.offsetTop;", element);
					} catch (Exception e1) {
					}

				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static boolean waitForElementVisible(Map<String, String> mapConfigMap, final WebDriver webDriver,
			final String sTargetObject, String userName) {

		int iWaitTime = DEFAULT_OBJECT_WAIT_TIME;

		if (mapConfigMap != null && null != mapConfigMap.get(OBJECT_WAIT_VARIABLE)) {
			try {
				iWaitTime = Integer.parseInt(mapConfigMap.get(OBJECT_WAIT_VARIABLE));
			} catch (NumberFormatException e) {
				iWaitTime = DEFAULT_OBJECT_WAIT_TIME;
			}
		}

		InitSAFALProperties initSAFALProperties = InitSAFALProperties.getInstance(userName);

		int iObjectRenderingWaitTime = initSAFALProperties.ObjectRenderingWaitTime;
		if (mapConfigMap != null && null != mapConfigMap.get(OBJECT_RENDERING_ITERATION_WAIT_VARIABLE)) {
			try {
				iObjectRenderingWaitTime = Integer.parseInt(mapConfigMap.get(OBJECT_RENDERING_ITERATION_WAIT_VARIABLE));
			} catch (NumberFormatException e) {
				iObjectRenderingWaitTime = initSAFALProperties.ObjectRenderingWaitTime;
			}
		}

		TimeWatcher timeWatcher = new TimeWatcher(iWaitTime);
		timeWatcher.startTimeWatcher();

		boolean sf = getSelectedFrames(mapConfigMap, webDriver, userName);

		if (sf == false) {
			return false;
		}

		try {
			while (!timeWatcher.isTimeUp(userName)) {
				try {
					try {
						if (isElementPresent(webDriver, sTargetObject)) {
							try {
								if (!isVisible(webDriver, sTargetObject)) {
									scrollToView(webDriver, sTargetObject, mapConfigMap);
								}
							} catch (Exception e) {
							}

							if (initSAFALProperties.isHighlight) {
								try {
									highlight(webDriver, sTargetObject, userName);
								} catch (Exception e) {

								}
							}
							return true;
						}
					} catch (Exception e) {
					}
					try {
						Thread.sleep(iObjectRenderingWaitTime);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				} catch (Exception e) {
				}
			}
		} catch (Exception e) {
		} finally {
			timeWatcher.cancel();
		}

		return false;

	}

	public static boolean waitForObjectPresentTA(Map<String, String> mapConfigMap, WebDriver webDriver, String objName,
			String userName) {

		int iWaitTime = DEFAULT_OBJECT_WAIT_TIME;

		if (mapConfigMap != null && null != mapConfigMap.get(OBJECT_WAIT_VARIABLE)) {
			try {
				iWaitTime = Integer.parseInt(mapConfigMap.get(OBJECT_WAIT_VARIABLE));
			} catch (NumberFormatException e) {
				iWaitTime = DEFAULT_OBJECT_WAIT_TIME;
			}
		}

		InitSAFALProperties initSAFALProperties = InitSAFALProperties.getInstance(userName);

		int iObjectRenderingWaitTime = initSAFALProperties.ObjectRenderingWaitTime;
		if (mapConfigMap != null && null != mapConfigMap.get(OBJECT_RENDERING_ITERATION_WAIT_VARIABLE)) {
			try {
				iObjectRenderingWaitTime = Integer.parseInt(mapConfigMap.get(OBJECT_RENDERING_ITERATION_WAIT_VARIABLE));
			} catch (NumberFormatException e) {
				iObjectRenderingWaitTime = initSAFALProperties.ObjectRenderingWaitTime;
			}
		}

		String checkObject =

				"		fun();																		 	 						"
						+ "																												"
						+ " 		function fun()																							"
						+ "   	{																										"
						+ "		 	 var nameObj = \"" + objName
						+ "\";																		"
						+ "		 	 var CurrentWindowDoc = document;							"
						+ "			 try																								"
						+ "				{																								"
						+ "		 			 var app=CurrentWindowDoc.getElementsByTagName(\"APPLET\");									"
						+ "					 var appScriptObj = app[0].getScriptObject();												"
						+ "					 var object = appScriptObj.findObject(nameObj);												"
						+ "					 if(object==null)																			"
						+ "					 {																							"
						+ "					 	return 0;																				"
						+ "					 }																							"
						+ "					 else																						"
						+ "					 {																							"
						+ "						 return 1;																				"
						+ "					 }																							"
						+ "				}																								"
						+ "			catch(e)																							"
						+ "				{																								"
						+ "					return 0;																					"
						+ "				}																								"
						+ "		}																										";

		for (int waitTime = 0;; waitTime++) {

			if (SAFALUserSession.getSciptStatus(userName) == ScriptStatus.TO_BE_STOPPED) {

				return false;
			}

			String res = "";

			try {
				res = ((JavascriptExecutor) webDriver).executeScript("return " + checkObject).toString();
			} catch (Exception e) {
				res = "FAIL";
			}

			if (res.equals("1")) {
				return true;
			}

			if (waitTime >= iWaitTime) {

				return false;
			}

			try {
				Thread.sleep(iObjectRenderingWaitTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public static String parseParamValue(Map<String, String> mapConfigMap, String sParamValue) {

		int iStartEnvInd = -1;
		int iEndEnvIndex = -1;
		int iLastIndex = -5;

		String sEnvVariable = null;
		String sResultString = KTTGuiConstants.EMPTY_STRING;
		String sFixString = KTTGuiConstants.EMPTY_STRING;
		if (mapConfigMap == null) {
			return "TOOL_CONFIG_MAP_ERROR";
		}

		do {

			iStartEnvInd = sParamValue.indexOf("<ENV>", iLastIndex);

			if (iStartEnvInd == -1) {
				if (iLastIndex != sParamValue.length()) {
					sResultString = sResultString + sParamValue.substring(iLastIndex + 5, sParamValue.length());
				}
				break;
			}

			sFixString = sParamValue.substring(iLastIndex + 5, iStartEnvInd);

			iEndEnvIndex = sParamValue.indexOf("</ENV>", iStartEnvInd);

			if (iEndEnvIndex == -1) {
				// Must added the & sign for concatenate string when identifying <ENV> values
				if (sParamValue.contains("&") && sParamValue.contains("Counter")) {
					sEnvVariable = Concatenate(sParamValue, mapConfigMap);
					return sEnvVariable;
				} else {
					sEnvVariable = sParamValue.substring(iStartEnvInd + 5, sParamValue.length());
					iStartEnvInd = -1;
				}
				// end not found
			} else {
				sEnvVariable = sParamValue.substring(iStartEnvInd + 5, iEndEnvIndex);
				iLastIndex = iEndEnvIndex + 1;
			}

			// variable not found
			if (null == mapConfigMap.get(sEnvVariable)) {// SAF-257 removed 'mapConfigMap == null ||' - Amit

				// error
				return "TOOL_CONFIG_MAP_ERROR";

			}

			sEnvVariable = mapConfigMap.get(sEnvVariable);
			sResultString = sResultString + sFixString + sEnvVariable;

		} while (iStartEnvInd != -1);

		return sResultString;
	}

	public static String getTableByXPath(String keyword) {

		String sTableByXPath = "		 if(iXpathFlag==1)																						"
				+ "		 {																										"
				+ "			var rtbl=fd.getElementById(\"" + keyword
				+ "\");														" +
				// " alert(\"Got table by xpath : \"+rtbl);"+
				"			if(rtbl==null)																						"
				+ "			{																									"
				+ "				return \"" + ERROR_TABLE_NOT_FOUND
				+ "\";															"
				+ "		 	}																									"
				+ "		 }																										";

		return sTableByXPath;
	}

	public static String formLocator(String actualLocator, String objName) {

		String sFinalXPath = "//LOC[OBJ]";

		if (objName.startsWith(OBJECT_SPECIFIER)) {
			sFinalXPath = sFinalXPath.replaceFirst("LOC", actualLocator);
			sFinalXPath = sFinalXPath.replaceFirst("OBJ", objName);
			sFinalXPath = KeywordUtilities.xpathFormation(sFinalXPath);
		} else {
			sFinalXPath = KeywordUtilities.xpathFormation(objName);
		}
		return sFinalXPath;

	}

	private static String xpathFormation(String sXpath) {
		String finalString = "", temp = "";
		String xpath[] = sXpath.split("//");
		for (int i = 1; i < xpath.length; i++) {
			String name[] = xpath[i].split("=");

			for (int j = 0; j < name.length;) // SAF-257 Removed Dead Code 'j++' - Amit
			{
				finalString = finalString.concat("//");
				finalString = finalString.concat(name[j]);
				finalString = finalString.concat("='");
				finalString = finalString.concat(name[j + 1]);
				break;
			}
		}
		String xpath1[] = finalString.split("]");
		for (int i = 0; i < xpath1.length; i++) {
			temp = temp.concat(xpath1[i]);
			temp = temp.concat("']");
		}

		return temp;

	}

	private static WebElement getTableByColumnHeader(String headerData, int tInstance, WebDriver webDriver,
			final Map<String, String> mapConfigMap) {
		WebElement reqTable = null;

		String getTableByColumnHeader = "		getWebTableUsingCol();function mytrim(str){return str.replace(/^\\s+|\\s+$/g, '');}"
				+ "		function getWebTableUsingCol() 																									"
				+ "		 {																										"
				+

				"			var HeaderData=\"" + headerData + "\";" + "			var iInstance=" + tInstance + ";"
				+ "			var tableInstTracker=-1;" + "var rTableId;"
				+ "			var allTables=document.getElementsByTagName(\"TABLE\");													"
				+ "           var SplitedHeaderData=HeaderData.split(\"|\");													"
				+ " 		    var SplitedHeaderDataCountLen=SplitedHeaderData.length;									"
				+ "var isFound=1;" +

				"			var targetTableNum=0;var isTableFound=false;"
				+ " 			 for (var i = 0; i < allTables.length; i++) 											"
				+ "			 {																									"
				+ "		 		 try{																							"
				+ "			 	var rtbl1=allTables[i];;																			"
				+ "				 if(rtbl1.rows.length>0)																		"
				+ "				 {																								"
				+ " 					var noOfRows=rtbl1.rows.length;" +

				"						var tRows=rtbl1.getElementsByTagName(\"tr\");" + // Do not use rtbl1.rows as
																							// some tables has thead
																							// defined outside of tbody
																							// tag
				" 					for(var rowCnt=0;rowCnt<noOfRows;rowCnt++){ "
				+ "						var noOfColums=tRows[rowCnt].cells.length;"
				+ "						var matchedColumns=0;"
				+ " 						if(tRows[rowCnt].innerText.indexOf(SplitedHeaderData[0])!=-1 && SplitedHeaderData.length <=noOfColums){							"
				+ "							for(var splitCnt=0;splitCnt<SplitedHeaderData.length;splitCnt++){"
				+ "								var flag=0;"
				+ "								for(var colCnt=0;colCnt<noOfColums;colCnt++){"
				+ "									var cellObj=tRows[rowCnt].cells[colCnt];" +

				"									if(mytrim(cellObj.innerText)==mytrim(SplitedHeaderData[splitCnt])){"
				+ "										flag=1;matchedColumns=matchedColumns+1;break;"
				+ "									}else{flag=0;}" + "								}"
				+ "						if(flag==0){" + "							break;}"
				+ "						}" + "					}"
				+ "					if(matchedColumns==SplitedHeaderData.length){tableInstTracker=tableInstTracker+1;}"
				+ " 					if(tableInstTracker==iInstance){"
				+ "						return allTables[i];" + "					}" + "				}" +
				// " }"+
				"			}" + "			}catch(erro1){" + "			}" +

				"		}" + "return null;"
				+ "}																												";

		String sgetBrowser = mapConfigMap.get("SAFAL_BROWSER_NAME");

		if (sgetBrowser == null) {
			sgetBrowser = EMPTY_STRING;
		}

		if (!sgetBrowser.toUpperCase().contains(KTTGuiConstants.INTERNET_EXPLORER_STRING)) {
			getTableByColumnHeader = getTableByColumnHeader.replaceAll("innerText", "textContent");
		}

		try {
			reqTable = (WebElement) ((JavascriptExecutor) webDriver).executeScript("return " + getTableByColumnHeader);
		} catch (StaleElementReferenceException e) {
			reqTable = null;
		} catch (Exception e) {
			reqTable = null;
		}

		return reqTable;

	}

	public static String getTableByColumnHeaderOrColumnCount() {
		String getTableByColumnHeader = "		 else																									"
				+ "		 {																										"
				+ "			var allTables=fd.getElementsByTagName(\"TABLE\");													"
				+ "           var SplitedHeaderData=HeaderData.split(\"|\");														"
				+ " 		    var SplitedHeaderDataCountLen=SplitedHeaderData.length;												"
				+

				"		 	for(j=0;j<iInstance;j++)																			"
				+ "		 	{																									"
				+ " 			 for (var i = lastRTableId+1; i < allTables.length; i++) 											"
				+ "			 {																									"
				+ "		 		 try{																							"
				+ "			 	var rtbl1=allTables[i];																			"
				+ "				 if(rtbl1.rows.length>0)																		"
				+ "				 {																								"
				+ "   			 var inp=rtbl1.rows[iHeader].cells;																"
				+ "   			 var xxx=rtbl1.rows[iHeader].cells;																"
				+ "				 var SplitedHeaderDataCount=0;																	"
				+ "				 if(iDFlag==1)																					"
				+ "					{																							"
				+ "						ColCount=rtbl1.rows[iHeader].cells.length;												"
				+ "						SplitedHeaderDataCount=0;																"
				+ "						for(y=0;y<inp.length;y++)																"
				+ "							{																					"
				+ "								if((SplitedHeaderDataCount!=SplitedHeaderDataCountLen)&&						"
				+ "								(trim(xxx[y].innerText)==trim(SplitedHeaderData[SplitedHeaderDataCount])))	"
				+ "								{																				"
				+ "									SplitedHeaderDataCount=SplitedHeaderDataCount+1;							"
				+ "								}																				"
				+ "							}																					"
				+ "						if(SplitedHeaderDataCount==SplitedHeaderDataCountLen)									"
				+ "							{																					"
				+
				// " alert(\"Got Table\"); "+
				"									rTableId = i;																"
				+ "									lastRTableId=rTableId;														"
				+ "									break;																		"
				+ "							}																					"
				+ "					}																							"
				+ "			 	else																							"
				+ "			  	  {																								"
				+ " 						if(inp.length==ColCount)																"
				+ "							{																					"
				+ "								rTableId = i;																	"
				+ "								break;																			"
				+ "							}																					"
				+ "					}																							"
				+ "				}																								"
				+ "	  	  	}																									"
				+ "			catch(error1)																						"
				+ "			{																									"
				+
				// "alert(error1.description); "+
				"			}																									"
				+ "		}																										"
				+ "	}																											"
				+ "		 if(rTableId==-1)																						"
				+ "		 {																										"
				+ "			return \"" + ERROR_TABLE_NOT_FOUND
				+ "\";																"
				+ "		 }																										"
				+ "  		var rtbl=allTables[rTableId];																			"
				+ "}																												";

		return getTableByColumnHeader;

	}

	public static int convertStringToInt(String string) {
		try {
			string = string.trim();
			return Integer.parseInt(string);
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	/*
	 * public static WebDriverBackedSelenium getWebBackedSelenium(WebDriver
	 * webDriver) { WebDriverBackedSelenium selenium =null; try { selenium = new
	 * WebDriverBackedSelenium(webDriver, webDriver.getCurrentUrl()); } catch
	 * (Exception e1) { selenium=null; } return selenium; }
	 */

	public static void waitForPageToLoadFromIcon(WebDriver webDriverAlert, int toWait, Map<String, String> mapConfigMap,
			String userName) {
		String CheckForLoadingIcon = "";
		try {
			CheckForLoadingIcon = mapConfigMap.get("CheckFor_LoadingIcon");
		} catch (Exception e2) {
			e2.printStackTrace();
		}
		try {
			if (CheckForLoadingIcon.equalsIgnoreCase("Y") || CheckForLoadingIcon.equalsIgnoreCase("YES")
					|| CheckForLoadingIcon.equalsIgnoreCase("True")) {

				TimeWatcher timeWatcher = new TimeWatcher(toWait);
				timeWatcher.startTimeWatcher();
				String loadingIconIdentifier = mapConfigMap.get("loadingIcon_Identifier");
				String LoadedIcon = "";

				try {
					while (!timeWatcher.isTimeUp(userName) && webDriverAlert != null) {
						while (KeywordUtilities.isAlertPresent(webDriverAlert)) {
							return;
						}
						try {
							LoadedIcon = mapConfigMap.get("loadedIcon_Identifier");
						} catch (Exception e1) {
							e1.printStackTrace();
						}
						WebElement PageloadedIcon = null;

						if (loadingIconIdentifier.startsWith(OBJECT_SPECIFIER)) {
							loadingIconIdentifier = loadingIconIdentifier.substring(OBJECT_SPECIFIER.length(),
									loadingIconIdentifier.length());
						}
						/*
						 * isElementPresent(webDriverAlert,loadingIconIdentifier,"1"); WebElement
						 * loadingIcon = getWebElement(webDriverAlert, loadingIconIdentifier);
						 */
						WebElement loadingIcon = getWebElementWithInstance(webDriverAlert, loadingIconIdentifier, "1");
						if (!(LoadedIcon == null || LoadedIcon.equalsIgnoreCase(""))) {
							if (LoadedIcon.startsWith(OBJECT_SPECIFIER)) {
								LoadedIcon = LoadedIcon.substring(OBJECT_SPECIFIER.length(), LoadedIcon.length());
							}
							// PageloadedIcon = getWebElement(webDriverAlert, LoadedIcon);
							PageloadedIcon = getWebElementWithInstance(webDriverAlert, LoadedIcon, "1");
							if (loadingIcon == null && PageloadedIcon != null) {
								break;
							}
						} else {
							if (loadingIcon == null) {
								break;
							}
						}
					}
				} catch (Exception e) {

				} finally {
					timeWatcher.cancel();
				}
			}
		} catch (Exception e) {

		}

	}

	// ===================================================================================================================================
	public static WebElement getWebElement(WebDriver webDriver, String seleniumObject) {
		WebElement webElement = null;
		if (seleniumObject.startsWith(OBJECT_SPECIFIER)) {
			seleniumObject = seleniumObject.replaceFirst(OBJECT_SPECIFIER, EMPTY_STRING);
		}
		try {

			String webElementString = EMPTY_STRING;

			String[] selObjects = null;
			String selObject = EMPTY_STRING;
			try {
				selObjects = seleniumObject.split("=");
				selObject = selObjects[0].toUpperCase() + "=";
			} catch (Exception e2) {
			}
			switch (selObject) {
			case ID:
				seleniumObject.toUpperCase().startsWith(ID);
				webElementString = seleniumObject.substring(ID.length());
				try {
					webElement = webDriver.findElement(By.id(webElementString));
				} catch (Exception e) {
					try {
						webElement = webDriver.findElement(By.xpath("//*[@id='" + webElementString + "']"));
					} catch (StaleElementReferenceException e2) {
						webElement = null;
					}
				}
				break;
			case NAME:
				seleniumObject.toUpperCase().startsWith(NAME);
				webElementString = seleniumObject.substring(NAME.length());
				webElement = webDriver.findElement(By.name(webElementString));
				break;
			case XPATH:
				seleniumObject.toUpperCase().startsWith(XPATH);
				webElementString = seleniumObject.substring(XPATH.length());
				webElement = webDriver.findElement(By.xpath(webElementString));
				break;
			case "CSS=":
				seleniumObject.toUpperCase().startsWith("CSS=");
				webElementString = seleniumObject.substring("CSS=".length());
				webElement = webDriver.findElement(By.cssSelector(webElementString));
				break;
			case CLASS:
				seleniumObject.toUpperCase().startsWith(CLASS);
				webElementString = seleniumObject.substring(CLASS.length());
				webElement = webDriver.findElement(By.className(webElementString));
				break;
			case WDLINK:
				seleniumObject.toUpperCase().startsWith(WDLINK);
				webElementString = seleniumObject.substring(WDLINK.length());
				try {
					webElement = webDriver.findElement(By.linkText(webElementString));
				} catch (StaleElementReferenceException e2) {

					webElement = null;
				}
				if (webElement == null) {
					webElement = webDriver.findElement(By.partialLinkText(webElementString));
				}
				break;
			default:
				try {
					webElement = webDriver.findElement(By.name(seleniumObject));
				} catch (Exception e) {
					try {
						webElement = webDriver.findElement(By.id(seleniumObject));
					} catch (Exception e1) {
						try {
							webElement = webDriver.findElement(By.xpath("//*[@id='" + seleniumObject + "']"));
						} catch (StaleElementReferenceException e2) {
							webElement = null;
						}
					}
				}
				break;
			}
		} catch (StaleElementReferenceException e) {
			webElement = null;

		} catch (Exception e1) {
			webElement = null;
		}

		boolean isMobile = false;

		if (webDriver.getClass().toString().contains("AndroidDriver")
				|| webDriver.getClass().toString().contains("IOSDriver")) {
			isMobile = true;
		}
		if (webElement != null && !isMobile) {
			try {
				if (webElement.isEnabled() && isElementDisplayed(webElement)) {
				}
				;
			} catch (StaleElementReferenceException e) {
				webElement = null;
			} catch (Exception e) {
				webElement = null;
			}
		}

		return webElement;
	}

	public static WebElement getWebElementWithInstance(WebDriver webDriver, String seleniumObject, String sInstance) {
		WebElement reqWebEle = null;
		try {
			int instance = 1;
			try {
				instance = Integer.parseInt(sInstance);
			} catch (Exception e) {
				instance = 1;
			}

			List<WebElement> elementsList = null;

			try {
				elementsList = getWebElements(webDriver, seleniumObject);
			} catch (Exception e) {
				return null;
			}

			if (instance > elementsList.size()) {
				return null;
			}

			int j = 1;
			for (WebElement e : elementsList) {
				if (j == instance) {
					reqWebEle = e;
					break;
				}
				j++;
			}
		} catch (Exception e) {

		}

		return reqWebEle;
	}

	public static List<WebElement> getWebElements(WebDriver webDriver, String seleniumObject) {

		List<WebElement> elementList = new ArrayList<WebElement>();
		try {

			if (seleniumObject.startsWith(OBJECT_SPECIFIER)) {
				seleniumObject = seleniumObject.replaceFirst(OBJECT_SPECIFIER, EMPTY_STRING);
			}

			String webElementString = "";

			String[] selObjects = null;
			String selObject = EMPTY_STRING;
			try {
				selObjects = seleniumObject.split("=");
				selObject = selObjects[0].toUpperCase() + "=";
			} catch (Exception e2) {

			}

			switch (selObject) {
			case ID:
				webElementString = seleniumObject.substring(ID.length());
				elementList = webDriver.findElements(By.id(webElementString));
				if (elementList == null || elementList.isEmpty()) {
					elementList = webDriver.findElements(By.xpath("//*[@id='" + webElementString + "']"));
				}
				break;
			case NAME: // seleniumObject.toUpperCase().startsWith(NAME);
				webElementString = seleniumObject.substring(NAME.length());
				elementList = webDriver.findElements(By.name(webElementString));
				break;
			case XPATH: // seleniumObject.toUpperCase().startsWith(XPATH);
				webElementString = seleniumObject.substring(XPATH.length());
				elementList = webDriver.findElements(By.xpath(webElementString));
				break;
			case "CSS=": // seleniumObject.toUpperCase().startsWith("CSS=");
				webElementString = seleniumObject.substring("CSS=".length());
				elementList = webDriver.findElements(By.cssSelector(webElementString));
				break;
			case CLASS: // seleniumObject.toUpperCase().startsWith(CLASS);
				webElementString = seleniumObject.substring(CLASS.length());
				elementList = webDriver.findElements(By.className(webElementString));
				break;
			case WDLINK: // seleniumObject.toUpperCase().startsWith(WDLINK);
				webElementString = seleniumObject.replaceFirst("link=", "");
				try {
					elementList = webDriver.findElements(By.linkText(webElementString));
				} catch (Exception e) {
					elementList = null;
				}
				if (elementList == null || elementList.isEmpty()) {
					try {
						elementList = webDriver.findElements(By.partialLinkText(webElementString));
					} catch (Exception e) {
						elementList = null;
					}
				}
				break;
			default:
				try {
					elementList = webDriver.findElements(By.name(seleniumObject));
					if (elementList == null || elementList.isEmpty()) {
						try {
							elementList = webDriver.findElements(By.id(seleniumObject));
							if (elementList == null || elementList.isEmpty()) {
								elementList = webDriver.findElements(By.xpath("//*[@id='" + seleniumObject + "']"));
							}
						} catch (Exception e1) {

							elementList = null;
						}
					}
				} catch (Exception e) {
					elementList = null;
				}
				break;
			}
		} catch (StaleElementReferenceException e) {
			elementList = null;

		} catch (Exception e1) {
			elementList = null;
		}
		return elementList;
	}

	public static boolean isElementPresent(WebDriver webDriver, String sTargetObject) {
		if (getWebElement(webDriver, sTargetObject) != null) {
			return true;
		}

		return false;
	}

	public static boolean isVisible(WebDriver webDriver, String sTargetObject) {
		if (getWebElement(webDriver, sTargetObject) != null
				&& isElementDisplayed(getWebElement(webDriver, sTargetObject))) {
			return true;
		}

		return false;
	}

	// ********************************************************************/
	public static boolean isAlertPresent(WebDriver webDriver) {
		boolean alertFlag = false;
		try {
			webDriver.switchTo().alert();
			alertFlag = true;
		} catch (Exception e1) {
			alertFlag = false;
		}
		return alertFlag;
	}

	// ********************************************************************/
	public static String getAlert(WebDriver webDriver) {
		String sActualDialogText = "";
		Alert alert = webDriver.switchTo().alert();
		try {
			webDriver.switchTo().alert();
			sActualDialogText = alert.getText();
			alert.accept();
		} catch (Exception e1) {
			sActualDialogText = "";
		}
		return sActualDialogText;
	}

	public static String[] getAllWindowNames(WebDriver driver) {
		List<String> attributes = new ArrayList<String>();
		String current = "";
		String[] windowNames = null;
		try {
			current = driver.getWindowHandle();
			for (String handle : driver.getWindowHandles()) {
				driver.switchTo().window(handle);
				attributes.add(((JavascriptExecutor) driver).executeScript("return window.name").toString());
			}

			driver.switchTo().window(current);
			windowNames = attributes.toArray(new String[attributes.size()]);
		} catch (Exception e) {
			windowNames = null;
		}
		return windowNames;
	}

	public static String[] getAllWindowTitles(WebDriver driver) {

		List<String> attributes = new ArrayList<String>();
		String current = "";
		String[] windowNames = null;

		try {
			current = driver.getWindowHandle();
			for (String handle : driver.getWindowHandles()) {
				driver.switchTo().window(handle);
				attributes.add(driver.getTitle().toString());
			}
			driver.switchTo().window(current);
			windowNames = attributes.toArray(new String[attributes.size()]);
		} catch (Exception e) {
			windowNames = null;
		}
		return windowNames;
	}

	public static void waitForPageToLoad(WebDriver webDriver, int toWait, String userName) {
		TimeWatcher timeWatcher = null;
		try {
			if (webDriver != null) {
				timeWatcher = new TimeWatcher(toWait);
				timeWatcher.startTimeWatcher();
				while (!timeWatcher.isTimeUp(userName)) {
					if (KeywordUtilities.isAlertPresent(webDriver)) {
						return;
					}
					String readyState = EMPTY_STRING;
					try {
						readyState = ((JavascriptExecutor) webDriver).executeScript(
								"var state; if(document==null){return 'loading';}try {state=document.readyState}catch(e){state=e.description;} return state;")
								.toString();
					} catch (NoSuchWindowException e) {
						readyState = "complete";
					} catch (Exception e) {
						readyState = EMPTY_STRING;
					}
					//// System.out.println("readyState:="+readyState);
					if (readyState.equalsIgnoreCase("complete") || "".equals(readyState)) {
						break;
					} else {
						try {
							Thread.sleep(100);
						} catch (Exception e) {
						}
					}
				}
				
				waitForPageToLoadFromIcon(webDriver, toWait, null, userName);

			}
			/*
			 * WebDriverWait wait = new WebDriverWait(webDriver,toWait); try {
			 * wait.until(ExpectedConditions.visibilityOfElementLocated(By.
			 * xpath("//*[not (.='')]"))); } catch (Exception e1) { }
			 */
		} catch (Exception e) {
		} finally {
			if (timeWatcher != null) {
				timeWatcher.cancel();
			}
		}
	}

	public static boolean waitForPageToLoadBoolean(WebDriver webDriver, int toWait, String userName) {

		TimeWatcher timeWatcher = null;
		try {
			/*
			 * WebDriverWait wait = new WebDriverWait(webDriver,toWait); try {
			 * wait.until(ExpectedConditions.visibilityOfElementLocated(By.
			 * xpath("//*[not (.='')]"))); return true; } catch (Exception e1) { return
			 * false; }
			 */

			if (webDriver != null) {
				timeWatcher = new TimeWatcher(toWait);
				timeWatcher.startTimeWatcher();

				while (!timeWatcher.isTimeUp(userName)) {
					if (KeywordUtilities.isAlertPresent(webDriver)) {
						return true;
					}
					String readyState = EMPTY_STRING;
					try {
						readyState = ((JavascriptExecutor) webDriver).executeScript(
								"var state; if(document==null){return 'loading';} try {state=document.readyState}catch(e){state=e.description;} return state;")
								.toString();
					} catch (NoSuchWindowException e) {
						readyState = "complete";
					} catch (Exception e) {
						readyState = EMPTY_STRING;
					}
					//// System.out.println("readyState:="+readyState);
					if (readyState.equalsIgnoreCase("complete")) {
						return true;
					} else {
						try {
							Thread.sleep(100);
						} catch (Exception e) {
						}
					}
				}

				return true;
			}
		} catch (Exception e) {
			return false;
		} finally {
			if (timeWatcher != null) {
				timeWatcher.cancel();
			}
		}
		return true;
	}

	public static String getAllSelectedFramesDriver(WebDriver webDriver) {
		String actualSelectedFrames = "";
		try {
			String selectedFrames = "f();function f()" + "{" + "	var AllFrames='';" + "	var win=window;"
					+ "	while(win.parent && win.parent != win)" + "	{" + "	var winframeid='';"
					+ "	try{winframeid=win.frameElement.id;}catch(e1){winframeid='';}" + "	if(winframeid=='')" + "	{"
					+ "		try{winframeid=win.frameElement.name;}catch(e1){winframeid='';}" + "	}"
					+ "		if(winframeid!=''){AllFrames=AllFrames+';'+winframeid;}" + "	try" + "		{"
					+ "			win=win.parent;" + "		}" + "	catch(e)" + "		{win=null;}" + "	}"
					+ "	return AllFrames;" + "}";

			String gotSelectedFrames = "";

			try {
				gotSelectedFrames = ((JavascriptExecutor) webDriver).executeScript("return " + selectedFrames)
						.toString();
				if (gotSelectedFrames.contains("timeout")) {
					gotSelectedFrames = "";
				}
			} catch (Exception e1) {
				gotSelectedFrames = "";
			}

			if (!EMPTY_STRING.equals(gotSelectedFrames)) {
				String[] selectedFrame = gotSelectedFrames.split(";");
				for (int j = selectedFrame.length - 1; j >= 0; j--) {
					if (j == selectedFrame.length - 1) {
						actualSelectedFrames = selectedFrame[j];
					} else {
						actualSelectedFrames = actualSelectedFrames + ";" + selectedFrame[j];
					}
				}
			}
			// System.out.println("actualSelectedFrame: "+actualSelectedFrames);
		} catch (Exception e) {
		}
		return actualSelectedFrames;
	}

	public static String getAllSelectedFrames(WebDriver webDriver) {
		String actualSelectedFrames = "";

		try {
			String selectedFrames = "f();function f(){var currentSelFrame=window.frameElement.id;var AllFrames='';AllFrames=currentSelFrame;var win=window;while(win.parent && win.parent != win){var winframeid='';try{winframeid=win.frameElement.id;}catch(e1){winframeid='';}AllFrames=AllFrames+';'+winframeid;}return AllFrames;}";

			String gotSelectedFrames = "";

			try {
				gotSelectedFrames = ((JavascriptExecutor) webDriver).executeScript("return " + selectedFrames)
						.toString();
			} catch (Exception e1) {
				gotSelectedFrames = "";
			}

			String[] selectedFrame = gotSelectedFrames.split(";");
			for (int j = selectedFrame.length - 1; j >= 0; j--) {
				if (j == selectedFrame.length - 1) {
					actualSelectedFrames = selectedFrame[j];
				} else {
					actualSelectedFrames = actualSelectedFrames + ";" + selectedFrame[j];
				}
			}

			// System.out.println("actualSelectedFrame: "+actualSelectedFrames);
		} catch (Exception e) {
		}
		return actualSelectedFrames;
	}

	public static String enter_Text_Html5(WebDriver webDriver, WebElement elementTextField, String sValueToEnter) {
		String sETV_Result = "FAIL";
		try {
			String SafalEnterTextValue = "f(arguments[0],arguments[1]);function f(ELE){var svalueToEnter = arguments[1];var Result='FAIL';try{var textBox=$(ELE); if(textBox.is('input') || textBox.is('textarea')){textBox.val(svalueToEnter);}else{textBox.text(svalueToEnter);}textBox.trigger('input');textBox.trigger('keydown');textBox.trigger('keyup');textBox.trigger('change');Result= 'PASS';}catch(e){Result='FAIL '+ e.description;}return Result;}";
			try {
				sETV_Result = ((JavascriptExecutor) webDriver)
						.executeScript("return " + SafalEnterTextValue, elementTextField, sValueToEnter).toString();
			} catch (Exception e1) {
				sETV_Result = "FAIL, " + e1.getMessage();
			}
			// System.out.println("Result: "+sETV_Result);
		} catch (Exception e) {
			sETV_Result = "FAIL, " + e.getMessage();
		}
		return sETV_Result;
	}

	public static String fireEvent_Html5(WebDriver webDriver, WebElement elementTextField, String sValueToEnter) {
		String sETV_Result = "FAIL";
		try {
			String SafalEnterTextValue = "f(arguments[0],arguments[1]);function f(ELE){var svalueToEnter = arguments[1];var Result='FAIL';try{var textBox=$(ELE); textBox.trigger(svalueToEnter);Result= 'PASS';}catch(e){Result='FAIL '+ e.description;}return Result;}";
			try {
				sETV_Result = ((JavascriptExecutor) webDriver)
						.executeScript("return " + SafalEnterTextValue, elementTextField, sValueToEnter).toString();
			} catch (Exception e1) {
				sETV_Result = "FAIL, " + e1.getMessage();
			}
			// System.out.println("Result: "+sETV_Result);
		} catch (Exception e) {
			sETV_Result = "FAIL, " + e.getMessage();
		}
		return sETV_Result;
	}

	public static String click_Html5(WebDriver webDriver, WebElement eleLink) {
		String sCL_Result = "FAIL";
		try {
			String SafalClickLink = "f(arguments[0]);function f(ELE){var Result='FAIL';try{var linkToClick=$(ELE);linkToClick.trigger('click');Result= 'PASS';}catch(e){Result='FAIL '+ e.description;}return Result;}";
			try {
				sCL_Result = ((JavascriptExecutor) webDriver).executeScript("return " + SafalClickLink, eleLink)
						.toString();
			} catch (Exception e1) {
				sCL_Result = "FAIL, " + e1.getMessage();
			}
			// System.out.println("Result: "+sCL_Result);
		} catch (Exception e) {
			sCL_Result = "FAIL, " + e.getMessage();
		}
		return sCL_Result;

	}

	public static String getDocumentSkinnyPage(String sFrameValue) {
		if (sFrameValue == null) {
			sFrameValue = "";
		}

		String sDocumentObject = "           var CurrentWindowDoc=null;                                                    "
				+ "             try" + "                   {" + "                          CurrentWindowDoc = document;"
				+ "                   }" + "             catch(e)" + "                    {"
				+ "                          CurrentWindowDoc=document;" + "                    }             "
				+ "                    if (CurrentWindowDoc==null)" + "                    {"
				+ "                                 CurrentWindowDoc=document;" + "                    }             "
				+ "        var sFrameValue = \"" + sFrameValue
				+ "\";                                                                        "
				+ "             if(sFrameValue != null && sFrameValue!=\"\")                                                                                                                                            "
				+ "             {                                                                                                                                                                                "
				+ "        var splittedfrms=sFrameValue.split(\";\");                                                                 "
				+ "        for(i=0;i<splittedfrms.length;i++)                                                                     "
				+ "             {                                                                                                   "
				+ "             try                                                                                             "
				+ "                    {                                                                                               "
				+ "                            var frameObj=CurrentWindowDoc.getElementById(splittedfrms[i]);                                "
				+ "                      var frameDoc=null;" + "                   if(frameObj==null)"
				+ "                   {" + "                      var found = 'FALSE';"
				+ "                       var ifarmObj = null;"
				+ "                       var temp=CurrentWindowDoc.getElementsByTagName('iframe');"
				+ "                       for(j=0;j<temp.length;j++){"
				+ "                               if(temp[j].id == splittedfrms[i]){"
				+ "                                    found='TRUE'; "
				+ "                                     ifarmObj=temp[j];"
				+ "                                     break;" + "                                  }"
				+ "                               if(temp[j].name == splittedfrms[i]){"
				+ "                                    found='TRUE'; "
				+ "                                     ifarmObj=temp[j];"
				+ "                                     break;" + "                                  }" +

				"                        }" + "                   if(found=='TRUE'){"
				+ "                        frameObj=ifarmObj;}"
				+ "                                        try {frameDoc=frameObj.contentWindow.document;}catch(e){frameDoc=null;} "
				+ "                                               if (frameDoc==null)"
				+ "                                               {"
				+ "                                                      try {frameDoc=frameObj.contentDocument;}catch(e){frameDoc=null;}"
				+ "                                                      if(frameDoc==null)"
				+ "                                                      {"
				+ "                                                             try {frameDoc=frameObj.document;}catch(e){frameDoc=null;}"
				+ "                                                      }"
				+ "                                               }"
				+ "                                 if (frameDoc==null)" + "                                 {"
				+ "                                        return \"Fail, unable to get the document object.\";"
				+ "                                 }                                                                                           "
				+ "                      CurrentWindowDoc = frameDoc;                                                            "
				+ "                   }" +

				"                            try {frameDoc=frameObj.contentWindow.document;}catch(e){frameDoc=null;} "
				+ "                                 if (frameDoc==null)" + "                                 {"
				+ "                                        try {frameDoc=frameObj.contentDocument;}catch(e){frameDoc=null;}"
				+ "                                               if(frameDoc==null)"
				+ "                                                      {"
				+ "                                                             try {frameDoc=frameObj.document;}catch(e){frameDoc=null;}"
				+ "                                                      }" + "                                 }" +

				"                                 if (frameDoc==null)" + "                                 {"
				+ "                                        return \"Fail, unable to get the document object.\";"
				+ "                                 }                                                                                           "
				+ "                      CurrentWindowDoc = frameDoc;                                                                  "
				+ "             }                                                                                               "
				+ "            catch(error)                                                                                    "
				+ "            {                                                                                               "
				+ "                      return  \"FRAME NOT FOUND\";                                                        "
				+ "            }                                                                                               "
				+ "            }                                                                                                  "
				+ "             }                                                                                                                                                                                 "
				+ "        var fd=CurrentWindowDoc;                                                                               ";

		return sDocumentObject;
	}

	public static String getSheetName(int sheetNum, int occurrence, HSSFWorkbook resultWorkBook, String sheetName) {

		if (sheetNum < resultWorkBook.getNumberOfSheets()) {
			if (resultWorkBook.getSheet(sheetName) != null
					&& resultWorkBook.getSheetName(sheetNum).equalsIgnoreCase(sheetName)) {
				sheetName = getSheetName(++sheetNum, ++occurrence, resultWorkBook, sheetName);

				if (occurrence == 1) {
					if (sheetName.length() == 31)
						sheetName = sheetName.substring(0, sheetName.length() - 4);
					else {
						if (sheetName.length() > 27) {
							if (occurrence < 10)
								sheetName = sheetName.substring(0, sheetName.length() - 2);
							else
								sheetName = sheetName.substring(0, sheetName.length() - 3);
						}
					}

					sheetName = sheetName + occurrence;
					sheetName = getSheetName(sheetNum, occurrence, resultWorkBook, sheetName);
					sheetNum++;

				} else if (occurrence > 1) {
					if (sheetName.length() == 31)
						sheetName = sheetName.substring(0, sheetName.length() - 4);
					else {
						if (sheetName.length() > 27) {
							if (occurrence < 10)
								sheetName = sheetName.substring(0, sheetName.length() - 2);
							else
								sheetName = sheetName.substring(0, sheetName.length() - 3);
						} else {
							if (occurrence < 10)
								sheetName = sheetName.substring(0, sheetName.length() - 1);
							else
								sheetName = sheetName.substring(0, sheetName.length() - 2);
						}
					}

					sheetName = sheetName + occurrence;
					sheetName = getSheetName(sheetNum, occurrence, resultWorkBook, sheetName);
				}
			} else {
				sheetName = getSheetName(++sheetNum, occurrence, resultWorkBook, sheetName);
			}

		}

		return sheetName;
	}

	private static boolean isElementDisplayed(WebElement ele) {
		boolean isVisible = false;
		/*
		 * try { return ele.isDisplayed(); } catch (Exception e) {
		 */
		try {
			Dimension d = ele.getSize();
			isVisible = (d.getHeight() > 0 && d.getWidth() > 0);
		} catch (Exception e1) {
		}

		if (!isVisible) {
			try {
				String elementStyle = ele.getAttribute("style");
				isVisible = !(elementStyle.equals("display: none;") || elementStyle.equals("visibility: hidden;"));
			} catch (Exception e2) {
			}
		}
		// }
		return isVisible;
	}

	/*
	 * Removed static and changes done for web safal
	 */
	/*
	 * public static void initializeHtmlConfig() {
	 * JasperUtil.summaryPageModelHtmlMap = new
	 * HashMap<String,List<SummaryPageModelHtml>>(); Utility.testcaseVoDMap = new
	 * HashMap<String,Map<String,String>>(); Utility.arrListdriver = new
	 * ArrayList<>(); ExecutionChannel.driverNameList = new ArrayList<>(); }
	 */
}
