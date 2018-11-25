package com.socialwork.keys

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.support.design.widget.Snackbar
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import kotlinx.android.synthetic.main.activity_wifi_list.*
import kotlinx.android.synthetic.main.wifi_list_content.view.*
import kotlinx.android.synthetic.main.wifi_list.*
import java.util.*

/**
 * An activity representing a list of Pings. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a [WifiDetailActivity] representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
class WifiListActivity : AppCompatActivity() {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private var twoPane: Boolean = false
//    private var resultList = MutableList<ScanResult>()
    private var resultList = mutableListOf<ScanResult>()
    private lateinit var wifiManager: WifiManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wifi_list)
        configureReceiver()
        setSupportActionBar(toolbar)
        toolbar.title = title

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        if (wifi_detail_container != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            twoPane = true
        }

        wifiManager = this.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        setupRecyclerView(wifi_list)
    }

    //val success = wifiManager.getScanResults()
//    if (!success) {
//        // scan failure handling
//        scanFailure()
//    }

    val wifiScanReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
//            val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
            Log.d("OnRecieve", "I am inside recieve scanner")
            Log.d("Results", wifiManager.scanResults.toString())
            val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
            if (success) {
                scanSuccess()
            } else {
                scanFailure()
            }
        }
    }

    private fun scanSuccess() {
        resultList = wifiManager.scanResults
        unregisterReceiver(wifiScanReceiver)
        Log.d("WifiRESULTsuccess", resultList.toString())
        //... use new scan results ...
    }

    private fun scanFailure() {
        // handle failure: new scan did NOT succeed
        // consider using old scan results: these are the OLD results!
        resultList = wifiManager.scanResults
        Log.d("WifiRESULTfailure", resultList.toString())
        //... potentially use older scan results ...
    }

    private fun configureReceiver() {
        val intentFilter = IntentFilter()
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        registerReceiver(wifiScanReceiver, intentFilter)
        Log.d("Configuring", "Inside broadcast configure")
        //wifiManager.startScan()

//        Handler().postDelayed({
//            stopScanning()
//        }, 10000)
    }

//    private fun stopScanning() {
//        unregisterReceiver(wifiScanReceiver)
//        val axisList = ArrayList<String>()
//        for (result:ScanResult in resultList) {
//            axisList.add(result.SSID)
////            axisList.add(result.level.toString())
//        }
//        Log.d("TESTING", axisList.toString())
//    }﻿

    private fun setupRecyclerView(recyclerView: RecyclerView) {
        recyclerView.adapter = SimpleItemRecyclerViewAdapter(this, resultList, twoPane)
    }

    class SimpleItemRecyclerViewAdapter(
        private val parentActivity: WifiListActivity,
        private val values: List<ScanResult>,
        private val twoPane: Boolean
    ) :
        RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder>() {

        private val onClickListener: View.OnClickListener

        init {
            onClickListener = View.OnClickListener { v ->
                val item = v.tag as ScanResult
                if (twoPane) {
                    val fragment = WifiDetailFragment().apply {
                        arguments = Bundle().apply {
                            putString(WifiDetailFragment.ARG_ITEM_ID, item.SSID)
                        }
                    }
                    parentActivity.supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.wifi_detail_container, fragment)
                        .commit()
                } else {
                    val intent = Intent(v.context, WifiDetailActivity::class.java).apply {
                        putExtra(WifiDetailFragment.ARG_ITEM_ID, item.SSID)
                    }
                    v.context.startActivity(intent)
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.wifi_list_content, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = values[position]
            holder.idView.text = item.SSID
            holder.contentView.text = item.level.toString()

            with(holder.itemView) {
                tag = item
                setOnClickListener(onClickListener)
            }
        }

        override fun getItemCount() = values.size

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val idView: TextView = view.id_text
            val contentView: TextView = view.content
        }
    }
}
