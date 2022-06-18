package com.sungard.ktt.business.keywords;


import static com.sungard.ktt.business.keywords.ErrorMessages.ERROR_BROWSER_NOT_INSTANTIATED;
import static com.sungard.ktt.business.keywords.ErrorMessages.ERROR_COULDNOT_DELETE_COOKIES;
import static com.sungard.ktt.business.keywords.ErrorMessages.ERROR_PARAMETERS_LIST;
import static com.sungard.ktt.view.config.KTTGuiConstants.DEFAULT_PAGE_LOAD_TIME;
import static com.sungard.ktt.view.config.KTTGuiConstants.EMPTY_STRING;
import static com.sungard.ktt.view.config.KTTGuiConstants.PASS;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.Socket;
import java.net.URL;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509ExtendedTrustManager;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.ie.InternetExplorerDriver;

import com.sungard.ktt.model.valueobjects.TestcaseExecutionResultVO;
/**
 * This Keyword will perform Browser Specific operations.
 */




public class PerformBrowserOperations extends AbstractKeyword  {


	TestcaseExecutionResultVO testcaseExecutionResult = new TestcaseExecutionResultVO();
	/**
	 * This is logger object used to log keyword actions into a log file
	 */

	Logger logger = Logger.getLogger(PerformBrowserOperations.class.getName());

	private String sBrowserOperation = null;
	
	private String sBrowserOperationParam1=null;
	
	private String sBrowserOperationEnvVariable=null;

	private String sProxyServerNamePort;
	
	private String sIsTrustAllCertificates; 
	
	/**
	 * This method runs after all the validations are successful

	 * @return ExecutionResults containing step execution status(pass/fail),
	 *         exact error message according to failure
	 */
	public TestcaseExecutionResultVO executeScript(String... listOfParameters) {


		//logger.debug("Inside Method validate Execute Script");

		if(sBrowserOperation.equalsIgnoreCase("REFRESH"))
		{
			logger.info("perform REFRESH");
			try {
				
				//SAF-3252
				if(sBrowserOperationParam1.isEmpty() || sBrowserOperationParam1.equalsIgnoreCase("TRUE") || sBrowserOperationParam1.equalsIgnoreCase("Y") || sBrowserOperationParam1.equalsIgnoreCase("YES"))
				{
					webDriver.navigate().refresh();
				}
				else if(sBrowserOperationParam1.equalsIgnoreCase("FALSE") || sBrowserOperationParam1.equalsIgnoreCase("N") || sBrowserOperationParam1.equalsIgnoreCase("NO"))
				{
					webDriver.get(webDriver.getCurrentUrl());//webDriver.navigate().to(webDriver.getCurrentUrl());//((JavascriptExecutor) webDriver).executeScript("location.reload()");
				}
				//SAF-3252
				
				try {
					int toWait = 120;
					String val = configurationMap.get(DEFAULT_PAGE_LOAD_TIME);
					if(null != val) {
						toWait =Integer.parseInt(val); 	
					}
					KeywordUtilities.waitForPageToLoad(webDriver, toWait,userName);
					KeywordUtilities.waitForPageToLoadFromIcon(webDriver, toWait, configurationMap,userName);
				} catch (Exception e) {
					logger.error("Error ocurred while opening the url "+ sBrowserOperationParam1, e);
					testcaseExecutionResult.setMessage("Error ocurred while opening the url "+ sBrowserOperationParam1);
				}
				testcaseExecutionResult.setStatus(PASS);
			} catch (Exception e) {
				testcaseExecutionResult.setMessage("Unable to Refresh the Browser");
				logger.error("Unable to Refresh the Browser");
				logger.error("Exception ",e);
			}
		}
		else if(sBrowserOperation.equalsIgnoreCase("DELETE_ALL_COOKIES"))
		{
			logger.info("perform DELETE_ALL_COOKIES");
			
			try {
				webDriver.manage().deleteAllCookies();
				testcaseExecutionResult.setStatus(PASS);
			} catch (Exception e) {
				testcaseExecutionResult.setMessage(ERROR_COULDNOT_DELETE_COOKIES);
				logger.error(ERROR_COULDNOT_DELETE_COOKIES);
				logger.error(e.getCause().toString(),e);
			}
		}
		else if(sBrowserOperation.equalsIgnoreCase("BACK"))
		{
			logger.info("perform BACK");			
			try {
				webDriver.navigate().back();
				testcaseExecutionResult.setStatus(PASS);
			} catch (Exception e) {
				testcaseExecutionResult.setMessage("Unable to Back the Browser");
				logger.error("Unable to Back the Browser",e);				
			}
		}
		else if(sBrowserOperation.equalsIgnoreCase("FORWARD"))
		{
			logger.info("perform FORWARD");				
			try {
				webDriver.navigate().forward();
				testcaseExecutionResult.setStatus(PASS);
			} catch (Exception e) {
				testcaseExecutionResult.setMessage("Unable to Forward the Browser");
				logger.error("Unable to Forward the Browser",e);
				//logger.error(e.getCause().toString());
			}
		}
		else if(sBrowserOperation.equalsIgnoreCase("MAXIMIZE"))
		{
			logger.info("perform MAXIMIZE");				
			try {
				webDriver.manage().window().maximize();	
				testcaseExecutionResult.setStatus(PASS);
			} catch (Exception e) {
				testcaseExecutionResult.setMessage("Unable to Maximize the Browser");
				logger.error("Unable to Maximize the Browser",e);
				//logger.error(e.getCause().toString());
			}
		}
		else if(sBrowserOperation.toUpperCase().contains("SET_SIZE"))
		{
			logger.info("perform SET_SIZE");				
			
			try {
				String testSizeString[]=sBrowserOperation.split(":");
				String sizeOfScreenText[]={};

				try {
					sizeOfScreenText=testSizeString[1].split("X");
				} catch (Exception e2) {
					logger.error("Exception",e2);
				}

				if(sizeOfScreenText.length==0)
				{
					try {
						sizeOfScreenText=testSizeString[1].split("x");
					} catch (Exception e2) {
						logger.error("Exception",e2);
					}
				}

				int b_Width = 0;
				int b_Height =0;
				Boolean parseErrFlag=false;

				try {
					b_Width=Integer.parseInt(sizeOfScreenText[0]);
				} catch (NumberFormatException e) {
					logger.error("NumberFormatException",e);
					parseErrFlag=true;
				}

				try {
					b_Height=Integer.parseInt(sizeOfScreenText[1]);
				} catch (NumberFormatException e) {
					logger.error("NumberFormatException",e);
					
					parseErrFlag=true;
				}

				Point targetPosition = new Point(0, 0);
				try {
					webDriver.manage().window().setPosition(targetPosition);
				} catch (Exception e1) { 
					logger.error("Error ocurred while Setting the target position of the window of the Window",e1);
					testcaseExecutionResult.setMessage("Error ocurred while Setting the target position of the window of the Window");
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

							e.printStackTrace();
						}
						targetSize = new Dimension(bwidth, bHeight);
					} catch (Exception e) {

						logger.error("Error ocurred while Setting the Size of the Window",e);
						testcaseExecutionResult.setMessage("Error ocurred while Setting the Size of the Window");
					}
				}
				else
				{
					targetSize = new Dimension(b_Width, b_Height);
				}

				try {
					webDriver.manage().window().setSize(targetSize);
					testcaseExecutionResult.setStatus(PASS);
				} catch (Exception e) {

					logger.error("Unable to Set the Size of the Window",e);
					testcaseExecutionResult.setMessage("Unable to Set the Size of the Window");
				}
			} catch (Exception e) {

				logger.error("Error ocurred while Setting the Size of the Window",e);
				testcaseExecutionResult.setMessage("Error ocurred while Setting the Size of the Window");
			}

		}
		else if(sBrowserOperation.equalsIgnoreCase("OPEN_NEW_URL") || sBrowserOperation.equalsIgnoreCase("NAVIGATE_NEW_URL"))
		{
			try {
				if(sBrowserOperationParam1!=null && !KeywordUtilities.isEmptyString(sBrowserOperationParam1))
				{
					webDriver.get(sBrowserOperationParam1);
					
					try {
						int toWait = 120;
						try{String val = configurationMap.get(DEFAULT_PAGE_LOAD_TIME); toWait =Integer.parseInt(val); }catch(Exception e1){}
						KeywordUtilities.waitForPageToLoad(webDriver, toWait,userName);
						// added for SAF-2399 - start
						webDriver.navigate().refresh();
						// added for SAF-2399 - end
					} catch (Exception e) {
						logger.error("Error ocurred while opening the url "+ sBrowserOperationParam1,e);
						testcaseExecutionResult.setMessage("Error ocurred while opening the url "+ sBrowserOperationParam1);
					}
					testcaseExecutionResult.setStatus(PASS);
				}
				else
				{
					testcaseExecutionResult.setMessage("URL not provided");
				}
				
			} catch (Exception e) {
				logger.error("Error ocurred while opening the url "+ sBrowserOperationParam1,e);				
				testcaseExecutionResult.setMessage("Error ocurred while opening the url "+ sBrowserOperationParam1);
			}
			
		}
		else if(sBrowserOperation.equalsIgnoreCase("OPEN_AND_SWITCH_TO_NEW_WINDOW") || sBrowserOperation.equalsIgnoreCase("OPEN_NEW_WINDOW"))
		{
			logger.info("OPEN_AND_SWITCH_TO_NEW_WINDOW || OPEN_NEW_WINDOW" );
					try {
						
						int sizePrevious=0;
						try {
							sizePrevious = webDriver.getWindowHandles().size();
						} catch (Exception e2) {
							logger.error("Exception occured while getting no. of window handles",e2);
						}
						
						String URL =listOfParameters[1];
						
						if("".equals(URL))
						{
							testcaseExecutionResult.setMessage("URL not provided to open new window");
							logger.error("URL not provided to open new window");
							return testcaseExecutionResult;
						}
						
						String w = "return screen.availWidth";
						String h = "return screen.availHeight";
						
						int bwidth=1024;
						int bHeight=768;
						try {
							try {
								bwidth = ((Long)((JavascriptExecutor) webDriver).executeScript(w)).intValue();
								bHeight = ((Long)((JavascriptExecutor) webDriver).executeScript(h)).intValue();
							} catch (Exception e) {	}							
						} catch (Exception e) {
						}
						
						String strWindowFeatures ="height="+bHeight+",width="+bwidth+",resizable=yes,scrollbars=yes,toolbar=yes,menubar=yes,location=yes')";
						
						String openNewWindowScript=
						"openNewWindow(arguments[0],arguments[1]);" +
						"function openNewWindow(URL,strWindowFeatures)" +
						"{" +
						"	try {window.open(URL,'_blank', strWindowFeatures);return 'true';}catch(e){return 'false';}" +
						"}";
						
						String resultOpenNewWindow="false";
						try {
							resultOpenNewWindow=((JavascriptExecutor) webDriver).executeScript("return " +openNewWindowScript,URL,strWindowFeatures).toString();
						} catch (Exception e2) {
						
						}
						
						if(resultOpenNewWindow.equalsIgnoreCase("false"))
						{
							testcaseExecutionResult.setMessage("Unable to open new window with given url");
							logger.error("Unable to open new window with given url");
							return testcaseExecutionResult;
						}
						
						
						int sizeAfter=0;
						try {
							sizeAfter = webDriver.getWindowHandles().size();
						} catch (Exception e1) {
							
						}
						if(sizeAfter==(sizePrevious+1))
						{
							if(sBrowserOperation.equalsIgnoreCase("OPEN_AND_SWITCH_TO_NEW_WINDOW"))
							{
								
								String currWinHandle="";
								try {
									currWinHandle = webDriver.getWindowHandle();
								} catch (Exception e2) {

								}
								
								Set<String> availableWindows=null;
								try {
									availableWindows = webDriver.getWindowHandles();
								} catch (Exception e1) {
								}
								
								if(availableWindows==null)
								{
									testcaseExecutionResult.setMessage("Unable to get Available Windows");
									logger.error("Unable to open new window with given url");
									return testcaseExecutionResult;
								}
								
								
								
								int reqwinCount=0;
								for(String winHandle : availableWindows)
								{
									reqwinCount=reqwinCount+1;
									if(reqwinCount==sizeAfter)
									{
										try {
											
											if(!winHandle.equals(currWinHandle))
											{
												webDriver.switchTo().window(winHandle);
												//webDriver.manage().window().maximize();
												
												String titleNewWin ="";
												String urlNewWin ="";
												
												try {
													titleNewWin=webDriver.getTitle().toString();
												} catch (Exception e) {}
												
												try {
													urlNewWin=webDriver.getCurrentUrl().toString();
												} catch (Exception e) {}
												
												if(!("".equals(titleNewWin)))
												{
													configurationMap.put("Title", titleNewWin);
													testcaseExecutionResult.setConfigUpdate(true);
												}
												
												if(!("".equals(urlNewWin)))
												{
													configurationMap.put("URL", urlNewWin);
													testcaseExecutionResult.setConfigUpdate(true);
												}
												
												testcaseExecutionResult.setStatus(PASS);
											}
											
											return testcaseExecutionResult;
										} catch (Exception e) {
											testcaseExecutionResult.setMessage("Unable to Switch to New Window");
										}
									}
								
								}
							
							}
							else
							{
								testcaseExecutionResult.setStatus(PASS);
								return testcaseExecutionResult;
							}
						}
						else
						{
							logger.error("No New Window Found");
							testcaseExecutionResult.setMessage("No New Window Found");
						}
					} catch (Exception e) {
						testcaseExecutionResult.setMessage("Unable to open new window with given url");
						logger.error("Unable to open new window with given url",e);
						logger.error(e.getCause().toString());
						
					}
		}
		else if(sBrowserOperation.equalsIgnoreCase("GET_CURRENT_URL") || sBrowserOperation.equalsIgnoreCase("GET_URL"))
		{
			logger.info("GET_CURRENT_URL || GET_URL" );
			
			String urlFromWebDriver="";
			try {
				urlFromWebDriver=webDriver.getCurrentUrl();
				configurationMap.put(sBrowserOperationEnvVariable,urlFromWebDriver);
				testcaseExecutionResult.setConfigUpdate(true);
				testcaseExecutionResult.setStatus(PASS);
			} catch (Exception e) {
				logger.error("Unable to get the URL.",e);
				testcaseExecutionResult.setMessage("Unable to get the URL.");
			}
		}
		else if(sBrowserOperation.equalsIgnoreCase("GET_CURRENT_TITLE") || sBrowserOperation.equalsIgnoreCase("GET_TITLE"))
		{
			logger.info("GET_CURRENT_TITLE || GET_TITLE" );
			
			String titleFromWebDriver="";
			try {
				titleFromWebDriver=webDriver.getTitle();
				configurationMap.put(sBrowserOperationEnvVariable,titleFromWebDriver);
				testcaseExecutionResult.setConfigUpdate(true);
				testcaseExecutionResult.setStatus(PASS);
			} catch (Exception e) {				
				logger.error("Unable to get the TITLE",e);
				testcaseExecutionResult.setMessage("Unable to get the TITLE.");
			}
		}
		else if(sBrowserOperation.equalsIgnoreCase("GET_PAGE_SOURCE"))
		{
			logger.info("GET_PAGE_SOURCE" );			
			String pageSourceFromWebDriver="";
			try {
				pageSourceFromWebDriver=webDriver.getPageSource();
				configurationMap.put(sBrowserOperationEnvVariable,pageSourceFromWebDriver);
				testcaseExecutionResult.setConfigUpdate(true);
				testcaseExecutionResult.setStatus(PASS);
			} catch (Exception e) {				
				logger.error("Unable to get PAGE_SOURCE",e);
				testcaseExecutionResult.setMessage("Unable to get PAGE_SOURCE");
			}
		}
		else if (sBrowserOperation.equalsIgnoreCase("GET_DOM_CONTAINS")) {
			InputStream ip=null;
			BufferedReader br1 = null;
			try {
				Proxy proxy = null;
				if (sProxyServerNamePort != null && !sProxyServerNamePort.isEmpty()) {
					proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(sProxyServerNamePort.split(":")[0],
							Integer.parseInt(sProxyServerNamePort.split(":")[1])));
				}

				if(sBrowserOperationParam1==null || sBrowserOperationParam1.isEmpty()) {
					sBrowserOperationParam1 = webDriver.getCurrentUrl();
				}
				
				
				if(sIsTrustAllCertificates.equalsIgnoreCase("Y") || sIsTrustAllCertificates.equalsIgnoreCase("TRUE")) {
				 TrustManager[] trustAllCerts = new TrustManager[]{
			                new X509ExtendedTrustManager() {
			                    @Override
			                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
			                        return null;
			                    }

			                    @Override
								public void checkClientTrusted(java.security.cert.X509Certificate[] chain,
										String authType) throws java.security.cert.CertificateException {
									
									
								}

								@Override
								public void checkServerTrusted(java.security.cert.X509Certificate[] chain,
										String authType) throws java.security.cert.CertificateException {
									
									
								}

								@Override
								public void checkClientTrusted(java.security.cert.X509Certificate[] chain,
										String authType, Socket socket) throws java.security.cert.CertificateException {
								
									
								}

								@Override
								public void checkServerTrusted(java.security.cert.X509Certificate[] chain,
										String authType, Socket socket) throws java.security.cert.CertificateException {
								
									
								}

								@Override
								public void checkClientTrusted(java.security.cert.X509Certificate[] chain,
										String authType, SSLEngine engine)
										throws java.security.cert.CertificateException {
									
									
								}

								@Override
								public void checkServerTrusted(java.security.cert.X509Certificate[] chain,
										String authType, SSLEngine engine)
										throws java.security.cert.CertificateException {
								
									
								}

			                }
			            };

			            SSLContext sc = SSLContext.getInstance("SSL");
			            sc.init(null, trustAllCerts, new java.security.SecureRandom());
			            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

			            // Create all-trusting host name verifier
			            HostnameVerifier allHostsValid = new HostnameVerifier() {
			                @Override
			                public boolean verify(String hostname, SSLSession session) {
			                    return true;
			                }
			            };
			            // Install the all-trusting host verifier
			            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
			           
				
				}
				
				/////////////////////////////////////////
				URL url = new URL(sBrowserOperationParam1);

				HttpURLConnection con = null;
				if (proxy != null) {
					con = (HttpURLConnection) url.openConnection(proxy);
				} else {
					con = (HttpURLConnection) url.openConnection();
				}

				// set the request method and properties.
				//con.setRequestMethod("GET");

				ip = con.getInputStream();
				br1 = new BufferedReader(new InputStreamReader(ip));
				StringBuilder pageSource = new StringBuilder();
				String line = null;
				while ((line = br1.readLine()) != null) {
					pageSource.append(line);
				}

				configurationMap.put(sBrowserOperationEnvVariable,pageSource.toString());
				testcaseExecutionResult.setConfigUpdate(true);
				testcaseExecutionResult.setStatus(PASS);
				
			} catch (MalformedURLException e) {
				logger.error("Invalid URL", e);
			} catch (IOException e) {
				logger.error("GET_DOM_CONTAINS", e);
			}
			catch(Exception e) {
				logger.error("Exception", e);
			}
			finally{
				if(ip!=null) {
					try {
						ip.close();
					} catch (IOException e) {
						
					}
				}
				if(br1!=null) {
					try {
						br1.close();
					} catch (IOException e) {
						
					}
				}
			}

		}
		else if(sBrowserOperation.equalsIgnoreCase("GET_CURRENT_WINDOW_HANDLE") || sBrowserOperation.equalsIgnoreCase("GET_WIN_HANDLE"))
		{
			logger.info("GET_CURRENT_WINDOW_HANDLE||GET_WIN_HANDLE " );			
			
			String winHandle="";
			try {
				winHandle=webDriver.getWindowHandle();
				configurationMap.put(sBrowserOperationEnvVariable,winHandle);
				testcaseExecutionResult.setConfigUpdate(true);
				testcaseExecutionResult.setStatus(PASS);
			} catch (Exception e) {				
				logger.error("Unable to get the URL.",e);
				testcaseExecutionResult.setMessage("Unable to get the URL.");
			}
		}else if(sBrowserOperation.equalsIgnoreCase("ZOOM"))
		{
			logger.info("ZOOM");
			if(!(webDriver instanceof InternetExplorerDriver) ){
				try {
					((JavascriptExecutor)webDriver).executeScript("var a = document.getElementsByTagName(\"html\"); a[0].style.zoom=\""+sBrowserOperationParam1+"%\";");
					Dimension d = webDriver.manage().window().getSize();
					webDriver.manage().window().maximize();
					webDriver.manage().window().setSize(d);	
					testcaseExecutionResult.setStatus(PASS);
				} catch (Exception e2) {
					logger.error("Error while zooming");
					testcaseExecutionResult.setMessage("Error while zooming");
				}
			}else{
				try {
					if(sBrowserOperationParam1.equalsIgnoreCase("in")){
						WebElement ele = webDriver.findElement(By.tagName("html"));
						ele.sendKeys(Keys.chord(Keys.CONTROL, Keys.ADD));
						testcaseExecutionResult.setStatus(PASS);
						
					}else if (sBrowserOperationParam1.equalsIgnoreCase("out")){
						WebElement ele = webDriver.findElement(By.tagName("html"));
						ele.sendKeys(Keys.chord(Keys.CONTROL, Keys.SUBTRACT));
						testcaseExecutionResult.setStatus(PASS);
					}else{
						
							((JavascriptExecutor)webDriver).executeScript("var a = document.body.style.zoom=\""+sBrowserOperationParam1+"%\";");						
							testcaseExecutionResult.setStatus(PASS);					
					}
				} catch (Exception e2) {
					logger.error("Error while zooming");
					testcaseExecutionResult.setMessage("Error while zooming");
				}
			}
			
		}
		//SAF-3247 - Script execution on Chrome is very fast due to which verification steps fails - Ashish Joshi
		else if(sBrowserOperation.equalsIgnoreCase("SET_SPEED"))
		{
			logger.info("SET_SPEED");
			int setSpeedTime=3000;
			try {
				setSpeedTime= Integer.parseInt(sBrowserOperationParam1);
			} catch (NumberFormatException e) {
			}
			try {
				webDriver.manage().timeouts().implicitlyWait(setSpeedTime, TimeUnit.MILLISECONDS);
				testcaseExecutionResult.setStatus(PASS);
			} catch (Exception e) {
				logger.error("Error while setting spped");
				testcaseExecutionResult.setMessage("Error while setting spped");
			}
		}
		//SAF-3247 - Script execution on Chrome is very fast due to which verification steps fails - Ashish Joshi
		else 
		{
			logger.error("Provided Wrong Operation");
			testcaseExecutionResult.setMessage("Provided Wrong Operation.");
		}
		return testcaseExecutionResult;
	}
	/**
	 * This method performs validation of the keyword

	 * @return ExecutionResults containing step execution status(pass/fail),
	 *         exact error message according to failure
	 */
	@Override

	// Not used this function as no validation is required
	public TestcaseExecutionResultVO validateKeyword(String... listOfParameters) {

		//logger.debug("Inside Method validate Execute Script");

		if (listOfParameters != null) {
			sBrowserOperation = listOfParameters[0];
			sBrowserOperationParam1=listOfParameters[1];
			sBrowserOperationEnvVariable=listOfParameters[2];
			sProxyServerNamePort=listOfParameters[3];
			sIsTrustAllCertificates=listOfParameters[4];
			
		} else {

			logger.error("ERROR_PARAMETERS_LIST");
			testcaseExecutionResult.setMessage(ERROR_PARAMETERS_LIST);
			return testcaseExecutionResult;
		}
		testcaseExecutionResult.setTestData(sBrowserOperation);
		if (KeywordUtilities.isEmptyString(sBrowserOperation)) {

			logger.error("Browser Operation not Passed");
			testcaseExecutionResult.setMessage("Browser Operation not Passed");
			return testcaseExecutionResult;
		}
		
		if(EMPTY_STRING.equals(sBrowserOperationEnvVariable))
		{
			sBrowserOperationEnvVariable="PERFORM_BROWSER_OPERATIONS_VARIABLE";
		}

		if(sIsTrustAllCertificates==null || EMPTY_STRING.equals(sIsTrustAllCertificates) || sIsTrustAllCertificates.equalsIgnoreCase("false")) {
			sIsTrustAllCertificates="N";
		}
		
		if(sProxyServerNamePort!=null && !sProxyServerNamePort.isEmpty() && !sProxyServerNamePort.contains(":")) {
			logger.error("Proxy server details passed are incorrect");
			testcaseExecutionResult.setMessage("set proxy server details as ProxyServer:PortNumber");			
		}
		
		testcaseExecutionResult.setValid(true);
		return testcaseExecutionResult;


	}
	/**
	 * This method validates the object on the browser

	 * @return ExecutionResults containing step execution status(pass/fail),
	 *         exact error message according to failure
	 */
	@Override
	// Not used this function as no validation is required
	public TestcaseExecutionResultVO validateObject(String... listOfParameters) {
		if (webDriver == null)
		{
			logger.error("ERROR_BROWSER_NOT_INSTANTIATED");
			testcaseExecutionResult.setMessage(ERROR_BROWSER_NOT_INSTANTIATED);
			testcaseExecutionResult.setValid(false);
			return testcaseExecutionResult;
		}

		testcaseExecutionResult.setValid(true);
		return testcaseExecutionResult;
	}

}
