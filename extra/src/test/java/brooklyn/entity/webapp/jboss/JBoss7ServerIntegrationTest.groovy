package brooklyn.entity.webapp.jboss

import static brooklyn.test.TestUtils.*
import static org.testng.Assert.*

import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

import brooklyn.entity.Application
import brooklyn.entity.webapp.JavaWebApp
import brooklyn.location.Location
import brooklyn.location.basic.LocalhostMachineProvisioningLocation
import brooklyn.test.entity.TestApplication

class JBoss7ServerIntegrationTest {

    private static final int DEFAULT_HTTP_PORT = 43210
    
    private Application app
    private Location testLocation

    @BeforeMethod(groups="Integration")
    public void setup() {
        app = new TestApplication();
        testLocation = new LocalhostMachineProvisioningLocation(name:'london', count:2)
    }
    
    @Test(groups="Integration")
    public void canStartupAndShutdown() {
        JBoss7Server jb = new JBoss7Server(owner:app, httpPort: DEFAULT_HTTP_PORT);
        jb.setConfig(JBoss7Server.SUGGESTED_JMX_HOST, "127.0.0.1")
        jb.start([ testLocation ])
        executeUntilSucceedsWithFinallyBlock ([:], {
            assertTrue jb.getAttribute(JavaWebApp.SERVICE_UP)
        }, {
            jb.stop()
        })
    }
}
