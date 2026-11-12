
package uk.olivercrebbin.codenameshost.Server.resc;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;

public class ImageHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String reference = Arrays.stream(exchange.getRequestURI().toString().split("/")).toList().getLast();
        System.out.println("Requested image: " + reference);

        // Load image as stream from classpath
        InputStream inputStream = ImageHandler.class.getResourceAsStream("/" + reference);
        if (inputStream == null) {
            inputStream = ImageHandler.class.getResourceAsStream(reference);
            if (inputStream == null) {
                exchange.sendResponseHeaders(404, -1);
                return;
            }
        }

        // Read all bytes from input stream
        byte[] response = inputStream.readAllBytes();
        inputStream.close();

        // Send response
        exchange.getResponseHeaders().put("Content-Type", Collections.singletonList("image/png"));
        exchange.sendResponseHeaders(200, response.length);
        OutputStream os = exchange.getResponseBody();
        os.write(response);
        os.close();
    }
}