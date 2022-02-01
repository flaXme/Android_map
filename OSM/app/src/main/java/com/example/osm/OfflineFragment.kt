package com.example.osm

import android.content.pm.PackageManager
import android.graphics.Paint
import android.os.Bundle
import android.os.Environment
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.content.PermissionChecker
import androidx.fragment.app.Fragment
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


private lateinit var map : MapView;
@Volatile
var startAndEnd: MutableList<GeoPoint> = ArrayList()
lateinit var  subGraph: Graph
lateinit var downloadArea: BoundingBox

/**
 * A simple [Fragment] subclass.
 * Use the [OfflineFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class OfflineFragment : Fragment() {




    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view:View = inflater.inflate(R.layout.fragment_offline, container, false)
        Configuration.getInstance().load(activity, PreferenceManager.getDefaultSharedPreferences(activity));


        //inflate and create the map
        map = view.findViewById(R.id.offline_map)
        map.setTileSource(
            XYTileSource(
                "worldMap",
                3, 20, 256, ".png", arrayOf(
                    "https://tiles.fmi.uni-stuttgart.de/"
                ), "© OpenStreetMap contributors",
                TileSourcePolicy(
                    2,
                    TileSourcePolicy.FLAG_NO_BULK
                            or TileSourcePolicy.FLAG_NO_PREVENTIVE
                            or TileSourcePolicy.FLAG_USER_AGENT_MEANINGFUL
                            or TileSourcePolicy.FLAG_USER_AGENT_NORMALIZED
                )
            )
        )
        map.minZoomLevel= 3.0
        map.tileProvider.setUseDataConnection(false)

        //enable pinch zoom in.
        map.setBuiltInZoomControls(true)
        map.setMultiTouchControls(true)




        /**
         * add marker onclick, click on existing marker will remove the marker
         */
        val mReceive = object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                Toast.makeText(activity, "Long click to choose Start/End points.", Toast.LENGTH_SHORT).show()
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






        val computePathButton: Button = view.findViewById(R.id.computePath)
        computePathButton.setOnClickListener{
            //check read external permission
            if(activity?.let { it1 ->
                    PermissionChecker.checkSelfPermission(
                        it1,
                        "android.permission.READ_EXTERNAL_STORAGE"
                    )
                } != PackageManager.PERMISSION_GRANTED){
                //if permission is not granted
                requestPermissions(arrayOf("android.permission.READ_EXTERNAL_STORAGE"),200);
            }else{//permission already granted
                if (startAndEnd.size == 2) {
                    var start = startAndEnd[0]
                    var end = startAndEnd[1]
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
        return view
    }
    /**
     * @param start: id of the start point in the subgraph
     * @param end: id of tne end point in the subgraph
     */
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
            map.invalidate()
        }else{
            Toast.makeText(activity, "No path Available!", Toast.LENGTH_SHORT).show()
        }
        path.setOnClickListener(Polyline.OnClickListener{ path, map, _ ->
            map.overlays.remove(path)
        })
    }


}