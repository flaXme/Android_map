package com.example.server.Routerplaner;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import org.apache.commons.text.StringEscapeUtils;



public class MyServer {
    //handler for http request
    private static class MyHttpHandler implements HttpHandler{

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            int scr = -1;
            int tar = -1;
            String path = null;
            if("GET".equals(exchange.getRequestMethod())){
                ArrayList<Integer> sourceAndTarget = new ArrayList<>();
                sourceAndTarget = handleGetRequest(exchange);
                scr = sourceAndTarget.get(0);
                tar = sourceAndTarget.get(1);
                System.out.println("scr="+scr);
                System.out.println("tar="+tar);
                Graph g = new Graph("/Users/xinpang/Desktop/Studium/7. Semester/Bachelor Arbeit/Graphfiles/germany.txt");
                Dijkstra dij = new Dijkstra(g, scr, tar);
                path = dij.getShortestPathInLonLat();
                System.out.println("computing path finished.");
            }
            handleResponse(exchange, path);
        }
        /**
         * 
         * @param exchange
         * @return
         */
        private ArrayList<Integer> handleGetRequest(HttpExchange exchange) {
            ArrayList<Integer> sourceAndTarget = new ArrayList<>();
            Pattern p = Pattern.compile("[0-9]+");
            Matcher m = p.matcher(exchange.getRequestURI().toString());
            while (m.find()) {
                int n = Integer.parseInt(m.group());
                sourceAndTarget.add(n);
            }
             return sourceAndTarget;
        }
        
        
        private void handleResponse(HttpExchange httpExchange, String path)  throws  IOException{
            OutputStream outputStream = httpExchange.getResponseBody();
            StringBuilder htmlBuilder = new StringBuilder();
            htmlBuilder.append("<html>").append("<body>").append("<h1>").append(path).append("</h1>").append("</body>").append("</html>");
            String htmlResponse = StringEscapeUtils.escapeHtml4(htmlBuilder.toString());
            httpExchange.sendResponseHeaders(200, htmlResponse.length());
            outputStream.write(htmlResponse.getBytes());
            outputStream.flush();
            outputStream.close();
            System.out.println("responds sent.");
        }
    }


    
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost",8081),0);
        server.createContext("/query", new MyHttpHandler());
        server.setExecutor(null); // creates a default executor
        server.start();
        System.out.println("server started.");
    }
    
}
