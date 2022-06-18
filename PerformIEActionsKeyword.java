package com.sungard.ktt.business.keywords;

import static com.sungard.ktt.business.keywords.ErrorMessages.ERROR_BROWSER_NOT_INSTANTIATED;
import static com.sungard.ktt.business.keywords.ErrorMessages.ERROR_PARAMETERS_LIST;
import static com.sungard.ktt.view.config.KTTGuiConstants.DELIMITER;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;

import com.sungard.ktt.model.valueobjects.TestcaseExecutionResultVO;
import com.sungard.ktt.view.config.KTTGuiConstants;
import com.sungard.ktt.view.config.KTTGuiConstants.ScriptStatus;
import com.sungard.ktt.web.util.SAFALUserSession;

/**
 * This keyword is used to perform actions on Internet Browser with out using Selenium getEval, We are using js file
 * @author Ashish.Joshi & Dnyaneshwar.Daphal 
 * @version : SAFAL 4.0.1
 */
public class PerformIEActionsKeyword extends AbstractKeyword
{


	private TestcaseExecutionResultVO testCaseExecutionResult=new TestcaseExecutionResultVO();
	/**
	 * This is logger object used to log keyword actions into a log file
	 */
	Logger logger = Logger.getLogger("Thread" + Thread.currentThread().getName());

	/*Partial Window name or Window URL*/
	private String sReqWindowname=null;
	/*tag name-for example id ,name any tag name */
	private String sReqTagName=null;
	/*Any property or Attribute*/
	private String sProperty=null;
	private String sProperty_Value=null;
	/*value of the Attribute to get the element object*/
	private String sUser_Action=null;
	/*Action-Above mentioned user action to perform*/
	private String sModal_Flag=null;
	private String sDelimiter=null;
	private String sWaitTime=null;

	@Override
	/**
	 * After all validation, this method perform actions on Internet explorer HTML-DOM elements using js file..

	 * @return ExecutionResults containing step execution status(pass/fail),
	 *         exact error message according to failure
	 */

	/**
	 * This method runs after all the validations are successful
	 * @return ExecutionResults containing step execution status(pass/fail),
	 *         exact error message according to failure
	 */
	public synchronized TestcaseExecutionResultVO executeScript(String... listOfParameters)
	{
		//logger.debug("entered in executeScript..");
		MyWait myWait=new MyWait();





		/*		if (KeywordUtilities.isEmptyString(sModal_Flag))
		{
			sModal_Flag="TRUE";
		}

		if (KeywordUtilities.isEmptyString(sDelimiter))
		{
			sDelimiter=";";
		}*/
		String sReqTagNameArr[]={};
		String sPropertyArr[]={};
		String sProperty_ValueArr[]={};
		String sUser_ActionArr[]={};

		sModal_Flag="FALSE";
		sDelimiter=";";

		sReqTagNameArr=sReqTagName.split(sDelimiter);
		sPropertyArr=sProperty.split(sDelimiter);
		sProperty_ValueArr=sProperty_Value.split(sDelimiter);
		sUser_ActionArr=sUser_Action.split(sDelimiter);

		if (! ((sReqTagNameArr.length == sPropertyArr.length) && (sReqTagNameArr.length == sProperty_ValueArr.length) && (sReqTagNameArr.length == sUser_ActionArr.length)))
		{
			logger.error("Parameters count mismatched.");
			  testCaseExecutionResult.setMessage("Parameters count mismatched.");
			return testCaseExecutionResult;
		}


		int iWaitTime=KeywordUtilities.getObjectWaitTime(sWaitTime,configurationMap);


		String VerifyPopUpPresent =
				"              var message='FAIL'; " +
						"              try " +
						"              {" +
						"              var shellWindows = new ActiveXObject('Shell.Application').Windows();"+
						"              for (var i = 0; i < shellWindows.Count; i++)"+
						"              {"+
						"                              var w=null;try{w =shellWindows.Item(i);}catch(e){w=null;}" +
						"                              if (w!=null)" +
						"                              {" +
						"           					   var wType='NOSUCHTYPEWINDOW';try{wType=w.FullName;}catch(e){wType='NOSUCHTYPEWINDOW';}" +
						"                                  var locURL='NOSUCHWINDOW';try{locURL=w.LocationURL;}catch(e){locURL='NOSUCHWINDOW';}" +
						"                                  var locName='NOSUCHWINDOW';try{locName=w.LocationName;}catch(e){locName='NOSUCHWINDOW';} "+
						"								   if (wType.toUpperCase().match('IEXPLORE.EXE') && (locURL.toUpperCase().match(\""+sReqWindowname+"\".toUpperCase()) || locName.toUpperCase().match(\""+sReqWindowname+"\".toUpperCase())) ) "+
						"                                      {" +
						"                                               message= 'PASS';" +
						"                                               break;" +
						"                                      }" +
						"                              }" +
						"              }" +
						"              shellWindows = null;" +
						"              }catch(err){message= 'FAIL, ' + err.description;}";



		String SetIEFormAction=
				"	var AppShellWindows = new ActiveXObject('Shell.Application').Windows();																																																														"+
						"	var Windowcnt = AppShellWindows.Count;																																																														"+
						"	var ParamArr = 'false';																																																														"+
						"	var ElementFoundFlag = 'false';																																																														"+
						"	var ContainsFlag = 'false';																																																														"+
						"	var ActionDone = 'false';																																																														"+
						"	var Windowflag = 'false';																																																														"+
						"	var UserActionFlag = 'true';																																																													" +
						"   var ReqWindowName=\""+sReqWindowname+"\";                                                                                                                                                                                                                                                               " +
						"   var ReqTagName=\""+sReqTagName+"\";                                                                                                                                                                                                                                                   " +
						"   var Property=\""+sProperty+"\";                                                                                                                                                                                                                                                       " +
						"   var Property_Value=\""+sProperty_Value+"\";                                                                                                                                                                                                                                     " +
						"   var User_Action=\""+sUser_Action+"\";                                                                                                                                                                                                                                                 " +
						"	var sTagArr = ReqTagName.split(';');																																																														"+
						"	var sWinFrameArr = ReqWindowName.split(';');																																																														"+
						"	var sPropArr = Property.split(';');																																																														"+
						"	var sPropValArr = Property_Value.split(';');																																																														"+
						"	var sUActionArr = User_Action.split(';');																																																														"+
						"	var message = '';																																																														"+
						"	var TagInstance = 0;																																																														"+
						"	var ActualInstance = 0;																																																														"+
						"	var instanceFlag = 'false';																																																														"+
						"	var reqTagEle = '';																																																														"+
						"	var reqTagElePropValue = '';																																																														"+
						"	var reqTagEleProp = '';																																																														"+
						"	var allreqTagEle = '';																																																														"+
						"	var allReqTagPRopAll = '';																																																														"+
						"	var tagpropFlag = '';	" +
						"	var flgurl=false;" +
						"	var flgLocName=false;																																																"+
						"	if (sTagArr.length == sPropArr.length && sTagArr.length == sPropArr.length && sTagArr.length == sPropValArr.length && sTagArr.length == sUActionArr.length) 																																																														"+
						"	{																																																														"+
						"		ParamArr = 'true';																																																													"+
						"	}																																																														"+
						"	if (ParamArr == 'true') 																																																														"+
						"	{																																																														"+
						"		for ( var j = 0; j < AppShellWindows.Count; j++) 																																																													"+
						"		{																																																													"+
						"			var w = AppShellWindows.Item(j);																																																												"+
						"           var wType='NOSUCHTYPEWINDOW';try{wType=w.FullName;}catch(e){wType='NOSUCHTYPEWINDOW';}" +
						"           var locURL='NOSUCHWINDOW';try{locURL=w.LocationURL;}catch(e){locURL='NOSUCHWINDOW';}" +
						"           var locName='NOSUCHWINDOW';try{locName=w.LocationName;}catch(e){locName='NOSUCHWINDOW';} "+
						"			if (locName.match(sWinFrameArr[0])){flgLocName=true;}else{flgLocName=false;}" +
						"			if (flgLocName==false){if(locURL.match(sWinFrameArr[0])){flgurl=true;}else{flgurl=false;}}" +
						"			if (wType.toUpperCase().match('IEXPLORE.EXE') && (locURL.toUpperCase().match(sWinFrameArr[0].toUpperCase()) || locName.toUpperCase().match(sWinFrameArr[0].toUpperCase())) ) 																																																												"+
						"			{																																																												"+
						"				Windowflag = 'true';																																																											"+
						"				var WAITtag;																																																											"+
						"				var WINtag;																																																											"+
						"				var otherflg;																																																											"+
						"				var CurrentWindowDoc = w.Document;																																																											"+
						"				var arrTagValues;																																																											"+
						"				reqTagEle = '';																																																											"+
						"				reqTagElePropValue = '';																																																											"+
						"				reqTagEleProp = '';																																																											"+
						"				allreqTagEle = '';																																																											"+
						"				allReqTagPRopAll = '';																																																											"+
						"				tagpropFlag = '';																																																											"+
						"				var FlagINNERTEXT = false;																																																											"+
						"				for ( var par = 0; par < sTagArr.length; par++) 																																																											"+
						"				{																																																											"+
						"					tagpropFlag = 'FALSE';																																																										"+
						"					FlagINNERTEXT = false;																																																										"+
						"					if (sTagArr[par].match('_')) 																																																										"+
						"					{																																																										"+
						"						arrTagValues = sTagArr[par].split('_');																																																									"+
						"						if (arrTagValues.length > 1) 																																																									"+
						"						{																																																									"+
						"							sTagArr[par] = arrTagValues[0];																																																								"+
						"							TagInstance = parseInt(arrTagValues[1]);																																														" +
						"							if(isNaN(Trim(arrTagValues[1])) || Trim(arrTagValues[1])=='' ){TagInstance=0;}else{TagInstance = parseInt(arrTagValues[1]);}																									" +
						"							if(TagInstance>0){TagInstance=TagInstance-1;}																																																								"+
						"						} 																																																									"+
						"						else 																																																									"+
						"						{																																																									"+
						"							sTagArr[par] = arrTagValues[0];																																																								"+
						"							TagInstance = 0;																																																								"+
						"						}																																																									"+
						"					} 																																																										"+
						"					else if (sTagArr[par].match('@')) 																																																										"+
						"					{																																																										"+
						"						allreqTagEle = sTagArr[par].split('@');																																																									"+
						"						if (allreqTagEle.length == 2) 																																																									"+
						"						{																																																									"+
						"							reqTagEle = allreqTagEle[0];																																																								"+
						"							if (allreqTagEle[1].match('=')) 																																																								"+
						"							{																																																								"+
						"								try 																																																							"+
						"								{																																																							"+
						"									allReqTagPRopAll = allreqTagEle[1].split('=');																																																						"+
						"									reqTagEleProp = allReqTagPRopAll[0];																																																						"+
						"									reqTagElePropValue = allReqTagPRopAll[1];																																																						"+
						"									var innerTag_elements = CurrentWindowDoc.all.tags(reqTagEle);																																																						"+
						"									if (innerTag_elements.length > 0) 																																																						"+
						"									{																																																						"+
						"										for (z = 0; z < innerTag_elements.length; z++) 																																																					"+
						"										{" +
						"											if(allreqTagEle[1].match('=')){TagInstance=z+1;}else{TagInstance=z;}																																										"+
						"											var reqValuefromtagElement = innerTag_elements[z].getAttribute(reqTagEleProp);																																																				"+
						"											if (reqValuefromtagElement == reqTagElePropValue) 																																																				"+
						"											{																																																				"+
						"												tagpropFlag = 'TRUE';																																																			"+
						"												break;																																																			"+
						"											}																																																				"+
						"										}																																																					"+
						"									} 																																																						"+
						"									else 																																																						"+
						"									{																																																						"+
						"										message = message + 'FAIL! No such Elements found with tagName= '+ reqTagEle;																																																					"+
						"									}																																																						"+
						"								} 																																																							"+
						"								catch (e) 																																																							"+
						"								{																																																							"+
						"									message = message + 'FAIL! error ocurred, '	+ e.description;																																																						"+
						"								}																																																							"+
						"																																																															"+
						"							} 																																																								"+
						"							else 																																																								"+
						"							{																																																								"+
						"								message = message + 'Wrong input provided, require = operator in tag value';																																																							"+
						"							}																																																								"+
						"						}																																																									"+
						"					}																																																										"+
						"					var tagparentchildflag = false;																																																										"+
						"					otherflg = 'false';																																																										"+
						"					if (Trim(sTagArr[par].toUpperCase()) == 'WAIT' && Trim(sPropArr[par].toUpperCase()) == 'WAIT' && Trim(sPropValArr[par].toUpperCase()) == 'WAIT') 																																																										"+
						"					{																																																										"+
						"						otherflg = 'true';																																																									"+
						"						try 																																																									"+
						"						{																																																									"+
						"							pausejs(parseInt(sUActionArr[par]) * 1000);																																																								"+
						"							message = message + '|' + ('PASS');																																																								"+
						"						} 																																																									"+
						"						catch (errpause) 																																																									"+
						"						{																																																									"+
						"							message = message + '|' + 'FAIL! Unable to perform WAIT action: ' + errpause.description;																																																								"+
						"						}																																																									"+
						"					} 																																																										"+
						"					else if (Trim(sTagArr[par].toUpperCase()) == 'WIN' && Trim(sPropArr[par].toUpperCase()) == 'WIN' && Trim(sPropValArr[par].toUpperCase()) == 'WIN') 																																																										"+
						"					{																																																										"+
						"						otherflg = 'true';																																																									"+
						"						try 																																																									"+
						"						{																																																									"+
						"							if (Trim(sUActionArr[par].toUpperCase()) == 'CLOSE') 																																																								"+
						"							{																																																								"+
						"								w.Quit();																																																							"+
						"								message = message + '|' + ('PASS');																																																							"+
						"							} 																																																								"+
						"							else 																																																								"+
						"							{																																																								"+
						"								message = message + '|' + 'FAIL! Wrong user Action provided for Window.';																																																							"+
						"							}																																																								"+
						"						} 																																																									"+
						"						catch (errwinclose) 																																																									"+
						"						{																																																									"+
						"							message = message + '|' + 'FAIL! Unable to perform Close action: ' + errwinclose.description;																																																								"+
						"						}																																																									"+
						"					}																																																										"+
						"					var Tag_elements = '';																																																										"+
						"					if (otherflg == 'false') 																																																										"+
						"					{																																																										"+
						"						if (sTagArr[par].match('@') && tagpropFlag == 'TRUE') 																																																									"+
						"						{																																																									"+
						"							sTagArr[par] = reqTagEle;																																																								"+
						"						}																																																									"+
						"						Tag_elements = CurrentWindowDoc.all.tags(sTagArr[par]);																																																									"+
						"						try 																																																									"+
						"						{																																																									"+
						"							if (Tag_elements.length > 0) 																																																								"+
						"							{																																																								"+
						"								ActualInstance = 0;																																																							"+
						"								instanceFlag = 'false';																																																							"+
						"								for (i = 0; i < Tag_elements.length; i++) 																																																							"+
						"								{																																																							"+
						"									ElementFoundFlag = 'false';																																																						"+
						"									UserActionFlag = 'true';																																																						"+
						"									ContainsFlag = 'false';																																																						"+
						"									var reqValue = '';																																																						"+
						"									if (sPropArr[par].toUpperCase() == 'TEXT') 																																																						"+
						"									{																																																						"+
						"										reqValue = Trim(Tag_elements[i].getAttribute('innerText'));																																																					"+
						"									} 																																																						"+
						"									else if (sPropArr[par].toUpperCase() == 'HREF') 																																																						"+
						"									{																																																						"+
						"										reqValue = Tag_elements[i].getAttribute('href');																																																					"+
						"										if (reqValue.match(sPropValArr[par])) 																																																					"+
						"										{																																																					"+
						"											ContainsFlag = 'true';																																																				"+
						"										}																																																					"+
						"									} 																																																						"+
						"									else if (sPropArr[par].match('@')) 																																																						"+
						"									{																																																						"+
						"										tagparentchildflag = true;																																																					"+
						"											var dotparent = sPropArr[par].split('@');																																																				"+
						"											var propdot = '';																																																				"+
						"											propdot = dotparent[0];																																																				"+
						"											var reqlooplength = 0;																																																				"+
						"											var reqEleParent = Tag_elements[i];																																																				"+
						"											if (propdot.toUpperCase() == 'INNERTEXT' || propdot.toUpperCase() == 'TEXT') 																																																				"+
						"											{																																																				"+
						"												propdot='innerText';" +
						"												FlagINNERTEXT = true;																																																			"+
						"											}																																																				"+
						"																																																															"+
						"											if (!isNaN(dotparent[2])) 																																																				"+
						"											{																																																				"+
						"												if (parseInt(dotparent[2]) > 1) 																																																			"+
						"												{																																																			"+
						"													reqlooplength = parseInt(dotparent[2]);																																																		"+
						"												} 																																																			"+
						"												else 																																																			"+
						"												{																																																			"+
						"													reqlooplength = 1;																																																		"+
						"												}																																																			"+
						"											} 																																																				"+
						"											else 																																																				"+
						"											{																																																				"+
						"												reqlooplength = 1;																																																			"+
						"											}																																																				"+
						"																																																															"+
						"											if (reqlooplength > 0 && !dotparent[1] == '') 																																																				"+
						"											{																																																				"+
						"												for (d = 0; d < reqlooplength; d++) 																																																			"+
						"												{																																																			"+
						"													try 																																																		"+
						"													{																																																		"+
						"														if (dotparent[1].toUpperCase() == 'PARENT') 																																																	"+
						"														{																																																	"+
						"															reqEleParent = reqEleParent.parentNode;																																																"+
						"														} 																																																	"+
						"														else if (dotparent[1].toUpperCase() == 'CHILD') 																																																	"+
						"														{																																																	"+
						"															reqEleParent = reqEleParent.childNode;																																																"+
						"														}																																																	"+
						"													} 																																																		"+
						"													catch (e99) 																																																		"+
						"													{																																																		"+
						"														message = message + 'FAIL! '+ e99.description;																																																	"+
						"													}																																																		"+
						"												}																																																			"+
						"											}																																																				"+
						"																																																															"+
						"											try 																																																				"+
						"											{																																																				"+
						"												reqValue = reqEleParent.getAttribute(propdot);																																																			"+
						"											} 																																																				"+
						"											catch (e12) 																																																				"+
						"											{																																																				"+
						"												reqValue = '';																																																			"+
						"											}																																																				"+
						"									}																																																						"+
						"									else 																																																					"+
						"									{																																																					"+
						"										reqValue = Tag_elements[i].getAttribute(sPropArr[par]);																																																				"+
						"									}																																																					"+
						"									if (FlagINNERTEXT == true) 																																																						"+
						"									{																																																						"+
						"										if (reqValue.match(' ')) 																																																					"+
						"										{																																																					"+
						"											reqValue = reqValue.split(' ').join('');																																																				"+
						"										}																																																					"+
						"																																																															"+
						"										if (sPropValArr[par].match(' ')) 																																																					"+
						"										{																																																					"+
						"											sPropValArr[par] = sPropValArr[par].split(' ').join('');																																																				"+
						"										}																																																					"+
						"									}																																																						"+
						"									if (reqValue == sPropValArr[par] || ContainsFlag == 'true' || (tagparentchildflag == true && reqValue==sPropValArr[par])) 																																																						"+
						"									{																																																						"+
						"										if (ActualInstance == TagInstance || (tagpropFlag == 'TRUE' && ActualInstance == TagInstance))																																																					"+
						"										{																																																					"+
						"											instanceFlag = 'true';																																																				"+
						"											ElementFoundFlag = 'true';																																																				"+
						"											if (sUActionArr[par].toUpperCase() == 'SET_FRAMEDOC') 																																																				"+
						"											{																																																				"+
						"												try																																																			"+
						"												{																																																			"+
						"													if (Tag_elements[i].tagName.toUpperCase() == 'IFRAME') 																																																		"+
						"													{																																																		"+
						"														try 																																																	"+
						"														{																																																	"+
						"															CurrentWindowDoc = Tag_elements[i].contentWindow.document;																																																"+
						"														} 																																																	"+
						"														catch (errorf0){continue;}																																																	"+
						"													} 																																																		"+
						"													else if (Tag_elements[i].tagName.toUpperCase() == 'FRAME') 																																																		"+
						"													{																																																		"+
						"														try {																																																	"+
						"															CurrentWindowDoc = Tag_elements[i].document;																																																"+
						"														} 																																																	"+
						"														catch (errorf1) {continue;}																																																	"+
						"													}																																																		"+
						"													ActionDone = 'true';																																																		"+
						"												} 																																																			"+
						"												catch (err1) 																																																			"+
						"												{																																																			"+
						"													message = message + '|'+ 'FAIL! Unable to perform required action: '+ sUActionArr[par]+ ', on element with tag: '+ sTagArr[par]+ ', Property: '+ sPropArr[par]+ ', Property_Value: '+ sPropValArr[par]+ '. '+ err1.description;																																																		"+
						"												}																																																			"+
						"											} 																																																				"+
						"											else if (sUActionArr[par].toUpperCase() == 'USER_CLICK') 																																																				"+
						"											{																																																				"+
						"												try 																																																			"+
						"												{																																																			"+
						"													Tag_elements[i].click();																																																		"+
						"													ActionDone = 'true';																																																		"+
						"												} 																																																			"+
						"												catch (err1) 																																																			"+
						"												{																																																			"+
						"													message = message + '|'+ 'FAIL! Unable to perform required action: '+ sUActionArr[par]+ ', on element with tag: '+ sTagArr[par]+ ', Property: '+ sPropArr[par]+ ', Property_Value: '+ sPropValArr[par]+ '. '+ err1.description;																																																		"+
						"												}																																																			"+
						"											} 																																																				"+
						"											else if (sUActionArr[par].toUpperCase() == 'USER_CHECK') 																																																				"+
						"											{																																																				"+
						"												try 																																																			"+
						"												{																																																			"+
						"													if (Tag_elements[i].checked == false) 																																																		"+
						"													{																																																		"+
						"														try 																																																	"+
						"														{																																																	"+
						"															Tag_elements[i].checked = true;																																																"+
						"														} 																																																	"+
						"														catch (errradio) 																																																	"+
						"														{																																																	"+
						"															Tag_elements[i].click();																																																"+
						"														}																																																	"+
						"													}																																																		"+
						"													ActionDone = 'true';																																																		"+
						"												} 																																																			"+
						"												catch (err1) 																																																			"+
						"												{																																																			"+
						"													message = message+ '|'+ 'FAIL! Unable to perform required action: '+ sUActionArr[par]+ ', on element with tag: '+ sTagArr[par]+ ', Property: '+ sPropArr[par]+ ', Property_Value: '+ sPropValArr[par]+ '. '+ err1.description;																																																		"+
						"												}																																																			"+
						"											} 																																																				"+
						"											else if (sUActionArr[par].toUpperCase() == 'USER_UNCHECK') 																																																				"+
						"											{																																																				"+
						"												try 																																																			"+
						"												{																																																			"+
						"													if (Tag_elements[i].checked == true) 																																																		"+
						"													{																																																		"+
						"														try																																																	"+
						"														{																																																	"+
						"															Tag_elements[i].checked = false;																																																"+
						"														}																																																	"+
						"														catch (errradio) 																																																	"+
						"														{																																																	"+
						"															Tag_elements[i].click();																																																"+
						"														}																																																	"+
						"													}																																																		"+
						"													ActionDone = 'true';																																																		"+
						"												} 																																																			"+
						"												catch (err1) 																																																			"+
						"												{																																																			"+
						"													message = message + '|'+ 'FAIL! Unable to perform required action: '+ sUActionArr[par]+ ', on element with tag: '+ sTagArr[par]+ ', Property: '+ sPropArr[par]+ ', Property_Value: '+ sPropValArr[par]+ '. '+ err1.description;"																																																		+
						"												}																																																			"+
						"											} 																																																				"+
						"											else if (sUActionArr[par].toUpperCase().match('FIREEVENT')) 																																																				"+
						"											{																																																				"+
						"												try {																																																			"+
						"													var EventArray = sUActionArr[par].split('_');																																																		"+
						"													var Fireevt = EventArray[1];																																																		"+
						"													if (CurrentWindowDoc.createEventObject) 																																																		"+
						"													{																																																		"+
						"														var evt = CurrentWindowDoc.createEventObject();																																						"+
						"														Tag_elements[i].fireEvent(Fireevt, evt);																																							"+
						"														ActionDone = 'true';																																												"+
						"													}																																																		"+
						"													else																																																		"+
						"													{																																																		"+
						"														message = message + '|'+ 'FAIL! Unable to create event object';																																																	"+
						"													}																																																		"+
						"												} 																																																			"+
						"												catch (err1) 																																																			"+
						"												{																																																			"+
						"													message = message + '|'+ 'FAIL! Unable to perform required action: '+ sUActionArr[par]+ ', on element with tag: '+ sTagArr[par]+ ', Property: '+ sPropArr[par]+ ', Property_Value: '+ sPropValArr[par]+ '. '+ err1.description;																																																		"+
						"												}																																																			"+
						"											} 																																																				"+
						"											else if (sUActionArr[par].toUpperCase().match('FORCEKEY')) 																																																				"+
						"											{																																																				"+
						"												try 																																																			"+
						"												{																																																			"+
						"													var WName = '';																																																		"+
						"													var WNameArr = '';																																																		"+
						"													if (flgurl==true) 																																																		"+
						"													{																																																		"+
						"														if (locURL.length>96){WName=appUrl.substr(0, 95)+' - Windows Internet Explorer';}else{WName=locURL +' - Windows Internet Explorer';}																																																	"+
						"													} 																																																		"+
						"													else 																																																		"+
						"													{																																																		"+
						"														if (locName.length > 96) {WName=locName.substr(0, 95)+' - Windows Internet Explorer';} else {WName = locName + ' - Windows Internet Explorer';}																																																	"+
						"													}																																																		"+
						"													var EventArray = sUActionArr[par].split('_');																																																		"+
						"													var Forcekey = EventArray[1];																																																		"+
						"													var Wshell = new ActiveXObject('WScript.Shell');																																																		"+
						"													Wshell.AppActivate(WName);" +
						"													Tag_elements[i].focus();																																																		"+
						"													pausejs(100);																																																		"+
						"													try 																																																		"+
						"													{																																																		"+
						"														var sp_Forcekey = '{' + Forcekey + '}';																																																	"+
						"														Wshell.Sendkeys(sp_Forcekey);																																																	"+
						"													} 																																																		"+
						"													catch (err5) 																																																		"+
						"													{																																																		"+
						"														Wshell.Sendkeys(Forcekey);																																																	"+
						"													}																																																		"+
						"													ActionDone = 'true';																																																		"+
						"													Wshell = null;																																																		"+
						"												} 																																																			"+
						"												catch (err1) 																																																			"+
						"												{																																																			"+
						"													message = message + '|'+ 'FAIL! Unable to perform required action: '+ sUActionArr[par]+ ', on element with tag: '+ sTagArr[par]+ ', Property: '+ sPropArr[par]+ ', Property_Value: '+ sPropValArr[par]+ '. '+ err1.description;																																																		"+
						"												}																																																			"+
						"											} 																																																				"+
						"											else if (sUActionArr[par].toUpperCase() == 'FOCUS') 																																																				"+
						"											{																																																				"+
						"												try 																																																			"+
						"												{																																																			"+
						"													var WName = '';																																																		"+
						"													var WNameArr = '';																																																		"+
						"													if (flgurl==true) 																																																		"+
						"													{																																																		"+
						"														if (locURL.length>96)" +
						"														{WName=appUrl.substr(0, 95)+' - Windows Internet Explorer';}else{WName=locURL +' - Windows Internet Explorer';}																																																	"+
						"													} 																																																		"+
						"													else 																																																		"+
						"													{																																																		"+
						"														WName = locName + ' - Windows Internet Explorer';																																																	"+
						"													}																																																		"+
						"													var Wshell = new ActiveXObject('WScript.Shell');																																																		"+
						"													Wshell.AppActivate(WName);																																																		"+
						"													Tag_elements[i].setActive();																																																		"+
						"													Tag_elements[i].focus();																																																		"+
						"													ActionDone = 'true';																																																		"+
						"													Wshell=null;																																																		"+
						"												} 																																																			"+
						"												catch (err1) 																																																			"+
						"												{																																																			"+
						"													message = message + '|'+ 'FAIL! Unable to perform required action: '+ sUActionArr[par]+ ', on element with tag: '+ sTagArr[par]+ ', Property: '+ sPropArr[par]+ ', Property_Value: '+ sPropValArr[par]+ '. '+ err1.description;																				"+
						"												}																																																			"+
						"											} 																																																				"+
						"											else if (sUActionArr[par].toUpperCase().match('SETATTRIBUTE')) 																																																				"+
						"											{																																																				"+
						"												try 																																																			"+
						"												{																																																			"+
						"													var AttArray = sUActionArr[par].split('_');																																																		"+
						"													var Attribute = AttArray[1];																																																		"+
						"													var AttributeValue = AttArray[2];																																																		"+
						"													Tag_elements[i].setAttribute(Attribute,AttributeValue);"																																																		+
						"													ActionDone = 'true';																																																		"+
						"												} 																																																			"+
						"												catch (err1) 																																																			"+
						"												{																																																			"+
						"													message = message + '|'+ 'FAIL! Unable to perform required action: '+ sUActionArr[par]+ ', on element with tag: '+ sTagArr[par]+ ', Property: '+ sPropArr[par]+ ', Property_Value: '+ sPropValArr[par]+ '. '+ err1.description;																																																		"+
						"												}																																																			"+
						"											} 																																																				"+
						"											else if (sUActionArr[par].toUpperCase().match('REMOVEATTRIBUTE')) 																																																				"+
						"											{																																																				"+
						"												try 																																																			"+
						"												{																																																			"+
						"													var AttArray = sUActionArr[par].split('_');																																																		"+
						"													var Attribute = AttArray[1];																																																		"+
						"													Tag_elements[i].removeAttribute(Attribute);																																																		"+
						"													pausejs(1);																																																		"+
						"													ActionDone = 'true';																																																		"+
						"												} 																																																			"+
						"												catch (err1) 																																																			"+
						"												{																																																			"+
						"													message = message+ '|'+ 'FAIL! Unable to perform required action: '+ sUActionArr[par] + ', on element with tag: ' + sTagArr[par] + ', Property: '+ sPropArr[par]+ ', Property_Value: '+ sPropValArr[par]+ '. '+ err1.description;																																																		"+
						"												}																																																			"+
						"											} 																																																				"+
						"											else if (!((sUActionArr[par].toUpperCase() == 'USER_CLICK')&& (sUActionArr[par].toUpperCase() == 'USER_CHECK')&& (sUActionArr[par].toUpperCase() == 'FOCUS')&& (sUActionArr[par].toUpperCase() == 'USER_UNCHECK')&& (sUActionArr[par].toUpperCase().match('FORCEKEY'))&& (sUActionArr[par].toUpperCase().match('SETATTRIBUTE'))&& (sUActionArr[par].toUpperCase().match('FIREEVENT'))&& (sUActionArr[par].toUpperCase() == 'SET_FRAMEDOC') && (sUActionArr[par].toUpperCase().match('REMOVEATTRIBUTE')))) 																																																				"+
						"											{																																																				"+
						"												if (sTagArr[par].toUpperCase() == 'SELECT') 																																																			"+
						"												{																																																			"+
						"													var SelectFoundTextFlag = 'false';																																																		"+
						"													try 																																																		"+
						"													{																																																		"+
						"														for (index = 0; index < Tag_elements[i].options.length; index++) 																																																	"+
						"														{																																																	"+
						"															if ((Tag_elements[i].options[index].text == sUActionArr[par])|| (Tag_elements[i].options[index].value == sUActionArr[par])) 																																																"+
						"															{																																																"+
						"																Tag_elements[i].options[index].selected = true;																																															"+
						"																SelectFoundTextFlag = 'true';																																															"+
						"																break;																																															"+
						"															}																																																"+
						"														}																																																	"+
						"														if (SelectFoundTextFlag == 'true') {																																																	"+
						"															ActionDone = 'true';																																																"+
						"														} 																																																	"+
						"														else 																																																	"+
						"														{																																																	"+
						"															message = message + '|' + 'FAIL! Unable to find the required option to select for the given dropdown.';																																																"+
						"														}																																																	"+
						"													} 																																																		"+
						"													catch (err1) 																																																		"+
						"													{																																																		"+
						"														message+ '|'+ 'FAIL! Unable to select the given option. '+ err1.description;																																																	"+
						"													}																																																		"+
						"												} 																																																			"+
						"												else if ((sTagArr[par].toUpperCase() == 'INPUT' && (Tag_elements[i].type.match('text') || Tag_elements[i].type == 'password'))|| sTagArr[par].toUpperCase() == 'TEXTAREA') 																																																			"+
						"												{																																																			"+
						"													try 																																																		"+
						"													{																																																		"+
						"														Tag_elements[i].value = sUActionArr[par];																																																	"+
						"														ActionDone = 'true';																																																	"+
						"													} 																																																		"+
						"													catch (err1) 																																																		"+
						"													{																																																		"+
						"														message = message+ '|'+ 'FAIL! Unable to set the given text. '+ err1.description;																																																	"+
						"													}																																																		"+
						"												} else {																																																			"+
						"													UserActionFlag = 'false';																																																		"+
						"												}																																																			"+
						"											}																																																				"+
						"										} 																																																					"+
						"										else{ ActualInstance = ActualInstance + 1;}																																																					"+
						"									}																																																						"+
						"									if (instanceFlag == 'true') 																																																						"+
						"									{																																																						"+
						"										TagInstance = 0;																																																					"+
						"										break;																																																					"+
						"									}																																																						"+
						"								}																																																							"+
						"								if (ElementFoundFlag == 'true' && UserActionFlag == 'true') 																																																							"+
						"								{																																																							"+
						"									if (ActionDone == 'true') 																																																						"+
						"									{																																																						"+
						"										message = message + '|' + ('PASS');																																																					"+
						"									} 																																																						"+
						"									else 																																																						"+
						"									{																																																						"+
						"										message = message + '|'+ 'FAIL! Unable to perform required action: '+ sUActionArr[par]+ ', on element with tag: '+ sTagArr[par] + ', Property: '+ sPropArr[par]+ ', Property_Value: '+ sPropValArr[par] + '.';																																																					"+
						"									}																																																						"+
						"								} 																																																							"+
						"								else 																																																							"+
						"								{																																																							"+
						"									if (UserActionFlag == 'false') 																																																						"+
						"									{																																																						"+
						"										message = message+ '|'+ 'FAIL! Wrong User Action provided, Action = '+ sUActionArr[par];																																																					"+
						"									} 																																																						"+
						"									else if (instanceFlag == 'false') 																																																						"+
						"									{																																																						"+
						"										message = message+ '|'+ 'FAIL! Unable to perform required action: '+ sUActionArr[par]+ ', on element with tag: '+ sTagArr[par]+ ' having instance: '+ (TagInstance+1) + ' , Property: '+ sPropArr[par]+ ', Property_Value: '+ sPropValArr[par] + '.';																																																					"+
						"									} 																																																						"+
						"									else																																																						"+
						"									{																																																						"+
						"										message = message+ '|'+ 'FAIL! Unable to perform required action: '+ sUActionArr[par]+ ', on element with tag: '+ sTagArr[par] + ', Property: '+ sPropArr[par]+ ', Property_Value: '+ sPropValArr[par] + '.';																																																					"+
						"									}																																																						"+
						"								}																																																							"+
						"							} 																																																								"+
						"							else 																																																								"+
						"							{																																																								"+
						"								message = message + '|'+ 'FAIL! Elements with required tag name: '+ sTagArr[par] + ' not Found.';																																																							"+
						"							}																																																								"+
						"						} 																																																									"+
						"						catch (err6) 																																																									"+
						"						{																																																									"+
						"							message = message + '|' + 'FAIL! ' + err6.description;																																																								"+
						"						}																																																									"+
						"					}																																																										"+
						"				}																																																											"+
						"				CurrentWindowDoc = null;																																																											"+
						"				break;																																																											"+
						"			}																																																												"+
						"		}																																																													"+
						"		if (Windowflag == 'false') 																																																													"+
						"		{																																																													"+
						"			message = message + '|' + 'FAIL! Unable to find the required Window.';																																																												"+
						"		}																																																													"+
						"		AppShellWindows = null;																																																													"+
						"	} 																																																														"+
						"	else 																																																														"+
						"	{																																																														"+
						"		message = message + '|' + 'FAIL! Parameters count mismatched';																																																													"+
						"	}																																																														"+
						"function pausejs(millis){var date = new Date();var curDate = null;do {curDate = new Date();}while(curDate-date < millis);}" +
						"function Trim(s){return s.replace(/^\\s*/,\"\").replace(/\\s*$/, \"\");}";    

		String line1=null;
		String Lastline1=null;
		File file1 = null;
		Process p1 =null;
		BufferedWriter bw1 =null;
		BufferedReader input1 =null;


		Lastline1=KTTGuiConstants.EMPTY_STRING;
		try {
			file1 = File.createTempFile("PopUpTestjs",".js");
			bw1 = new BufferedWriter(new FileWriter(file1, true));			 
			bw1.write(VerifyPopUpPresent);
			bw1.write("WScript.Stdout.Write(message);");
			bw1.close();
		} catch (Exception e) {

			logger.error ("FAIL! Unable to run the Script on Browser.",e);
			testCaseExecutionResult.setMessage("FAIL! Unable to run the Script on Browser.");
			return testCaseExecutionResult;
		}

		try
		{
			MyWait myWait1=new MyWait();
			for(int i=0;i<iWaitTime;i++)
			{
				//SAF-670 if loop
				if(SAFALUserSession.getSciptStatus(userName) == ScriptStatus.TO_BE_STOPPED)
				{		
					break;
				}

				p1 = Runtime.getRuntime().exec("cmd /c wscript/b " + file1.getPath());
				p1.waitFor();
				input1 = new BufferedReader(new InputStreamReader(p1.getInputStream()));
				while ((line1 = input1.readLine()) != null) 
				{
					Lastline1=line1;
					logger.info("Output from js:  "+ Lastline1);
				}			
				if(Lastline1.equalsIgnoreCase("PASS"))
				{	
					break;
				}

				myWait1.waitFor(1000, null);
			}		
			//============================================================================================
			if (Lastline1.toUpperCase().contains("FAIL"))
			{
				logger.error ("Window or PopUp not available.");
				testCaseExecutionResult.setMessage("Window or PopUp not available");
				return testCaseExecutionResult;
			}
		}
		catch(Exception e1){}
		finally
		{
			p1.destroy();
			file1.delete();
		}
		String line=null;
		String Lastline=null;
		File file = null;
		Process p =null;
		BufferedWriter bw =null;
		BufferedReader input =null;

		Lastline=KTTGuiConstants.EMPTY_STRING;
		try {

			file = File.createTempFile("Testjs",".js");
			bw = new BufferedWriter(new FileWriter(file, true));			 

			bw.write(SetIEFormAction);
			bw.write("WScript.Stdout.Write(message);");
			bw.close();
			try
			{
				logger.info("cmd /c wscript/b " + file.getPath());
				p = Runtime.getRuntime().exec("cmd /c wscript/b " + file.getPath());
			}
			catch(Exception e1)
			{
			}
			myWait.waitFor(2000, null);
			p.waitFor();
			input = new BufferedReader(new InputStreamReader(p.getInputStream()));
			while ((line = input.readLine()) != null) 
			{
				Lastline=line;
				logger.info("Output from js:  "+ Lastline);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		finally
		{
			p.destroy();
			file.delete();
		}


		if (Lastline.toUpperCase().contains("FAIL"))
		{
			logger.error ("Window or PopUp not available.");
			testCaseExecutionResult.setMessage(Lastline);
		}
		else
		{
			logger.error ("Window or PopUp not available.");
			testCaseExecutionResult.setMessage(Lastline);
			testCaseExecutionResult.setStatus(1);
		}
		return testCaseExecutionResult;	

	}
	/**
	 * This method performs validation of the keyword

	 * @return ExecutionResults containing step execution status(pass/fail),
	 *         exact error message according to failure
	 */
	@Override
	public TestcaseExecutionResultVO validateKeyword(String... listOfParameters)
	{
		
		if (listOfParameters != null) {

			sReqWindowname=listOfParameters[0];
			sReqTagName=listOfParameters[1];
			sProperty=listOfParameters[2];
			sProperty_Value=listOfParameters[3];
			sUser_Action=listOfParameters[4];
			sWaitTime=listOfParameters[5];


		} else {

			logger.error ("ERROR_PARAMETERS_LIST");
			testCaseExecutionResult.setMessage(ERROR_PARAMETERS_LIST);
			return testCaseExecutionResult;
		}



		testCaseExecutionResult.setTestData(sReqWindowname+DELIMITER+sReqTagName+DELIMITER+sProperty+DELIMITER+sProperty_Value +DELIMITER+ sUser_Action+DELIMITER+sModal_Flag+DELIMITER+sDelimiter);

		if(KeywordUtilities.isEmptyString(sReqWindowname))
		{
			logger.error ("Window name is required");
			testCaseExecutionResult.setMessage("Window name is required");
			return testCaseExecutionResult;
		}

		if(KeywordUtilities.isEmptyString(sReqTagName))
		{
			logger.error ("Tag name is required");
			testCaseExecutionResult.setMessage("Tag name is required");
			return testCaseExecutionResult;
		}


		if(KeywordUtilities.isEmptyString(sProperty))
		{
			logger.error ("Property name is required");
			testCaseExecutionResult.setMessage("Property name is required");
			return testCaseExecutionResult;
		}


		if(KeywordUtilities.isEmptyString(sProperty_Value))
		{
			logger.error ("Window or PopUp not available.");
			testCaseExecutionResult.setMessage("Property value is required");
			return testCaseExecutionResult;
		}

		if(KeywordUtilities.isEmptyString(sUser_Action))
		{
			logger.error ("Window or PopUp not available.");
			testCaseExecutionResult.setMessage("User Action value required");
			return testCaseExecutionResult;
		}

		testCaseExecutionResult.setValid(true);
		return testCaseExecutionResult;
	}
	/**
	 * This method validates the object on the browser
	     exact error message according to failure
	 */
	@Override
	public TestcaseExecutionResultVO validateObject(String... params)
	{
		if (webDriver == null)
		{
			logger.error ("ERROR_BROWSER_NOT_INSTANTIATED");
			testCaseExecutionResult.setMessage(ERROR_BROWSER_NOT_INSTANTIATED);
			testCaseExecutionResult.setValid(false);
			return testCaseExecutionResult;
		}

		testCaseExecutionResult.setValid(true);
		return testCaseExecutionResult;
	}
}