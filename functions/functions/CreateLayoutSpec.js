createLayout(arguments[0],arguments[1],arguments[2],arguments[3],arguments[4],arguments[5],arguments[6],arguments[7]);
/* this function iterates through all elements in DOM and captures locator and atributes of said tags 
 * 
 */
function createLayout(topElement,htmlControlToCheck,uniqueLocatorSequence,topLocator,skipIdenticalElement,attributesToCheck,matchExactTextFilter,positionTolerance){
	
	var htmlControlToCheck=htmlControlToCheck.split(",");	
	var positionTolerance=parseInt(positionTolerance);
	var topElement=topElement;
	var htmlElements;
	var uniqueAttribute="";
	var varName = "";
	var isElementCaptured=false;
	var tagName ="";
	var isOkToContinueForContainerTags=true;
	var eachWebElement=null;
	var tmpNameLocator;
	var uniqueLocatorSequence=uniqueLocatorSequence;
	var topLocator=topLocator;
	var REMOVE_SPECIAL_CHARS="/[()=#\$\/\.\\[\\]\+\~\*`,<>]/g";
	var REMOVE_SPACE="/\\s/g";
	var REPLACE_SPECIAL_CHARS="/[\+\~\*:;,\?'@%\^\-]/g";
	var elementsToSkipHeightWidthTextAttributes=["div", "table", "tr"];
	var skipIdenticalElement=skipIdenticalElement;
	var mapOfDuplicateElements = new Object();
	var mapOfLogicalNameAndLocator = new Object();
	var mapOfLocatorAndAtributes=new Object();
	var attributesToCheck=attributesToCheck;
	var matchExactTextFilter=matchExactTextFilter;
	
	
	for(var eachHtmlTagCnt=0;eachHtmlTagCnt < htmlControlToCheck.length; eachHtmlTagCnt++){
		
		htmlElements=topElement.getElementsByTagName(htmlControlToCheck[eachHtmlTagCnt]);
		
		for(var eachElementCnt=0;eachElementCnt < htmlElements.length;eachElementCnt++){
			try{
				
				eachWebElement=htmlElements[eachElementCnt];
				
				uniqueAttribute = "";
				varName = "";
				isElementCaptured = false;
				tagName = eachWebElement.tagName;
			
				isOkToContinueForContainerTags = true;
				
				if (tagName.toLowerCase()=="td") {
						if (hasChildElements(eachWebElement)) {
							isOkToContinueForContainerTags = false;
						} else {							
							isOkToContinueForContainerTags = true;
							
						}
				}
						
				
				
				tmpNameLocator = calculateObjectNameAndLocatorBasedOnUniqueAttributes(eachWebElement,
							uniqueLocatorSequence, getTopLocator(topLocator));
				
				
				
				if (tmpNameLocator != null && tmpNameLocator.length == 2) {
					
						if (tmpNameLocator[0] != null && trim(tmpNameLocator[0]).length > 0 && tmpNameLocator[1] != null && trim(tmpNameLocator[1]).length > 0) {
							varName = tagName + "_" + tmpNameLocator[0];
							varName = varName.replace(eval(REMOVE_SPECIAL_CHARS), "");
							varName = varName.replace(eval(REMOVE_SPACE), "");
							varName = varName.replace(eval(REPLACE_SPECIAL_CHARS), "_");
							
							
							uniqueAttribute = tmpNameLocator[1].substring((getTopLocator(topLocator) + "//" + tagName + "[").length, tmpNameLocator[1].length - 1);
							
							

							var xpathIndex = 0;
							var trimAttrib="";
							var arrUniqueAttributes=uniqueAttribute.split("@");
							
							for (var attribute in arrUniqueAttributes) {
								
								trimAttrib=trim(arrUniqueAttributes[attribute]);
								
								if ( trimAttrib in   mapOfDuplicateElements) {
									mapOfDuplicateElements[trimAttrib]= mapOfDuplicateElements[trimAttrib] + 1;

								} else {
									mapOfDuplicateElements[trimAttrib]=1;

								}
							}
							

							var tmpIndex = 0;
							for (var attribute in arrUniqueAttributes) {
								trimAttrib=trim(arrUniqueAttributes[attribute]);
								tmpIndex = mapOfDuplicateElements[trimAttrib];

								if (xpathIndex == 0 || xpathIndex > tmpIndex) {
									xpathIndex = tmpIndex;
								}
								

							}

							
								
							var objLocator=tmpNameLocator[1];
							if (isOkToContinueForContainerTags) {
								uniqueAttribute=uniqueAttribute.substring(1,objLocator.length);
								
								if(uniqueAttribute.indexOf("@")!=-1){ 
										uniqueAttribute=uniqueAttribute.replace(/@/g," and @");
										uniqueAttribute="@"+uniqueAttribute;
										objLocator=getTopLocator(topLocator) + "//" + tagName + "["+uniqueAttribute+"]";
									}
								if (skipIdenticalElement=="N") {									

									varName = varName + "_" + xpathIndex;
									
									if (xpathIndex > 1) {
										mapOfLogicalNameAndLocator[varName]="(" + objLocator + ")[" + xpathIndex + "]";
									} else {
										mapOfLogicalNameAndLocator[varName]=objLocator;
									}
									isElementCaptured = true;
								} else {
									if (xpathIndex == 1) {
										mapOfLogicalNameAndLocator[varName]= objLocator;
										isElementCaptured = true;
									}
								}
							}
						}
					}
				
				/*If element is not located using speciifed attributes, try with it's innerText */
				
					if (tmpNameLocator == null || tmpNameLocator.length < 2) {
						try {
							elementText = trim(getElementText(tagName,eachWebElement));
							if (elementText != null && elementText.length > 0 && !hasUnicode(elementText)) {							
								
								if (elementText.indexOf("\n")==-1) {
									elementText = trim(elementText);
									if (elementText.length > 255) {
										varName = tagName + "_" + elementText.substring(0, 255);
									} else {
										varName = tagName + "_" + elementText;
									}
									varName = varName.replace(eval(REMOVE_SPECIAL_CHARS), "");
									varName = varName.replace(eval(REMOVE_SPACE), "");
									varName = varName.replace(eval(REPLACE_SPECIAL_CHARS), "_");

									if (varName in mapOfDuplicateElements) {
										mapOfDuplicateElements[varName]=mapOfDuplicateElements[varName] + 1;
									} else {
										mapOfDuplicateElements[varName]=1;
									}
									
									try {
										

										if (isOkToContinueForContainerTags) {
											if (skipIdenticalElement=="N") {

												if (mapOfDuplicateElements[varName] > 1) {
													objectLocator = "(" + getTopLocator(topLocator) + "//" + tagName
															+ "[contains(.,\"" + elementText + "\")])["
															+ mapOfDuplicateElements[varName] + "]";
													varName = varName + "_" + mapOfDuplicateElements[varName];
												} else {
													objectLocator = getTopLocator(topLocator) + "//" + tagName
															+ "[contains(.,\"" + elementText + "\")]";
												}

											} else {
												if (mapOfDuplicateElements[varName] == 1) {
													objectLocator = "(" + getTopLocator(topLocator) + "//" + tagName
															+ "[contains(.,\"" + elementText + "\")])[1]";
												}
											}

											if (varName != null && trim(varName).length > 0
													&& isOkToContinueForContainerTags) {
												mapOfLogicalNameAndLocator[varName]=objectLocator;
												isElementCaptured = true;
											}
										}
									} catch (e) {
										isElementCaptured = false;
										continue;
									}
								}

							}

							
							if (hasUnicode(elementText)) {
								continue;
							}
						} catch (e) {
							
						}
					}
					
					/* Capture element preperties such as height, width and text */
					
					writeAttributesToFile = "";
					if (isElementCaptured )  {

						if (tagName != null && trim(tagName).length > 0) {
							var attributes = attributesToCheck.toLowerCase().split(",");
							for (var attribCnt = 0; attribCnt < attributes.length; attribCnt++) {

								if (elementsToSkipHeightWidthTextAttributes.indexOf(tagName.toLowerCase())==-1) {
									switch (attributes[attribCnt]) {
									case "height":
										writeAttributesToFile = "    		height ~ " + eachWebElement.offsetHeight
												+ "px\n";
										break;
									case "width":
										writeAttributesToFile += "    		width ~ " + eachWebElement.offsetWidth
												+ "px\n";
										break;
									case "screenxyposition":
										var bodyRect=document.body.getBoundingClientRect();
										var eleRect=eachWebElement.getBoundingClientRect();
										var xLocation=Math.round(eleRect.left - bodyRect.left);
										var yLocation=Math.round(bodyRect.right - eleRect.right);
										var topLocation=Math.round(eleRect.top);
										if(!isNaN(positionTolerance)){
											var minVal=xLocation - positionTolerance;
											var maxVal=xLocation + positionTolerance;
											xLocation=minVal +" to " +  maxVal;
											
											minVal=yLocation - positionTolerance;
											maxVal=yLocation + positionTolerance;
											yLocation=minVal +" to " +  maxVal;
											
											minVal=topLocation - positionTolerance;
											maxVal=topLocation + positionTolerance;
											topLocation=minVal +" to " +  maxVal;
											
										}
										else{
											xLocation="~"+xLocation;
											yLocation="~"+yLocation;
											topLocation="~"+topLocation;											
										}
										
										writeAttributesToFile=writeAttributesToFile+"    		inside partly screen "+xLocation+"px left\n";
										writeAttributesToFile=writeAttributesToFile+"    		inside partly screen "+yLocation+"px right\n";			
										writeAttributesToFile=writeAttributesToFile+"    		inside partly screen "+topLocation+"px top\n";
										break;																		
									case "text":
										if (tagName.toUpperCase()=="SELECT") {
											elementText=eachWebElement.options[0].innerText;
											
										} else {
											elementText = getElementText(tagName,eachWebElement);
										}

										
										if (elementText != null && trim(elementText).length > 0
												&& !hasUnicode(elementText) && elementText.indexOf("\n")==-1) {
												
											if(trim(elementText).length > 100){													
													elementText=trim(elementText).substring(0,100);
											}
											if (matchExactTextFilter=="N") {
												writeAttributesToFile += "    		text contains  \""+ trim(elementText) + "\"\n";
											} else {
												writeAttributesToFile += "    		text is \"" + trim(elementText) + "\"\n";
											}
											
										} else if (elementText != null && trim(elementText).length > 0 
												 && elementText.indexOf("\n")!=-1 && trim(trim(elementText).split("\n"))[0].length > 0 && !hasUnicode(trim(trim(elementText).split("\n"))[0])) {
											try {
												if(trim(elementText).length > 100){
													elementText=trim(elementText).substring(0,100);
												}												
												elementText = trim(elementText).split("\n")[0];
												writeAttributesToFile += "    		text contains \""
														+ trim(elementText) + "\"\n";
											} catch (e) {
											}
										}
										break;
									

									}
								}

							}
							if (trim(writeAttributesToFile).length > 0) {
								mapOfLocatorAndAtributes[varName]=[mapOfLogicalNameAndLocator[varName],writeAttributesToFile];
								
							}
						}
					}
					
				
				
			}
			catch(e){
				continue;
			}
			
		}
	}
			
			return mapOfLocatorAndAtributes;

}

/*Function to search elements using unique attributes */

function calculateObjectNameAndLocatorBasedOnUniqueAttributes(element,uniqueLocatorSequence,rootLocator){
		var arrUniqueLocators=uniqueLocatorSequence.split(",");
		
  		var webObject=element;
		var rootElementXpath=rootLocator;
  		var uniqueAttrib="";
		var objLogicalName="";
  		var objLocator="";

		for(var index=0;index<arrUniqueLocators.length;index++){
				uniqueAttrib=webObject.getAttribute(arrUniqueLocators[index]);
				

				if(uniqueAttrib!=null && uniqueAttrib!=undefined && trim(uniqueAttrib).length > 0 && !hasUnicode(uniqueAttrib)  ){
					
					objLogicalName="_"+uniqueAttrib+"_";

					objLocator=objLocator + "@" + arrUniqueLocators[index]+  "=\""+uniqueAttrib+"\"";
	
				}

		}

		if(objLocator!=null && objLocator!=undefined && trim(objLocator).length > 0 && objLocator.indexOf("@")!=-1){				
				objLocator=rootElementXpath+"//"+webObject.tagName+"["+objLocator+"]";
		} 

		if(objLogicalName!=null && undefined!=objLogicalName && trim(objLogicalName).length > 0){
			
				return [ objLogicalName ,  objLocator ];   	
		}
  		else{
			return null;
		}
		
		

 }


/*This function returns true if text has unicode characters*/
function hasUnicode (str) {
	 
	 for (var i = 0; i < str.length; i++) {
		if (str.charCodeAt(i) > 127){			
			return true;
		}
	}  
	return false;  
}

/*Trims the leading and trailing spaces*/
function trim(s){
	if(s!=null || s!=undefined) {
		s= s.replace(/^\s*/,"").replace(/\s*$/, "");		
		return s;
	}
	else{
		
		return "";
	}
}

/* removes @xpath= like string from provided locator */
function getTopLocator(topLocator) {

		var part1 = topLocator.substring(0, topLocator.indexOf("="));
		var part2 = topLocator.substring(topLocator.indexOf("=") + 1, topLocator.length);
		if (part1.indexOf("@")==0) {
			part1 = part1.substring(1, part1.length);
		}

		if (part1.indexOf("xpath")==-1) {
			return "//*[@" + part1 + "=\"" + part2 + "\"]";
		} else {
			return part2;
		}
}
	
	
/*Checks if elements has any child elements.*/
function hasChildElements(element){
	
	if(element.childElementCount > 0 ){
		return true; 
	}
	else {
		return false;
	}
}


/*Returns element text*/
function getElementText(tagName,element){
	var txt="";
	if(element!=null && element != undefined) {
	
		if(tagName.toLowerCase()=="input"){			
			txt=element.value;
			
			if(txt==null || trim(txt).length == 0) {				
				txt="";
			}
		}
		else {
			txt=element.getAttribute("value");
			if(txt==null || trim(txt).length == 0) {
				txt=element.innerText;
				if(trim(txt).length == 0){
					txt="";
				}
			}
		}
	}
	
  
	return txt;
   
}