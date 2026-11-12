package uk.olivercrebbin.codenameshost.Server;

import java.io.*;
import java.net.InetSocketAddress;

import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import uk.olivercrebbin.codenameshost.Game.Card;
import uk.olivercrebbin.codenameshost.Game.CurrentPlayer;
import uk.olivercrebbin.codenameshost.Game.GameState;
import uk.olivercrebbin.codenameshost.HomeController;
import uk.olivercrebbin.codenameshost.Server.resc.ImageHandler;
import uk.olivercrebbin.codenameshost.Server.resc.RescManager;

import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import static uk.olivercrebbin.codenameshost.Server.SessionManager.padLeftZeros;

public class ServerHandler {
    // Main Method
    public enum State{
        RUNNING,
        SERVER_NO_START,
        STOPPED,
        NOT_STARTED;
    }
    private static SessionManager sessionManager = new SessionManager();
    private static List<String> messages = new ArrayList<>();
    private static UpdateServer updateServer;
    public static List<String> getMessages() {
        return messages;
    }
    public static String getMostRecentMessage(){
        return messages.getLast();
    }

    public static void setCode(String code) {
        ServerHandler.code = code;
        HomeController.controller.joinCode.setText("Join Code: "+code);

    }

    private static String code;

    public static  HttpServer server;
    public static State state= State.NOT_STARTED;
    public static GameState gameState;
    private static int port = 8000;
    // Keep track of all connected client output streams
    private static final CopyOnWriteArrayList<OutputStream> clients = new CopyOnWriteArrayList<>();
    public static State start(GameState gameState)
    {
        Random rgen = new Random();
        code = padLeftZeros("" + rgen.nextInt(0, 100000), 5);
        if(server == null) {
            try {
                ServerHandler.gameState = gameState;

                // Try to find an available port
                int attemptPort = port;
                while (true) {
                    try {
                        // Create an HttpServer instance
                        server = HttpServer.create(new InetSocketAddress(attemptPort), 0);
                        port = attemptPort; // Update the port to the one that worked
                        break; // Exit loop if successful
                    } catch (IOException e) {
                        attemptPort++;
                        if (attemptPort > 65535) { // Port numbers range from 0 to 65535
                            throw e; // Re-throw if no ports are available
                        }
                    }
                }

                // Create a context for a specific path and set the handler
                createContect("/", new GeneralHandler("home.html"));
                createContect("/resc/", new RescManager());
                createContect("/request", new RequestManager());
                createContect("/image/",new ImageHandler());


                // Start the server
                server.setExecutor(null); // Use the default executor
                server.start();
                updateServer = new UpdateServer();
                updateServer.start(8080);

                System.out.println("Server is running on port " + port);
                state = State.RUNNING;
                setCode(code);
            } catch (IOException e) {

                state = State.SERVER_NO_START;
                messages.add(e.getMessage());
                System.out.println("Error starting the server: " + e.getMessage());
            }
        }
        return state;

    }
    public static void closeServer(){
     if(server !=null){
        server.stop(0);
        server = null;
        state = State.STOPPED;
         try {
             updateServer.stop();
             HomeController.controller.joinCode.setText("");
         } catch (IOException e) {
             throw new RuntimeException(e);
         }
     }
    }

    public static State getState() {
        return state;
    }

    public static int getPort() {
        return server.getAddress().getPort();

    }
    public static String getAdress(){
        return server.getAddress().getHostName();

    }

    public static void createContect(String path, HttpHandler handler){
        server.createContext(path,handler);
    }

    // Define a custom HttpHandle
    public static class GeneralHandler implements HttpHandler{
        private String resource;
        public GeneralHandler( String resource){
            super();
            this.resource = resource;


        }
        protected List<String> getFile(HttpExchange exchange) throws IOException {
            InputStream inputStream = ServerHandler.class.getResourceAsStream(resource);
            if (inputStream == null) {
                throw new IOException("Resource not found: " + resource);
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                return reader.lines().collect(Collectors.toList());
            }
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException
        {
            // Handle the request
            try {


            List<String> response = null;
                response = getFile(exchange);
            if(response == null){
                    throw new IOException("Error Creating File");
                }
                exchange.sendResponseHeaders(200, response.stream().collect(Collectors.joining("\n")).getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.stream().collect(Collectors.joining("\n")).getBytes());
                os.close();
            } catch (Exception e) {
                System.out.println(e);
                String response = "We had an error loading the page" ;
                exchange.sendResponseHeaders(500, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        }
    }
    static class RequestManager extends GeneralHandler{

        public RequestManager() {
            super("");
        }

        @Override
        protected List<String> getFile(HttpExchange exchange) throws IOException {
            String requestBody = new InputStreamReader(exchange.getRequestBody()).readAllAsString();
            String resp = "";
            System.out.println(requestBody);

            Gson gson = new Gson();
            try {
                Request request = gson.fromJson(requestBody, Request.class);
                resp = request.toString();
                exchange.getResponseHeaders().put("Content-Type", Collections.singletonList("application/json"));
                resp = handleRequest(request);

            } catch (JsonSyntaxException e) {
                System.out.println(e);
                return null;
            }
            List<String> list =new ArrayList<>();
            list.add(resp);
            return list;
        }
    }

    public static String getJoinLink(){
        try {
            // Get the local IP address that can be accessed on the network
            java.net.InetAddress localAddress = java.net.InetAddress.getLocalHost();
            String ip = localAddress.getHostAddress();
            int port = server.getAddress().getPort();
            return "http://" + ip + ":" + port;
        } catch (Exception e) {
            // Fallback to localhost if network address cannot be determined
            int port = server.getAddress().getPort();
            return "http://localhost:" + port;
        }
    }

    public static class Request{
        public Request(){};
        public String requestType;
        public String requestName;
        public String requestBody;
        public String requestCode;
        public String requestID;

        public void setRequestBody(String requestBody) {
            this.requestBody = requestBody;
        }
        public void setRequestCode(String requestCode) {
            this.requestCode = requestCode;
        }

        public void setRequestID(String requestID) {
            this.requestID = requestID;
        }

        public void setRequestName(String requestName) {
            this.requestName = requestName;
        }

        public void setRequestType(String requestType) {
            this.requestType = requestType;
        }

        public String getRequestBody() {
            return requestBody;
        }

        public String getRequestCode() {
            return requestCode;
        }

        public String getRequestID() {
            return requestID;
        }

        public String getRequestName() {
            return requestName;
        }

        public String getRequestType() {
            return requestType;
        }

        @Override
        public String toString() {
            return "Request{" +
                    "requestType='" + requestType + '\'' +
                    ", requestName='" + requestName + '\'' +
                    ", requestBody='" + requestBody + '\'' +
                    ", requestCode='" + requestCode + '\'' +
                    ", requestID='" + requestID + '\'' +
                    '}';
        }
    }
    public static class Response{
        public boolean success;
        public String responseType;
        public String responseContent;
        public ResponseCard[][] cards;
        public String currentTurn;
    }
    public static class ResponseCard{
        public String text;
        public boolean flipped;
        public String role;
    }
    public static String handleRequest(Request request){
        Response response;
        try {

        switch (request.getRequestType()) {
            case "join":
                if (validateCode(request.getRequestCode())) {
                    response = new Response();
                    String id = sessionManager.addUser(request.getRequestName(), request.getRequestCode(), request.getRequestBody());
                    response.responseContent = id;
                    response.success = true;
                    response.responseType = "SuccessfulJoinId";
                } else {
                    throw new RuntimeException("Error: Code incorrect");
                }

                break;
            case "update":
                response = manageUpdate(request);
                break;
            case "getUpdatePort":
                response = new Response();
                response.responseContent = updateServer.getPort() +"";
                response.success = true;
                response.responseType="UpdateSocketLink";
                break;
            case "NextTurn":
                if(sessionManager.validateSession(request.getRequestName(),request.getRequestCode(),request.getRequestID())) {
                    response = new Response();
                    if(gameState.isGameOver()){
                        throw  new RuntimeException("Error: Game is Over");
                    }
                    if(sessionManager.getSession(request.getRequestID()).getRole() == SessionManager.SessionRole.OBSERVER){
                        throw  new RuntimeException("Error: observers can edit the game");
                    }
                    if(sessionManager.getSession(request.getRequestID()).getRole() == SessionManager.SessionRole.BlUE_TEAM && gameState.getCurrentPlayer().getCurrent() == CurrentPlayer.Player.RedTeam){
                        throw  new RuntimeException("Error: Blue Team Members cant act for Red team");
                    }
                    if(sessionManager.getSession(request.getRequestID()).getRole() == SessionManager.SessionRole.RED_TEAM && gameState.getCurrentPlayer().getCurrent() == CurrentPlayer.Player.BlueTeam){
                        throw  new RuntimeException("Error: Red Team Members cant act on Blue team");
                    }

                    gameState.nextTeam();
                    response.success = true;
                    response.responseType = "TurnChange";
                }else{

                        throw  new RuntimeException("Error: Incorrect Login Details");

                }
                    break;
            case "Click":
                if(sessionManager.validateSession(request.getRequestName(),request.getRequestCode(),request.getRequestID())) {
                    response = new Response();
                    if(gameState.isGameOver()){
                        throw  new RuntimeException("Error: Game is Over");
                    }
                    if(sessionManager.getSession(request.getRequestID()).getRole() == SessionManager.SessionRole.OBSERVER){
                        throw  new RuntimeException("Error: observers can edit the game");
                    }
                    if(sessionManager.getSession(request.getRequestID()).getRole() == SessionManager.SessionRole.BlUE_TEAM && gameState.getCurrentPlayer().getCurrent() == CurrentPlayer.Player.RedTeam){
                        throw  new RuntimeException("Error: Blue Team Members cant act for Red team");
                    }
                    if(sessionManager.getSession(request.getRequestID()).getRole() == SessionManager.SessionRole.RED_TEAM && gameState.getCurrentPlayer().getCurrent() == CurrentPlayer.Player.BlueTeam){
                        throw  new RuntimeException("Error: Red Team Members cant act on Blue team");
                    }
                    String[] body = request.getRequestBody().split(",");
                    int x = Integer.parseInt(body[0]);
                    int y = Integer.parseInt(body[1]);
                    gameState.handleClick(x,y);
                    response.success = true;
                    response.responseType = "Click";
                }else{

                    throw  new RuntimeException("Error: Incorrect Login Details");

                }
                break;
            default:
                throw new RuntimeException("Error: Incorrect Type");

        }

        } catch (Exception e) {
            System.err.println(e);
            System.err.println(e.getStackTrace());
            response = new Response();
            response.success = false;
            response.responseType = "ERROR_MESSAGE";
            response.responseContent = e.getMessage();
        }

        Gson gson = new Gson();
        return gson.toJson(response);
    }
    public static boolean validateCode(String Givencode){
        return Givencode.equals(code);
    }

    public static String getCode() {
        return code;
    }

    private static ServerHandler.Response manageUpdate(ServerHandler.Request request) {

    Response response = new Response();
        if(sessionManager.validateSession(request.getRequestName(),request.getRequestCode(),request.getRequestID())) {
            Card[][] cards = gameState.getCards();
            response.cards = new ResponseCard[cards.length][cards[0].length];
            for (int x = 0; x < cards.length; x++)
                for (int y = 0; y < cards[0].length; y++) {
                    ResponseCard card = new ResponseCard();
                    card.text = cards[x][y].getOriginalText();
                    card.flipped = cards[x][y].isFlipped();
                    card.role = cards[x][y].getRole().toString();
                    response.cards[x][y] = card;
                }
            response.currentTurn = gameState.getCurrentPlayer().getCurrent().getName();
            response.responseType = "update";
            response.success = true;
    } else {
        throw  new RuntimeException("Error: Incorrect Login Details");
    }
return response;
}

public static void trigger(){
        try {
            updateServer.broadcastData("{\"type\":\"fullUpdate\"}");
        } catch (NullPointerException e) {

        }
}
    public static void triggerNewTeam(){
        try {
            updateServer.broadcastData("{\"type\":\"newTeam\",\"team\":\""+gameState.getCurrentPlayer().getCurrent().getName()+"\"}");
        } catch (NullPointerException e) {

        }
    }

public static void cardFlip(String coords){
        try {
            updateServer.broadcastData("{\"type\":\"CARDFLIP\",\"coords\":\"" + coords + "\"}");
        } catch (NullPointerException e) {
            System.out.println("FLIP");
        }
}

public static void triggerEndGame(){
    try {
        String team = gameState.getEndGameInfo().winnerTeam();
        String reason = gameState.getEndGameInfo().winReason();
        updateServer.broadcastData("{\"type\":\"ENDGAME\",\"winTeam\":\"" + team + "\",\"reason\":\"" + reason + "\"}");
    } catch (NullPointerException e) {
        System.out.println("ENDGAME");
    }
}
static SessionManager getSessionManager() {
        return sessionManager;
    }
}






