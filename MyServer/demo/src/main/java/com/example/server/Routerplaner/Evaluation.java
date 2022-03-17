package com.example.server.Routerplaner;

public class Evaluation {
    public static void main(String[] args) {
        Graph graphWithoutCH = new Graph("/Users/xinpang/Desktop/Studium/7.Semester/Bachelor Arbeit/Graphfiles/germany.txt");
        GraphWithCH graphWithCH = new GraphWithCH("/Users/xinpang/Desktop/Studium/7.Semester/Bachelor Arbeit/CH/ch_germany.txt");
        DijkstraWithCH.settledNodes = new boolean[graphWithCH.getNodeNr()];
        
        int nrOfIteration = 100;
        int nrOfValidQuery = 0;
        int equalCost = 0;
        long totalTimeUsedDij = 0;
        long totalTimeUsedDijCH = 0;
        int totalNrOfSettledNodesDij = 0;
        int totalNrOfSettledNodesDijCH = 0;
        for (int i = 0; i < nrOfIteration; i++) {
            System.out.println(i+1 + "-th test:");
            //choose a random source node
            int source = (int) (Math.random() * graphWithoutCH.getNodeNr());
            
            //choose a random target node
            int target = (int) (Math.random() * graphWithoutCH.getNodeNr());
           
            
            
            //compute shortest path and measure the time:
            long sTime = System.currentTimeMillis();
            Dijkstra dij = new Dijkstra(graphWithoutCH, source, target);
            if(!dij.getPathAvailable()){
                System.out.println("source is: " + source);
                System.out.println("target is: " + target);
                System.out.println("no path available!");
                continue;
            }
            //dij.getShortestPathInLonLat();
            long eTime = System.currentTimeMillis();
		    long time = eTime - sTime;
            totalTimeUsedDij += time;
            dij.getShortestPathInLonLat();
            nrOfValidQuery++;

            sTime = System.currentTimeMillis();
            DijkstraWithCH dijCH = new DijkstraWithCH(graphWithCH, source, target);
            //dijCH.getShortestPathInLonLat();
            eTime = System.currentTimeMillis();
            time = eTime - sTime;
            totalTimeUsedDijCH += time;
            dijCH.getShortestPathInLonLat();
            //compute number of settled nodes:
            totalNrOfSettledNodesDij += dij.getNrOfSettledNodes();
            totalNrOfSettledNodesDijCH += dijCH.getNrOfSettledNodes();
            
            //compare the cost of shortest paths
            if(dij.getCost(target) == dijCH.getCostOfPath()){
                equalCost++;
            }else{
                System.out.println("source is: " + source);
                System.out.println("target is: " + target);
                System.out.println("Cost with CH: " + dijCH.getCostOfPath());
                System.out.println("Cost without CH: " + dij.getCost(target));
            }

        }

        long averageTimeDij = totalTimeUsedDij / nrOfValidQuery;
        long averageTimeDijCH = totalTimeUsedDijCH / nrOfValidQuery;
        int averageNrOfSetteledNodeDij = totalNrOfSettledNodesDij / nrOfValidQuery;
        int averageNrOfSetteledNodeDijCH = totalNrOfSettledNodesDijCH / nrOfValidQuery;
        System.out.println("-----------------------------------------------------------------------------");
        System.out.println("Here comes the results:");
        System.out.println(nrOfValidQuery + "/" + nrOfIteration + " queries are valid.");
        System.out.println("Dijkstra takes in average [" + averageTimeDij +"ms].");
        System.out.println("Dijkstra with CH takes in average [" + averageTimeDijCH +"ms].");
        System.out.println("Both yields the path with same cost in : " + equalCost + "/" + nrOfValidQuery);
        System.out.println("Dijkstra settled in average: " + averageNrOfSetteledNodeDij + " nodes.");
        System.out.println("Dijkstra with CH settled in average: " + averageNrOfSetteledNodeDijCH + " nodes.");
    }
}
