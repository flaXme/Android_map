package com.example.server.Routerplaner;

public class Evaluation {
    public static void main(String[] args) {
        //read graph file:
        Graph graphWithoutCH = new Graph("/Users/xinpang/Desktop/Studium/7.Semester/Bachelor Arbeit/Graphfiles/germany.txt");
        GraphWithCH graphWithCH = new GraphWithCH("/Users/xinpang/Desktop/Studium/7.Semester/Bachelor Arbeit/CH/ch_germany.txt");
        //instantiate dij and dij with CH.
        DijkstraWithCH.settledNodes = new boolean[graphWithCH.getNodeNr()];
        DijkstraWithCH dijCH = new DijkstraWithCH(graphWithCH);
        Dijkstra dij = new Dijkstra(graphWithoutCH);
        //set all necessary evaluation values:
        int nrOfIteration = 100;
        int nrOfValidQuery = 0;
        int equalCost = 0;
        long totalTimeUsedDij = 0;
        long totalTimeUsedDijCH = 0;
        int totalNrOfSettledNodesDij = 0;
        int totalNrOfSettledNodesDijCH = 0;
        long totalTimeConcatinatePathDij = 0;
        long totalTimeConcatinatePathDijCH = 0;
        long timeComputPath = 0;
        long timeConcatinatePath = 0;
        long totalResetTimeDij = 0;
        long totalResetTimeDijCH = 0;
        long resetTime = 0;

        //Here begins the evaluation:
        for (int i = 0; i < nrOfIteration; i++) {
            System.out.println("Computing " + (i+1) + "-th test...");
            //choose a random source node
            int source = (int) (Math.random() * graphWithoutCH.getNodeNr());
            
            //choose a random target node
            int target = (int) (Math.random() * graphWithoutCH.getNodeNr());
           
            
            
            //Dijkstra:

            //compute path:
            timeComputPath = System.currentTimeMillis();
            dij.computePath(source, target);
            if(!dij.getPathAvailable()){
                System.out.println("source is: " + source);
                System.out.println("target is: " + target);
                System.out.println("no path available!");
                dij.reset();
                continue;
            }
            //concatinate path:
            timeConcatinatePath = System.currentTimeMillis();
            dij.getShortestPathInLonLat();
            timeConcatinatePath = System.currentTimeMillis() - timeConcatinatePath;
            totalTimeConcatinatePathDij +=timeConcatinatePath;

		    timeComputPath = System.currentTimeMillis() - timeComputPath;
            totalTimeUsedDij += timeComputPath;
            nrOfValidQuery++;

            //CH:
            //compute path:
            timeComputPath = System.currentTimeMillis();
            dijCH.computePath(source, target);
            
            //concatinate path:
            timeConcatinatePath = System.currentTimeMillis();
            dijCH.getShortestPathInLonLat();
            timeConcatinatePath = System.currentTimeMillis() - timeConcatinatePath;
            totalTimeConcatinatePathDijCH += timeConcatinatePath;
            timeComputPath = System.currentTimeMillis() - timeComputPath;
            totalTimeUsedDijCH += timeComputPath;
            


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

            //reset dijCH attributtes:
            resetTime = System.currentTimeMillis();
            dijCH.reset();
            resetTime = System.currentTimeMillis() - resetTime;
            totalResetTimeDijCH += resetTime;
            
            //reset dij attributes:
            resetTime = System.currentTimeMillis();
            dij.reset();
            resetTime = System.currentTimeMillis() - resetTime;
            totalResetTimeDij += resetTime;
        }
        //compute average values:
        long averageTimeDij = totalTimeUsedDij / nrOfValidQuery;
        long averageTimeDijCH = totalTimeUsedDijCH / nrOfValidQuery;
        long averageTimeConcatinatePathDij = totalTimeConcatinatePathDij / nrOfValidQuery;
        long averageTimeConcatinatePathDijCH = totalTimeConcatinatePathDijCH / nrOfValidQuery;
        long averageTimeResetDij = totalResetTimeDij / nrOfValidQuery;
        long averageTimeResetDijCH = totalResetTimeDijCH / nrOfValidQuery;
        int averageNrOfSetteledNodeDij = totalNrOfSettledNodesDij / nrOfValidQuery;
        int averageNrOfSetteledNodeDijCH = totalNrOfSettledNodesDijCH / nrOfValidQuery;

        System.out.println("-----------------------------------------------------------------------------");
        System.out.println("Here comes the results:");
        System.out.println(nrOfValidQuery + "/" + nrOfIteration + " queries are valid.");
        System.out.println("Both yields the path with same cost in : " + equalCost + "/" + nrOfValidQuery);
        System.out.println("Dijkstra takes total in average [" + averageTimeDij +"ms].");
        System.out.println("Dijkstra with CH takes total in average [" + averageTimeDijCH +"ms].");
        System.out.println("-----------------------------------------------------------------------------");
        System.out.println("Dijkstra settled in average: " + averageNrOfSetteledNodeDij + " nodes.");
        System.out.println("Dijkstra with CH settled in average: " + averageNrOfSetteledNodeDijCH + " nodes.");
        System.out.println("-----------------------------------------------------------------------------");
        System.out.println("Dijkstra takes in average [" + (averageTimeDij - averageTimeConcatinatePathDij) +"ms] to compute the path.");
        System.out.println("Dijkstra with CH takes in average [" + (averageTimeDijCH - averageTimeConcatinatePathDijCH) +"ms] to compute the path.");
        System.out.println("-----------------------------------------------------------------------------");
        System.out.println("Dijstra concatinates path takes in average: [" + averageTimeConcatinatePathDij + "ms]");
        System.out.println("Dijkstra with CH concatinates path takes in average: [" + averageTimeConcatinatePathDijCH + "ms]");
        System.out.println("-----------------------------------------------------------------------------");
        System.out.println("Dijkstra takes in average [" + averageTimeResetDij + "ms] to reset.");
        System.out.println("Dijkstra with CH takes in average [" + averageTimeResetDijCH + "ms] to reset.");
    }
}
