package com.fndemo.functions;

import java.sql.*;
import java.util.*;
import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.*;
import net.sf.json.*;

/**
 * Azure Functions with HTTP Trigger.
 */
public class Fn2mysql {
    /**
     * This function listens at endpoint "/api/Fn2mysql". Two ways to invoke it
     * using "curl" command in bash: 1. curl -d "HTTP Body" {your host}/api/Fn2mysql
     * 2. curl {your host}/api/Fn2mysql?name=HTTP%20Query
     */
    @FunctionName("Fn2mysql")
    public HttpResponseMessage run(@HttpTrigger(name = "req", methods = { HttpMethod.GET,
            HttpMethod.POST }, authLevel = AuthorizationLevel.FUNCTION) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Java HTTP trigger processed a request.");

        // Parse query parameter
        String query = request.getQueryParameters().get("name");
        String name = request.getBody().orElse(query);
        
        // ASA output message will add "[]" to json format data, so need to delete it.
        String msgdata = name.replace("[", "").replace("]","");
        
        // Parse json format message data, and get the values
        JSONObject jsonObject = JSONObject.fromObject(msgdata);
        String envtemp = jsonObject.get("envtemp").toString();
        String envhumity = jsonObject.get("envhumity").toString();
        
        try {
            // check that the driver is installed
            try {
                Class.forName("com.mysql.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                throw new ClassNotFoundException("MySQL JDBC driver NOT detected in library path.", e);
            }
            System.out.println("MySQL JDBC driver detected in library path.");

            Connection connection = null;

            // Initialize connection object
            try {
                String url = System.getenv("mysql_connection");
                String mysql_user = System.getenv("mysql_user");
                String mysql_pwd = System.getenv("mysql_pwd");

                // get connection
                connection = DriverManager.getConnection(url, mysql_user, mysql_pwd);
            } catch (SQLException e) {
                throw new SQLException("Failed to create connection to database.", e);
            }

            if (connection != null) {
                System.out.println("Successfully created connection to database.");

                // Perform some SQL queries over the connection.
                try {
                    // Insert some data into table.
                    int nRowsInserted = 0;
                    PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO iottab (envtemp, envhumity) VALUES (?, ?);");
                    preparedStatement.setString(1, envtemp);
                    preparedStatement.setString(2, envhumity);
                    nRowsInserted += preparedStatement.executeUpdate();

                    System.out.println(String.format("Inserted %d row(s) of data.", nRowsInserted));

                    // NOTE No need to commit all changes to database, as auto-commit is enabled by default.

                } catch (SQLException e) {
                    throw new SQLException("Encountered an error when executing given sql statement.", e);
                }
            } else {
                System.out.println("Failed to create connection to database.");
            }
            
        } catch (Exception e) {
            System.out.println(e);
        }
        
        if (name == null) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Please confirm message is not null.").build();
        } else {
            return request.createResponseBuilder(HttpStatus.OK).body("Data has been imported to function app.").build();
        }
    }
}