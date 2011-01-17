
project.properties['svn.username'] = "";
project.properties['svn.password'] = "";

println "### user.dir = " + System.getProperty("user.dir");

def name = "barchart-google-code";
def server = settings.servers.find{ it.id.equals(name) }
println "### server = " + server;

