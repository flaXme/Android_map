package com.example.osm

import android.app.DownloadManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Paint
import android.net.ConnectivityManager
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Bundle
import android.os.Environment
import android.preference.PreferenceManager
import android.text.format.Formatter.formatIpAddress
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker.checkSelfPermission
import androidx.fragment.app.Fragment
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.cachemanager.CacheManager
import org.osmdroid.tileprovider.tilesource.TileSourcePolicy
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.PI
import kotlin.math.asinh
import kotlin.math.floor
import kotlin.math.tan


/**
 * A simple [Fragment] subclass.
 * Use the [OnlineFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class OnlineFragment : Fragment() {
    private lateinit var map : MapView;
    @Volatile
    var rectangle: MutableList<GeoPoint> = ArrayList()
    var startAndEnd: MutableList<GeoPoint> = ArrayList()
    lateinit var  subGraph: Graph
    lateinit var downloadArea: BoundingBox


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        /**
         * get ip address of the user
         */
        val context = requireContext().applicationContext
        val wm = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val ip: String = formatIpAddress(wm.connectionInfo.ipAddress)

        // Inflate the layout for this fragment
        val view:View = inflater.inflate(R.layout.fragment_online, container, false)
        Configuration.getInstance().load(activity, PreferenceManager.getDefaultSharedPreferences(activity));



        //inflate and create the map
        map = view.findViewById(R.id.map)
        map.setTileSource(
            XYTileSource(
                "worldMap",
                3, 20, 256, ".png", arrayOf(
                    "https://tiles.fmi.uni-stuttgart.de/"
                ), "© OpenStreetMap contributors",
                TileSourcePolicy(
                    2,
                    //TileSourcePolicy.FLAG_NO_BULK
                            TileSourcePolicy.FLAG_NO_PREVENTIVE
                            or TileSourcePolicy.FLAG_USER_AGENT_MEANINGFUL
                            or TileSourcePolicy.FLAG_USER_AGENT_NORMALIZED
                )
            )
        )

        map.minZoomLevel= 3.0


        //enable pinch zoom in.
        map.setBuiltInZoomControls(true)
        map.setMultiTouchControls(true)




        /**
         * add marker onclick, click on existing marker will remove the marker
         */
        val mReceive = object : MapEventsReceiver {
            //single click to select subgraph area, i.e. rectangle
            override fun singleTapConfirmedHelper(p: GeoPoint):Boolean{
                Toast.makeText(activity,"Border point selected.", Toast.LENGTH_SHORT).show()
                val marker = Marker(map)
                //click on existing marker to delete it.
                marker.setOnMarkerClickListener { marker, map ->
                    Toast.makeText(activity, "Border point deleted.", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(activity,"Start/End point selected.", Toast.LENGTH_SHORT).show()
                val marker = Marker(map)
                //click existing marker to delete it.
                marker.setOnMarkerClickListener { marker, map ->
                    Toast.makeText(activity, "Start/End point deleted.", Toast.LENGTH_SHORT).show()
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







        //download button:
        val sendDownloadRequestButton: Button = view.findViewById(R.id.download)
        val computePathButton: Button = view.findViewById(R.id.computePath)
        computePathButton.setOnClickListener{
            //check read external permission
            if(activity?.let { it1 -> checkSelfPermission(it1,"android.permission.READ_EXTERNAL_STORAGE") } != PackageManager.PERMISSION_GRANTED){
                //if permission is not granted
                requestPermissions(arrayOf("android.permission.READ_EXTERNAL_STORAGE"),200);
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
                        Toast.makeText(activity, "Start or End point is not in subgraph!", Toast.LENGTH_SHORT).show()
                    }
                }else{
                    Toast.makeText(activity,"Need exactly two markers for start and end!", Toast.LENGTH_SHORT).show()
                }
            }

        }
        sendDownloadRequestButton.setOnClickListener {
            if(rectangle.size == 4) {
                //check internet connection
                if(isNetworkConnected()) {
                    Toast.makeText(activity, "Start downloading graph data", Toast.LENGTH_LONG).show()
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
                    downloadArea = BoundingBox(maxLat,maxLong,minLat,minLong)
                    val area = Polyline(map)
                    area.addPoint(bottomLeft)
                    area.addPoint(bottomRight)
                    area.addPoint(topRight)
                    area.addPoint(topLeft)
                    area.addPoint(bottomLeft)
                    map.overlays.add(area)
                    map.invalidate()

                    //check run time premission
                    if (activity?.let { it1 ->
                            ContextCompat.checkSelfPermission(
                                it1,
                                "android.permission.WRITE_EXTERNAL_STORAGE"
                            )
                        } != PackageManager.PERMISSION_GRANTED
                    ) {
                        //if permission is not granted
                        if (activity != null) {
                            ActivityCompat.requestPermissions(
                                requireActivity(),
                                arrayOf("android.permission.WRITE_EXTERNAL_STORAGE"),
                                100
                            )
                        };
                    } else {//permission already granted
                        download(minLat, maxLat, minLong, maxLong)
                    }
                }else{
                    Toast.makeText(activity,"No Internet connection!", Toast.LENGTH_SHORT).show()
                }

            }else{
                Toast.makeText(activity,"Exactly 4 Markers are needed to define a area!", Toast.LENGTH_LONG).show()
            }
        }






        return view
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
        val dm = activity?.getSystemService(AppCompatActivity.DOWNLOAD_SERVICE) as DownloadManager
        dm.enqueue(dataRequest)

        //download tile data with DownloadManager:
//        for (zoomLevel in 3..18){
//            val minTileCoor = getXYTile(minLat,minLong,zoomLevel)
//            val minX = minTileCoor.first
//            val maxY = minTileCoor.second
//            val maxTileCoor = getXYTile(maxLat,maxLong,zoomLevel)
//            val maxX = maxTileCoor.first
//            val minY = maxTileCoor.second
//            for (x in minX..maxX){
//                for (y in minY..maxY){
//                    val url = "https://tiles.fmi.uni-stuttgart.de/$zoomLevel/$x/$y.png"
//                    val tileRequest = DownloadManager.Request(Uri.parse(url))
//                        .setTitle("graphTileDownloadRequest")
//                        .setDescription("Downloading graph tiles.")
//                        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
//                        .setAllowedOverMetered(true)
//                        .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,"osm/tiles/$zoomLevel/$x/$y.png" )
//                    dm.enqueue(tileRequest)
//                }
//            }
//
//        }

        //download tiles with cacheManager
        //setHasOptionsMenu(false)
        val cm = CacheManager(map)
        cm.downloadAreaAsync(activity,downloadArea,3,19)



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
            Toast.makeText(activity, "No path Available!", Toast.LENGTH_SHORT).show()
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
        val cm = activity?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.activeNetworkInfo != null && cm.activeNetworkInfo!!.isConnected
    }


}