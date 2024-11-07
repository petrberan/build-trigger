package org.jboss.set.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/rest/api/2")
@RegisterRestClient(baseUri = "https://issues.redhat.com")
public interface JiraRestClient {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/issue/bulk")
    void sendIssue(@HeaderParam("Authorization") String authorization, ObjectNode issue);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/user")
    JsonNode getUserFromEmail(@HeaderParam("Authorization") String authorization, @QueryParam("key") String user);
}
