package com.example.osm


import android.app.DownloadManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Paint
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.preference.PreferenceManager
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourcePolicy
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import kotlin.math.PI
import kotlin.math.asinh
import kotlin.math.floor
import kotlin.math.tan




class MainActivity : AppCompatActivity() {
    private lateinit var map : MapView;
    @Volatile
    var subgraphDataDownloaded = false
    var rectangle: MutableList<GeoPoint> = ArrayList()
    var startAndEnd: MutableList<GeoPoint> = ArrayList()
    lateinit var  subGraph: Graph
    lateinit var downloadArea: BoundingBox
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));


        //inflate and create the map
        setContentView(R.layout.activity_main);
        map = findViewById(R.id.map)
//        map.setTileSource(
//            XYTileSource(
//                "worldMap",
//                3, 20, 256, ".png", arrayOf(
//                    "https://tiles.fmi.uni-stuttgart.de/"
//                ), "© OpenStreetMap contributors",
//                TileSourcePolicy(
//                    2,
//                    TileSourcePolicy.FLAG_NO_BULK
//                            or TileSourcePolicy.FLAG_NO_PREVENTIVE
//                            or TileSourcePolicy.FLAG_USER_AGENT_MEANINGFUL
//                            or TileSourcePolicy.FLAG_USER_AGENT_NORMALIZED
//                )
//            ))
//        map.minZoomLevel= 3.0
        //local map tiles:
        if(ContextCompat.checkSelfPermission(this,"android.permission.READ_EXTERNAL_STORAGE") != PackageManager.PERMISSION_GRANTED){
            //if permission is not granted
            ActivityCompat.requestPermissions(this, arrayOf("android.permission.READ_EXTERNAL_STORAGE"),200);
        }
        val tileLocation =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + "/osm/tiles")
        map.setTileSource(
            XYTileSource(
                "localMap",
                3, 20, 256, ".png", arrayOf(
                    tileLocation.absolutePath+"/"
                ), "© OpenStreetMap contributors",
                TileSourcePolicy(
                    2,
                    TileSourcePolicy.FLAG_NO_BULK
                            or TileSourcePolicy.FLAG_NO_PREVENTIVE
                            or TileSourcePolicy.FLAG_USER_AGENT_MEANINGFUL
                            or TileSourcePolicy.FLAG_USER_AGENT_NORMALIZED
                )
            ))
        map.minZoomLevel= 3.0
        //enable pinch zoom in.
        map.setBuiltInZoomControls(true)
        map.setMultiTouchControls(true)



        /**
         * add marker onclick, click on existing marker will remove the marker
         */
        val mReceive = object :MapEventsReceiver{
            //single click to select subgraph area, i.e. rectangle
            override fun singleTapConfirmedHelper(p: GeoPoint):Boolean{
                Toast.makeText(baseContext,"Border point selected.",Toast.LENGTH_SHORT).show()
                val marker = Marker(map)
                //click on existing marker to delete it.
                marker.setOnMarkerClickListener { marker, map ->
                    Toast.makeText(baseContext, "Border point deleted.",Toast.LENGTH_SHORT).show()
                    rectangle.remove(marker.position)
                    map.overlays.remove(marker)
                }
                marker.position=p
                map.overlays.add(marker)
                rectangle.add(p)

                return false
            }
            //long click to select start and end point.
            override fun longPressHelper(p: GeoPoint): Boolean {
                Toast.makeText(baseContext,"Start/End point selected.",Toast.LENGTH_SHORT).show()
                val marker = Marker(map)
                    //click existing marker to delete it.
                marker.setOnMarkerClickListener { marker, map ->
                    Toast.makeText(baseContext, "Start/End point deleted.",Toast.LENGTH_SHORT).show()
                    startAndEnd.remove(marker.position)
                    map.overlays.remove(marker)
                }
                marker.position=p
                map.overlays.add(marker)
                startAndEnd.add(p)

                return false
            }
        }
        map.overlays.add(MapEventsOverlay(mReceive))





        val mapController = map.controller
        mapController.setZoom(19.0)
        val csBuilding = GeoPoint(48.74518,9.10665)
        val startPoint = csBuilding
        mapController.setCenter(startPoint)

        downloadArea = BoundingBox(48.76,9.108,48.724,9.1046)
        
        var centerOfArea = downloadArea.centerWithDateLine
        var markOfCenter = Marker(map)
        markOfCenter.position=centerOfArea
        map.overlays.add(markOfCenter)
        map.invalidate()





        //download button:
        val sendDownloadRequestButton: Button = findViewById(R.id.download)
        val computePathButton:Button = findViewById(R.id.computePath)
        computePathButton.setOnClickListener{
            //check read external permission
            if(ContextCompat.checkSelfPermission(this,"android.permission.READ_EXTERNAL_STORAGE") != PackageManager.PERMISSION_GRANTED){
                //if permission is not granted
                ActivityCompat.requestPermissions(this, arrayOf("android.permission.READ_EXTERNAL_STORAGE"),200);
            }else{//permission already granted
                 if (startAndEnd.size == 2) {
                     var start = startAndEnd.get(0)
                     var end = startAndEnd.get(1)
                     //check whether start and end point are in the area.
                     var graphData =
                         Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + "/osm/data/graphData.txt")
                     subGraph = Graph(graphData.absolutePath)
//                     val latInSubgraph:Boolean =
//                         start.latitude >= subGraph.minLat &&
//                             start.latitude <= subGraph.maxLat &&
//                             end.latitude >= subGraph.minLat &&
//                             end.latitude <= subGraph.maxLat
//                     val longiInSubgraph:Boolean =
//                         start.longitude >= subGraph.minLongi &&
//                                 start.longitude <= subGraph.maxLongi &&
//                                 end.longitude >= subGraph.minLongi &&
//                                 end.longitude <= subGraph.maxLongi
//                     val startEndInSubgraph:Boolean = latInSubgraph && longiInSubgraph
                     if (true) {
                         var startId = subGraph.nearestNeighbour(start.latitude, start.longitude)
                         var endId = subGraph.nearestNeighbour(end.latitude, end.longitude)
                         computePath(startId, endId)
                     }else{
                         Toast.makeText(baseContext, "Start or End point is not in subgraph!", Toast.LENGTH_SHORT).show()
                     }
                }else{
                    Toast.makeText(baseContext,"Need exactly two markers for start and end!",Toast.LENGTH_SHORT).show()
                }
            }

        }
        sendDownloadRequestButton.setOnClickListener {
            if(rectangle.size == 4) {
                //check internet connection
                if(isNetworkConnected()) {
                    Toast.makeText(this, "Start downloading graph data", Toast.LENGTH_LONG).show()
                    var minLat = Double.MAX_VALUE
                    var maxLat = Double.MIN_VALUE
                    var minLong = Double.MAX_VALUE
                    var maxLong = Double.MIN_VALUE

                    for (p in rectangle) {
                        if (p.latitude < minLat) {
                            minLat = p.latitude
                        }
                        if (p.latitude > maxLat) {
                            maxLat = p.latitude
                        }
                        if (p.longitude < minLong) {
                            minLong = p.longitude
                        }
                        if (p.longitude > maxLong) {
                            maxLong = p.longitude
                        }
                    }
                    var bottomLeft = GeoPoint(minLat, minLong)
                    var bottomRight = GeoPoint(minLat, maxLong)
                    var topRight = GeoPoint(maxLat, maxLong)
                    var topLeft = GeoPoint(maxLat, minLong)

                    val area = Polyline(map)
                    area.addPoint(bottomLeft)
                    area.addPoint(bottomRight)
                    area.addPoint(topRight)
                    area.addPoint(topLeft)
                    area.addPoint(bottomLeft)
                    map.overlays.add(area)
                    map.invalidate()

                    //check run time premission
                    if (ContextCompat.checkSelfPermission(
                            this,
                            "android.permission.WRITE_EXTERNAL_STORAGE"
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        //if permission is not granted
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf("android.permission.WRITE_EXTERNAL_STORAGE"),
                            100
                        );
                    } else {//permission already granted
                        download(minLat, maxLat, minLong, maxLong)
                    }
                }else{
                    Toast.makeText(this,"No Internet connection!", Toast.LENGTH_SHORT).show()
                }

            }else{
                Toast.makeText(this,"Exactly 4 Markers are needed to define a area!",Toast.LENGTH_LONG).show()
            }
        }



    }


    /**
     * download graphdata in external files
     */
    private fun download(minLat:Double, maxLat:Double, minLong:Double, maxLong:Double){
        //download graph data:
        //user defined server ip
        val dataUrl = "http://192.168.0.10:8081/subgraph?minLat=$minLat&maxLat=$maxLat&minLong=$minLong&maxLong=$maxLong"
        val dataRequest = DownloadManager.Request(Uri.parse(dataUrl))
            .setTitle("graphDataDownloadRequest")
            .setDescription("Downloading graph data")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setAllowedOverMetered(true)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,"osm/data/graphData.txt" )
        val dm = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        dm.enqueue(dataRequest)

        //download tile data:
        for (zoomLevel in 19..19){
            val minTileCoor = getXYTile(minLat,minLong,zoomLevel)
            val minX = minTileCoor.first
            val maxY = minTileCoor.second
            val maxTileCoor = getXYTile(maxLat,maxLong,zoomLevel)
            val maxX = maxTileCoor.first
            val minY = maxTileCoor.second
            for (x in minX..maxX){
                for (y in minY..maxY){
                    val url = "https://tiles.fmi.uni-stuttgart.de/$zoomLevel/$x/$y.png"
                    val tileRequest = DownloadManager.Request(Uri.parse(url))
                        .setTitle("graphTileDownloadRequest")
                        .setDescription("Downloading graph tiles.")
                        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                        .setAllowedOverMetered(true)
                        .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,"osm/tiles/$zoomLevel/$x/$y.png" )
                    dm.enqueue(tileRequest)
                }
            }

        }

    }
    private fun computePath(start: Int, end:Int){
        var dij = Dijkstra(subGraph,start,end)
        drawLineWithStringOfCoordinates(dij.shortestPathInLonLat)
    }



    /**
     * display the path on the map, given a string of path in lat, long
     * string format:"[[firstLat,_firstLong],_[secondLat,_secondLong],_...,_[lastLat,_lastLong]]" space represented undersocre because easy of read.
     */
    private fun drawLineWithStringOfCoordinates(stringPath: String){
        //get rid of the first and last 2 characters
        val newStringPath = stringPath.drop(2).dropLast(2)
        //now the string looks like this:"firstLat,_firstLong],_[secondLat,_secondLong],_...,_[lastLat,_lastLong"
        val path = Polyline()
        //first split:
        val latLongPath: List<String> = newStringPath.split("], [")
        //now the string element in the list looks like this: firstLat,_firstLong
        val pointList:MutableList<GeoPoint> = mutableListOf()
        for (coordinates in latLongPath){
            val latLong:List<String> = coordinates.split(", ")
            //Lat and Long are given to GeoPoint constructor in reverse order because GeoPoint expects first argument to be longitude and second argument to be latitude
            pointList.add(GeoPoint(latLong[1].toDouble(),latLong[0].toDouble()))
        }
        if (pointList.size > 2) {
            path.setPoints(pointList)
            //polyline refinement, no gaps between two subline.
            path.outlinePaint.strokeJoin = Paint.Join.ROUND
            path.outlinePaint.strokeCap = Paint.Cap.ROUND
            map.overlays.add(path)
            map.controller.setCenter(path.actualPoints.get(0))
            map.invalidate()
        }else{
            Toast.makeText(baseContext, "No path Available!", Toast.LENGTH_SHORT).show()
        }

    }



    fun getXYTile(lat : Double, lon: Double, zoom : Int) : Pair<Int, Int> {
        val latRad = Math.toRadians(lat)
        var xtile = floor( (lon + 180) / 360 * (1 shl zoom) ).toInt()
        var ytile = floor( (1.0 - asinh(tan(latRad)) / PI) / 2 * (1 shl zoom) ).toInt()

        if (xtile < 0) {
            xtile = 0
        }
        if (xtile >= (1 shl zoom)) {
            xtile= (1 shl zoom) - 1
        }
        if (ytile < 0) {
            ytile = 0
        }
        if (ytile >= (1 shl zoom)) {
            ytile = (1 shl zoom) - 1
        }

        return Pair(xtile, ytile)
    }

    fun isNetworkConnected(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.activeNetworkInfo != null && cm.activeNetworkInfo!!.isConnected
    }

    override fun onResume() {
        super.onResume();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        map.onResume(); //needed for compass, my location overlays, v6.0.0 and up
    }

    override fun onPause() {
        super.onPause();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        map.onPause();  //needed for compass, my location overlays, v6.0.0 and up
    }

}