
package uk.olivercrebbin.codenameshost.Server;

import com.google.gson.Gson;

import java.net.*;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.logging.Level;

public class UpdateServer {
    private static final Logger logger = Logger.getLogger(UpdateServer.class.getName());

    private ServerSocket serverSocket;
    private final AtomicInteger clientIdCounter = new AtomicInteger(0);
    private final ConcurrentHashMap<Integer, ClientHandler> clients = new ConcurrentHashMap<>();

    private volatile boolean running = false;
    private Thread serverThread;
    private int port;

    public int getPort() {
        return port;
    }

    /**
     * Starts the update server on the specified port
     */
    public void start(int port) throws IOException {
        if (running) {
            logger.warning("Server is already running");
            return;
        }
        this.port = port;

        serverSocket = new ServerSocket(port);
        running = true;

        serverThread = new Thread(() -> {
            logger.info("Update server started on port " + port);
            while (running) {
                try {
                   System.out.println("THREAD!");
                    Socket clientSocket = serverSocket.accept();
                    int clientId = clientIdCounter.incrementAndGet();

                    ClientHandler handler = new ClientHandler(clientSocket, clientId);
                    clients.put(clientId, handler);

                    Thread clientThread = new Thread(handler);
                    clientThread.setDaemon(true);
                    clientThread.start();

                    logger.info("New client connected with ID: " + clientId);
                } catch (IOException e) {
                    if (running) {
                        logger.log(Level.SEVERE, "Error accepting client connection", e);
                    }
                }
            }
        });

        serverThread.start();
    }

    /**
     * Stops the update server
     */
    public void stop() throws IOException {
        if (!running) {
            logger.warning("Server is not running");
            return;
        }

        running = false;

        // Close all client connections
        for (ClientHandler handler : clients.values()) {
            handler.close();
        }
        clients.clear();

        if (serverSocket != null && !serverSocket.isClosed()) {
            serverSocket.close();
        }

        logger.info("Update server stopped");
    }

    /**
     * Sends data to a specific client
     */
    public boolean sendDataToClient(int clientId, String data) {
        ClientHandler handler = clients.get(clientId);
        if (handler != null && handler.isConnected()) {
            return handler.sendData(data);
        }
        return false;
    }

    /**
     * Sends data to all connected clients
     */
    public void broadcastData(String data) {
        clients.values().removeIf(handler -> {
            if (!handler.isConnected()) {
                return true; // Remove disconnected clients
            }
            if(handler.isVerified()) {
                handler.sendData(data);
            }
            return false;
        });
    }

    /**
     * Gets the number of currently connected clients
     */
    public int getConnectedClientsCount() {
        // Clean up disconnected clients and count connected ones
        clients.values().removeIf(handler -> !handler.isConnected());
        return clients.size();
    }

    /**
     * Removes a disconnected client from the clients map
     */
    private void removeClient(int clientId) {
        clients.remove(clientId);
        logger.info("Client " + clientId + " removed from server");
    }

    /**
     * Handler for individual client connections
     */
    private class ClientHandler implements Runnable {
        private final Socket clientSocket;
        private final int clientId;
        private InputStream input;
        private OutputStream output;
        private volatile boolean connected = false;
        private boolean isWebSocket = false;
        private boolean verified = false;

        public boolean isVerified() {
            return verified;
        }

        public ClientHandler(Socket socket, int clientId) {
            this.clientSocket = socket;
            this.clientId = clientId;
        }

        @Override
        public void run() {
            try {
                input = clientSocket.getInputStream();
                output = clientSocket.getOutputStream();
                connected = true;

                // Try to establish WebSocket connection
                if (performWebSocketHandshake()) {
                    isWebSocket = true;
                    logger.info("WebSocket connection established with client " + clientId);

                    // Send welcome message

                    // Listen for messages from client
                    handleWebSocketCommunication();
                } else {
                    // Handle as regular HTTP request
                    handleHttpRequest();
                }
            } catch (IOException e) {
                if (connected && !(e instanceof SocketException && e.getMessage().contains("Connection reset"))) {
                    logger.log(Level.WARNING, "Client " + clientId + " connection error", e);
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Unexpected error with client " + clientId, e);
            } finally {
                close();
            }
        }

        /**
         * Performs WebSocket handshake according to RFC 6455
         */
        private boolean performWebSocketHandshake() throws IOException {
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            String line;
            Map<String, String> headers = new HashMap<>();

            // Read HTTP request line
            String requestLine = reader.readLine();
            if (requestLine == null || !requestLine.startsWith("GET")) {
                return false;
            }

            // Read headers
            while ((line = reader.readLine()) != null && !line.isEmpty()) {
                int colonIndex = line.indexOf(":");
                if (colonIndex > 0) {
                    // FIX: Convert header name to lowercase for case-insensitive lookup
                    String headerName = line.substring(0, colonIndex).trim().toLowerCase();
                    String headerValue = line.substring(colonIndex + 1).trim();
                    headers.put(headerName, headerValue);
                }
            }

            // FIX: Look up headers using lowercase keys
            String upgradeHeader = headers.get("upgrade");
            String connectionHeader = headers.get("connection");
            String secWebSocketKey = headers.get("sec-websocket-key");

            if (upgradeHeader == null || !upgradeHeader.equalsIgnoreCase("websocket") ||
                    connectionHeader == null || !connectionHeader.toLowerCase().contains("upgrade") ||
                    secWebSocketKey == null) {

                // Helpful Debugging: Print what headers we actually got if handshake fails
                logger.warning("Handshake failed. Headers received: " + headers.keySet());
                return false;
            }

            // Generate WebSocket accept key
            String magicString = secWebSocketKey + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
            String sha1Hash = "";
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-1");
                byte[] digest = md.digest(magicString.getBytes());
                sha1Hash = Base64.getEncoder().encodeToString(digest);
            } catch (NoSuchAlgorithmException e) {
                throw new IOException("SHA-1 algorithm not available", e);
            }

            // Send WebSocket handshake response
            String response = "HTTP/1.1 101 Switching Protocols\r\n" +
                    "Upgrade: websocket\r\n" +
                    "Connection: Upgrade\r\n" +
                    "Sec-WebSocket-Accept: " + sha1Hash + "\r\n\r\n";

            output.write(response.getBytes());
            output.flush();

            return true;
        }

        /**
         * Handles WebSocket communication
         */
        private void handleWebSocketCommunication() throws IOException {
            byte[] buffer = new byte[1024];
            while (connected) {
                int bytesRead = input.read(buffer);
                if (bytesRead == -1) {
                    break; // Client disconnected
                }

                if (bytesRead > 0) {
                    // Parse WebSocket frame (simplified for text frames only)
                    byte[] payload = parseWebSocketFrame(buffer, bytesRead);
                    if (payload != null) {
                        String message = new String(payload, "UTF-8");
                        logger.info("Received from client " + clientId + ": " + message);
                        Gson gson = new Gson();
                        ServerHandler.Request r = gson.fromJson(message,ServerHandler.Request.class);
                       if(ServerHandler.getSessionManager().validateSession(r.getRequestName(),r.getRequestCode(),r.getRequestID())){
                           verified = true;
                       }
                    }
                }
            }
        }

        /**
         * Parses a WebSocket frame (simplified implementation)
         */
        private byte[] parseWebSocketFrame(byte[] buffer, int length) {
            if (length < 2) return null;

            byte opcode = (byte) (buffer[0] & 0x0F);
            boolean isMasked = (buffer[1] & 0x80) != 0;

            // Only handle text frames (opcode 1)
            if (opcode != 1) return null;

            int payloadLength = buffer[1] & 0x7F;
            int offset = 2;

            if (payloadLength == 126) {
                if (length < 4) return null;
                payloadLength = ((buffer[2] & 0xFF) << 8) | (buffer[3] & 0xFF);
                offset = 4;
            } else if (payloadLength == 127) {
                if (length < 10) return null;
                payloadLength = (int) (((long)(buffer[2] & 0xFF) << 56) |
                        ((long)(buffer[3] & 0xFF) << 48) |
                        ((long)(buffer[4] & 0xFF) << 40) |
                        ((long)(buffer[5] & 0xFF) << 32) |
                        ((long)(buffer[6] & 0xFF) << 24) |
                        ((long)(buffer[7] & 0xFF) << 16) |
                        ((long)(buffer[8] & 0xFF) << 8) |
                        (buffer[9] & 0xFF));
                offset = 10;
            }

            if (isMasked) {
                if (length < offset + 4 + payloadLength) return null;
                byte[] mask = new byte[4];
                System.arraycopy(buffer, offset, mask, 0, 4);
                offset += 4;

                byte[] payload = new byte[payloadLength];
                for (int i = 0; i < payloadLength; i++) {
                    payload[i] = (byte) (buffer[offset + i] ^ mask[i % 4]);
                }
                return payload;
            } else {
                if (length < offset + payloadLength) return null;
                byte[] payload = new byte[payloadLength];
                System.arraycopy(buffer, offset, payload, 0, payloadLength);
                return payload;
            }
        }

        /**
         * Handles regular HTTP requests
         */
        private void handleHttpRequest() throws IOException {
            String response = "HTTP/1.1 200 OK\r\n" +
                    "Content-Type: text/plain\r\n" +
                    "Connection: close\r\n\r\n" +
                    "This server only accepts WebSocket connections.";
            output.write(response.getBytes());
            output.flush();
        }

        /**
         * Sends data to the client via WebSocket
         */
        public boolean sendData(String data) {
            if (!connected || !isWebSocket) return false;

            try {
                byte[] bytes = data.getBytes("UTF-8");
                byte[] frame = createWebSocketFrame(bytes);
                output.write(frame);
                output.flush();
                return true;
            } catch (Exception e) {
                logger.log(Level.FINE, "Failed to send data to client " + clientId + ", closing connection", e);
                close();
                return false;
            }
        }

        /**
         * Creates a WebSocket frame for text data
         */
        private byte[] createWebSocketFrame(byte[] data) {
            int length = data.length;
            byte[] frame;

            if (length <= 125) {
                frame = new byte[2 + length];
                frame[1] = (byte) length;
            } else if (length <= 65535) {
                frame = new byte[4 + length];
                frame[1] = (byte) 126;
                frame[2] = (byte) (length >> 8);
                frame[3] = (byte) length;
            } else {
                frame = new byte[10 + length];
                frame[1] = (byte) 127;
                // For simplicity, assuming length fits in int
                frame[2] = 0;
                frame[3] = 0;
                frame[4] = 0;
                frame[5] = 0;
                frame[6] = (byte) (length >> 24);
                frame[7] = (byte) (length >> 16);
                frame[8] = (byte) (length >> 8);
                frame[9] = (byte) length;
            }

            frame[0] = (byte) 0x81; // FIN bit + text frame opcode

            int payloadStart = frame.length - length;
            System.arraycopy(data, 0, frame, payloadStart, length);

            return frame;
        }

        /**
         * Checks if the client is still connected
         */
        public boolean isConnected() {
            if (!connected) return false;
            if (clientSocket.isClosed() || clientSocket.isInputShutdown() || clientSocket.isOutputShutdown()) {
                connected = false;
                return false;
            }

            return true;
        }

        /**
         * Closes the connection to the client
         */
        public void close() {
            if (connected) {
                connected = false;

                try {
                    if (output != null) {
                        output.close();
                    }
                    if (input != null) {
                        input.close();
                    }
                    if (clientSocket != null && !clientSocket.isClosed()) {
                        clientSocket.close();
                    }
                } catch (IOException e) {
                    logger.log(Level.FINE, "Error closing client " + clientId + " connection", e);
                }

                removeClient(clientId);
                logger.info("Client " + clientId + " disconnected");
            }
        }
    }
}