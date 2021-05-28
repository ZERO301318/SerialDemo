package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android_serialport_api.SerialPort;
import android_serialport_api.SerialPortFinder;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    TextView receiver;
    EditText inputText;
    Button sendBtn,openPort;
    Spinner serialPath,bote;
    SerialPortFinder portFinder;
    SerialPort serialPort;
    OutputStream outputStream;
    InputStream inputStream;
    ReadThread readThread;

    String SP_NAME,KEY_PATH,KEY_RATE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bindWidget();
        initSpinner();
        SP_NAME = getResources().getString(R.string.sp_name);
        KEY_PATH = getResources().getString(R.string.sp_path);
        KEY_RATE = getResources().getString(R.string.sp_rate);
        load();

    }

    void initSpinner(){
        ArrayAdapter<String> baudrateAdapter = new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,this.getResources().getStringArray(R.array.baudrates_value));
        baudrateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //获取串口path
        portFinder = new SerialPortFinder();
        ArrayAdapter<String> devPath = new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,portFinder.getAllDevicesPath());
        devPath.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        serialPath = findViewById(R.id.serialPath);
        serialPath.setPrompt("请选择串口");
        serialPath.setAdapter(devPath);
        bote = findViewById(R.id.bote);
        bote.setPrompt("请选择波特率");
        bote.setAdapter(baudrateAdapter);

        serialPath.setOnItemSelectedListener(this);
        bote.setOnItemSelectedListener(this);
    }

    void bindWidget(){
        sendBtn = findViewById(R.id.btnSend);
        openPort = findViewById(R.id.openPort);
        receiver = findViewById(R.id.textView);
        inputText = findViewById(R.id.editTextTextMultiLine);

        //set Listener
        sendBtn.setOnClickListener(this);
        openPort.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            //发送data
            case R.id.btnSend:

                break;
//                打开串口
            case R.id.openPort:
                try {
                    if(true){
                        //open port
                        serialPort = new SerialPort(new File(serialPath.getSelectedItem().toString()), Integer.decode(bote.getSelectedItem().toString()),0);
                        outputStream = serialPort.getOutputStream();
                        inputStream = serialPort.getInputStream();

                        //creat a receive thread
                        readThread = new ReadThread();
                        readThread.start();
                    }else{
                        Toast.makeText(this,"Please select appropriate parameter",Toast.LENGTH_SHORT);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Log.d("openPort:", "successful!");
                break;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    class ReadThread extends Thread{
        @Override
        public void run() {
            super.run();
            while(!isInterrupted()) {
                int size;
                try {
                    byte[] buffer = new byte[64];
                    if (inputStream == null) {
                        Log.d("inputStream", "is null");
                        return;
                    }
                    Log.d("RUN", "aaa");
                    size = inputStream.read(buffer);
//                    Log.d("TAG",String.valueOf(size));
                    if (size > 0) {
                        onDataReceived(buffer, 2);
                    }
                } catch (IOException e) {
                    Log.d("TAG",e.getMessage());
                    e.printStackTrace();
                    return;
                }
            }
        }

        protected void onDataReceived(final byte[] buffer, final int size) {
            runOnUiThread(new Runnable() {
                public void run() {
                    if (receiver != null) {
                        Log.d("onDataReceived:",new String(buffer, 0, size));
//                        receiver.append(new String(buffer, 0, size));
                    }
                }
            });
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        save();
        closeSerialPort();
    }
    void save(){
        SharedPreferences sp = getSharedPreferences(SP_NAME,MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(KEY_PATH,serialPath.getSelectedItemPosition());
        editor.putInt(KEY_RATE,bote.getSelectedItemPosition());
        editor.apply();
    }

    void load(){
        SharedPreferences sp = getSharedPreferences(SP_NAME,MODE_PRIVATE);
        //设置选中项
        serialPath.setSelection(sp.getInt(KEY_PATH,0));
        bote.setSelection(sp.getInt(KEY_RATE,0));
    }

    //close serial port
    void closeSerialPort(){
        if(serialPort != null){
            serialPort.close();
            serialPort = null;
        }
    }

}
