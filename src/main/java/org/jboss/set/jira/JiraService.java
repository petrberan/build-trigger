package org.jboss.set.jira;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.quarkus.runtime.LaunchMode;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;
import org.jboss.set.client.JiraRestClient;
import org.jboss.set.model.json.BuildInfo;
import org.jboss.set.model.json.Stream;

import java.util.Arrays;

@ApplicationScoped
public class JiraService {

    Logger logger = Logger.getLogger(JiraService.class);

    @ConfigProperty(name = "jira-key")
    String jiraKey;

    @Inject
    @RestClient
    JiraRestClient jiraRestClient;

    @Inject
    LaunchMode mode;

    public void sendIssueBatch(BuildInfo buildInfo, String email) {
        ObjectNode issueBatch = createIssueBatch(buildInfo, email);
        if (!mode.isDevOrTest()) {
            try {
                jiraRestClient.sendIssue("Bearer " + jiraKey, issueBatch);
                logger.infof("Issue sent to Jira successfully.");
            } catch (Exception e) {
                logger.errorf("Failed to send issue to Jira: " + e.getMessage());
                logger.errorf(Arrays.toString(e.getStackTrace()));
            }
        } else {
            logger.infof("Dev or Test mode detected, issue hasn't been created, logging the payload:");
            logger.infof(issueBatch.toPrettyString());
        }

    }

    private ObjectNode createIssueBatch(BuildInfo buildInfo, String email) {
        String user = getUser(email);

        // Creates a batch of issues. This is necessary when build has multiple streams
        // to make sure we create all issues at once and not just some.
        JsonNodeFactory jsonNodeFactory = JsonNodeFactory.instance;
        ObjectNode payload = jsonNodeFactory.objectNode();
        ArrayNode issues = payload.putArray("issueUpdates");

        for (String stream : buildInfo.getStreams()) {
            ObjectNode issue = issues.addObject();
            ObjectNode fields = issue.putObject("fields");

            String[] jiraParams = Stream.getJiraParametersByBackEnd(stream);

            // All information below can be checked by going to https://issues.redhat.com/rest/api/2/issue/<key>
            // Where <key> is some Component Upgrade issue key, such as JBEAP-27711

            fields.putObject("customfield_12311240").put("id", jiraParams[1]); // customfield_12311240 is "Target Release"
            fields.putObject("issuetype").put("id", "12"); // Issue type 12 is "Component Upgrade"
            fields.putObject("priority").put("id", "3"); // Priority 3 means "Major"
            fields.putObject("project").put("id", "12313422"); // Project 12313422 is JBEAP

            String gitRepo = buildInfo.getGitRepo();
            if (buildInfo.getGitRepo().endsWith("/")) {
                gitRepo = gitRepo.substring(0, gitRepo.length() - 1);
            }
            fields.put("summary", jiraParams[0] +
                    " Upgrade " +
                    gitRepo.substring(gitRepo.lastIndexOf('/') + 1) +
                    " to version " +
                    buildInfo.getProjectVersion());

            String description = "Tag: " + buildInfo.getTag() + "\n" +
                    "Commit: " + buildInfo.getCommitSha() + "\n" +
                    "Version: " + buildInfo.getProjectVersion() + "\n\n" +
                    "cc [~eleandro]/[~dkreling]\n\n";

            if (user != null && !user.isBlank()) {
                fields.putObject("assignee").put("name", user);
                fields.put("description", "This is an automated issue created by Build Trigger on behalf of [~" + user + "]\n\n" +
                        description + "Please make sure to attach all JBEAP issues resolved by this component upgrade [~" + user + "]!");
            } else {
                fields.put("description", "This is an automated issue created by Build Trigger\n\n" +
                        description + "Please make sure to attach all JBEAP issues resolved by this component upgrade!");
            }
        }
        return payload;
    }

    private String getUser(String user) {
        if (user == null || user.isBlank() || user.equals("Email not provided in the token")) {
            return null;
        }
        JsonNode userJson = jiraRestClient.getUserFromEmail("Bearer " + jiraKey, user.substring(0, user.lastIndexOf('@')));
        return userJson != null ? userJson.get("name").asText() : null;
    }

}
