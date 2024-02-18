package org.example.server;

import org.example.model.Request;
import org.example.utils.RequestParse;
import org.example.utils.ResponseUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Optional;

public class ConnectHandler {

    private final Socket socket;
    private final Map<String, Map<String, Handler>> handlers;

    public ConnectHandler(Socket socket, Map<String, Map<String, Handler>> handlers) {
        this.socket = socket;
        this.handlers = handlers;
    }

    public void handle() {
        try (
                socket;
                final var in = new BufferedInputStream(socket.getInputStream());
                final var out = new BufferedOutputStream(socket.getOutputStream())
        ) {
            Optional<Request> optionalRequest = RequestParse.parseRequest(in, out);
            Request request = null;
            if (optionalRequest.isPresent()) {
                request = optionalRequest.get();
            }
            if (request != null) {
                Handler handler = handlers.get(request.getMethod()).get(request.getPath());
                if (handler != null) {
                    handler.handle(request, out);
                } else {
                    ResponseUtils.notFound(out);
                }
            } else {
                ResponseUtils.badRequest(out);
            }
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }
}
