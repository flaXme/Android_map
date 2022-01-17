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
        public Graph g;
        public MyHttpHandler(){
            g = new Graph("/Users/xinpang/Desktop/Studium/7.Semester/Bachelor Arbeit/Graphfiles/germany.txt");
        }
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            double minLat = -1;
            double maxLat = -1;
            double minLong = -1;
            double maxLong = -1;
            String subgraph = null;
            if("GET".equals(exchange.getRequestMethod()) && exchange.getHttpContext().getPath().equals("/subgraph")){
                ArrayList<Double> minLatMaxLatMinLongMaxLong = new ArrayList<>();
                minLatMaxLatMinLongMaxLong = handleDownloadGraphGetRequest(exchange);
                minLat = minLatMaxLatMinLongMaxLong.get(0);
                maxLat = minLatMaxLatMinLongMaxLong.get(1);
                minLong = minLatMaxLatMinLongMaxLong.get(2);
                maxLong = minLatMaxLatMinLongMaxLong.get(3);
                System.out.println("minLat = "+minLat);
                System.out.println("maxLat = "+maxLat);
                System.out.println("minLong = "+minLong);
                System.out.println("maxLong = "+maxLong);
                subgraph = g.calculateSubgraph(minLat, maxLat, minLong, maxLong);
                System.out.println("\ncomputing subgraph finished.");
            }
            handleResponse(exchange, subgraph);
        }
        /**
         * 
         * @param exchange
         * @return
         */
        private ArrayList<Double> handleDownloadGraphGetRequest(HttpExchange exchange) {
            ArrayList<Double> argsForSubGraph = new ArrayList<>();
            //The url looks like: http://serverIP/subgraph?minLat=$minLat&maxLat=$maxLat&minLong=$minLong&maxLong=$maxLong
            Pattern p = Pattern.compile("(-)?[0-9]+.[0-9]+");//matches number like -49.23, 9.02
            Matcher m = p.matcher(exchange.getRequestURI().toString());
            while (m.find()) {
                double n = Double.parseDouble(m.group());
                argsForSubGraph.add(n);
            }
             return argsForSubGraph;
        }
        
        
        private void handleResponse(HttpExchange httpExchange, String body)  throws  IOException{
            OutputStream outputStream = httpExchange.getResponseBody();
            StringBuilder htmlBuilder = new StringBuilder();
            //htmlBuilder.append("<html>").append("<body>").append("<h1>").append(body).append("</h1>").append("</body>").append("</html>");
            htmlBuilder.append(body);
            String htmlResponse = StringEscapeUtils.escapeHtml4(htmlBuilder.toString());
            httpExchange.sendResponseHeaders(200, htmlResponse.length());
            outputStream.write(htmlResponse.getBytes());
            outputStream.flush();
            outputStream.close();
            System.out.println("responds sent.");
        }
    }


    
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8081),0);
        String hostName = server.getAddress().getHostName();
        server.createContext("/subgraph", new MyHttpHandler());
        server.setExecutor(null); // creates a default executor
        server.start();
        System.out.println("server started. host name is "+ hostName);
    }
    
    
}
