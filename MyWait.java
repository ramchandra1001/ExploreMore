package com.sungard.ktt.business.keywords;

import org.apache.log4j.Logger;

import com.sungard.ktt.view.config.KTTGuiConstants.ScriptStatus;
import com.sungard.ktt.web.util.SAFALUserSession;

/**
 * @author Dnyaneshwar.Daphal
 *
 */
public class MyWait
{
	private static final Logger logger = Logger.getLogger(MyWait.class.getName());

	public void waitFor(long milliSeconds, String userName)
	{
		logger.info("inside method waitFor");
		try {
			synchronized(this) {
				Thread.sleep(milliSeconds);
				if(SAFALUserSession.getSciptStatus(userName) == ScriptStatus.TO_BE_STOPPED) {
					return;
				}
			} 
		}catch(InterruptedException e) {
			//logger.error("InterruptedException", e);
		}
	}
}
