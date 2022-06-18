package com.sungard.ktt.business.keywords;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.log4j.Logger;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import com.sungard.ktt.model.valueobjects.TestcaseExecutionResultVO;

public class ActionBuilderKeyword extends AbstractKeyword {

private String listOfMethods = null;
private String methodParams=null;
private String locators=null;

TestcaseExecutionResultVO testCaseExecutionResult = new TestcaseExecutionResultVO();
Logger logger = Logger.getLogger("Thread" + Thread.currentThread().getName());
	@Override
	public TestcaseExecutionResultVO validateKeyword(String... listOfParameters) {
		locators=listOfParameters[0];
		listOfMethods=listOfParameters[1];
		methodParams=listOfParameters[2];
		Actions actions=new Actions(webDriver);
		int numOfLocators=locators.split(";").length;
		if(locators.endsWith(";")) 
			numOfLocators=numOfLocators+1;
		/*if(listOfMethods.split(";").length != numOfLocators){
			logger.error ("Mismatched arguments:check No. of locators and No. of methods passed");
			testCaseExecutionResult.setMessage("Mismatched arguments:check No. of locators and No of methods passed");
			testCaseExecutionResult.setValid(false);
			return testCaseExecutionResult;
		}*/
		
		Method[] methods=actions.getClass().getMethods();
		HashSet<String> supportedMethodsNames=new HashSet<String>();
		for(Method m:methods){
			supportedMethodsNames.add(m.getName());
		}
		for(String method:listOfMethods.split(";")){
			if(!supportedMethodsNames.contains(method)){
				logger.error ("Method not found:"+method);
				
				testCaseExecutionResult.setMessage("Method not found:"+method);
				testCaseExecutionResult.setValid(false);
				return testCaseExecutionResult;
			}
		}
		testCaseExecutionResult.setValid(true);
		return testCaseExecutionResult;
	}

	@Override
	public TestcaseExecutionResultVO validateObject(String... listOfParameters) {
		
		testCaseExecutionResult=KeywordUtilities.noValidationRequired();
		
		return testCaseExecutionResult;
	}

	@Override
	public TestcaseExecutionResultVO executeScript(String... listOfParameters) {	
		
		
		@SuppressWarnings("rawtypes")
		HashMap<String,Class[]> methodParamMap=new HashMap<String, Class[]>();
		
		Actions actions=new Actions(webDriver);
		
	
		String name="";
		
		logger.info("Building supported method map");
		for(Method m:actions.getClass().getMethods()){
			name=m.getName() + m.getParameterTypes().length;
			methodParamMap.put(name, m.getParameterTypes());
			
		}
	
		HashMap<Integer,Integer> numOfParams=new HashMap<>();		
		int methodIndex=0;
		//String[] locatorsArr=locators.split(";");
		ArrayList<String> locatorsList=new ArrayList<String>(Arrays.asList(locators.split(";")));
		
		if(locators.endsWith(";"))
			locatorsList.add("");
		int index=0;
		//for(String params:methodParams.split(";") ){
		String[] methodParameters=methodParams.split(";");
		String params="";
		
		logger.info("Building Action builder method's parameter list");
		
		for(String method:listOfMethods.split(";")){
			try{
				params=methodParameters[index];
			}
			catch(Exception e){params="";}
			
			
			for(String param:params.split(",")){
				if(param.contains("+")){
					if(locatorsList.get(index).length() > 0 && 
							!params.isEmpty())
						numOfParams.put(methodIndex,2);
					else
						numOfParams.put(methodIndex,1);
				}
				else{
					if(locatorsList.get(index).length() > 0 && !locatorsList.get(index).equalsIgnoreCase("<BLANK>") &&
							 !params.equalsIgnoreCase("<BLANK>") && !params.isEmpty()){
						numOfParams.put(methodIndex, params.split(",").length+(locatorsList.get(index).split("<LOC>").length));
					}
					else if(locatorsList.get(index).length() > 0 && !locatorsList.get(index).equalsIgnoreCase("<BLANK>") &&
							 (params.equalsIgnoreCase("<BLANK>") || params.isEmpty())){
						numOfParams.put(methodIndex, locatorsList.get(index).split("<LOC>").length);
					}
					else if(params.contains("<BLANK>") || params.isEmpty()) {
						numOfParams.put(methodIndex, 0);
					}
					else {
						numOfParams.put(methodIndex, params.split(",").length);
					}
				}
			}
			index++;
			methodIndex++;
		}
		methodIndex=0;
		
		Method m=null;
		String[] paramList=methodParams.split(";");
		WebElement element=null;
		
		ArrayList<Object> objectParamList = new ArrayList<Object>();
		CharSequence[] arrChar=(CharSequence[]) Array.newInstance(CharSequence.class,1);
		
		logger.info("Building Actions class object by invoking each method");
		try{
			index=0;
		for(String method:listOfMethods.split(";")){
			
			m=actions.getClass().getMethod(method, methodParamMap.get(method+numOfParams.get(methodIndex)));
			
			if(numOfParams.get(methodIndex) > 0){
				///for(String param:paramList[methodIndex].split(",")){
					//String s=locatorsList.get(methodIndex);
					
					if(locatorsList.get(methodIndex).length() >0 ){
						for (String elementLocator : locatorsList.get(methodIndex).split("<LOC>")) {
							if (!elementLocator.equalsIgnoreCase("<BLANK>")) {
								element = KeywordUtilities.waitForElementPresentAndEnabledInstance(configurationMap,
										webDriver, elementLocator, "", userName);
								if (element == null) {
									actions = null;
									logger.error("WebElement not found" + elementLocator);
									testCaseExecutionResult.setMessage("WebElement not found:" + elementLocator);
									testCaseExecutionResult.setStatus(0);
									return testCaseExecutionResult;
								}
								objectParamList.add(element);
							}
						}
					}
					if((paramList.length== 1  && !paramList[0].isEmpty()) || paramList.length>= (methodIndex+1) ){
						for(String param:paramList[methodIndex].split(",")){
								
									if(param.toUpperCase().endsWith("KEY") && !method.equalsIgnoreCase("sendKeys")){
										
										objectParamList.add(Keys.valueOf(param.substring(0,param.length()-3)));
									}
									
									else if(method.equalsIgnoreCase("sendKeys")){
										if(param.toUpperCase().endsWith("KEY") && !param.contains("+")){
											arrChar[0]=Keys.valueOf(param.substring(0,param.length()-3));
											objectParamList.add(arrChar);
										}
										else if(param.contains("+")){
											int i=0;
											arrChar=(CharSequence[]) Array.newInstance(CharSequence.class,param.split("\\+").length);
											for(String p:param.split("\\+")){
												if(p.toUpperCase().endsWith("KEY"))
													arrChar[i]=Keys.valueOf(p.substring(0,p.length()-3));
												else
													arrChar[i]=p;
												i++;
												
											}
											objectParamList.add(arrChar);
										}
										else{
											arrChar=(CharSequence[]) Array.newInstance(CharSequence.class,1);
											arrChar[0]=param;
											objectParamList.add(arrChar);
										}
										
									}
									else if(param!=null & !param.isEmpty()){
										int value = 0;
										try {
											value = Integer.parseInt(param);
										}
										catch(Exception e) {
											value = -91234;
										}
										if(value!=-91234)
											objectParamList.add(value);
									}
								
							//index++;
						}
					}
					if(!objectParamList.isEmpty())
						actions=(Actions) m.invoke(actions,objectParamList.toArray());
					else{
						//objectParamList.add(null);
						actions=(Actions) m.invoke(actions,null);//actions,objectParamList); 
					}
				
				objectParamList = new ArrayList<Object>();
			}
			else{
				actions=(Actions) m.invoke(actions);
			}
			methodIndex++;
		}

		logger.info("Performing actions");
		actions.build().perform();
		}
		catch(InvocationTargetException e1){
			logger.error ("An invocation target error occured while execution"+e1.getTargetException());
			testCaseExecutionResult.setMessage("An invocation target error occured while execution:"+e1.getTargetException());
			testCaseExecutionResult.setStatus(0);
			return testCaseExecutionResult;
		}
		catch(Exception e2){
			
			logger.error ("An error occured while execution",e2);
			testCaseExecutionResult.setMessage("An error occured while execution:"+e2.getMessage());
			testCaseExecutionResult.setStatus(0);
			return testCaseExecutionResult;
		}
			
		testCaseExecutionResult.setStatus(1);
		return testCaseExecutionResult;
	}

}




/*WebElement e=webDriver.findElement(By.linkText("Next Chapter »"));
actions.keyDown(Keys.CONTROL).click(e).keyUp(Keys.CONTROL).build().perform();
webDriver.findElement(By.xpath("//a[text()='Next Chapter »']")).click();
WebElement targetElement=null;
switch(operation){
case "KEYMOUSEOPERATION":
	
	String keys=listOfParameters[1];
	String mouseAction=listOfParameters[2];
	String element=listOfParameters[3];
	String[] keyArr=keys.split("\\+");
	for(String key:keyArr){
		actions.keyDown(keyMap.get(key.toUpperCase()));
	}
	if(element.isEmpty()){
		if(mouseAction.equalsIgnoreCase("CLICK")){
			actions.click();
		}
		else if(mouseAction.equalsIgnoreCase("RIGHTCLICK")){
			actions.contextClick();
		}
		else if(mouseAction.equalsIgnoreCase("DOUBLECLICK")){
			actions.doubleClick();
		}
	}
	else {
		targetElement=KeywordUtilities.waitForElementPresentAndEnabledInstance(configurationMap,webDriver, element,"", userName);
		if (targetElement==null) {

			logger.error ("Target Element not found");
			testCaseExecutionResult.setMessage("Target Element not found");
			testCaseExecutionResult.setValid(false);
			return testCaseExecutionResult;
		}
		if(mouseAction.equalsIgnoreCase("CLICK")){
			actions.click(targetElement);
		}
		else if(mouseAction.equalsIgnoreCase("RIGHTCLICK")){
			actions.contextClick(targetElement);
		}
		else if(mouseAction.equalsIgnoreCase("DOUBLECLICK")){
			actions.doubleClick(targetElement);
		}
	}
	for(String key:keyArr){
		actions.keyUp(keyMap.get(key.toUpperCase()));
	}
	
	break;
}
*/
