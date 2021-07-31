package com.example.kotlintest.bluetoothchat

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.marginBottom
import androidx.transition.TransitionManager
import com.example.kotlintest.ExceptionHandler
import com.example.kotlintest.HelperConstant
import com.example.kotlintest.R
import kotlinx.android.synthetic.main.activity_bluetooth_main.*


private lateinit var service: BluetoothChatService
fun logit(msg: Any?) {
    if (HelperConstant.debug) {
        val trace: StackTraceElement? = Thread.currentThread().stackTrace[3]
        val lineNumber = trace?.lineNumber
        val methodName = trace?.methodName
        val className = trace?.fileName?.replaceAfter(".", "")?.replace(".", "")
        Log.d("Line $lineNumber", "↓↓↓  $className::$methodName()  ↓↓↓")
        Log.e("MSG", "$msg")
    }
}

class BluetoothMainActivity : AppCompatActivity() {
    private var connectedTo: String? = ""
    private var handler = object : Handler(Looper.myLooper()!!) {
        override fun handleMessage(msg: Message) {
            if (!mIsBluetoothOn) {
                return
            }
            logit(msg)
            when (msg.what) {
                Constants.MESSAGE_TOAST -> {
                    logit(msg.data.getString(Constants.TOAST))
                    setStatus(msg.data.getString(Constants.TOAST, ""))
                }
                Constants.MESSAGE_WRITE -> {
                    sendReceivedMsgToFragment(this@BluetoothMainActivity sendFromBuffMsg msg)
                }
                Constants.MESSAGE_READ -> {
                    sendIncomingMsgToFragment(this@BluetoothMainActivity sendFromBuffMsg msg)
                }
                Constants.MESSAGE_STATE_CHANGE -> {
                    toolbar.title = when (msg.arg1) {
                        0 -> "Disconnected"
                        1 -> "Waiting for connection"
                        2 -> "Connecting"
                        3 -> {
                            startChatFragment()
                            "Connected to $connectedTo"
                        }
                        else -> getString(R.string.app_name)
                    }
                    if (msg.arg1 in 0..2)
                        removeThisFrag()
                }
                Constants.MESSAGE_DEVICE_NAME -> {
                    connectedTo = msg.data.getString(Constants.DEVICE_NAME)
                }
                //setStatus(msg)
            }
            //setStatus(msg)
        }
    }

    private fun removeThisFrag() {
        supportFragmentManager.findFragmentById(contentMain.id)?.apply {
            supportFragmentManager.beginTransaction().remove(this).commitAllowingStateLoss()
        }
    }

    private lateinit var bluetoothAdapter: BluetoothAdapter
    private var mIsBluetoothOn: Boolean = false
    private val listOfDevices = mutableListOf<Pair<String, String>>()

    private fun startChatFragment() {
        supportFragmentManager.beginTransaction()
            .add(contentMain.id, ChatFragment.newInstance()).addToBackStack(null).commit()
    }

    private fun sendIncomingMsgToFragment(string: String) {
        supportFragmentManager.run {
            findFragmentById(contentMain.id)?.let {
                val bundle = Bundle()
                bundle.putString(Constants.KEY_MESSAGE_SENT, string)
                //logit(bundle)
                setFragmentResult(Constants.KEY_FRAGMENT_SENT_REQUEST, bundle)
            }
        }
    }

    private fun sendReceivedMsgToFragment(string: String) {
        supportFragmentManager.run {
            findFragmentById(contentMain.id)?.let {
                val bundle = Bundle()
                bundle.putString(Constants.KEY_MESSAGE_RECEIVED, string)
                setFragmentResult(Constants.KEY_FRAGMENT_RECEIVED_REQUEST, bundle)
            }
        }
    }

    private fun setupList() {
        listOfDevices.clear()
        bluetoothAdapter.bondedDevices?.forEach {
            listOfDevices.add(it.name to it.address)
        }

        listViewBtDevices.adapter =
            ArrayAdapter(this, android.R.layout.simple_list_item_1, listOfDevices)
    }

    var currentHeight: Float = 0f
    private val bluetoothReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(
            context: Context,
            intent: Intent
        ) {
            intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1).run {
                if (this == BluetoothAdapter.STATE_OFF || this == BluetoothAdapter.STATE_ON) {
                    mIsBluetoothOn = this == BluetoothAdapter.STATE_ON
                    setStatus(if (mIsBluetoothOn) "Bluetooth enabled" else "Bluetooth disabled")
                    logit("Bluetooth on? $mIsBluetoothOn")
                    if (mIsBluetoothOn) {
                        toolbar.title = getString(R.string.app_name)
                        setupList()
                    } else {
                        removeThisFrag()
                        try {
                            service.connectionLost()
                        } catch (_: Exception) {
                        }
                        toolbar.title = "Bluetooth disabled"
                    }
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onStart() {
        super.onStart()
        setupList()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Thread.setDefaultUncaughtExceptionHandler(ExceptionHandler(this));

        setContentView(R.layout.activity_bluetooth_main)
        setSupportActionBar(toolbar)
        supportActionBar?.setHomeAsUpIndicator(
            ContextCompat.getDrawable(
                this,
                R.drawable.ic_baseline_arrow_back_24
            )
        )
        supportActionBar?.setDisplayShowHomeEnabled(false)
        getBluetoothManager().adapter?.let {
            bluetoothAdapter = it
        } ?: also {
            Toast.makeText(it, "Bluetooth adapter not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        statusOnGoing.post {
            currentHeight = (statusOnGoing.height + statusOnGoing.marginBottom).toFloat()
        }

        registerReceiver(bluetoothReceiver, IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED))

        mIsBluetoothOn = bluetoothAdapter.isEnabled.run {
            if (!this)
                bluetoothAdapter.enable()
            this
        }
        //bluetoothAdapter.startDiscovery()
        service = BluetoothChatService(this, handler)
        /*bluetoothAdapter.bondedDevices?.forEach {
            logit(it.name + it.address)
        }*/
        listViewBtDevices.setOnItemClickListener { _, _, i, _ ->
            run {
                listOfDevices[i].run {
                    this@BluetoothMainActivity connectToMac this.second
                    setStatus("Connecting to ${this.first}")
                }
            }
        }

        // pixel 22:22:31:7E:53:67
        // 2nd 5C:70:A3:EE:93:F7
        /*oof.setOnClickListener {
            this transferText "Hello..."
        }*/
    }

    override fun onBackPressed() {
        logit(supportFragmentManager.backStackEntryCount)
        if (supportFragmentManager.backStackEntryCount > 0) {
            if (!isDestroyed) {
                AlertDialog.Builder(this).run {
                    setMessage("Close chat?")
                    setNegativeButton("Cancel", null)
                    setPositiveButton(
                        "Yes"
                    ) { _, _ ->
                        run {
                            removeThisFrag()
                            supportFragmentManager.popBackStack()
                            service.connectionLost()
                        }
                    }
                    show()
                }
            }
        } else
            super.onBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(bluetoothReceiver)
        service.stop()
    }

    infix fun transferText(string: String) = service.write(string.toByteArray())

    /***
     * Pass String of MAC ADDRESS
     * @receiver bluetoothAdapter
     * Takes that string to connect
     */
    private infix fun connectToMac(macAdd: String) =
        service.connect(bluetoothAdapter.getRemoteDevice(macAdd))


    fun setStatus(x: Any?) {
        statusOnGoing.run {
            post {
                TransitionManager.beginDelayedTransition(this.parent as ViewGroup)
                text = x.toString()
                visibility = View.VISIBLE
                postDelayed({
                    TransitionManager.beginDelayedTransition(this.parent as ViewGroup)
                    visibility = View.GONE
                }, 2000)
            }
        }
    }

    private fun getBluetoothManager(): BluetoothManager =
        this.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

    private infix fun sendFromBuffMsg(message: Message): String {
        val bytes = message.obj as ByteArray
        return if (message.what == Constants.MESSAGE_WRITE) {
            String(bytes)
        } else {
            String(bytes, 0, message.arg1)
        }
    }
}

