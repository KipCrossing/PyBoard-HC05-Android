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


