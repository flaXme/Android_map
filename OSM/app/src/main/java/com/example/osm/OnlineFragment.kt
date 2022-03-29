package com.example.osm

import android.app.DownloadManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.cachemanager.CacheManager
import org.osmdroid.tileprovider.tilesource.TileSourcePolicy
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import java.util.*
import kotlin.math.*


/**
 * A simple [Fragment] subclass.
 * Use the [OnlineFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class OnlineFragment : Fragment() {
    private lateinit var map : MapView;
    @Volatile
    var rectangle: MutableList<Marker> = ArrayList()
    lateinit var  subGraph: Graph
    lateinit var downloadArea: BoundingBox

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        /**
         * Inflate the layout for this fragment
         */
        val view:View = inflater.inflate(R.layout.fragment_online, container, false)
        Configuration.getInstance().load(activity, PreferenceManager.getDefaultSharedPreferences(activity));


        /**
         * inflate and create the map
         */
        map = view.findViewById(R.id.online_map)
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


        /**
         * enable pinch zoom in.
         */
        //map.setBuiltInZoomControls(true)
        map.zoomController.setVisibility(CustomZoomButtonsController.Visibility.ALWAYS)
        map.setMultiTouchControls(true)



        /**
         * set the initial map location to computer science building of university of stuttgart.
         */
        val mapController = map.controller
        mapController.setZoom(19.0)
        val csBuilding = GeoPoint(48.74518,9.10665)
        val startPoint = csBuilding
        mapController.setCenter(startPoint)

        /**
         * draggable marker
         */
        var bottomLeft = Marker(map)
        bottomLeft.isDraggable = true
        bottomLeft.position = GeoPoint(startPoint.latitude - 0.0005, startPoint.longitude - 0.0005)
        map.overlays.add(bottomLeft)
        var bottomRight = Marker(map)
        bottomRight.isDraggable = true
        bottomRight.position = GeoPoint(startPoint.latitude - 0.0005,startPoint.longitude + 0.0005 )
        map.overlays.add(bottomRight)
        var topLeft = Marker(map)
        topLeft.isDraggable = true
        topLeft.position = GeoPoint(startPoint.latitude + 0.0005, startPoint.longitude - 0.0005)
        map.overlays.add(topLeft)
        var topRight = Marker(map)
        topRight.isDraggable = true
        topRight.position = GeoPoint(startPoint.latitude + 0.0005,startPoint.longitude+0.0005)
        map.overlays.add(topRight)
        rectangle.add(bottomLeft)
        rectangle.add(bottomRight)
        rectangle.add(topLeft)
        rectangle.add(topRight)
        topLeft.setVisible(false)
        topRight.setVisible(false)
        bottomLeft.setVisible(false)
        bottomRight.setVisible(false)
        topLeft.setAnchor(Marker.ANCHOR_CENTER,Marker.ANCHOR_CENTER)
        topRight.setAnchor(Marker.ANCHOR_CENTER,Marker.ANCHOR_CENTER)
        bottomLeft.setAnchor(Marker.ANCHOR_CENTER,Marker.ANCHOR_CENTER)
        bottomRight.setAnchor(Marker.ANCHOR_CENTER,Marker.ANCHOR_CENTER)
        /**
         * borders
         */
        var upperBorder = Polyline()
        var lowerBorder = Polyline()
        var leftBorder = Polyline()
        var rightBorder = Polyline()
        /**
         * drag event for top right marker
         */
        upperBorder.setPoints(listOf(GeoPoint(topLeft.position),GeoPoint(topRight.position)))
        map.invalidate()
        map.overlays.add(upperBorder)
        var topRightDragEvent = object :Marker.OnMarkerDragListener{
            override fun onMarkerDrag(marker: Marker?) {
                if (marker != null) {
                    topLeft.position = GeoPoint(marker.position.latitude,topLeft.position.longitude)
                    bottomRight.position = GeoPoint(bottomRight.position.latitude,marker.position.longitude)
                    upperBorder.setPoints(listOf(GeoPoint(topLeft.position),GeoPoint(topRight.position)))
                    lowerBorder.setPoints(listOf(GeoPoint(bottomLeft.position),GeoPoint(bottomRight.position)))
                    leftBorder.setPoints(listOf(GeoPoint(bottomLeft.position),GeoPoint(topLeft.position)))
                    rightBorder.setPoints(listOf(GeoPoint(bottomRight.position),GeoPoint(topRight.position)))
                }
                map.invalidate()
            }

            override fun onMarkerDragEnd(marker: Marker?) {
            }

            override fun onMarkerDragStart(marker: Marker?) {
            }
        }
        /**
         * drag event for bottom left marker
         */

        lowerBorder.setPoints(listOf(GeoPoint(bottomLeft.position),GeoPoint(bottomRight.position)))
        map.invalidate()
        map.overlays.add(lowerBorder)
        var bottomLeftDragEvent = object :Marker.OnMarkerDragListener{
            override fun onMarkerDrag(marker: Marker?) {
                if (marker != null) {
                    topLeft.position = GeoPoint(topLeft.position.latitude,marker.position.longitude)
                    bottomRight.position = GeoPoint(marker.position.latitude,bottomRight.position.longitude)
                    upperBorder.setPoints(listOf(GeoPoint(topLeft.position),GeoPoint(topRight.position)))
                    lowerBorder.setPoints(listOf(GeoPoint(bottomLeft.position),GeoPoint(bottomRight.position)))
                    leftBorder.setPoints(listOf(GeoPoint(bottomLeft.position),GeoPoint(topLeft.position)))
                    rightBorder.setPoints(listOf(GeoPoint(bottomRight.position),GeoPoint(topRight.position)))
                }
                map.invalidate()
            }

            override fun onMarkerDragEnd(marker: Marker?) {
            }

            override fun onMarkerDragStart(marker: Marker?) {
            }
        }
        /**
         * drag event for top left marker
         */

        leftBorder.setPoints(listOf(GeoPoint(bottomLeft.position),GeoPoint(topLeft.position)))
        map.invalidate()
        map.overlays.add(leftBorder)
        var topLeftDragEvent = object :Marker.OnMarkerDragListener{
            override fun onMarkerDrag(marker: Marker?) {
                if (marker != null) {
                    bottomLeft.position = GeoPoint(bottomLeft.position.latitude,marker.position.longitude)
                    topRight.position = GeoPoint(marker.position.latitude,topRight.position.longitude)
                    upperBorder.setPoints(listOf(GeoPoint(topLeft.position),GeoPoint(topRight.position)))
                    lowerBorder.setPoints(listOf(GeoPoint(bottomLeft.position),GeoPoint(bottomRight.position)))
                    leftBorder.setPoints(listOf(GeoPoint(bottomLeft.position),GeoPoint(topLeft.position)))
                    rightBorder.setPoints(listOf(GeoPoint(bottomRight.position),GeoPoint(topRight.position)))
                }
                map.invalidate()

            }

            override fun onMarkerDragEnd(marker: Marker?) {
            }

            override fun onMarkerDragStart(marker: Marker?) {
            }
        }

        /**
         * drag event for bottom right marker
         */

        rightBorder.setPoints(listOf(GeoPoint(bottomRight.position),GeoPoint(topRight.position)))
        map.invalidate()
        map.overlays.add(rightBorder)
        var bottomRightDragEvent = object :Marker.OnMarkerDragListener{
            override fun onMarkerDrag(marker: Marker?) {
                if (marker != null) {
                    bottomLeft.position = GeoPoint(marker.position.latitude,bottomLeft.position.longitude)
                    topRight.position = GeoPoint(topRight.position.latitude,marker.position.longitude)
                    upperBorder.setPoints(listOf(GeoPoint(topLeft.position),GeoPoint(topRight.position)))
                    lowerBorder.setPoints(listOf(GeoPoint(bottomLeft.position),GeoPoint(bottomRight.position)))
                    leftBorder.setPoints(listOf(GeoPoint(bottomLeft.position),GeoPoint(topLeft.position)))
                    rightBorder.setPoints(listOf(GeoPoint(bottomRight.position),GeoPoint(topRight.position)))
                }
                map.invalidate()
            }

            override fun onMarkerDragEnd(marker: Marker?) {
            }

            override fun onMarkerDragStart(marker: Marker?) {
            }
        }

        bottomRight.setOnMarkerDragListener(bottomRightDragEvent)
        topLeft.setOnMarkerDragListener(topLeftDragEvent)
        bottomLeft.setOnMarkerDragListener(bottomLeftDragEvent)
        topRight.setOnMarkerDragListener(topRightDragEvent)
        map.invalidate()


        /**
         * download button
         */
        val sendDownloadRequestButton: Button = view.findViewById(R.id.download)
        sendDownloadRequestButton.setOnClickListener {
            if(rectangle.size == 4) {
                //check internet connection
                if(isNetworkConnected()) {
                    Toast.makeText(activity, "Start downloading graph data", Toast.LENGTH_LONG).show()
                    var minLat = Double.MAX_VALUE
                    var maxLat = Double.MIN_VALUE
                    var minLong = Double.MAX_VALUE
                    var maxLong = Double.MIN_VALUE

                    for (marker in rectangle) {
                        if (marker.position.latitude < minLat) {
                            minLat = marker.position.latitude
                        }
                        if (marker.position.latitude > maxLat) {
                            maxLat = marker.position.latitude
                        }
                        if (marker.position.longitude < minLong) {
                            minLong = marker.position.longitude
                        }
                        if (marker.position.longitude > maxLong) {
                            maxLong = marker.position.longitude
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
     * download graph data in external DOWNLOAD folder and tiles data in cache.
     */
    private fun download(minLat:Double, maxLat:Double, minLong:Double, maxLong:Double){
        //download graph data:
        val dataUrl = "http://192.168.178.21:8000/subgraph?minLat=$minLat&maxLat=$maxLat&minLong=$minLong&maxLong=$maxLong"
        val dataRequest = DownloadManager.Request(Uri.parse(dataUrl))
            .setTitle("graphDataDownloadRequest")
            .setDescription("Downloading graph data")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setAllowedOverMetered(true)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,"osm/data/graphData.txt" )
        val dm = activity?.getSystemService(AppCompatActivity.DOWNLOAD_SERVICE) as DownloadManager
        dm.enqueue(dataRequest)
        val cm = CacheManager(map)
        cm.downloadAreaAsync(activity,downloadArea,3,19)
    }




    /**
     * display the path on the map, given a string of path in lat, long
     * string format:"[[firstLat,_firstLong],_[secondLat,_secondLong],_...,_[lastLat,_lastLong]]" space represented undersocre because easy of read.
     */






    /**
     * check whether is internet connection availiable.
     */
    private fun isNetworkConnected(): Boolean {
        val cm = activity?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.activeNetworkInfo != null && cm.activeNetworkInfo!!.isConnected
    }



}