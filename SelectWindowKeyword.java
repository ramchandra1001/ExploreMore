package com.sungard.ktt.business.keywords;

import static com.sungard.ktt.business.keywords.ErrorMessages.ERROR_BROWSER_NOT_INSTANTIATED;
import static com.sungard.ktt.business.keywords.ErrorMessages.ERROR_FRAME_NAME_NOT_PASSED;
import static com.sungard.ktt.business.keywords.ErrorMessages.ERROR_NOT_ABLE_TO_SELECT_WINDOW;
import static com.sungard.ktt.business.keywords.ErrorMessages.ERROR_PARAMETERS_LIST;
import static com.sungard.ktt.view.config.KTTGuiConstants.FAIL;
import static com.sungard.ktt.view.config.KTTGuiConstants.PASS;

import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;

import com.sungard.ktt.model.util.TimeWatcher;
import com.sungard.ktt.model.valueobjects.TestcaseExecutionResultVO;
import com.sungard.ktt.view.config.KTTGuiConstants;
/**
 * @author Ashish Joshi
 *This keyword is used to select any window/child window.
  If null given original window will get pointed
 */
public class SelectWindowKeyword extends AbstractKeyword
{

	/**
	 * This is logger object used to log keyword actions into a log file
	 */
	Logger logger = Logger.getLogger("Thread" + Thread.currentThread().getName());
	/**
	 * Id of the window to select, if null is given, control will get assigned to main source page
	 */
	private String sWindow = null;
	private int maxWaitTime = 0;
	private String sCheckForPartialWindowTitle = null;
	TestcaseExecutionResultVO testCaseExecutionResult = new TestcaseExecutionResultVO();

	/**
	 * After all validation, this method actually Select Frame

	 * @return ExecutionResults containing step execution status(pass/fail),
	 *         exact error message according to failure
	 */
	@Override
	public TestcaseExecutionResultVO executeScript(String... listOfParameters)
	{
		boolean windowFoundFlag=false;
		try
		{			
			
			if(sWindow.equalsIgnoreCase("null")){
				try{
					String mainWindowHandle = configurationMap.get("SAFAL_MAIN_WINDOW_HANDLE");
					webDriver.switchTo().window(mainWindowHandle);
					testCaseExecutionResult.setStatus(1);
				}catch(Exception e){
					logger.error("Not able to select main window");
					testCaseExecutionResult.setMessage("Not able to select main window");
					testCaseExecutionResult.setStatus(0);
				}
				return testCaseExecutionResult;
			}

			TimeWatcher timeWatcher = new TimeWatcher(maxWaitTime);
			timeWatcher.startTimeWatcher();
			

			while(!timeWatcher.isTimeUp(userName) && webDriver != null)
			{
				windowFoundFlag=false;
				try 
				{
					Set<String> availableWindows=null;
					try {
						availableWindows = webDriver.getWindowHandles();
					} catch (Exception e1) {

					}
					WebDriver popup_Window=null;

					if(availableWindows.size()>0)
					{
						for (Iterator<String> iterator = availableWindows.iterator(); iterator.hasNext();)
						{ 
							String windowHandle = iterator.next();
							try {
								popup_Window = webDriver.switchTo().window(windowHandle);
								if(windowHandle.equals(sWindow))
								{
									windowFoundFlag=true;
									break;
								}
								else
								{
									String titleOfWindow="";
									try {
										titleOfWindow=popup_Window.getTitle();
									} catch (Exception e) {
									}

									String urlOfWindow="";
									try {
										urlOfWindow=popup_Window.getCurrentUrl();
									} catch (Exception e) {

									}
									
									if (sCheckForPartialWindowTitle.equals("N") && (titleOfWindow.equals(sWindow) || urlOfWindow.contains(sWindow))) 
									{
										windowFoundFlag=true;
										break;
									}
									else if (sCheckForPartialWindowTitle.equals("Y") && titleOfWindow.contains(sWindow) ){
										windowFoundFlag=true;
										break;
									}
								}

							} catch (Exception e) {
								windowFoundFlag = false;
							} 
						}

					}
					else
					{
						windowFoundFlag = false;
					}
				} catch (Exception e) 
				{
					windowFoundFlag = false;
				} 

				if(windowFoundFlag)
				{
					break;
				}

			}
			timeWatcher.cancel();
		} catch (Exception e)
		{
			logger.error(ERROR_NOT_ABLE_TO_SELECT_WINDOW,e);
			testCaseExecutionResult.setMessage(ERROR_NOT_ABLE_TO_SELECT_WINDOW);
		}

		if (windowFoundFlag)
		{
			testCaseExecutionResult.setStatus(PASS);
		}
		else
		{  
			logger.error(ERROR_NOT_ABLE_TO_SELECT_WINDOW);
			testCaseExecutionResult.setMessage(ERROR_NOT_ABLE_TO_SELECT_WINDOW);
			
		}
		return testCaseExecutionResult;
	

	}

	/**
	 * This method does Keyword level validation i.e. checking required
	 * parameters ,its format etc


	 * @return ExecutionResults containing step execution status(pass/fail),
	 *         exact error message according to failure
	 */
	@Override
	public TestcaseExecutionResultVO validateKeyword(String... listOfParameters)
	{
		String waitTime="";
		if (listOfParameters != null)
		{
			sWindow = listOfParameters[0];
			waitTime = listOfParameters[1];
			sCheckForPartialWindowTitle = listOfParameters[2];
			
		} else
		{ 
			logger.error(ERROR_PARAMETERS_LIST);
			testCaseExecutionResult.setMessage(ERROR_PARAMETERS_LIST);
			return testCaseExecutionResult;
		}
		testCaseExecutionResult.setTestData(sWindow);

		if (KeywordUtilities.isEmptyString(sWindow))
		{
			logger.error(ERROR_FRAME_NAME_NOT_PASSED);
			testCaseExecutionResult.setMessage(ERROR_FRAME_NAME_NOT_PASSED);
			return testCaseExecutionResult;
		}
		if (sCheckForPartialWindowTitle==null || KeywordUtilities.isEmptyString(sCheckForPartialWindowTitle) || (!sCheckForPartialWindowTitle.equalsIgnoreCase("Y") && !sCheckForPartialWindowTitle.equalsIgnoreCase("YES") && !sCheckForPartialWindowTitle.equalsIgnoreCase("TRUE")))
		{
			sCheckForPartialWindowTitle="N";
		}
		else{
			sCheckForPartialWindowTitle="Y";
		}
		
		if(waitTime.isEmpty()){
			//int toWait_PgLoad=120;
			try{
				String val = configurationMap.get(KTTGuiConstants.PAGE_LOAD_WAIT_VARIABLE);
				if(val!=null)
				{
					maxWaitTime =Integer.parseInt(val); 
				}
			}catch(Exception e){maxWaitTime=120;
			//e.printStackTrace();
			}
		}
		else{
			try{
				maxWaitTime = Integer.parseInt(waitTime);
			}
			catch(Exception e){
				logger.error("Invalid wait time specified. Must be number",e);
				testCaseExecutionResult.setMessage("Invalid wait time specified. Must be number");
				testCaseExecutionResult.setStatus(FAIL);
				return testCaseExecutionResult;
			}
		}
		
		testCaseExecutionResult.setValid(true);
		return testCaseExecutionResult;
	}
	/**
	 * This method validates the object on the browser

	 * @return ExecutionResults containing step execution status(pass/fail),
	 *         exact error message according to failure
	 */
	@Override
	public TestcaseExecutionResultVO validateObject(String... listOfParameters)
	{
		if (webDriver == null) {
			logger.error ("ERROR_BROWSER_NOT_INSTANTIATED");
			testCaseExecutionResult.setMessage(ERROR_BROWSER_NOT_INSTANTIATED);
			testCaseExecutionResult.setValid(false);
			return testCaseExecutionResult;
		}testCaseExecutionResult.setValid(true);
		return testCaseExecutionResult;
	}
}