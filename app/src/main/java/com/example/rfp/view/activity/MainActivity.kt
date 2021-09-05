package com.example.rfp.view.activity

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
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.rfp.R
import kotlinx.android.synthetic.main.activity_main.*

import com.example.rfp.data.colorAnimation
import com.example.rfp.view.fragment.BlueToothFragment
import com.example.rfp.view.fragment.GraphFragment
import com.example.rfp.view.fragment.MainFragment
import com.example.rfp.view.fragment.MoreFragment
import com.ismaeldivita.chipnavigation.ChipNavigationBar
import org.jetbrains.anko.startActivity

class MainActivity : AppCompatActivity() {

    private val mainFragment: MainFragment = MainFragment()
    private val graphFragment: GraphFragment = GraphFragment()
    private val bluetoothFragment: BlueToothFragment = BlueToothFragment()
    private val moreFragment: MoreFragment = MoreFragment()

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
        setOnClickListener()
        setChangeUI()

    }


    private fun setOnClickListener() {
        setting_btn.setOnClickListener {
            startActivity<SettingActivity>()
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

    private fun setChangeUI() {

        main_navigation.expand()

        lastColor = ContextCompat.getColor(this, R.color.blank)

        main_navigation.setItemSelected(R.id.main)

        main_navigation.setOnItemSelectedListener(object :
            ChipNavigationBar.OnItemSelectedListener {
            override fun onItemSelected(id: Int) {
                val option = when (id) {
                    R.id.main -> R.color.main to "Home"
                    R.id.bluetooth -> R.color.bluetooth to "Bluetooth"
                    R.id.graph -> R.color.graph to "Graph"
                    R.id.more -> R.color.more to "more"
                    else -> R.color.white to ""
                }
                val color = ContextCompat.getColor(this@MainActivity, option.first)
                container.colorAnimation(lastColor, color)
                lastColor = color
            }
        })

        openFragment(mainFragment)

        main_navigation.setOnItemSelectedListener {
            when (it) {
                R.id.main -> openFragment(mainFragment)
                R.id.bluetooth -> openFragment(bluetoothFragment)
                R.id.graph -> openFragment(graphFragment)
                R.id.more -> openFragment(moreFragment)
            }
        }
    }

    private fun openFragment(fragment: Fragment) {

        val transactions = supportFragmentManager.beginTransaction()
        transactions.replace(R.id.mainFragmentFrame, fragment)
        transactions.addToBackStack(null)
        transactions.commit()

    }

    private fun arduinoBluetooth() {

        btAdapter = BluetoothAdapter.getDefaultAdapter()
        if (!btAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }
    }
}