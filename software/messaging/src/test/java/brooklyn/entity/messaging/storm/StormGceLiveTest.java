package brooklyn.entity.messaging.storm;

import brooklyn.config.BrooklynProperties;
import brooklyn.util.collections.MutableMap;
import org.testng.annotations.Test;

import java.util.Map;

@Test(groups="Live")
public class StormGceLiveTest extends AbstractCloudLiveTest {

    private static final String NAMED_LOCATION = "gce-europe-west1";
    private static final String LOCATION_ID = "gce-europe-west1-a";
    private static final String URI = "https://www.googleapis.com/compute/v1beta15/projects/google/global/images/centos-6-v20130325";
    private static final String IMAGE_ID = "centos-6-v20130325";

    private final BrooklynProperties brooklynProperties = BrooklynProperties.Factory.newDefault();

    @Override
    public String getLocation() {
        return NAMED_LOCATION;
    }

    @Override
    public Map<String, ?> getFlags() {
        return MutableMap.of(
                "identity", getIdentity(),
                "credential", getCredential(),
                "locationId", LOCATION_ID,
                "imageId", IMAGE_ID,
                "uri", URI + IMAGE_ID,
                "groupId", "storm-test",
                "stopIptables", "true"
        );
    }

    private String getIdentity() {
        return brooklynProperties.getFirst("brooklyn.location.named." + NAMED_LOCATION + ".identity");
    }

    private String getCredential() {
        return brooklynProperties.getFirst("brooklyn.location.named." + NAMED_LOCATION + ".credential");
    }
}
