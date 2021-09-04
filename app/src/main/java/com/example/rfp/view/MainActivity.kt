package com.example.rfp.view

import android.Manifest
import android.bluetooth.BluetoothAdapter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import android.content.Intent
import android.widget.ArrayAdapter

import android.bluetooth.BluetoothDevice
import android.transition.ChangeBounds
import android.transition.TransitionManager
import androidx.core.content.ContextCompat
import com.example.rfp.R
import kotlinx.android.synthetic.main.activity_main.*

import com.example.rfp.data.applyWindowInsets
import com.example.rfp.data.colorAnimation
import com.ismaeldivita.chipnavigation.ChipNavigationBar

class MainActivity : AppCompatActivity() {

    private lateinit var btAdapter: BluetoothAdapter
    private val REQUEST_ENABLE_BT = 1

    lateinit var pairedDevices: Set<BluetoothDevice>
    lateinit var btArrayAdapter: ArrayAdapter<String>
    lateinit var deviceAddressArray: ArrayList<String>

    private var lastColor: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btArrayAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1)
        deviceAddressArray = ArrayList()

        getPermission()
        arduinoBluetooth()

        lastColor = ContextCompat.getColor(this, R.color.blank)

        main_navigation.setOnItemSelectedListener(object : ChipNavigationBar.OnItemSelectedListener {
            override fun onItemSelected(id: Int) {
                val option = when (id) {
                    R.id.home -> R.color.home to "Home"
                    R.id.bluetooth -> R.color.bluetooth to "Bluetooth"
                    R.id.Graph -> R.color.graph to "Graph"
                    R.id.more -> R.color.more to "more"
                    else -> R.color.white to ""
                }
                val color = ContextCompat.getColor(this@MainActivity, option.first)
                container.colorAnimation(lastColor, color)
                lastColor = color
            }
        })

        expand_button.setOnClickListener {
            if (main_navigation.isExpanded()) {
                TransitionManager.beginDelayedTransition(container, ChangeBounds())
                main_navigation.collapse()
            } else {
                TransitionManager.beginDelayedTransition(container, ChangeBounds())
                main_navigation.expand()
            }
        }

        expand_button.applyWindowInsets(bottom = true)

    }

    private fun arduinoBluetooth() {
        btAdapter = BluetoothAdapter.getDefaultAdapter()
        if (!btAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }
    }

    private fun getPermission() {
        Dexter.withContext(this).withPermissions(
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        ).withListener(object : MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport) {
            }

            override fun onPermissionRationaleShouldBeShown(
                permissions: List<PermissionRequest>,
                token: PermissionToken
            ) {
            }
        }).check()

    }
}