package org.jboss.set;

import org.jboss.set.model.json.BuildInfo;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import org.jboss.set.model.json.BuildJMSTriggerPayload;

@ApplicationScoped
@Alternative
@Priority(1)
public class MockBuildTrigger extends BuildTrigger {

    @Override
    public void triggerBuild(BuildInfo buildInfo, String email) {
        BuildJMSTriggerPayload payloadMessage = BuildJMSTriggerPayload.from(buildInfo, email);
        // test, do nothing
    }
}
