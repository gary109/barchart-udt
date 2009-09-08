// set current time property in the running maven project context 

import java.util.Date;
import java.text.MessageFormat;
								
def	timestamp = MessageFormat.format("{0,date,yyyy-MM-dd_HH-mm-ss}", new Date());

project.properties['timestamp'] = timestamp;
