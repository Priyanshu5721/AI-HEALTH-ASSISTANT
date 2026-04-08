package com.mycompany.health.backend.resources;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.*;
import java.net.*;

@Path("/health")
public class JakartaEE8Resource {

    @POST
    @Path("/predict")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response predictDisease(String input) {

        try {
            URL url = new URL("http://localhost:5000/predict");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            OutputStream os = conn.getOutputStream();
            os.write(input.getBytes());
            os.flush();

            BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream())
            );

            String output = "";
            String line;
            while ((line = br.readLine()) != null) {
                output += line;
            }

            return Response.ok(output).build();

        } catch (Exception e) {
            return Response.status(500).entity(e.getMessage()).build();
        }
    }
}