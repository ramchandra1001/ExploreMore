package com.sungard.ktt.business.keywords;


import static com.sungard.ktt.business.keywords.ErrorMessages.ERROR_IE_CANNOT_DSIPLAY_PAGE;
import static com.sungard.ktt.business.keywords.ErrorMessages.ERROR_LAUNCH_URL;
import static com.sungard.ktt.business.keywords.ErrorMessages.ERROR_PARAMETERS_LIST;
import static com.sungard.ktt.business.keywords.ErrorMessages.ERROR_URL_NOT_PASSED;
import static com.sungard.ktt.view.config.KTTGuiConstants.DELIMITER;
import static com.sungard.ktt.view.config.KTTGuiConstants.EMPTY_STRING;
import static com.sungard.ktt.view.config.KTTGuiConstants.FAIL;
import static com.sungard.ktt.view.config.KTTGuiConstants.FIREFOX;
import static com.sungard.ktt.view.config.KTTGuiConstants.PASS;
import static com.sungard.ktt.view.config.KTTGuiConstants.SKIP_URL_CHECK;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.openqa.selenium.Alert;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Point;
import org.openqa.selenium.SessionNotCreatedException;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.firefox.internal.ProfilesIni;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.ie.InternetExplorerOptions;
import org.openqa.selenium.opera.OperaOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.safari.SafariOptions;

import com.sungard.ktt.business.cloud.BrowserStackCloud;
import com.sungard.ktt.business.cloud.SafalCloud;
import com.sungard.ktt.business.cloud.utils.CloudUtils;
import com.sungard.ktt.business.keywords.mobile.AndroidAuthenticationKeyword;
import com.sungard.ktt.business.keywords.mobile.SetMobileDeviceInfoKeyword;
import com.sungard.ktt.model.bo.KeywordExecutionBO;
import com.sungard.ktt.model.config.BrowserLanguagePropertiesReader;
import com.sungard.ktt.model.config.InitSAFALProperties;
import com.sungard.ktt.model.valueobjects.RemoteWebDriverConfigDetails;
import com.sungard.ktt.model.valueobjects.TestcaseExecutionResultVO;
import com.sungard.ktt.view.config.KTTGuiConstants.modes;
import com.sungard.ktt.view.run.ScriptExecutionService;
///***
import com.sungard.ktt.web.util.SAFALUserSession;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;

/**
 * @author Dnyaneshwar.Daphal
 *Starts Internet Explorer browser and loads the URL specified in the 
 *Environment XML file or in Test case. According to browser setting, URL get launched in appropriate browser i.e. IE or Firefox etc.


 */
public class LaunchBrowserKeyword extends AbstractKeyword  {
	/**
	 * This is logger object used to log keyword actions into a log file
	 */
	Logger logger = Logger.getLogger("Thread" + Thread.currentThread().getName());
	/**
	 * expected URL to launch
	 */
	private String sURLToLaunch = null;
	/**
	 * If �SkipURLValidation� is given, keyword will not match the URL from launched browser with expected URL
	 */
	private String sCheck = null;
	/**
	 * Expected max page load time.
    Default is 3 seconds.
	 */
	private String sWaitCounter = null;
	/**
	 * Default N
       If user want to load with cookies then need to update  
      (SELENIUM_COOKIE_VARIABLE) which gets updated under ClosebBrowser
	 */
	private String loadWithCookies=null;
	/**
	 * Default en-us, user can give nay one of the language which is supported by Browser
	 */
	private String loadWithLaunguage=null;
	/**
	 * Default N
	 */
	private String maximizeBrowser= null;
	
	private String fullScreenBrowser= null;
	/**
	 * Default Blank
	 */
	private String nameOfInstance=null;
	/**
	 * @param environment Browser string
	 * Please do not change the name of below method, it has to be there
	 */
	private String environment;

	
	private WebDriver tmpWebDriver;
	
	private String devicestatus= "Connected";

	private static HashMap<String,Boolean> devConStattusWithAppiumServer = new HashMap<String, Boolean>();
	
	private RemoteWebDriverConfigDetails remoteWebDriverConfigDetails;
	private String remoteServerURL;
	private String executionMode="local";
	private String sWidth="";
	private String sHeight="";
	
	TestcaseExecutionResultVO testCaseExecutionResult = new TestcaseExecutionResultVO();	
	
	public void setLaunchParameters(String environment, RemoteWebDriverConfigDetails remoteWebDriverConfigDetails)
	{
		this.environment = environment;
		this.remoteWebDriverConfigDetails = remoteWebDriverConfigDetails; // Added for SAF-3380
		
	}

	private boolean initiateCloudTest(DesiredCapabilities capabilities,URL hubUrl){
		logger.info("Starting cloud test using URL "+hubUrl.toString());
		try{
			String proxyUserName=configurationMap.get("ProxyUserName");
			String proxyPassword=configurationMap.get("ProxyPassword");
			String proxyDomainName=configurationMap.get("ProxyDomainName");
			
			String cloudName=remoteWebDriverConfigDetails.getCloudName().trim().toLowerCase();
			configurationMap.put("CloudName", cloudName);			
			
			String httpProxyServer = configurationMap.get("ProxyHostName");
			String proxyPortNumber = configurationMap.get("ProxyPortNumber");
			
			if(!CloudUtils.isProxySet() || httpProxyServer==null || httpProxyServer.isEmpty()){		
				logger.info("Scanning for proxy ....");
				CloudUtils.scanForProxyServer(hubUrl.toString());
				httpProxyServer=CloudUtils.getProxyHost();
				logger.info("Proxy detected is :"+httpProxyServer);
				configurationMap.put("ProxyHostName",httpProxyServer);
				try{
					proxyPortNumber=Integer.toString(CloudUtils.getProxyPort());
					configurationMap.put("ProxyPortNumber",proxyPortNumber);
				}
				catch(Exception e){
					configurationMap.put("ProxyPortNumber","8080");
				}
			}
			else if(httpProxyServer!=null && CloudUtils.getProxyHost()==null){
				CloudUtils.setProxyHost(httpProxyServer);
				try{
					CloudUtils.setProxyPort(Integer.parseInt(proxyPortNumber));
				}
				catch(Exception e){
					CloudUtils.setProxyPort(8080);
					configurationMap.put("ProxyPortNumber","8080");
				}
			}
			
			
			switch(cloudName){			
				case "browserstack":
					BrowserStackCloud browserStackCloud=null;
					if(configurationMap.get("SAFAL_MAIN_WINDOW_HANDLE")==null ||  configurationMap.get("SAFAL_MAIN_WINDOW_HANDLE").isEmpty()){
						browserStackCloud=BrowserStackCloud.getInstance(null,true);
					}
					else{
						browserStackCloud=BrowserStackCloud.getInstance(null,false);
					}
					 
					capabilities=browserStackCloud.buildBrowserStackCapabilities(capabilities,remoteWebDriverConfigDetails);
					if(browserStackCloud.isCapabilitiesValid() && capabilities!=null){
						if(httpProxyServer!=null){							
							
							this.webDriver = new CloudUtils().connectViaProxy(hubUrl,capabilities, proxyUserName, proxyPassword, proxyDomainName);
							
							if(this.webDriver==null){
								logger.info("Unable to connect to URL using proxy.");
								testCaseExecutionResult.setMessage("Unable to connect to URL using proxy.");
								return false;
							}
						}
						else{
							this.webDriver = new RemoteWebDriver(hubUrl, capabilities);
						}
						
						String sessionID= ((RemoteWebDriver) this.webDriver).getSessionId().toString();
						if(sessionID!=null && !sessionID.isEmpty()){
							remoteWebDriverConfigDetails.setSessionID(sessionID);
							testCaseExecutionResult.setMessage("BrowserStack SessionID:"+ sessionID);
							browserStackCloud.updateSessionMap(sessionID);							
							logger.info("Successfully connected to cloud");
							return true;
						}
						else{
							try{
								this.webDriver.getTitle();
							}
							catch(Exception e){
								logger.info("Failed to create session",e);
								testCaseExecutionResult.setMessage("Failed to create session: "+e.getMessage());
								return false;
							}
						}
					}
					else{
						logger.error("An error has occured while setting remote capabilities.Please check script.");
						testCaseExecutionResult.setMessage("An error has occured while setting remote capabilities.Please check script.");				
					}
					break;
					
				case "safalcloud":
					SafalCloud safalCloud=null;
					if(configurationMap.get("SAFAL_MAIN_WINDOW_HANDLE")==null ||  configurationMap.get("SAFAL_MAIN_WINDOW_HANDLE").isEmpty()){
						safalCloud=SafalCloud.getInstance(null,true);
					}
					else{
						safalCloud=SafalCloud.getInstance(null,false);
					}					 
					capabilities=safalCloud.buildSafalCloudCapabilities(capabilities,remoteWebDriverConfigDetails);
					if(safalCloud.isCapabilitiesValid() && capabilities!=null){
						if(httpProxyServer!=null){
							
							this.webDriver = new CloudUtils().connectViaProxy(hubUrl,capabilities, proxyUserName, proxyPassword, proxyDomainName);
							
							if(this.webDriver==null){
								logger.error("Unable to connect to URL using proxy.");
								testCaseExecutionResult.setMessage("Unable to connect to URL using proxy.");
								return false;
							}
						}
						else{
							this.webDriver = new RemoteWebDriver(hubUrl, capabilities);
						}
						String sessionID= ((RemoteWebDriver) this.webDriver).getSessionId().toString();
						if(sessionID!=null && !sessionID.isEmpty()){
							remoteWebDriverConfigDetails.setSessionID(sessionID);
							testCaseExecutionResult.setMessage("SAFAL Cloud SessionID:"+ sessionID);
							safalCloud.updateSessionMap(sessionID);							
							logger.info("Successfully connected to cloud");
							return true;
						}
						else{
							try{
								this.webDriver.getTitle();
							}
							catch(Exception e){
								logger.info("Failed to create session",e);
								testCaseExecutionResult.setMessage("Failed to create session: "+e.getMessage());
								return false;
							}
						}
					}
					else{
						logger.error("An error has occured while setting remote capabilities.Please check script.");
						testCaseExecutionResult.setMessage("An error has occured while setting remote capabilities.Please check script.");				
					}
					break;
				
				default:
					logger.error("Invalid clould name specified.Please check script.");
					testCaseExecutionResult.setMessage("Invalid clould name specified.Please check script.");				
			}
		}
		catch(Exception e){
			logger.error("An error occured while connecting to cloud",e);
			testCaseExecutionResult.setMessage("An error occured while connecting to cloud");
		}
		return false;
	}
	
	@Override
	/**
	 * After all validation, this method actually launches the required url
	 * @return ExecutionResults containing step execution status(pass/fail),
	 *         exact error message according to failure
	 */	
	
	public TestcaseExecutionResultVO executeScript(String... listOfParameters) {
		
		logger.info("Launch browser environment:"+environment);

		String HANDLE_UNEXPECTED_ALERT ="false";
		try {
			HANDLE_UNEXPECTED_ALERT = configurationMap.get("HANDLE_UNEXPECTED_ALERT");
			if(HANDLE_UNEXPECTED_ALERT==null || "".equals(HANDLE_UNEXPECTED_ALERT))
			{
				HANDLE_UNEXPECTED_ALERT="false";
			}
			else{
				HANDLE_UNEXPECTED_ALERT="true";
			}
		} catch (Exception e) {
			HANDLE_UNEXPECTED_ALERT="false";
		}
		
		String sIE_DISABLE_NATIVE_EVENTS="false";
		try {
			sIE_DISABLE_NATIVE_EVENTS = configurationMap.get("IE_DISABLE_NATIVE_EVENTS");
			if(sIE_DISABLE_NATIVE_EVENTS==null || "".equals(sIE_DISABLE_NATIVE_EVENTS))
			{
				sIE_DISABLE_NATIVE_EVENTS="false";
			}
		} catch (Exception e) {
			sIE_DISABLE_NATIVE_EVENTS="false";
		}


		String env_Interactive_Mode="false";

		try {
			env_Interactive_Mode = configurationMap.get("INTERACTIVE MODE");
			if(env_Interactive_Mode==null || "".equals(env_Interactive_Mode))
			{
				env_Interactive_Mode="false";
			}
		} catch (Exception e) {
			env_Interactive_Mode="false";
		}

		boolean cookieLoadFlag=false;

		if(KeywordUtilities.isEmptyString(loadWithCookies))
		{
			cookieLoadFlag=false;
		}
		else if (loadWithCookies.equalsIgnoreCase("YES") || loadWithCookies.equalsIgnoreCase("Y") || loadWithCookies.equalsIgnoreCase("TRUE"))
		{
			cookieLoadFlag=true;
		}

		if (KeywordUtilities.isEmptyString(maximizeBrowser))
		{
			maximizeBrowser="N";
		}
		
		String ip = SAFALUserSession.getClientIP(userName);
		if(ip == null) {
			ip = "localhost";
		}
		
		if(SAFALUserSession.getMode(userName) == modes.WEB){
			remoteServerURL="http://" + ip + ":4444/wd/hub";
			executionMode="web";
		}
		else if(remoteWebDriverConfigDetails!=null && (remoteWebDriverConfigDetails.getRemoteServerUrl()!=null && !remoteWebDriverConfigDetails.getRemoteServerUrl().isEmpty())){
			remoteServerURL=remoteWebDriverConfigDetails.getRemoteServerUrl();	
			executionMode="cloud";
		}
		
		
		
		logger.info("Inside LaunchBrowserKeyword executeScript userName:"+ userName+" userIP: "+ip);
		
		DesiredCapabilities capabilities =null;

		String browserName = "";
		String browserVersion = "";

		/*if(env_Interactive_Mode.equalsIgnoreCase("TRUE")||this.environment.toUpperCase().contains("PHANTOMJS"))
		{
			
			capabilities = DesiredCapabilities.phantomjs();
			//DesiredCapabilities.htmlUnitWithJs();
			//capabilities.setCapability(CapabilityType.PROXY,new Proxy().setHttpProxy("we1proxy01:8080"));
			capabilities.setCapability(CapabilityType.HAS_NATIVE_EVENTS, true);
			capabilities.setCapability(CapabilityType.TAKES_SCREENSHOT, true);
			capabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
			capabilities.setCapability(CapabilityType.SUPPORTS_JAVASCRIPT,true);
			capabilities.setCapability(CapabilityType.SUPPORTS_APPLICATION_CACHE,true);
			capabilities.setCapability(CapabilityType.SUPPORTS_FINDING_BY_CSS,true);
			capabilities.setCapability(CapabilityType.SUPPORTS_LOCATION_CONTEXT,true);
			capabilities.setCapability(CapabilityType.SUPPORTS_WEB_STORAGE,true);
			capabilities.setCapability(CapabilityType.SUPPORTS_ALERTS,true);
			capabilities.setCapability("requireWindowFocus", "true");
			capabilities.setCapability(CapabilityType.ForSeleniumServer.ENSURING_CLEAN_SESSION, true);
			capabilities.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY, "./SafalConfigs/Drivers/phantomjs.exe");
			
			//synchronized (ScriptExecutionService.getInstance()) 
			synchronized (this)
			{
				String overideUSerAgentString = "false";
				try {
					overideUSerAgentString = configurationMap.get("USERAGENTSTRING");
					if(overideUSerAgentString==null || "".equals(overideUSerAgentString))
					{
						overideUSerAgentString="false";
					}
				} catch (Exception e) {
					overideUSerAgentString="false";
				}
				if(!overideUSerAgentString.equalsIgnoreCase("false")){
					capabilities.setCapability("phantomjs.page.settings.userAgent",overideUSerAgentString);
				}
				
				try{
					if((SAFALUserSession.getMode(userName) == modes.DESKTOP ||
							SAFALUserSession.getMode(userName) == modes.NONUI) && executionMode.equals("local")) {
						PhantomJSDriver phantomJS = new PhantomJSDriver(capabilities);
						this.webDriver = phantomJS;
					}
					else{
						URL hubUrl = null;
						hubUrl = new URL(remoteServerURL);
						if(initiateCloudTest(capabilities,hubUrl)==false){
							return testCaseExecutionResult;
						}	
					}
				}catch(Throwable e){
					logger.error("Failed to instantiate PhantonJSDriver",e);
				}
			}
		}
		else*/
		{
			
			File file = null;

			Properties pr = System.getProperties();
			String propValue = pr.getProperty("os.arch");
			
			if(this.environment.toUpperCase().contains("FIREFOX"))
			{
				logger.info("In FireFox");
				
				if(propValue.equalsIgnoreCase("x86"))
				{
					file = new File("./SafalConfigs/Drivers/geckodriver32.exe");
				}
				else
				{
					file = new File("./SafalConfigs/Drivers/geckodriver64.exe");
				}
				
				System.setProperty("webdriver.gecko.driver", file.getAbsolutePath());
				
				capabilities = DesiredCapabilities.firefox();
				FirefoxOptions options = new FirefoxOptions();

				ProfilesIni allProfiles = new ProfilesIni();
				FirefoxProfile profile = null;
				
				if(executionMode.equals("local")){
					profile = allProfiles.getProfile("default");					
				}
				else{
					profile=new FirefoxProfile();
				}
				profile.setAcceptUntrustedCertificates(true); 
				//FirefoxProfile profile = allProfiles.getProfile("SeleniumProfile");
				

				//FirefoxProfile profile = new FirefoxProfile();
				//profile.setPreference("dom.max_chrome_script_run_time", "600"); 
				//profile.setPreference("dom.max_script_run_time", "600");
				//===SAF-3460
				profile.setAcceptUntrustedCertificates(true);
				//===SAF-3460

				if (!"".equals(loadWithLaunguage))
				{
					profile.setPreference("intl.accept_languages",loadWithLaunguage);
					//capabilities.setCapability("firefox_profile", profile);
				}

				capabilities.setCapability("firefox_profile", profile);
				options.setCapability("firefox_profile", profile);
				
				//Added because latest gecko driver doesn't support unexpected alert capability.Need to remove this , when gecko driver will start supporting this capability 
				String skipAlertCheck = configurationMap.get("SkipUnexpectedAlertCheckForFirefox");
				if(skipAlertCheck==null ||skipAlertCheck.equalsIgnoreCase("false") ){
					skipAlertCheck="false";
				}
				else{
					skipAlertCheck="true";
				}
				
				if(HANDLE_UNEXPECTED_ALERT.equals("false") && skipAlertCheck.equals("false"))
				{
					capabilities.setCapability(CapabilityType.UNEXPECTED_ALERT_BEHAVIOUR,org.openqa.selenium.UnexpectedAlertBehaviour.IGNORE);
					options.setCapability(CapabilityType.UNEXPECTED_ALERT_BEHAVIOUR,org.openqa.selenium.UnexpectedAlertBehaviour.IGNORE);
					
				}
				//===SAF-3460
				capabilities.setCapability("acceptInsecureCerts", true);
				//===SAF-3460
				capabilities.setCapability(CapabilityType.HAS_NATIVE_EVENTS, true);
				capabilities.setCapability(CapabilityType.TAKES_SCREENSHOT, true);
				capabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
				capabilities.setCapability(CapabilityType.SUPPORTS_JAVASCRIPT,true);
				capabilities.setCapability(CapabilityType.SUPPORTS_APPLICATION_CACHE,true);
				capabilities.setCapability(CapabilityType.SUPPORTS_FINDING_BY_CSS,true);
				capabilities.setCapability(CapabilityType.SUPPORTS_LOCATION_CONTEXT,true);
				capabilities.setCapability(CapabilityType.SUPPORTS_WEB_STORAGE,true);
				capabilities.setCapability(CapabilityType.SUPPORTS_ALERTS,true);
				capabilities.setCapability("requireWindowFocus", "true");
				capabilities.setCapability(CapabilityType.ForSeleniumServer.ENSURING_CLEAN_SESSION, true);
				
				options.setCapability("browserName","firefox");
				options.setCapability("acceptInsecureCerts", true);				
				options.setCapability(CapabilityType.HAS_NATIVE_EVENTS, true);
				options.setCapability(CapabilityType.TAKES_SCREENSHOT, true);
				options.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
				options.setCapability(CapabilityType.SUPPORTS_JAVASCRIPT,true);
				options.setCapability(CapabilityType.SUPPORTS_APPLICATION_CACHE,true);
				options.setCapability(CapabilityType.SUPPORTS_FINDING_BY_CSS,true);
				options.setCapability(CapabilityType.SUPPORTS_LOCATION_CONTEXT,true);
				options.setCapability(CapabilityType.SUPPORTS_WEB_STORAGE,true);
				options.setCapability(CapabilityType.SUPPORTS_ALERTS,true);
				options.setCapability("requireWindowFocus", "true");
				options.setCapability(CapabilityType.ForSeleniumServer.ENSURING_CLEAN_SESSION, true);
				
				if((SAFALUserSession.getMode(userName) == modes.DESKTOP ||
						SAFALUserSession.getMode(userName) == modes.NONUI || SAFALUserSession.getMode(userName) == modes.CMDLINE || SAFALUserSession.getMode(userName) == modes.TESTEXECUTIONSERVER) && executionMode.equals("local")) {
					synchronized (this) {
						this.webDriver = new FirefoxDriver(options);
					}
				} else {
					try {
						capabilities.setBrowserName("firefox");
						URL hubUrl = null;
						hubUrl = new URL(remoteServerURL);
						if(executionMode.equals("cloud")){
							if(initiateCloudTest(capabilities,hubUrl)==false){
								return testCaseExecutionResult;
							}							
						}
						else if(executionMode.equals("web")){
							this.webDriver = new RemoteWebDriver(hubUrl, capabilities);
						}
					} catch (MalformedURLException e) {
						logger.error("Failed to connect to Firefox remote web driver",e);
					}
				}
			}
			else if(this.environment.toUpperCase().contains("IEXPLORE") || this.environment.toUpperCase().contains("INTERNET EXPLORER") || this.environment.toUpperCase().contains("INTERNETEXPLORER"))
			{
				logger.info("In IEXPLORE mode : " + SAFALUserSession.getMode(userName));
				
				if(propValue.equalsIgnoreCase("x86"))
				{
					file = new File("./SafalConfigs/Drivers/IEDriverServer32.exe");
				}
				else
				{
					file = new File("./SafalConfigs/Drivers/IEDriverServer64.exe");
				}

				if(!file.exists())
				{
					logger.error("Unable to find required IEDriver file at " + file.getAbsolutePath());
					
					testCaseExecutionResult.setMessage("Unable to find required IEDriver file at " + file.getAbsolutePath());
					return testCaseExecutionResult;
				}

				try {
					System.setProperty("webdriver.ie.driver", file.getAbsolutePath());
				} catch (Exception e2) {
					logger.error("Unable to set System Property : webdriver.ie.driver, ", e2 );
					
					testCaseExecutionResult.setMessage("Unable to set System Property : webdriver.ie.driver, "+ e2.getMessage());
					//return testCaseExecutionResult;
				}
				
				logger.info("In IEXPLORE 2");
				
				capabilities = DesiredCapabilities.internetExplorer();
				capabilities.setCapability(InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS,true);

				if(sIE_DISABLE_NATIVE_EVENTS.equalsIgnoreCase("TRUE"))
				{
					capabilities.setCapability(InternetExplorerDriver.NATIVE_EVENTS, false);
				}
				else
				{
					capabilities.setCapability(InternetExplorerDriver.NATIVE_EVENTS, true);
				}
				//capabilities.setCapability(InternetExplorerDriver.ELEMENT_SCROLL_BEHAVIOR,true);
				//capabilities.setCapability(InternetExplorerDriver.IE_ENSURE_CLEAN_SESSION, true);
				if(HANDLE_UNEXPECTED_ALERT.equals("false")){
					capabilities.setCapability(InternetExplorerDriver.UNEXPECTED_ALERT_BEHAVIOR,org.openqa.selenium.UnexpectedAlertBehaviour.IGNORE);
				}
				capabilities.setCapability(CapabilityType.TAKES_SCREENSHOT, true);
				capabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
				capabilities.setCapability(CapabilityType.SUPPORTS_JAVASCRIPT,true);
				capabilities.setCapability(CapabilityType.SUPPORTS_APPLICATION_CACHE,true);
				capabilities.setCapability(CapabilityType.SUPPORTS_FINDING_BY_CSS,true);
				capabilities.setCapability(CapabilityType.SUPPORTS_LOCATION_CONTEXT,true);
				capabilities.setCapability(CapabilityType.SUPPORTS_WEB_STORAGE,true);
				capabilities.setCapability(CapabilityType.SUPPORTS_ALERTS,true);
				capabilities.setCapability(InternetExplorerDriver.REQUIRE_WINDOW_FOCUS,true);
				capabilities.setCapability(InternetExplorerDriver.ENABLE_PERSISTENT_HOVERING, false);
				capabilities.setCapability(InternetExplorerDriver.ENABLE_ELEMENT_CACHE_CLEANUP,true);
				capabilities.setCapability(CapabilityType.ForSeleniumServer.ENSURING_CLEAN_SESSION, true);	

				
				try {
					capabilities.setCapability(InternetExplorerDriver.IGNORE_ZOOM_SETTING, true);
				} catch (Exception e) {
					logger.error("Failed to set IGNORE ZOOM capability for IE",e);
				}
				
				logger.info("In IEXPLORE 3");
				
				//synchronized (ScriptExecutionService.getInstance())
				if((SAFALUserSession.getMode(userName) == modes.DESKTOP ||
						SAFALUserSession.getMode(userName) == modes.NONUI || SAFALUserSession.getMode(userName) == modes.CMDLINE || SAFALUserSession.getMode(userName) == modes.TESTEXECUTIONSERVER)  && executionMode.equals("local")){
					synchronized (this)
					{
						try {
							InternetExplorerOptions ieOptions = new InternetExplorerOptions(capabilities);
							this.webDriver = new InternetExplorerDriver(ieOptions);
							logger.info("inside InternetExplorerDriver(capabilities)");
						} catch (Exception e) {
							try {
								logger.error("inside InternetExplorerDriver");
								this.webDriver = new InternetExplorerDriver();
							} catch (Exception e1) {
								logger.error("inside Unable to Launch Internet Explorer, "+ e1.getMessage() );
								testCaseExecutionResult.setMessage("Unable to Launch Internet Explorer, "+System.getProperty("webdriver.ie.driver")+", "+ e1.getMessage());
								return testCaseExecutionResult;
							}
						}
					} 
				} else {					
					capabilities.setPlatform(org.openqa.selenium.Platform.WINDOWS);
					URL hubUrl = null;
					try {
						hubUrl = new URL(remoteServerURL);
						if(executionMode.equals("cloud")){
							if(initiateCloudTest(capabilities,hubUrl)==false){
								return testCaseExecutionResult;
							}							
						}
						else if(executionMode.equals("web")){
							capabilities.setBrowserName("iexplore");
							this.webDriver = new RemoteWebDriver(hubUrl, capabilities);
						}
					} catch (IOException e) {
						logger.error("executeScript 6 err" , e);
						
					}					
					
					logger.info("End if");
				}
			}
			else if(this.environment.toUpperCase().contains("CHROME"))
			{
				
				logger.info("In CHROME");
				ChromeOptions options = new ChromeOptions();
				String sCHROME_ENABLE_INCOGNITO = configurationMap.get("CHROME_ENABLE_INCOGNITO");
				
				if(sCHROME_ENABLE_INCOGNITO!=null && !sCHROME_ENABLE_INCOGNITO.isEmpty() && (sCHROME_ENABLE_INCOGNITO.equalsIgnoreCase("Y") || sCHROME_ENABLE_INCOGNITO.equalsIgnoreCase("TRUE"))) { 
					options.addArguments("--incognito");
				}
				
				
				capabilities = DesiredCapabilities.chrome();

				if (!"".equals(loadWithLaunguage))
				{
					HashMap<String, String> chromeSettings = new HashMap<String, String>();
					chromeSettings.put("intl.accept_languages",loadWithLaunguage );
					capabilities.setCapability("chrome.prefs",chromeSettings);
					options.setCapability("chrome.prefs",chromeSettings);
					
				}
				if(HANDLE_UNEXPECTED_ALERT.equals("false")){
					capabilities.setCapability(CapabilityType.UNEXPECTED_ALERT_BEHAVIOUR,org.openqa.selenium.UnexpectedAlertBehaviour.IGNORE);
					options.setCapability(CapabilityType.UNEXPECTED_ALERT_BEHAVIOUR,org.openqa.selenium.UnexpectedAlertBehaviour.IGNORE);
				}
				capabilities.setCapability(CapabilityType.HAS_NATIVE_EVENTS, true);
				capabilities.setCapability(CapabilityType.TAKES_SCREENSHOT, true);
				capabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
				capabilities.setCapability(CapabilityType.SUPPORTS_JAVASCRIPT,true);
				capabilities.setCapability(CapabilityType.SUPPORTS_APPLICATION_CACHE,true);
				capabilities.setCapability(CapabilityType.SUPPORTS_FINDING_BY_CSS,true);
				capabilities.setCapability(CapabilityType.SUPPORTS_LOCATION_CONTEXT,true);
				capabilities.setCapability(CapabilityType.SUPPORTS_WEB_STORAGE,true);
				capabilities.setCapability(CapabilityType.SUPPORTS_ALERTS,true);
				capabilities.setCapability("requireWindowFocus", "true");
				capabilities.setCapability(CapabilityType.ForSeleniumServer.ENSURING_CLEAN_SESSION, true);

				options.setCapability(CapabilityType.BROWSER_NAME, "chrome");
				options.setCapability(CapabilityType.HAS_NATIVE_EVENTS, true);
				options.setCapability(CapabilityType.TAKES_SCREENSHOT, true);
				options.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
				options.setCapability(CapabilityType.SUPPORTS_JAVASCRIPT,true);
				options.setCapability(CapabilityType.SUPPORTS_APPLICATION_CACHE,true);
				options.setCapability(CapabilityType.SUPPORTS_FINDING_BY_CSS,true);
				options.setCapability(CapabilityType.SUPPORTS_LOCATION_CONTEXT,true);
				options.setCapability(CapabilityType.SUPPORTS_WEB_STORAGE,true);
				options.setCapability(CapabilityType.SUPPORTS_ALERTS,true);
				options.setCapability("requireWindowFocus", "true");
				options.setCapability(CapabilityType.ForSeleniumServer.ENSURING_CLEAN_SESSION, true);
				

				options.addArguments("disable-infobars");
			
				file = new File("./SafalConfigs/Drivers/chromedriver.exe");

				if(System.getProperty("os.name").contains("Linux")) {
					file = new File("./SafalConfigs/Drivers/chromedriver");
				}
				if(!file.exists())
				{
					testCaseExecutionResult.setMessage("Unable to find required Chrome Driver file at " + file.getAbsolutePath());
					return testCaseExecutionResult;
				}
				
				
				///Added by Ashish to avoid the Chrome driver error for security	
				//ChromeOptions remote_options = new ChromeOptions();
				options.addArguments("test-type");
				//remote_options.addArguments("test-type");
				
				String useOrigicalChromeProfile = configurationMap.get("enableSystemChromeProfile");
				if(useOrigicalChromeProfile!=null && useOrigicalChromeProfile.equalsIgnoreCase("true")){
					if(System.getProperty("os.name").contains("Windows")){						
						String localAppDataPath=System.getenv("LOCALAPPDATA");
						logger.info("Using LOCALAPPDATA:"+localAppDataPath);
						options.addArguments("user-data-dir=" + localAppDataPath +"/Google/Chrome/User Data");	
						//remote_options.addArguments("user-data-dir=C:/Users/"+userName+"/AppData/Local/Google/Chrome/User Data");
					}
				}
				else if(useOrigicalChromeProfile!=null && !useOrigicalChromeProfile.isEmpty()){
					if(new File(useOrigicalChromeProfile).exists()) {		
						logger.info("Using CUSTOM PROFILE:"+useOrigicalChromeProfile);
						options.addArguments("user-data-dir="+useOrigicalChromeProfile);						
					}
				}
				if (!"".equals(loadWithLaunguage)){
					options.addArguments("--lang="+loadWithLaunguage);
					//remote_options.addArguments("--lang="+loadWithLaunguage);
				}
				
				//SAF-3011 - disable developer mode extensions - Ashish Joshi, -23-Aug-16
				String sCHROME_ENABLE_ADDONS="false";
				try {
					sCHROME_ENABLE_ADDONS = configurationMap.get("CHROME_ENABLE_ADDONS");
					if(sCHROME_ENABLE_ADDONS==null || "".equals(sCHROME_ENABLE_ADDONS))
					{
						sCHROME_ENABLE_ADDONS="false";
					}
				} catch (Exception e) {
				}
				
				if(sCHROME_ENABLE_ADDONS.equalsIgnoreCase("TRUE") || sCHROME_ENABLE_ADDONS.equalsIgnoreCase("YES")|| sCHROME_ENABLE_ADDONS.equalsIgnoreCase("Y"))
				{
					sCHROME_ENABLE_ADDONS="true";
				}
				else
				{
					sCHROME_ENABLE_ADDONS="false";
				}
				
				if(sCHROME_ENABLE_ADDONS.equalsIgnoreCase("false"))
				{
					options.addArguments("--disable-extensions");
				}
				//SAF-3011- disable developer mode extensions
				
				//SAF-3008 - disable multipleFileDownload prompt
				
				String sCHROME_ENABLE_MULTIPLEFILEDOWNLOADPROPMPT="false";
				try {
					sCHROME_ENABLE_MULTIPLEFILEDOWNLOADPROPMPT = configurationMap.get("MULTIPLE_FILEDOWNLOAD_PROPMPT");
					if(sCHROME_ENABLE_MULTIPLEFILEDOWNLOADPROPMPT==null || "".equals(sCHROME_ENABLE_MULTIPLEFILEDOWNLOADPROPMPT))
					{
						sCHROME_ENABLE_MULTIPLEFILEDOWNLOADPROPMPT="false";
					}
				} catch (Exception e) {
				}                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          
				                                                                                                               	                                 	                      	               	 	 	  	 	 	                                                                                                                                         
				Map<String, Object> prefs = new HashMap<String, Object>();
				
				String sCHROME_DisablePDFPlugin="true";
				try {
					sCHROME_DisablePDFPlugin = configurationMap.get("CHROME_DISABLE_PDF_VIEW_IN_BROWSER");
					if(sCHROME_DisablePDFPlugin==null || "".equals(sCHROME_DisablePDFPlugin))
					{
						sCHROME_DisablePDFPlugin="true";
					}
				} catch (Exception e) {
				}            
				
				if(sCHROME_DisablePDFPlugin==null || sCHROME_DisablePDFPlugin.isEmpty())
				{
					sCHROME_DisablePDFPlugin="true";
				}
				else 	if(sCHROME_DisablePDFPlugin.equalsIgnoreCase("TRUE") || sCHROME_DisablePDFPlugin.equalsIgnoreCase("YES")|| sCHROME_DisablePDFPlugin.equalsIgnoreCase("Y"))
				{
					sCHROME_DisablePDFPlugin="true";
				}
				else
				{
					sCHROME_DisablePDFPlugin="false";
				}
				
			
				if(sCHROME_DisablePDFPlugin.equals("true"))
				{
					prefs.put("plugins.always_open_pdf_externally", true);
				}
				
				String sCHROME_ENABLESAVEASPROMPT="true";
				try {
					sCHROME_ENABLESAVEASPROMPT = configurationMap.get("CHROME_ENABLE_DOWNLOAD_PROMPT");
					if(sCHROME_ENABLESAVEASPROMPT==null || "".equals(sCHROME_ENABLESAVEASPROMPT))
					{
						sCHROME_ENABLESAVEASPROMPT="true";
					}
				} catch (Exception e) {
				}            
				
				if(sCHROME_ENABLESAVEASPROMPT.equalsIgnoreCase("TRUE") || sCHROME_ENABLESAVEASPROMPT.equalsIgnoreCase("YES")|| sCHROME_ENABLESAVEASPROMPT.equalsIgnoreCase("Y"))
				{
					sCHROME_ENABLESAVEASPROMPT="true";
				}
				else
				{
					sCHROME_ENABLESAVEASPROMPT="false";
				}
				
				if(sCHROME_ENABLESAVEASPROMPT.equals("true"))
				{
					prefs.put("download.prompt_for_download", true);
				}
				
				if(sCHROME_ENABLE_MULTIPLEFILEDOWNLOADPROPMPT.equalsIgnoreCase("TRUE") || sCHROME_ENABLE_MULTIPLEFILEDOWNLOADPROPMPT.equalsIgnoreCase("YES")|| sCHROME_ENABLE_MULTIPLEFILEDOWNLOADPROPMPT.equalsIgnoreCase("Y"))
				{
					sCHROME_ENABLE_MULTIPLEFILEDOWNLOADPROPMPT="true";
				}
				else
				{
					sCHROME_ENABLE_MULTIPLEFILEDOWNLOADPROPMPT="false";
				}
				
				if(sCHROME_ENABLE_MULTIPLEFILEDOWNLOADPROPMPT.equalsIgnoreCase("false"))
				{
					logger.info("=============SETTING MULTIPLE FILEDOWNLOAD============");
					//options.addArguments("--disable-extensions");
				    //To Turns off multiple download warning
				    prefs.put("profile.default_content_settings.popups", 0);
				    prefs.put("profile.default_content_setting_values.automatic_downloads", 1 );
				    //Turns off download prompt
				    //prefs.put("download.prompt_for_download", false);
				    options.setExperimentalOption("prefs", prefs);
				}
				
				/*if(prefs!=null)
				{
					remote_options.setExperimentalOption("prefs", prefs);
				}*/
				
				//SAF-3008 - disable multipleFileDownload prompt
				
				capabilities.setCapability(ChromeOptions.CAPABILITY, options);
				///Added by Ashish to avoid the Chrome driver error for security

				System.setProperty("webdriver.chrome.driver", file.getAbsolutePath());
				if((SAFALUserSession.getMode(userName) == modes.DESKTOP ||
						SAFALUserSession.getMode(userName) == modes.NONUI || SAFALUserSession.getMode(userName) == modes.CMDLINE || SAFALUserSession.getMode(userName) == modes.TESTEXECUTIONSERVER)  && executionMode.equals("local")){
					synchronized (this) {
						this.webDriver = new ChromeDriver(options);						
					}

				} else {
					try {
						capabilities.setBrowserName("chrome");
						capabilities.setPlatform(org.openqa.selenium.Platform.WINDOWS);
						URL hubUrl=null;
						hubUrl = new URL(remoteServerURL);
						if(executionMode.equals("cloud")){
							if(initiateCloudTest(capabilities,hubUrl)==false){
								return testCaseExecutionResult;
							}							
						}
						else if(executionMode.equals("web")){
							this.webDriver = new RemoteWebDriver(hubUrl, capabilities);
						}
					} catch (MalformedURLException e) {
						logger.error("Failed to connect to chrome remote web driver",e);
					}	
				}
			}
			else if(this.environment.toUpperCase().contains("OPERA"))
			{
				logger.info("In OPERA");
				OperaOptions options = new OperaOptions();
				capabilities = DesiredCapabilities.opera();
				
				if (!"".equals(loadWithLaunguage))
				{
					HashMap<String, String> operaSettings = new HashMap<String, String>();
					operaSettings.put("intl.accept_languages",loadWithLaunguage );
					capabilities.setCapability("opera.pref",operaSettings);
					options.setCapability("opera.pref",operaSettings);
				}
				
				if(propValue.equalsIgnoreCase("x86"))
				{
					file = new File("./SafalConfigs/Drivers/operadriver_32.exe");
				}
				else
				{
					file = new File("./SafalConfigs/Drivers/operadriver_64.exe");
				}

				if(!file.exists())
				{
					testCaseExecutionResult.setMessage("Unable to find required Opera Driver file at " + file.getAbsolutePath());
					return testCaseExecutionResult;
				}
				
				System.setProperty("webdriver.opera.driver", file.getAbsolutePath());
				capabilities.setCapability(CapabilityType.HAS_NATIVE_EVENTS, true);
				capabilities.setCapability(CapabilityType.TAKES_SCREENSHOT, true);
				capabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
				capabilities.setCapability(CapabilityType.SUPPORTS_JAVASCRIPT,true);
				capabilities.setCapability(CapabilityType.SUPPORTS_APPLICATION_CACHE,true);
				capabilities.setCapability(CapabilityType.SUPPORTS_FINDING_BY_CSS,true);
				capabilities.setCapability(CapabilityType.SUPPORTS_LOCATION_CONTEXT,true);
				capabilities.setCapability(CapabilityType.SUPPORTS_WEB_STORAGE,true);
				capabilities.setCapability(CapabilityType.SUPPORTS_ALERTS,true);
				capabilities.setCapability("requireWindowFocus", "true");
				capabilities.setCapability(CapabilityType.ForSeleniumServer.ENSURING_CLEAN_SESSION, true);
				
				options.setCapability(CapabilityType.BROWSER_NAME, "opera");
				options.setCapability(CapabilityType.HAS_NATIVE_EVENTS, true);
				options.setCapability(CapabilityType.TAKES_SCREENSHOT, true);
				options.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
				options.setCapability(CapabilityType.SUPPORTS_JAVASCRIPT,true);
				options.setCapability(CapabilityType.SUPPORTS_APPLICATION_CACHE,true);
				options.setCapability(CapabilityType.SUPPORTS_FINDING_BY_CSS,true);
				options.setCapability(CapabilityType.SUPPORTS_LOCATION_CONTEXT,true);
				options.setCapability(CapabilityType.SUPPORTS_WEB_STORAGE,true);
				options.setCapability(CapabilityType.SUPPORTS_ALERTS,true);
				options.setCapability("requireWindowFocus", "true");
				options.setCapability(CapabilityType.ForSeleniumServer.ENSURING_CLEAN_SESSION, true);
				
				if((SAFALUserSession.getMode(userName) == modes.DESKTOP ||
						SAFALUserSession.getMode(userName) == modes.NONUI || SAFALUserSession.getMode(userName) == modes.CMDLINE || SAFALUserSession.getMode(userName) == modes.TESTEXECUTIONSERVER) && executionMode.equals("local")) {
					synchronized (this) {
						this.webDriver = new org.openqa.selenium.opera.OperaDriver(options);
					}	
				} else {
					capabilities.setBrowserName("opera");
					try {
						URL hubUrl=null;
						hubUrl = new URL(remoteServerURL);
						if(executionMode.equals("cloud")){
							if(initiateCloudTest(capabilities,hubUrl)==false){
								return testCaseExecutionResult;
							}							
						}
						else if(executionMode.equals("web")){
							this.webDriver = new RemoteWebDriver(hubUrl, capabilities);
						}
					} catch (MalformedURLException e) {
						logger.error("Failed to connect to opera remote web driver",e);
					}
				}
			}
			else if(this.environment.toUpperCase().contains("SAFARI"))
			{
				
				logger.info("In SAFARI");
				
				capabilities = DesiredCapabilities.safari();
				if (!"".equals(loadWithLaunguage))
				{
					HashMap<String, String> safariSettings = new HashMap<String, String>();
					safariSettings.put("intl.accept_languages",loadWithLaunguage );
					capabilities.setCapability("safari.pref",safariSettings);
				}
				capabilities.setCapability(CapabilityType.HAS_NATIVE_EVENTS, true);
				capabilities.setCapability(CapabilityType.TAKES_SCREENSHOT, true);
				capabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
				capabilities.setCapability(CapabilityType.SUPPORTS_JAVASCRIPT,true);
				capabilities.setCapability(CapabilityType.SUPPORTS_APPLICATION_CACHE,true);
				capabilities.setCapability(CapabilityType.SUPPORTS_FINDING_BY_CSS,true);
				capabilities.setCapability(CapabilityType.SUPPORTS_LOCATION_CONTEXT,true);
				capabilities.setCapability(CapabilityType.SUPPORTS_WEB_STORAGE,true);
				capabilities.setCapability(CapabilityType.SUPPORTS_ALERTS,true);
				capabilities.setCapability("requireWindowFocus", "true");
				capabilities.setCapability(CapabilityType.ForSeleniumServer.ENSURING_CLEAN_SESSION, true);
				if((SAFALUserSession.getMode(userName) == modes.DESKTOP ||
						SAFALUserSession.getMode(userName) == modes.NONUI || SAFALUserSession.getMode(userName) == modes.CMDLINE || SAFALUserSession.getMode(userName) == modes.TESTEXECUTIONSERVER) && executionMode.equals("local")) {
					synchronized (this) {
						SafariOptions safariOptions = new SafariOptions(capabilities);
						this.webDriver = new SafariDriver(safariOptions);
					}	
				} else {
					capabilities.setBrowserName("safari");
					try	{
						URL hubUrl=null;
						hubUrl = new URL(remoteServerURL);
						if(executionMode.equals("cloud")){
							if(initiateCloudTest(capabilities,hubUrl)==false){
								return testCaseExecutionResult;
							}							
						}
						else if(executionMode.equals("web")){
							this.webDriver = new RemoteWebDriver(hubUrl, capabilities);
						}
					} catch (MalformedURLException e) {
						logger.error("Failed to connect to SAFARI remote web driver",e);
					}

				}
			}else  if (this.environment.toUpperCase().contains("EDGE"))
			 {
				EdgeOptions edgeOptions = new EdgeOptions();
			      capabilities = DesiredCapabilities.edge();
			      if (propValue.equalsIgnoreCase("x86")) {
			        file = new File("./SafalConfigs/Drivers/MicrosoftWebDriver.exe");
			      } else {
			        file = new File("./SafalConfigs/Drivers/MicrosoftWebDriver.exe");
			      }
			      if (!file.exists())
			      {
			        this.testCaseExecutionResult.setMessage("Unable to find required Edge driver file at " + file.getAbsolutePath());
			        return this.testCaseExecutionResult;
			      }
			      try
			      {
			        System.setProperty("webdriver.edge.driver", file.getAbsolutePath());
			      }
			      catch (Exception e2)
			      {
			        logger.error("Unable to set System Property : webdriver.edge.driver, " + e2.getMessage());
			        this.testCaseExecutionResult.setMessage("Unable to set System Property : webdriver.edge.driver, " + e2.getMessage());
			      }
			      capabilities = DesiredCapabilities.edge();
			      //For modal credentials dialog.
			      
			      if(HANDLE_UNEXPECTED_ALERT.equals("false"))
			    	  capabilities.setCapability(CapabilityType.UNEXPECTED_ALERT_BEHAVIOUR,org.openqa.selenium.UnexpectedAlertBehaviour.IGNORE);
			      capabilities.setCapability("acceptSslCerts", true);
			      capabilities.setCapability("takesScreenshot", true);
			      capabilities.setCapability("acceptSslCerts", true);
			      capabilities.setCapability("javascriptEnabled", true);
			      capabilities.setCapability("applicationCacheEnabled", true);
			      capabilities.setCapability("cssSelectorsEnabled", true);
			      capabilities.setCapability("locationContextEnabled", true);
			      capabilities.setCapability("webStorageEnabled", true);
			      capabilities.setCapability("handlesAlerts", true);
			      
			      
			      if(HANDLE_UNEXPECTED_ALERT.equals("false"))
			    	  edgeOptions.setCapability(CapabilityType.UNEXPECTED_ALERT_BEHAVIOUR,org.openqa.selenium.UnexpectedAlertBehaviour.IGNORE);
			      edgeOptions.setCapability("acceptSslCerts", true);
			      edgeOptions.setCapability("takesScreenshot", true);
			      edgeOptions.setCapability("acceptSslCerts", true);
			      edgeOptions.setCapability("javascriptEnabled", true);
			      edgeOptions.setCapability("applicationCacheEnabled", true);
			      edgeOptions.setCapability("cssSelectorsEnabled", true);
			      edgeOptions.setCapability("locationContextEnabled", true);
			      edgeOptions.setCapability("webStorageEnabled", true);
			      edgeOptions.setCapability("handlesAlerts", true);
			      
			      
			      
			      if((SAFALUserSession.getMode(userName) == modes.DESKTOP ||
							SAFALUserSession.getMode(userName) == modes.NONUI || SAFALUserSession.getMode(userName) == modes.CMDLINE || SAFALUserSession.getMode(userName) == modes.TESTEXECUTIONSERVER) && executionMode.equals("local")) {
				      synchronized (ScriptExecutionService.getInstance())
				      {
				    	  
				        this.webDriver = new EdgeDriver(edgeOptions);
				      }
			      }
			      else{
						capabilities.setBrowserName("edge");
						try	{
							URL hubUrl=null;
							hubUrl = new URL(remoteServerURL);
							if(executionMode.equals("cloud")){
								if(initiateCloudTest(capabilities,hubUrl)==false){
									return testCaseExecutionResult;
								}							
							}
							else if(executionMode.equals("web")){
								this.webDriver = new RemoteWebDriver(hubUrl, capabilities);
							}
						} catch (MalformedURLException e) {
							logger.error("Failed to connect to edge remote web driver",e);
						}					
			      }
			    }
			else if(this.environment.toUpperCase().contains("MOBILE")){

				try{
							
							final String deviceName = configurationMap.get("MobileDeviceName");
							if(deviceName==null || deviceName.isEmpty()){
								testCaseExecutionResult.setStatus(FAIL);
								logger.error("MobileDeviceName environment variable is not initialized");
								testCaseExecutionResult.setMessage("MobileDeviceName environment variable is not initialized");
								return testCaseExecutionResult;
							}
							
							//String platformName = SetMobileDeviceInfoKeyword.getMobileOS(deviceName);
							/*String tmpAdbHome=configurationMap.get("ADBHOME");
							if(tmpAdbHome==null || tmpAdbHome.isEmpty()){
								
								File f=new File(".");
								String basePath=f.getAbsolutePath();
								tmpAdbHome=basePath.substring(0, basePath.length()-1)+"SafalConfigs\\Drivers\\Android\\platform-tools\\";
								configurationMap.put("ADBHOME",tmpAdbHome);
								testCaseExecutionResult.setConfigUpdate(true);
							}*/
							final String adbHome= configurationMap.get("ADBHOME");
							
							
						final String tmpServerURL=SetMobileDeviceInfoKeyword.getAppiumServerURL(deviceName);
						final DesiredCapabilities tmpcapabilities=SetMobileDeviceInfoKeyword.getCapabilities(deviceName);
						String appiumServerLocation=configurationMap.get("APP_SERVER_LOCATION");
						tmpWebDriver = this.webDriver;
						String platformName=configurationMap.get("MobilePlatformName");
						Thread t1 = new Thread(new Runnable() 
						{ 
							public void run()
							{ 
								try {
									
									logger.info("Connecting to Appium Server:"+tmpServerURL);
									
									
									if(platformName.equalsIgnoreCase("Android")){
										tmpWebDriver=new AndroidDriver(new URL(tmpServerURL),tmpcapabilities);
									}
									else if(platformName.equalsIgnoreCase("iOS")){
										tmpWebDriver=new IOSDriver(new URL(tmpServerURL),tmpcapabilities);
									}
									
									
								} catch(SessionNotCreatedException session){
									logger.error("In Thread. Session Error:Either device is not connected or devices id is incorrect");									
									
									
									/*if(appiumServerLocation!=null && !appiumServerLocation.isEmpty()){
											logger.info("Recovery Action: Stopping Appium Server & disconnecting a device");
											AppiumServer.stopAppiumServer(deviceName);
											AppiumServer.DisConnectDevice(deviceName, adbHome);
											devConStattusWithAppiumServer.put(deviceName, false);										
											devicestatus = "Disconnected";										
									}*/
									testCaseExecutionResult.setStatus(FAIL);
									testCaseExecutionResult.setMessage("In Thread. Session Error:Either device is not connected or devices id is incorrect");										
								}
								catch (Exception e) {
									testCaseExecutionResult.setStatus(FAIL);
									devicestatus = "Disconnected:"+e;										
									
									logger.error("Error occured while launching app on mobile:",e);
									
									testCaseExecutionResult.setMessage("Error occured while launching app on mobile");
									
									/*if(appiumServerLocation!=null && !appiumServerLocation.isEmpty()){
										logger.info("Recovery Action: Stopping Appium Server & disconnecting a device:");
										AppiumServer.stopAppiumServer(deviceName);
										AppiumServer.DisConnectDevice(deviceName, adbHome);
										devConStattusWithAppiumServer.put(deviceName, false);
									}*/
									logger.error("Error occured while launching app on mobile"+e);
									
								}
							}
						});
						
						logger.info("Launching browser on Android Device");
						t1.start();
						
						Thread.sleep(1000);					
						while(tmpWebDriver==null && devicestatus.equals("Connected")){
							Thread.sleep(500);
						}
						
						
						logger.info("Connected to Appium Server");
						if(devicestatus.contains("Disconnected")){
							logger.error("Device connection error:"+devicestatus);
							testCaseExecutionResult.setStatus(FAIL);
							testCaseExecutionResult.setMessage("Either device is not connected or devices id is incorrect");
							return testCaseExecutionResult;
						}
						
						this.webDriver=tmpWebDriver;
						if(platformName.equalsIgnoreCase("Android")){
							String authDetail=configurationMap.get("AndroidAuthenticationDetails");
							if(authDetail!=null && !"".equals(authDetail)){
								String[] arrAuthDetails = null;
								arrAuthDetails=authDetail.split(";");
								AndroidAuthenticationKeyword androidAuthentication = new AndroidAuthenticationKeyword();								
								testCaseExecutionResult = androidAuthentication.execute(scriptName, this.webDriver,configurationMap,workBookMap, userName, arrAuthDetails);								
							}		
						}
				
			} //END of try
			
			catch (Exception e) {
				testCaseExecutionResult.setStatus(FAIL);					
				testCaseExecutionResult.setMessage("Error occured while launching app on mobile");
				logger.error("Error occured while launching app on mobile",e);
				return testCaseExecutionResult;
			}
			
			
		}//END of MOBILE
		else if(executionMode!=null && executionMode.equals("cloud")){
			try	{
				URL hubUrl=null;
				hubUrl = new URL(remoteServerURL);	
				capabilities=new DesiredCapabilities();
				if(initiateCloudTest(capabilities,hubUrl)==false){
					return testCaseExecutionResult;
				}
			} catch (MalformedURLException e) {
				logger.error("Failed to connect to cloud",e);
			}	
		}
			
				
			
		}

		logger.info("Inside LaunchBrowserKeyword executeScript 1 : WebDriver "+webDriver.getTitle());
		
//Removing HTMLunit driver code as adding support for latest selenium jar 2.53.1. This jar doesn't provide support for HTMLUnit driver
			try
			{
				Capabilities caps = null;
				/*if (webDriver instanceof PhantomJSDriver){
					caps = ((RemoteWebDriver) this.webDriver).getCapabilities();				

				}else{*/
					caps = ((RemoteWebDriver) this.webDriver).getCapabilities();

				//}
				//caps = caps.getCapabilities();
				 browserName = caps.getBrowserName();
				 browserVersion = caps.getVersion();
				 
				 if(executionMode.equals("cloud")){
					 browserName = capabilities.getBrowserName();
					 browserVersion = capabilities.getVersion();
					 
					 // Added for Run Macro with Param Keyword
					 if(browserVersion==null || browserVersion.isEmpty()){
						 browserVersion = caps.getVersion();
					 }
					 if(browserName==null || browserName.isEmpty()){
						 browserName = caps.getBrowserName();
					 }
				 }
				 
				 
				 
				 
				logger.info(browserName+" "+browserVersion);
				configurationMap.put("SAFAL_BROWSER_NAME", browserName.toUpperCase());
				configurationMap.put("ThreadID_"+Thread.currentThread().getId()+"_SAFAL_BROWSER_VERSION", browserVersion);
			}catch(Exception e){
				logger.error("Failed to get capabilities of PhantomJS",e);
			}
		//}

		if (maximizeBrowser.equalsIgnoreCase("y")||maximizeBrowser.equalsIgnoreCase("true") || maximizeBrowser.equalsIgnoreCase("yes"))
		{
			try{
				this.webDriver.manage().window().maximize();				
			}catch(Exception e){logger.error("Failed to maximize browser");}
		}
		/*Added code by Ashish for SAF-1782 Ability to set the size of the launched browser*/
		else if(maximizeBrowser.contains("x") || maximizeBrowser.contains("X") )
		{
			int b_Width = 0;
			int b_Height =0;
			Boolean parseErrFlag=false;

			try {
				b_Width=Integer.parseInt(sWidth);
			} catch (NumberFormatException e) {

				parseErrFlag=true;
			}

			try {
				b_Height=Integer.parseInt(sHeight);
			} catch (NumberFormatException e) {

				parseErrFlag=true;
			}

			Point targetPosition = new Point(0, 0);
			try {
				webDriver.manage().window().setPosition(targetPosition);
			} catch (Exception e1) {

				logger.error("Failed to set browser window position",e1);
			}

			Dimension targetSize=null;

			if(parseErrFlag)
			{
				String w = "return screen.availWidth";
				String h = "return screen.availHeight";
				int bwidth=1024;
				int bHeight=768;
				try {
					try {
						bwidth = ((Long)((JavascriptExecutor) webDriver).executeScript(w)).intValue();
						bHeight = ((Long)((JavascriptExecutor) webDriver).executeScript(h)).intValue();
					} catch (Exception e) {

						logger.error("Failed to get browser width and height",e);
					}
					targetSize = new Dimension(bwidth, bHeight);
				} catch (Exception e) {

					logger.error("Failed to set browser dimensions",e);
				}
			}
			else
			{
				targetSize = new Dimension(b_Width, b_Height);
			}

			try {
				webDriver.manage().window().setSize(targetSize);
			} catch (Exception e) {

				logger.error("Failed to set browser window size",e);
			}
		}
		else if(fullScreenBrowser.equalsIgnoreCase("Y") || fullScreenBrowser.equalsIgnoreCase("TRUE")){
			try {
				webDriver.manage().window().fullscreen();
			} catch (Exception e) {

				logger.error("Failed to fullscreen browser window",e);
			}
		}
		
		logger.info("Inside LaunchBrowserKeyword executeScript 2");
		
		/*Added code by Ashish for SAF-1782 Ability to set the size of the launched browser*/


		//======================================================================================================================
		/*
		 * Made changes for Storing Cookies with LaunchBrowser 
		 * SAF-1421: SAFAL-"Firefox" browser profile issue; unable to remember UserID through Firefox browser.
		 * */

		if (cookieLoadFlag)
		{
			try {
				this.webDriver.get(sURLToLaunch);
			} catch (Exception e1) {
				logger.error("Failed to launch URL",e1);
			}

			logger.info("Inside LaunchBrowserKeyword executeScript 3");
			
			String cookies_Arr="";
			try {
				cookies_Arr = configurationMap.get("SELENIUM_COOKIE_VARIABLE");
			} catch (Exception e1) {
				cookies_Arr="";
			}

			if("".equals(cookies_Arr))
			{
				try {
					cookies_Arr = this.webDriver.manage().getCookies().toString();
				} catch (Exception e1) {
					cookies_Arr="";
				}
			}

			if (cookies_Arr!=null && !"".equals(cookies_Arr))
			{
				String[] allCookies=cookies_Arr.split("@");

				for (int i=0;i<allCookies.length;i++)
				{
					if (!"".equals(allCookies[i]))
					{
						String name=allCookies[i].split(";")[0];
						String value=allCookies[i].split(";")[1];
						String domain=allCookies[i].split(";")[2];
						String path=allCookies[i].split(";")[3];

						try {
							this.webDriver.manage().addCookie(new Cookie(name, value, domain, path, null));
						} catch (Exception e) {
							logger.error("Failed to add cookies",e);
						}
					}
				}
				try {
					this.webDriver.get(sURLToLaunch);
				} catch (Exception e) {
					logger.error("Failed to launch URL2",e);
				}
			}
		}

		//======================================================================================================================


		String HANDLE_DIALOG_IN_LAUNCH_BROWSER ="FALSE";
		try {
			HANDLE_DIALOG_IN_LAUNCH_BROWSER =configurationMap.get("HANDLE_DIALOG_IN_LAUNCH_BROWSER");
		} catch (Exception e1) {
			HANDLE_DIALOG_IN_LAUNCH_BROWSER =EMPTY_STRING;
		}
		
		if(HANDLE_DIALOG_IN_LAUNCH_BROWSER ==null || EMPTY_STRING.equals(HANDLE_DIALOG_IN_LAUNCH_BROWSER ))
		{
			HANDLE_DIALOG_IN_LAUNCH_BROWSER ="FALSE";
		}
		
		if(HANDLE_DIALOG_IN_LAUNCH_BROWSER .equalsIgnoreCase("Y") || HANDLE_DIALOG_IN_LAUNCH_BROWSER .equalsIgnoreCase("YES") || HANDLE_DIALOG_IN_LAUNCH_BROWSER .equalsIgnoreCase("TRUE") )
		{
			HANDLE_DIALOG_IN_LAUNCH_BROWSER ="TRUE";
		}
		else
		{
			HANDLE_DIALOG_IN_LAUNCH_BROWSER ="FALSE";
		}

		if (cookieLoadFlag==false) 
		{
			try	{
				//SAF-2837
				final WebDriver tempWebDriver=this.webDriver;
			if(HANDLE_DIALOG_IN_LAUNCH_BROWSER .equalsIgnoreCase("TRUE"))
			{
			
				Thread t1 = new Thread(new Runnable() 
				{ 
					public void run()
					{ 
						try {
							tempWebDriver.get(sURLToLaunch);
						} catch (Exception e) {logger.error("Failed to lauch URL during handle",e);}
					}
				});
				t1.start();
				//t1.join();
				this.webDriver=tempWebDriver;
				configurationMap.put("SAFAL_BROWSER_INSTANCE", nameOfInstance);
				configurationMap.put("SAFAL_URL_TO_LAUNCH", sURLToLaunch);
				if(sCheck.trim().equalsIgnoreCase(SKIP_URL_CHECK))
				{
					configurationMap.put("SAFAL_SKIP_URL_CHECK", "TRUE");
				}
				testCaseExecutionResult.setConfigUpdate(true);
				testCaseExecutionResult.setStatus(PASS);
				return testCaseExecutionResult;
				//	SAF-2837
			}
			else
			{
				this.webDriver.get(sURLToLaunch);
			}
				
			
			}	catch (Exception e){
				logger.error("Failed to launch URL3",e);
			}
		}
		
		//added by rakesh
		Alert loginBox = null;
		try {
			loginBox = this.webDriver.switchTo().alert();

			logger.info("Inside LaunchBrowserKeyword executeScript 6");
			
		} catch (Exception e1) {
			logger.error("Unable to switch to alert");
		}
		
		boolean modalDialogPresentFlag=false;

		if (loginBox != null && sCheck.trim().equalsIgnoreCase(SKIP_URL_CHECK))
		{

			int iWait=0;
			if(KeywordUtilities.isEmptyString(sWaitCounter))
			{
				sWaitCounter=KeywordExecutionUtilities.getPageLoadTime(configurationMap);
			}

			else
			{
				iWait = Integer.parseInt(sWaitCounter);
				sWaitCounter=String.valueOf(iWait);
			}

			try {
				webDriver.manage().timeouts().pageLoadTimeout(iWait, TimeUnit.SECONDS);
			} catch (Exception e1) {
				logger.error("Error setting Page Load time",e1);
			}
			
			try {						
				KeywordUtilities.waitForPageToLoad(this.webDriver, Integer.parseInt(sWaitCounter),userName);
			} catch (Exception e) {
				logger.error("Error occured while waiting for page to load",e);
			}

			logger.info("Inside LaunchBrowserKeyword executeScript 7" + Calendar.getInstance().getTime().toString());
			
			testCaseExecutionResult.setStatus(PASS);
			return testCaseExecutionResult;

		}

		String sLaunchURL="";			
		try {
			sLaunchURL=this.webDriver.getCurrentUrl();
		} catch (UnhandledAlertException e1) {
			//SAF-2837
			if(e1.getMessage().toUpperCase().contains("MODAL DIALOG PRESENT"))
			{
				modalDialogPresentFlag=true;
				logger.info("sLaunchURL: "+e1.getMessage());
			}
			//SAF-2837
		}
		catch (Exception e2) {
			logger.error("Unable to get current URL",e2);
		}
		
		//SAF-2837
		if(modalDialogPresentFlag || (sLaunchURL.equals("data:,") && browserName.equalsIgnoreCase("CHROME")))
		{
			testCaseExecutionResult.setMessage("Found Modal Dialog");
			configurationMap.put("SAFAL_BROWSER_INSTANCE", nameOfInstance);
			configurationMap.put("SAFAL_URL_TO_LAUNCH", sURLToLaunch);
			if(sCheck.trim().equalsIgnoreCase(SKIP_URL_CHECK))
			{
				configurationMap.put("SAFAL_SKIP_URL_CHECK", "TRUE");
			}
			testCaseExecutionResult.setConfigUpdate(true);
			testCaseExecutionResult.setStatus(1);
			return testCaseExecutionResult;
		}
		//SAF-2837
		// end rakesh Added

		int iWait;
		if(sWaitCounter.isEmpty())
		{
			sWaitCounter=KeywordExecutionUtilities.getPageLoadTime(configurationMap);
		}

		else
		{
			iWait = Integer.parseInt(sWaitCounter);
			sWaitCounter=String.valueOf(iWait);
		}
		
		try {
			logger.info("Inside LaunchBrowserKeyword executeScript 8");
			KeywordUtilities.waitForPageToLoad(this.webDriver, Integer.parseInt(sWaitCounter),userName);
			logger.info("Inside LaunchBrowserKeyword executeScript 9");
		} catch (Exception e) {
			logger.error("Error occured while Page to load2",e);
		}
		if(!nameOfInstance.isEmpty())
		{
			KeywordExecutionBO.browserDriverMap.put(nameOfInstance,this.webDriver);
		}

		//Added by Ashish on 9-may-13 to get Main Window HAndle under SAFAL_MAIN_WINDOW_HANDLE variable
		String mainWindowHandle="";
		try {
			mainWindowHandle = this.webDriver.getWindowHandle();
		} catch (Exception e) {
			logger.error("Unable to get Main Window Handle",e);
			
			testCaseExecutionResult.setMessage("Unable to get Main Window Handle");
			return testCaseExecutionResult;
		}

		configurationMap.put("SAFAL_MAIN_WINDOW_HANDLE", mainWindowHandle);


		try {
			if(this.webDriver.getPageSource().contains(ERROR_IE_CANNOT_DSIPLAY_PAGE))
			{
				logger.error("ERROR_IE_CANNOT_DSIPLAY_PAGE");
				testCaseExecutionResult.setMessage(ERROR_IE_CANNOT_DSIPLAY_PAGE);
				return testCaseExecutionResult;
			}


			if(this.webDriver.getPageSource().contains("Network Error")  &&  this.webDriver.getPageSource().contains("could not be resolved by DNS") )
			{
				logger.error("Invalid URL");
				testCaseExecutionResult.setMessage("Invalid URL");
				return testCaseExecutionResult;
			}

			if(sCheck.trim().equalsIgnoreCase(SKIP_URL_CHECK))
			{
				testCaseExecutionResult.setStatus(PASS);
				return testCaseExecutionResult;
			}

		} catch (Exception e) {

			logger.error("Network Error not checked",e);
			
		}

		if (sLaunchURL.contains(sURLToLaunch)) {
			testCaseExecutionResult.setStatus(PASS);
		} else {
			if(environment.toUpperCase().contains(FIREFOX))
			{
				logger.error(ERROR_LAUNCH_URL+ " Check Firefox Profile");
				testCaseExecutionResult.setMessage(ERROR_LAUNCH_URL+ " Check Firefox Profile");
				return testCaseExecutionResult;	
			}

			testCaseExecutionResult.setMessage(ERROR_LAUNCH_URL);
			return testCaseExecutionResult;
		}
		
		logger.info("Inside LaunchBrowserKeyword executeScript 10");		
		
		return testCaseExecutionResult;

	}

	@Override
	/**
	 * This method does Keyword level validation i.e. checking required parameters,its format etc
	 * @param listOfParameters contains list of parameters
	 * listOfParameters[0] (Optional) -  URL  	 -  URL to launch
	 * @return ExecutionResults containing step execution status(pass/fail),
	 *         exact error message according to failure
	 */
	public TestcaseExecutionResultVO validateKeyword(String... listOfParameters) {
		// check for required parameter 
		if (listOfParameters != null) {

			sURLToLaunch = listOfParameters[0];
			sCheck = listOfParameters[1];
			sWaitCounter = listOfParameters[2];
			loadWithCookies=listOfParameters[3];
			loadWithLaunguage=listOfParameters[4];
			maximizeBrowser= listOfParameters[5];
			nameOfInstance=listOfParameters[6];
			fullScreenBrowser=listOfParameters[7];

		} else {
			logger.error("ERROR_PARAMETERS_LIST");
			testCaseExecutionResult.setMessage(ERROR_PARAMETERS_LIST);
			return testCaseExecutionResult;
		}

		if(fullScreenBrowser == null || fullScreenBrowser.trim().isEmpty())
		{
			fullScreenBrowser = "N";
		}
		
		if(loadWithLaunguage == null || loadWithLaunguage.trim().isEmpty())
		{
			InitSAFALProperties initSAFALProperties = InitSAFALProperties.getInstance(userName);					
			loadWithLaunguage = initSAFALProperties.broswerLang;
			loadWithLaunguage = BrowserLanguagePropertiesReader.getLanguageCode(loadWithLaunguage);
		}
		else
		{
			loadWithLaunguage = loadWithLaunguage.trim();
			loadWithLaunguage = loadWithLaunguage.replaceAll(" ", "");
			loadWithLaunguage = BrowserLanguagePropertiesReader.getLanguageCode(loadWithLaunguage);
		}

		if(loadWithLaunguage == null || loadWithLaunguage.trim().isEmpty())
		{
			loadWithLaunguage = "en-us";
		}
		// Setting test data

		if (KeywordUtilities.isEmptyString(sURLToLaunch)) {
			logger.error("ERROR_PARAMETERS_LIST");
			
			testCaseExecutionResult.setMessage(ERROR_URL_NOT_PASSED);
			return testCaseExecutionResult;

		} 

		String sPageLoadTime=configurationMap.get("PageLoadTime");
		if(sPageLoadTime !=null && !KeywordUtilities.isEmptyString(sPageLoadTime))
		{
			try {
				Integer.parseInt(sPageLoadTime);
			} catch (NumberFormatException e) {
				logger.error("Invalid value provided for PageLoadTime in environment file");				
				testCaseExecutionResult.setMessage("Invalid value provided for PageLoadTime in environment file");
				return testCaseExecutionResult;
			}
		}

		if(!KeywordUtilities.isEmptyString(sWaitCounter))
		{
			try {
				Integer.parseInt(sWaitCounter);
			} catch (NumberFormatException e) {
				logger.error("Invalid value provided for PageLoadTime");				
				testCaseExecutionResult.setMessage("Invalid value provided for PageLoadTime");
				return testCaseExecutionResult;
			}
		}

		if(maximizeBrowser.toUpperCase().contains("X"))
		{
			maximizeBrowser=maximizeBrowser.toLowerCase();
			String sDimensionArray[]=null;

			sDimensionArray=maximizeBrowser.split("x");
			if (sDimensionArray.length==2)
			{
				sWidth=sDimensionArray[0];
				sHeight=sDimensionArray[1];
			}
			else
			{   logger.error("Invalid value provided for Browser Dimensions");
				
			testCaseExecutionResult.setMessage("Invalid value provided for Browser Dimensions");
			return testCaseExecutionResult;
			}
		}

		if(nameOfInstance!=null && !EMPTY_STRING.equals(nameOfInstance))
		{
			try {
				configurationMap.put("SAFAL_INSTANCE_NAME", nameOfInstance);
				testCaseExecutionResult.setConfigUpdate(true);
			} catch (Exception e) {
				logger.error("Error occured while setting SAFAl instance name",e);
			}
		}
		else
		{
			configurationMap.put("SAFAL_INSTANCE_NAME", EMPTY_STRING);
			testCaseExecutionResult.setConfigUpdate(true);
		}

		testCaseExecutionResult.setTestData(sURLToLaunch+DELIMITER+sCheck+DELIMITER+ sWaitCounter+DELIMITER+ loadWithCookies+DELIMITER+loadWithLaunguage+DELIMITER+maximizeBrowser+DELIMITER+nameOfInstance);
		testCaseExecutionResult.setValid(true);
		return testCaseExecutionResult;
	}

	@Override
	/**
	 * This method does Object level validation 
	 * @return ExecutionResults containing step execution status(pass/fail),
	 *         exact error message according to failure
	 */
	public TestcaseExecutionResultVO validateObject(String... listOfParameters) {
		return KeywordUtilities.noValidationRequired();
	}

	/**
	 * @return
	 */
	public WebDriver getSelenium() {

		return this.webDriver;
	}
}