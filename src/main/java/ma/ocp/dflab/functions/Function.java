/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package ma.ocp.dflab.functions;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.logging.Level;

/**
 * Azure Functions with HTTP Trigger.
 */
public class Function {

    BpConnector bpConnector = new BpConnector();

    /**
     * This function listens at endpoint "/api/GbpStatements". to invoke it using "curl" command in bash:
     * 1. curl -d "{ account:'' , pwd:''}" {your host}/api/GbpStatements
     */
    @FunctionName("GbpStatements")
    public HttpResponseMessage run(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.POST},
                authLevel = AuthorizationLevel.ANONYMOUS)
                HttpRequestMessage<Optional<UserAccount>> request,
            final ExecutionContext context) {
        context.getLogger().info("Java HTTP trigger processed a request.");

        // Parse query parameter
        final UserAccount user = request.getBody().orElse(null);

        if (user == null) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Please pass a valid request body").build();
        } else {
            String response ="";
            try {
                bpConnector.login(user.getPwd(),user.getAccount());
                response = bpConnector.getStatements();
                return request.createResponseBuilder(HttpStatus.OK).body(response).build();
            } catch (URISyntaxException | IOException e) {
                context.getLogger().log(Level.SEVERE, "error getting statements", e);
            }
            return request.createResponseBuilder(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
    }



    /**
     * This function listens at endpoint "/api/HttpTriggerJavaVersion".
     * It can be used to verify the Java home and java version currently in use in your Azure function
     */
    @FunctionName("HttpTriggerJavaVersion")
    public static HttpResponseMessage HttpTriggerJavaVersion(
        @HttpTrigger(
            name = "req",
            methods = {HttpMethod.GET, HttpMethod.POST},
            authLevel = AuthorizationLevel.ANONYMOUS)
            HttpRequestMessage<Optional<String>> request,
        final ExecutionContext context
    ) {
        context.getLogger().info("Java HTTP trigger processed a request.");
        final String javaVersion = getJavaVersion();
        context.getLogger().info("Function - HttpTriggerJavaVersion" + javaVersion);
        return request.createResponseBuilder(HttpStatus.OK).body("HttpTriggerJavaVersion").build();
    }

    public static String getJavaVersion() {
        return String.join(" - ", System.getProperty("java.home"), System.getProperty("java.version"));
    }
}
