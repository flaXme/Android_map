package com.example.osm

import java.io.BufferedReader
import java.io.FileReader
import java.io.IOException
import java.util.*

/**
 * read static Graph date from File represent it as Arrays
 *
 *
 */
class Graph(path: String) {
    var nodeNr //number of the nodes
            = 0
        private set
    var edgeNr //number of the edges
            = 0
        private set
    private val lengthOfEdgeElement = 3 //Edge Element consist of endNode and metric vector
    private lateinit var latitude //mapping: nodeId -> latitude of that node
            : DoubleArray
    private lateinit var longitude //mapping: nodeId -> longitude of that node
            : DoubleArray
    private lateinit var nrOfOutgoingEdges //mapping: nodeId -> number of outgoing edges of that node
            : IntArray
    lateinit var edgeArray //stores serials of edge information in form: start node, end nodeId, cost
            : IntArray
        private set
    lateinit var nodeArray // mapping: nodeId -> index of edgeArray for that node.
            : IntArray
        private set

    /**
     * main function of read graph information from the file.
     * @param path
     */
    private fun readGraphFile(path: String) {
        try {
            val br = BufferedReader(FileReader(path))
            while (br.readLine()
                    .startsWith("#")
            ) { //read and ignore the comments and one blank line
                //empty body
            }
            //reading graph data
            nodeNr = br.readLine().toInt() // read the number of nodes
            edgeNr = br.readLine().toInt() // read the number of edges
            latitude = DoubleArray(nodeNr) //initialize latitude array
            longitude = DoubleArray(nodeNr) //initialize longitude array
            nrOfOutgoingEdges = IntArray(nodeNr) //init
            edgeArray = IntArray(edgeNr * lengthOfEdgeElement) //init
            nodeArray = IntArray(nodeNr) //init
            var line: String
            // initialize nodeArray and fill lat long array
            for (i in 0 until nodeNr) { // read and save node information node by node(line by line).
                if (i % 1000000 == 0) {
                    print("#") //loading bar.
                }
                line = br.readLine()
                val tempString = line.split(" ").toTypedArray() // parse node information
                latitude[i] = java.lang.Double.valueOf(tempString[2]) //store latitude of node i
                longitude[i] = java.lang.Double.valueOf(tempString[3]) //store longitude of node i
                nodeArray[i] = -1 //initialize with value -1
            }
            // fill edgeArray
            var index = 0
            for (i in 0 until edgeNr) { // read and save edge information edge by edge(line by line).
                if (i % 1000000 == 0) {
                    print("#")
                }
                line = br.readLine()
                val tempStringArray = line.split(" ").toTypedArray()
                edgeArray[index] = tempStringArray[0].toInt() //store start node of edge i
                index++
                edgeArray[index] = tempStringArray[1].toInt() //store end node of edge i
                index++
                edgeArray[index] = tempStringArray[2].toInt() //store cost of edge i
                index++
            }
            br.close()
            //fill nodeArray and nrOfoutgoingEdges array.
            for (i in 0 until edgeNr) {
                if (nodeArray[edgeArray[i * lengthOfEdgeElement]] == -1) { //if the start if edge i is not in nodeArray initialized
                    nodeArray[edgeArray[i * lengthOfEdgeElement]] =
                        i * lengthOfEdgeElement //then save the index of edgeArray in nodeArray
                }
                nrOfOutgoingEdges[edgeArray[i * lengthOfEdgeElement]] += 1 //count the number of outgoing edge for start node of every edge.
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun getNrOfOutgoingEdges(nodeID: Int): Int {
        return nrOfOutgoingEdges[nodeID]
    }

    fun getOutgoingEdgesArray(nodeID: Int): IntArray? {
        if (nrOfOutgoingEdges[nodeID] >= 1) {
            val startIndex = nodeArray[nodeID]
            val endIndex = getNrOfOutgoingEdges(nodeID) * lengthOfEdgeElement + startIndex
            return Arrays.copyOfRange(edgeArray, startIndex, endIndex)
        }
        return null
    }

    fun getLatitude(nodeID: Int): Double {
        return latitude[nodeID]
    }

    fun getLongitude(nodeID: Int): Double {
        return longitude[nodeID]
    }

    /**
     *
     * @param nodeID
     */
    fun getOutgoingEdgesArrayIndex(nodeID: Int): IntArray? {
        if (nrOfOutgoingEdges[nodeID] >= 1) {
            val startIndex = nodeArray[nodeID]
            val endIndex =
                getNrOfOutgoingEdges(nodeID) * lengthOfEdgeElement + startIndex
            return intArrayOf(startIndex, endIndex)
        }
        return null
    }

    fun maxNrOfOutgoingEdges(): Int {
        var max = 0
        for (i in 0 until nodeNr) {
            if (getNrOfOutgoingEdges(i) > max) {
                max = getNrOfOutgoingEdges(i)
            }
        }
        return max
    }

    /**
     * Constructor of the class Graph
     * @param path - path of the graph file(with suffix .fmi or .txt), has to be valid path.
     */
    init {
        println()
        print("Reading file ")
        val startTime = System.currentTimeMillis()
        readGraphFile(path)
        val endTime = System.currentTimeMillis()
        val totalTime = endTime - startTime
        println()
        println("Reading file took [$totalTime] milliseconds")
    }
}