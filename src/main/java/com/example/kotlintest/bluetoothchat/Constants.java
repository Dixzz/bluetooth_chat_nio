package com.example.kotlintest.bluetoothchat;

public interface Constants {

    // Message types sent from the BluetoothChatService Handler
    int MESSAGE_STATE_CHANGE = 1;
    int MESSAGE_READ = 2;
    int MESSAGE_WRITE = 3;
    int MESSAGE_DEVICE_NAME = 4;
    int MESSAGE_TOAST = 5;

    // Key names received from the BluetoothChatService Handler
    String DEVICE_NAME = "device_name";
    String TOAST = "toast";

    String KEY_MESSAGE_RECEIVED = "message_received";
    String KEY_MESSAGE_SENT = "message_sent";
    String KEY_FRAGMENT_RECEIVED_REQUEST = "fragment_received_request";
    String KEY_FRAGMENT_SENT_REQUEST = "fragment_sent_request";
}
