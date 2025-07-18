package ru.omega;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;

public class ServerMain {
    public static void main(String[] args) throws Exception {
        Server server = new Server(8888);
        ServletContextHandler handler = new ServletContextHandler();
        handler.setContextPath("/");

        handler.addServlet(RestSoapBridge.class, "/soap");

        server.setHandler(handler);
        server.start();
        System.out.println("Server started on http://localhost:8888/soap");
        server.join();
    }
}