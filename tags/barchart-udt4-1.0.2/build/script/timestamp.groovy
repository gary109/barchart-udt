// set current time property in the running maven project context 

import java.util.Date;
import java.text.MessageFormat;

def date = new Date();
def	buildtime = date.getTime();
def	timestamp = MessageFormat.format("{0,date,yyyy-MM-dd_HH-mm-ss}", date);

project.properties['buildtime'] = Long.toString(buildtime);
project.properties['timestamp'] = timestamp;
