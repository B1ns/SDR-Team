package com.example.rfp.view.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
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
import com.karumi.dexter.listener.single.DialogOnDeniedPermissionListener
import com.karumi.dexter.listener.single.PermissionListener
import io.socket.client.Socket
import org.jetbrains.anko.startActivity
import java.security.MessageDigest

class MainActivity : AppCompatActivity() {

    private val mainFragment: MainFragment = MainFragment()
    private val graphFragment: GraphFragment = GraphFragment()
    private val bluetoothFragment: BlueToothFragment = BlueToothFragment()
    private val moreFragment: MoreFragment = MoreFragment()

    private var lastColor: Int = 0
    var NETWORK_STATE_CODE = 0

    lateinit var mSocket: Socket

    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        getPermission()
        setOnClickListener()
        setChangeUI()
        bluetoothPermissionsCheck()

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
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_NETWORK_STATE
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

    private fun bluetoothPermissionsCheck() {
        val dialogPermissionListener: PermissionListener =
            DialogOnDeniedPermissionListener.Builder.withContext(applicationContext)
                .withTitle("Bluetooth 권한 설정")
                .withMessage("블루투스를 사용하기 위한 권한 입니다. 설정하십쇼").withButtonText(android.R.string.ok)
                .withIcon(R.mipmap.ic_main)
                .build()

        Dexter.withContext(applicationContext)
            .withPermission(Manifest.permission.BLUETOOTH_ADMIN)
            .withListener(dialogPermissionListener).check()
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

    fun changeUI() {
        main_navigation.setItemSelected(R.id.main)
    }

    fun successUI() {
        main_navigation.setItemSelected(R.id.graph)
    }

    fun openFragment(fragment: Fragment) {

        val transactions = supportFragmentManager.beginTransaction()
        transactions.replace(R.id.mainFragmentFrame, fragment)
        transactions.addToBackStack(null)
        transactions.commit()

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            NETWORK_STATE_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}