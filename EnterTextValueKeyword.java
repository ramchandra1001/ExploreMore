package com.sungard.ktt.business.keywords;

import static com.sungard.ktt.business.keywords.ErrorMessages.ERROR_BROWSER_NOT_INSTANTIATED;
import static com.sungard.ktt.business.keywords.ErrorMessages.ERROR_INSTANCE_VARIABLE_ERROR;
import static com.sungard.ktt.business.keywords.ErrorMessages.ERROR_PARAMETERS_LIST;
import static com.sungard.ktt.business.keywords.ErrorMessages.ERROR_TEXTFIELD_NOT_FOUND;
import static com.sungard.ktt.business.keywords.ErrorMessages.ERROR_TEXTFIELD_PARA_NOT_PASSED;
import static com.sungard.ktt.view.config.KTTGuiConstants.CANCEL;
import static com.sungard.ktt.view.config.KTTGuiConstants.CURRENTDATE;
import static com.sungard.ktt.view.config.KTTGuiConstants.CURRENTTIME;
import static com.sungard.ktt.view.config.KTTGuiConstants.DELIMITER;
import static com.sungard.ktt.view.config.KTTGuiConstants.EMPTY_STRING;
import static com.sungard.ktt.view.config.KTTGuiConstants.ENV_ACTUAL_DIALOG_TEXT;
import static com.sungard.ktt.view.config.KTTGuiConstants.ENV_DIALOG_OPEARTION;
import static com.sungard.ktt.view.config.KTTGuiConstants.MACRO_CLICK_ALERT_BUTTON;
import static com.sungard.ktt.view.config.KTTGuiConstants.OBJECT_RENDERING_ITERATION_WAIT_VARIABLE;
import static com.sungard.ktt.view.config.KTTGuiConstants.OBJECT_SPECIFIER;
import static com.sungard.ktt.view.config.KTTGuiConstants.OK;
import static com.sungard.ktt.view.config.KTTGuiConstants.PASS;

import org.apache.log4j.Logger;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

import com.sungard.ktt.business.keywords.utility.excel.RunMacroWithParam;
import com.sungard.ktt.model.config.InitSAFALProperties;
import com.sungard.ktt.model.util.TimeWatcher;
import com.sungard.ktt.model.valueobjects.TestcaseExecutionResultVO;
/**
 * This Keyword enters text into the named text field. 
 * Using CURRENTDATE will enter the current system date. 
 * Using CURRENTDATE will enter the current system time. If Param2 is left blank, it will clear a text field.
 * @author Dnyaneshwar.Daphal
 */
public class EnterTextValueKeyword extends AbstractKeyword 
{
	private WebElement elementTextField;


	private String Result="FAIL";
	TestcaseExecutionResultVO testCaseExecutionResult = new TestcaseExecutionResultVO();


	/**
	 * This is logger object used to log keyword actions into a log file
	 */
	Logger logger = Logger.getLogger("Thread" + Thread.currentThread().getName());

	private String sTextField = null;
	private String sValue = null;
	private String sInstance = null;
	private String sFlagToBlankField = null;
	private String sFlagToType = null;
	private String sFlagToFireEvent = null;
	private String sEvent=null;
	/**
	 * After all validation, this method actually enters text into textfield
	 * @return ExecutionResults containing step execution status(pass/fail),
	 *         exact error message according to failure
	 */

	public TestcaseExecutionResultVO executeScript(String... listOfParameters) {
		logger.info ("Inside Method validate Execute Script");

		if (sValue.equals(CURRENTDATE)) {

			sValue = KeywordExecutionUtilities.getSystemDate();

		} else if (sValue.equals(CURRENTTIME)) {

			sValue = KeywordExecutionUtilities.getSystemTime();
		}

		if (sValue.contains("\n") )
		{
			sValue=sValue.replace("\n", "\\n");
		}
		else if (sValue.contains("\r"))
		{
			sValue=sValue.replace("\r", "\\r");
		}

		if (EMPTY_STRING.equals(sFlagToType))
		{
			sFlagToType="N";
		}

		//SAF-2659 Removing default behavior of having fireevent to call. (EMPTY_STRING.equals(sFlagToFireEvent) ||) is removed
		if ( sFlagToFireEvent.equalsIgnoreCase("Y")|| sFlagToFireEvent.equalsIgnoreCase("YES")||sFlagToFireEvent.equalsIgnoreCase("TRUE"))
		{
			sFlagToFireEvent="Y";
		}
		else if(!EMPTY_STRING.equals(sFlagToFireEvent))
		{
			sFlagToFireEvent="N";
		}

		//As per discussion, keyword parameter will have highest preference than value of ENV variable SKIP_ENTERTEXT_FIREEVENT.
		//If keyword parameter for fireevent is not specified then only reads ENV variable SKIP_ENTERTEXT_FIREEVENT
		if(EMPTY_STRING.equals(sFlagToFireEvent)){
			String SKIP_ENTERTEXT_FIREEVENT ="FALSE";		
			try { SKIP_ENTERTEXT_FIREEVENT = configurationMap.get("SKIP_ENTERTEXT_FIREEVENT"); } 
			catch (Exception e2) {
				logger.debug("SKIP_ENTERTEXT_FIREEVENT not specified",e2);
			}
			
			String var_Product_Check =EMPTY_STRING;		
			try { var_Product_Check = configurationMap.get("ProductName"); } 
			catch (Exception e2) {logger.debug("Product name not specifed",e2);	}
			
			//Added code for INVESTONE product. If user has not specified SKIP_ENTERTEXT_FIREEVENT or it's empty and user has not specified fireevent parameter to EnterTextValue
			//then it should execute fireevent.
			if( (KeywordUtilities.isEmptyString(SKIP_ENTERTEXT_FIREEVENT) || (null==SKIP_ENTERTEXT_FIREEVENT)) && (!KeywordUtilities.isEmptyString(var_Product_Check) && var_Product_Check.toUpperCase().contains("INVESTONE")))
			{
				SKIP_ENTERTEXT_FIREEVENT="N";
			}
	
			if (null==SKIP_ENTERTEXT_FIREEVENT || EMPTY_STRING.equals(SKIP_ENTERTEXT_FIREEVENT) || (SKIP_ENTERTEXT_FIREEVENT!=null && (SKIP_ENTERTEXT_FIREEVENT.equalsIgnoreCase("Y")||SKIP_ENTERTEXT_FIREEVENT.equalsIgnoreCase("YES")||SKIP_ENTERTEXT_FIREEVENT.equalsIgnoreCase("TRUE"))))
			{
				sFlagToFireEvent="N";
			}
			else{
				sFlagToFireEvent="Y";
			}
		
		}

		String html5Flag="false";
		try {
			html5Flag = configurationMap.get("HTML5");
			if(html5Flag==null)
			{
				html5Flag="false";
			}
		} catch (Exception e) {
			html5Flag="false";
		}

		if(html5Flag.equalsIgnoreCase("TRUE"))
		{
			try {

				try {
					KeywordUtilities.enter_Text_Html5(webDriver,elementTextField,"");
					} catch (Exception e1) {		
						logger.error("Error occured while entering text for html5",e1);
					}

				String get_Enter_TextHTML5="FAIL";

				get_Enter_TextHTML5=KeywordUtilities.enter_Text_Html5(webDriver,elementTextField,sValue);

				if(get_Enter_TextHTML5.toUpperCase().contains("FAIL"))
				{
					try {
						((JavascriptExecutor)webDriver).executeScript("arguments[0].value=arguments[1]", elementTextField,sValue);
						testCaseExecutionResult.setStatus(PASS);
					} catch (Exception e1) 
					{

						try {
							elementTextField.sendKeys(sValue);
							testCaseExecutionResult.setStatus(PASS);
						} catch (Exception e) {
							logger.error("Error while setting the value",e);
							testCaseExecutionResult.setMessage("Error while setting the value, "+e.getMessage());
							testCaseExecutionResult.setStatus(0);
						}
					}
				}
				else
				{
					testCaseExecutionResult.setStatus(PASS);
				}

			} catch (Exception e) {

				logger.error ("Error occured while entering value",e);
				testCaseExecutionResult.setMessage("Error occured while entering value");
				return testCaseExecutionResult;
			}

		}
		else
		{
			try
			{

				// to make the field value blank if 4th param is passed as Y ot TRUE
				if(sFlagToBlankField.equalsIgnoreCase("Y")||sFlagToBlankField.equalsIgnoreCase("TRUE"))
				{
					//try{((JavascriptExecutor)webDriver).executeScript("selenium.browserbot.findElement(\""+sTextField+"\").value='';");}	catch(Exception e){}
					//try{((JavascriptExecutor)webDriver).executeScript("arguments[0].value='';",elementTextField);}catch(Exception e){}
					elementTextField.clear();
				}

				if ((html5Flag!=null && html5Flag.equalsIgnoreCase("true")) || (sFlagToType.equalsIgnoreCase("YES")||sFlagToType.equalsIgnoreCase("Y") || sFlagToType.equalsIgnoreCase("TRUE")) )
				{
					try {

						if (EMPTY_STRING.equals(sValue))
						{
							try {

								String getLengthfromTextBox = elementTextField.getAttribute("value").toString();
								int slength=getLengthfromTextBox.length();
								for (int o=0;o<slength;o++)
								{
									//webDriver.typeKeys(sTextField, "\b");
									elementTextField.sendKeys(Keys.BACK_SPACE);
								}
								elementTextField.clear();
							} catch (Exception e) 
							{
								logger.error ("Error occured while clearing the text box as Blank value",e);
								testCaseExecutionResult.setMessage("Error occured while clearing the text box as Blank value");
								return testCaseExecutionResult;
							}
						}
						else
						{
							elementTextField.sendKeys(sValue);
							//((JavascriptExecutor)webDriver).executeScript("arguments[0].value=arguments[1]", elementTextField,sValue);
						}
						testCaseExecutionResult.setStatus(PASS);
					} catch (Exception e) {

						logger.error ("Error occured while entering (typing) the value",e);
						testCaseExecutionResult.setMessage("Error occured while entering (typing) the value");
						return testCaseExecutionResult;
					}
				}
				else
				{
					// entering text
					try {
						//webDriver.type(sTextField, sValue);
						try {
							//element.clear();
							((JavascriptExecutor)webDriver).executeScript("arguments[0].value=''", elementTextField);
						} catch (Exception e) {
							logger.error("An exception occured while clearing text field",e);
						}
						//elementTextField.sendKeys(sValue);
						((JavascriptExecutor)webDriver).executeScript("arguments[0].value=arguments[1]", elementTextField,sValue);
						testCaseExecutionResult.setStatus(PASS);
					} catch (Exception e) {

						logger.error ("Error occured while entering value",e);
						testCaseExecutionResult.setMessage("Error occured while entering value");
						return testCaseExecutionResult;
					}		
				}
			}
			catch(Exception e)
			{
				logger.error ("An exception occured while entering the value",e);
				testCaseExecutionResult.setMessage("An exception occured while entering the value");
				return testCaseExecutionResult;
			}

			if(sFlagToFireEvent.equals("Y"))
			{
				try 
				{
					executeScriptFireEvent(userName);
				}
				catch (Exception e) 
				{
					logger.error("Unable to fire an event",e);
				}
			}
		}


		return testCaseExecutionResult;
	}

	/**
	 * Check for required parameters, parameter format
	 * 
	 * @param listOfParameters
	 *              contains list of parameters 
	 *              listOfParameters[0] (Mandatory) - TextField - The text field name or label.
	 *              listOfParameters[1] (Mandatory) - TextFieldText - value to enter in textfield 
	 *              listOfParameters[2] (Optional)  - Instance - instance of textfield after Label
	 * @return ExecutionResults object contains status of validation, exact
	 *         error message according to failure
	 */

	/**
	 * This method performs validation of the keyword
	 *  @return ExecutionResults containing step execution status(pass/fail),
	 *         exact error message according to failure
	 */
	@Override
	public TestcaseExecutionResultVO validateKeyword(String... listOfParameters) {

		logger.info ("Inside Method validate Keyword");
		// check for required parameter count
		if (listOfParameters != null) {

			sTextField = listOfParameters[0];
			sValue = listOfParameters[1];
			sInstance = listOfParameters[2];
			sFlagToBlankField = listOfParameters[3];
			sFlagToType = listOfParameters[4];
			sFlagToFireEvent = listOfParameters[5];

		} else {

			logger.error ("ERROR_PARAMETERS_LIST");
			testCaseExecutionResult.setMessage(ERROR_PARAMETERS_LIST);
			return testCaseExecutionResult;
		}

		testCaseExecutionResult.setTestData(sTextField + DELIMITER + sValue + DELIMITER + sInstance + DELIMITER
				+ sFlagToBlankField + DELIMITER + sFlagToType + DELIMITER + sFlagToFireEvent);

		// check for textfield name parameter value
		if (KeywordUtilities.isEmptyString(sTextField)) {

			logger.error ("ERROR_TEXTFIELD_PARA_NOT_PASSED");
			testCaseExecutionResult.setMessage(ERROR_TEXTFIELD_PARA_NOT_PASSED);
			return testCaseExecutionResult;
		}

		// check for numeric instance value
		if (!KeywordUtilities.isValidPositiveNumbericValue(sInstance)) {

			logger.error ("ERROR_INSTANCE_VARIABLE_ERROR");
			testCaseExecutionResult.setMessage(ERROR_INSTANCE_VARIABLE_ERROR);
			return testCaseExecutionResult;
		}

		// all validation done
		testCaseExecutionResult.setValid(true);
		return testCaseExecutionResult;
	}

	/**
	 * This method validates the object on the browser
	 * * @return ExecutionResults containing step execution status(pass/fail),
	 *         exact error message according to failure
	 */
	public TestcaseExecutionResultVO validateObject(String... listOfParameters) {

		logger.info ("Inside Method validate Object");
		if (webDriver == null) {

			logger.error ("ERROR_BROWSER_NOT_INSTANTIATED");
			testCaseExecutionResult.setMessage(ERROR_BROWSER_NOT_INSTANTIATED);
			testCaseExecutionResult.setValid(false);
			return testCaseExecutionResult;
		}

		// direct object specified
		if (sTextField.startsWith(OBJECT_SPECIFIER)) {

			sTextField = sTextField.substring(OBJECT_SPECIFIER.length(), sTextField.length());
		} 

		/*SAF-1900  Is there way to check state "enable / disable" of Control in keyword before performing action ?*/
		
		elementTextField=KeywordUtilities.waitForElementPresentAndEnabledInstance(configurationMap,webDriver, sTextField,sInstance, userName);
		/*SAF-1900  Is there way to check state "enable / disable" of Control in keyword before performing action */

		if (elementTextField==null) {

			logger.error ("ERROR_TEXTFIELD_NOT_FOUND");
			testCaseExecutionResult.setMessage(ERROR_TEXTFIELD_NOT_FOUND);
			testCaseExecutionResult.setValid(false);
			return testCaseExecutionResult;
		}

		testCaseExecutionResult.setObject(sTextField);
		testCaseExecutionResult.setValid(true);
		return testCaseExecutionResult;
	}

	
	
	
	public String executeScriptFireEvent(String userName) 
	{

		String messageToReturn="FAIL";

		sEvent = "change";
		try {
			try {


				int iWaitTime=5;
				TimeWatcher timeWatcher = new TimeWatcher(iWaitTime);
				timeWatcher.startTimeWatcher();
				
				//SAF-2629 changing static  millisecond wait to  OBJECT_RENDERING_ITERATION_WAIT_VARIABLE provided by user
				//or if not use DEFAULT_OBJECT_RENDERING_WAIT_TIME
				
				InitSAFALProperties initSAFALProperties = InitSAFALProperties.getInstance(userName);
				
				int iObjectIterationWaitTime=initSAFALProperties.ObjectRenderingWaitTime;
				try{
						iObjectIterationWaitTime=Integer.parseInt(configurationMap.get(OBJECT_RENDERING_ITERATION_WAIT_VARIABLE));
				}
				catch(Exception e){iObjectIterationWaitTime=initSAFALProperties.ObjectRenderingWaitTime;}
				//==========================	
				
				if (elementTextField!=null)
				{

					try
					{	
						try {

							Thread t1 = new Thread(new Runnable() 
							{ 
								public void run()
								{ 
									try
									{		      
										try 
										{
											
											Result=((JavascriptExecutor)webDriver).executeScript("var result='FAIL';try {arguments[0].fireEvent(\"on"+sEvent+"\");result='PASS';}catch(e){result='FAIL'+e.description;}return result;", elementTextField).toString();
											//for IE 11//SAF-2545

											if(Result.toUpperCase().contains("FAIL"))
											{
												//System.out.println("STAGE3");
												//Result=null;
												Result=((JavascriptExecutor)webDriver).executeScript("var result='FAIL';try {var e = null;if (document.createEventObject) {e = document.createEventObject(); arguments[0].fireEvent(\"on"+sEvent+"\", e);result='PASS';} else {e = document.createEvent('HTMLEvents');e.initEvent(\""+sEvent+"\", true, true); arguments[0].dispatchEvent(e);result='PASS';}}catch(e){result='FAIL2 '+e.description;}return result;", elementTextField).toString();
												logger.debug(">>"+Result);
											}

										} catch (Exception e) 
										{
											
											Result=((JavascriptExecutor)webDriver).executeScript("var result='FAIL';try {var e = null;if (document.createEventObject) {e = document.createEventObject(); arguments[0].fireEvent(\"on"+sEvent+"\", e);result='PASS';} else {e = document.createEvent('HTMLEvents');e.initEvent(\""+sEvent+"\", true, true); arguments[0].dispatchEvent(e);result='PASS';}}catch(e){result='FAIL '+e.description;}return result;", elementTextField).toString();
											//for IE 11//SAF-2545
											if(Result.toUpperCase().contains("FAIL"))
											{
												Result=((JavascriptExecutor)webDriver).executeScript("var result='FAIL';try {arguments[0].fireEvent(\"on"+sEvent+"\");result='PASS';}catch(e){result='FAIL'+e.description;}return result;", elementTextField).toString();
											}
										}



									} catch (Exception e) { 
										logger.error("Could not perform fire event",e);
										Result = "Could not perform FireEvent";
									} 
								}
							});
							t1.start();
							Thread.sleep(1000);
							while(true)
							{
								if(!Result.contains("FAIL"))
								{
									break;
								}
								if(timeWatcher.isTimeUp(userName)) 
								{
									try {
										String sDialogOperation = configurationMap.get(ENV_DIALOG_OPEARTION);
										if(!sDialogOperation.equalsIgnoreCase(CANCEL))
											configurationMap.put(ENV_DIALOG_OPEARTION,OK);
									} catch (Exception e) {
										configurationMap.put(ENV_DIALOG_OPEARTION,OK);
									}
									String ButtonText=configurationMap.get(ENV_DIALOG_OPEARTION);

									RunMacroWithParam runMacroWithParam = new RunMacroWithParam();
									String[] params = new String[8]; 
									params[0] = MACRO_CLICK_ALERT_BUTTON;
									params[1] = ButtonText; 
									params[2] = EMPTY_STRING;
									params[3] = EMPTY_STRING;
									params[4] = EMPTY_STRING;
									params[5] = EMPTY_STRING;
									params[6] = EMPTY_STRING;//sModal_Flag
									params[7] = EMPTY_STRING;//Excel_Kill_Flag

									try{

										String testCaseExecutionResultMsg  = testCaseExecutionResult.getMessage();
										testCaseExecutionResult = runMacroWithParam.execute(
												scriptName, webDriver, configurationMap,workBookMap,userName,
												params);

										if (testCaseExecutionResult.getStatus() == PASS) 
										{
											String sDialogText = testCaseExecutionResult.getMessage();
											sDialogText = sDialogText.replace("PASS ! Found Alert Text =", "").trim();
											if (sDialogText==null)
											{
												configurationMap.put(ENV_ACTUAL_DIALOG_TEXT,EMPTY_STRING);
											}
											else
											{	
												configurationMap.put(ENV_ACTUAL_DIALOG_TEXT,sDialogText);
											}

											testCaseExecutionResult.setMessage(testCaseExecutionResultMsg);
										} 

										configurationMap.put(ENV_DIALOG_OPEARTION,OK);
									}
									catch(Exception e)
									{
										messageToReturn=e.getMessage();

									}
									//SAF-2629 replacing static 1000 milliseconds wait to iObjectIterationWaitTime
									Thread.sleep(iObjectIterationWaitTime);
									
									timeWatcher.cancel();
									
									break;
								}						
							}
						} catch (Exception e) {
							messageToReturn="Unable to Fire Event="+sEvent+" on locator="+sTextField;

						}
						if (Result!=null && Result.equalsIgnoreCase("PASS"))
						{
							messageToReturn= "PASS";
						}
						else
						{
							messageToReturn= "Unable to Fire Event, "+ Result;
						}

					} catch (Exception e) {
						messageToReturn="Could Not Perform The Action";
					}

				}

			} catch (Exception e) {
				messageToReturn="Got error while obtaning WebDriver Object";

			}

		} catch (Exception e) {
			messageToReturn="Could Not Perform The Action";
		}
		return messageToReturn;
	}	

	/*
	 * Following code is kept to have FireEvent keyword to be called in future.
	 * Though following code is working but it is accepting object locator and FireEvent is finding the object again, which is unnecessary & time consuming.
	 * Need to check possibility if calling through API can support Object, instead of object locator.   
	 * public void executeScriptFireEvent_tmp(String userName){		
		Keyword fireEvent = KeywordFactoryImpl.getInstance().get("FireEvent", "false");
		String[] listOfParameters = new String[3];
		listOfParameters[0]=sTextField;
		listOfParameters[1]="change";
		TestcaseExecutionResultVO sfVo=fireEvent.execute(null, webDriver,configurationMap,null, userName, listOfParameters);
	}*/
}