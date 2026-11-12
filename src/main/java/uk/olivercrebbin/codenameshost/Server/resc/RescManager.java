package uk.olivercrebbin.codenameshost.Server.resc;

import com.sun.net.httpserver.HttpExchange;
import uk.olivercrebbin.codenameshost.Server.ServerHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class RescManager extends ServerHandler.GeneralHandler {


    public RescManager() {
        super("");
    }

    @Override
    protected List<String> getFile(HttpExchange exchange) throws IOException {
        String location = Arrays.stream(exchange.getRequestURI().toString().split("/")).toList().getLast();
        System.out.println(location);
//        try {
//            //String location = "bootstrap.min.css";
//            Path filePath = Paths.get(RescManager.class.getResource(location).toURI());
//            List<String> response = Files.readAllLines(filePath);
//            return response;
//        }catch (Exception e){
//
//            List<String>  r = new ArrayList<>();
//            r.add("We had an error" + e.getMessage()+"\n"+location);
//            return r;
//        }
        InputStream inputStream = RescManager.class.getResourceAsStream(location);
        if (inputStream == null) {
            throw new IOException("Resource not found: " + location);
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.toList());
        }
    }
}
