package plugin.com.fisglobal.ktt.business.keywords.utility.functions;

import java.io.File;
import java.io.IOException;
import com.google.common.io.Files;

public class FileUtility {
		
	public static void move(File sourceLocation, File targetLocation) throws IOException {
		
	    if (sourceLocation.isDirectory()) {	    	
	    	moveDirectory(sourceLocation, targetLocation);
	    	sourceLocation.delete();
	        
	    } else {
	    	moveFile(sourceLocation, targetLocation);
	        
	    }
	}
	
	private static  void moveDirectory(File source, File target) throws IOException {
	    if (!target.exists()) {
	        target.mkdir();
	    }
	
	    for (String f : source.list()) {
	    	move(new File(source, f), new File(target, f));
	    }
	}
	
	private static void moveFile(File source, File target) throws IOException {        
		Files.move(source, target);
	}
}

