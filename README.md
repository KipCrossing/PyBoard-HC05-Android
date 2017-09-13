# PyBoard-HC05-Android
Micropython code for the HC05 Bluetooth adaptor and an example application for android devices made specifically for the HC05.


##  main.py

```python
# code by Kipling
print("(Main program started)")

import pyb

# HC05 connection with the PyBoard
#
# HC06 - PyBoard
# --------------
#  GND - GND
#  VCC - VCC
#  RXD - X3 (TX)
#  TXD - x4 (RX)


blue_uart = pyb.UART(2, 9600)
blue_uart.init(9600, bits=8, stop=1, parity=None)
#pyb.repl_uart(blue_uart)

while True:
    if blue_uart.any():
        line = blue_uart.readline()
        line = str(line,'utf-8')
        if line[-5:-1] == "BTM-":
            if line[-5:] == "BTM-U":
                print("UP")
                blue_uart.write("GO UP")

                # Write you code here

            elif line[-5:] == "BTM-D":
                print("DOWN")
                blue_uart.write("GO DOWN")

                # Write you code here

            elif line[-5:] == "BTM-L":
                print("LEFT")
                blue_uart.write("GO LEFT")

                # Write you code here

            elif line[-5:] == "BTM-R":
                print("RIGHT")
                blue_uart.write("GO RIGHT")

                # Write you code here

        else:
            print(line)
            blue_uart.write("You sent: " + line)

            # Write you code here


```

## MainActivity.java

```java

package com.example.kipling.bletooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;

import android.widget.EditText;

import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

import static android.content.ContentValues.TAG;



public class MainActivity extends Activity  {

    EditText bluetoothSend;
    private BluetoothAdapter bluetoothAdapter;
    private Set<BluetoothDevice>pairedDevices;

    private static UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private OutputStream mmOutStream;
    private InputStream mmInStream;

    private BluetoothSocket mmSocket;
    private byte[] mmBuffer; // mmBuffer store for the stream
    private TextView textView;

    private Handler mHandler; // handler that gets info from Bluetooth service


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bluetoothSend = (EditText) findViewById(R.id.bluetooth_word);

        textView = (TextView) findViewById(R.id.textView2);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mmSocket = null;



        on();
        connector();
        th.start();

    }




    // This is to turn on the bluetooth adapter if it is not already on
    public void on(){
        if (!bluetoothAdapter.isEnabled()) {
            Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOn, 0);
            Toast.makeText(getApplicationContext(), "Turned on",Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(), "Already on", Toast.LENGTH_LONG).show();
        }
    }


    // Call this to turn off the bluetooth adapter (not used)
    public void off(View v){
        bluetoothAdapter.disable();
        Toast.makeText(getApplicationContext(), "Turned off" ,Toast.LENGTH_LONG).show();
    }



    // If connection is not established on app startup (onCreate) try again with this method
    public void connect(View v){


        try{
            String name = "CONNECTED";
            byte[] bytes = name.getBytes();
            mmOutStream.write(bytes);
        }catch (IOException e){
            Toast.makeText(getApplicationContext(), "Connecting..." ,Toast.LENGTH_LONG).show();
            connector();


        }
    }


    public void connector(){

        OutputStream tmpOut = null;
        InputStream tmpIn = null;

        // Get list of paired devices

        BluetoothSocket tmp = null;

        String dname;


        pairedDevices = bluetoothAdapter.getBondedDevices();
        BluetoothDevice device = null;
        if(pairedDevices.size() >0) {
            for (BluetoothDevice bt : pairedDevices) {
                Log.d("TAG", bt.getName());
                dname = bt.getName();
                if (dname.equals("HC-05")) {
                    device = bt;
                    Log.d("TAG", "HC-05 PARED!!!");
                    //Toast.makeText(getApplicationContext(), device.getName(), Toast.LENGTH_LONG).show();


                } else {
                    Log.d("TAG", "Not HC-05");
                }

            }

            try {
                // MY_UUID is the app's UUID string, also used by the client code.
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);

            } catch (IOException e) {
                Log.d("TAG", "Socket's listen() method failed", e);
                Toast.makeText(getApplicationContext(), "Error 1" ,Toast.LENGTH_LONG).show();
            }
            mmSocket = tmp;


            bluetoothAdapter.cancelDiscovery();



            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                mmSocket.connect();


                Log.d("TAG", "Socket connected!!!!!");
                Toast.makeText(getApplicationContext(), "Connected" ,Toast.LENGTH_LONG).show();
            } catch (IOException connectException) {}



            try {
                tmpIn = mmSocket.getInputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating input stream", e);
            }


            try {

                tmpOut = mmSocket.getOutputStream();


            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating output stream", e);
                Toast.makeText(getApplicationContext(), "Error 2" ,Toast.LENGTH_LONG).show();
            }

            mmOutStream = tmpOut;
            mmInStream = tmpIn;



        }else{
            Log.d("TAG", "No devices");
            Toast.makeText(getApplicationContext(), "HC-05 is not pared", Toast.LENGTH_LONG).show();
        }




    }


    // thread to listen to the input data from HC05 (not perfect)
    Thread th = new Thread(new Runnable() {
        public void run() {


            mmBuffer = new byte[4096];
            int numBytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs.
            while (true) {
                try {
                    if(mmInStream.available()>2) {
                        Log.d("TAG","mmInStream.available()>2");

                        // Read from the InputStream.
                        numBytes = mmInStream.read(mmBuffer);



                        final String readMessage = new String(mmBuffer, 0, numBytes);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                textView.setText(readMessage);
                            }
                        });



                        Log.d("TAG", readMessage);
                    }else{
                        SystemClock.sleep(100);
                        Log.d("TAG", "No Data");
                    }





                } catch (IOException e) {
                    Log.d("TAG", "Input stream was disconnected", e);
                    break;
                }
            }


        }
    });





        // Receives commands from the UI to send to HC05
    public void write(View v) {

        String name = bluetoothSend.getText().toString();
        byte[] bytes = name.getBytes();
        Log.d("TAG","Pressed: "+name);

        try {
            mmOutStream.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("TAG","");
            Toast.makeText(getApplicationContext(), "Send failed" ,Toast.LENGTH_LONG).show();
        }


    }


    public  void up(View v){
        String name = "BTM-U";
        byte[] bytes = name.getBytes();
        Log.d("TAG","Pressed: "+name);
        try {
            mmOutStream.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("TAG",""+e);
            Toast.makeText(getApplicationContext(), "Send failed" ,Toast.LENGTH_LONG).show();
        }

    }

    public  void down(View v){
        String name = "BTM-D";
        byte[] bytes = name.getBytes();
        Log.d("TAG","Pressed: "+name);
        try {
            mmOutStream.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("TAG",""+e);
            Toast.makeText(getApplicationContext(), "Send failed" ,Toast.LENGTH_LONG).show();
        }

    }


    public  void left(View v){
        String name = "BTM-L";
        byte[] bytes = name.getBytes();
        Log.d("TAG","Pressed: "+name);
        try {
            mmOutStream.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("TAG",""+e);
            Toast.makeText(getApplicationContext(), "Send failed" ,Toast.LENGTH_LONG).show();
        }

    }

    public  void right(View v){
        String name = "BTM-R";
        byte[] bytes = name.getBytes();
        Log.d("TAG","Pressed: "+name);
        try {
            mmOutStream.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("TAG",""+e);
            Toast.makeText(getApplicationContext(), "Send failed" ,Toast.LENGTH_LONG).show();
        }

    }



}


```

## activity_main.xml

```xml

<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".MainActivity"
    android:transitionGroup="true">

    <TextView android:text="HC05"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:id="@+id/textview"
        android:textSize="35dp"
        android:layout_alignParentTop="true"
        android:layout_centerHorizontal="true" />




    <ImageView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:id="@+id/imageView"
        android:layout_below="@+id/textView"
        android:layout_centerHorizontal="true"
        android:theme="@style/Base.TextAppearance.AppCompat" />



    <Button
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Connect"
        android:onClick="connect"
        android:id="@+id/button3"
        android:layout_below="@+id/textview"
        android:layout_alignLeft="@+id/buttonLEFT"
        android:layout_alignStart="@+id/buttonLEFT" />



    <Button
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="UP"
        android:onClick="up"
        android:id="@+id/buttonUP"
        android:layout_marginTop="11dp"
        android:layout_below="@+id/textview"
        android:layout_alignLeft="@+id/buttonDOWN"
        android:layout_alignStart="@+id/buttonDOWN" />

    <Button
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="DOWN"
        android:onClick="down"
        android:id="@+id/buttonDOWN"
        android:layout_below="@+id/buttonLEFT"
        android:layout_centerHorizontal="true" />
    <Button
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="LEFT"
        android:onClick="left"
        android:id="@+id/buttonLEFT"
        android:layout_below="@+id/buttonUP"
        android:layout_alignParentLeft="true"
        android:layout_alignParentStart="true"
        android:layout_marginLeft="11dp"
        android:layout_marginStart="11dp" />

    <Button
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="RIGHT"
        android:onClick="right"
        android:id="@+id/buttonRIGHT"
        android:layout_below="@+id/buttonUP"
        android:layout_alignParentRight="true"
        android:layout_alignParentEnd="true"
        android:layout_marginRight="13dp"
        android:layout_marginEnd="13dp" />



    <EditText
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:id="@+id/bluetooth_word"
        android:text="Input text"
        android:layout_centerVertical="true"
        android:layout_alignParentLeft="true"
        android:layout_alignParentStart="true"
        android:layout_toLeftOf="@+id/buttonSEND"
        android:layout_toStartOf="@+id/buttonSEND" />

    <Button
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="SEND"
        android:onClick="write"
        android:id="@+id/buttonSEND"
        android:layout_alignBottom="@+id/bluetooth_word"
        android:layout_alignLeft="@+id/buttonRIGHT"
        android:layout_alignStart="@+id/buttonRIGHT" />


    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Received Data"
        android:id="@+id/textView2"
        android:textColor="#ff34ff06"
        android:textSize="25dp"
        android:layout_alignParentBottom="true"
        android:layout_alignRight="@+id/buttonRIGHT"
        android:layout_alignEnd="@+id/buttonRIGHT"
        android:layout_alignParentLeft="true"
        android:layout_alignParentStart="true"
        android:layout_below="@+id/bluetooth_word" />

</RelativeLayout>

```
## AndroidManifest.xml

```xml

<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.example.kipling.bletooth">

    <uses-permission android:name="android.permission.BLUETOOTH"/>
    <uses-permission android:name="android.permission.BLUETOOTH_ADMIN"/>

    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/AppTheme">
        <activity android:name=".MainActivity">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />

                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>

</manifest>

```
