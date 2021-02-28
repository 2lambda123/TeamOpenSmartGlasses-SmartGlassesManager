package com.example.wearableaidisplaymoverio;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadLocalRandom;

//singleton clientsocket class
public class ClientSocket {
    //broadcast intent string
    public final static String ACTION_RECEIVE_MESSAGE = "com.example.wearableaidisplaymoverio.ACTION_RECEIVE_DATA";
    public final static String EXTRAS_MESSAGE = "com.example.wearableaidisplaymoverio.EXTRAS_MESSAGE";

    public static String TAG = "WearableAiDisplayMoverio";
    //singleton instance
    private static ClientSocket clientsocket;
    //socket data
    static Thread SocketThread = null;
    static Thread ReceiveThread = null;
    static Thread SendThread = null;
    static private DataOutputStream output;
    //ids of message types
    static final byte [] eye_contact_info_id = {0x12, 0x13};
    static final byte [] img_id = {0x01, 0x10}; //id for images
    static final byte [] heart_beat_id = {0x19, 0x20}; //id for heart beat
    static final byte [] ack_id = {0x13, 0x37};


    //static private BufferedReader input;
    static private DataInputStream input;
    static String SERVER_IP = "192.168.1.175"; //temporarily hardcoded
    static int SERVER_PORT = 4567;
    private static int mConnectState = 0;

    private static boolean gotAck = false;

    //our actual socket connection object
    private static Socket socket;

    //remember how many packets we have in our buffer
    private static int packets_in_buf = 0;

    //queue of data to send through the socket
    private static BlockingQueue<byte []> queue;

    //we need a reference to the context of whatever called this class so we can send broadcast updates on receving new info
    private static Context mContext;

    private ClientSocket(Context context){
        //create send queue and a thread to handle sending
        queue = new ArrayBlockingQueue<byte[]>(50);

        //service context set 
        mContext = context;
    }

    public static ClientSocket getInstance(Context c){
        if (clientsocket == null){
            clientsocket = new ClientSocket(c);
        }
        return clientsocket;
    }

    public static ClientSocket getInstance(){
        if (clientsocket == null){
            return null;
        }
        return clientsocket;
    }

    public void startSocket(){
        //start first socketThread
        if (socket == null) {
            mConnectState = 1;
            Log.d(TAG, "onCreate starting");
            SocketThread = new Thread(new SocketThread());
            SocketThread.start();
            Log.d(TAG, "STARTED");

            //setup handler to handle keeping connection alive, all subsequent start of SocketThread
            //start a new handler thread to send heartbeats
            HandlerThread thread = new HandlerThread("HeartBeater");
            thread.start();
            Handler handler = new Handler(thread.getLooper());
            final int delay = 1000;
            final int min_delay = 3000;
            final int max_delay = 4000;
            Random rand = new Random();
            handler.postDelayed(new Runnable() {
                public void run() {
                    heartBeat();
                    //random delay for heart beat so as to disallow synchronized failure between client and server
                    int random_delay = rand.nextInt((max_delay - min_delay) + 1) + min_delay;
                    handler.postDelayed(this, random_delay);
                }
            }, delay);
        }
    }

    private void heartBeat(){
        //check if we are still connected.
        //if not , reconnect,
        //we don't need to actively send heart beats from the client, as it's assumed that we are ALWAYS streaming data. Later, if we have periods of time where no data is sent, we will want to send a heart beat perhaps. but the client doesn't really need to, we just need to check if we are still connected
        if (mConnectState == 0) {
            restartSocket();
        }
    }

    public static void restartSocket() {
        Log.d(TAG, "Restarting socket");
        mConnectState = 1;
        if (socket != null && (!socket.isClosed())){
            try {
                output.close();
                input.close();
                socket.close();
            } catch (IOException e) {
                System.out.println("FAILED TO CLOSE SOCKET, SOMETHING IS WRONG");
            }
        }


//        //kill threads
//        stopThread(SendThread);
//        stopThread(ReceiveThread);

        //restart socket thread
        SocketThread = new Thread(new SocketThread());
        SocketThread.start();
    }

    public static void stopThread(Thread thread){
        if(thread!=null){
            thread.interrupt();
            thread = null;
        }
    }

    public static  byte[] my_int_to_bb_be(int myInteger){
        return ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(myInteger).array();
    }

    public void sendBytes(byte[] id, byte [] data){
        //first, send hello
        byte [] hello = {0x01, 0x02, 0x03};
        //then send length of body
        byte[] len;
        if (data != null) {
             len = my_int_to_bb_be(data.length);
        } else {
            len = my_int_to_bb_be(0);
        }
        //then send id of message type
        byte [] msg_id = id;
        //then send data
        byte [] body = data;
        //then send end tag - eventually make this unique to the image
        byte [] goodbye = {0x3, 0x2, 0x1};
        //combine those into a payload
        ByteArrayOutputStream outputStream;
        try {
            outputStream = new ByteArrayOutputStream();
            outputStream.write(hello);
            outputStream.write(len);
            outputStream.write(msg_id);
            if (body != null) {
                outputStream.write(body);
            }
            outputStream.write(goodbye);
        } catch (IOException e){
            mConnectState = 0;
            return;
        }
        byte [] payload = outputStream.toByteArray();

        //send it in a background thread
        //new Thread(new SendThread(payload)).start();
        queue.add(payload);
    }

    public int getConnected(){
        return mConnectState;
    }

    static class SocketThread implements Runnable {
        public void run() {
            try {
                System.out.println("TRYING TO CONNECT");
                socket = new Socket(SERVER_IP, SERVER_PORT);
                System.out.println("CONNECTED!");
                output = new DataOutputStream(socket.getOutputStream());
                //input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                input = new DataInputStream(new DataInputStream(socket.getInputStream()));
                mConnectState = 2;
                Log.d(TAG, "SET MCONNECT STATE TO 2");
                //make the threads that will send and receive
                if (ReceiveThread == null) { //if the thread is null, make a new one (the first one)
                    ReceiveThread = new Thread(new ReceiveThread());
                    ReceiveThread.start();
                } else if (!ReceiveThread.isAlive()) { //if the thread is not null but it's dead, let it join then start a new one
                    Log.d(TAG, "IN SocketThread< WAITING FOR receive THREAD JOING");
                    try {
                        ReceiveThread.join(); //make sure socket thread has joined before throwing off a new one
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Log.d(TAG, "receive JOINED");
                    ReceiveThread = new Thread(new ReceiveThread());
                    ReceiveThread.start();
                }
                if (SendThread == null) { //if the thread is null, make a new one (the first one)
                    SendThread = new Thread(new SendThread());
                    SendThread.start();
                } else if (!SendThread.isAlive()) { //if the thread is not null but it's dead, let it join then start a new one
                    Log.d(TAG, "IN SocketThread< WAITING FOR send THREAD JOING");
                    try {
                        SendThread.join(); //make sure socket thread has joined before throwing off a new one
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Log.d(TAG, "send JOINED");
                    SendThread =  new Thread(new SendThread());
                    SendThread.start();
                }
            } catch (IOException e) {
                Log.d(TAG, "Connection Refused on socket");
                e.printStackTrace();
                mConnectState = 0;
            }
        }
    }

    public static int my_bb_to_int_be(byte [] byteBarray){
        return ByteBuffer.wrap(byteBarray).order(ByteOrder.BIG_ENDIAN).getInt();
    }

    static class ReceiveThread implements Runnable {
        @Override
        public void run() {
            while (true) {
                if (mConnectState != 2){
                    System.out.println("MCONNECTED IS FALSE IN REEIVE THREAD, BREAKING");
                    break;
                }
                byte b1, b2;
                byte [] raw_data = null;
                byte goodbye1, goodbye2, goodbye3;
                //just read in data here
                try {
                    byte hello1 = input.readByte(); // read hello of incoming message
                    byte hello2 = input.readByte(); // read hello of incoming message
                    byte hello3 = input.readByte(); // read hello of incoming message

                    //make sure header is verified
                    if (hello1 != 0x01 || hello2 != 0x02 || hello3 != 0x03){
                        Log.d(TAG, "Socket hello header broken, restarting socket");
                        break;
                    }
                    //length of body
                    int body_len = input.readInt();
                    Log.d(TAG,"BODY LENGTH IS " + body_len);

                    //read in message id bytes
                    b1 = input.readByte();
                    b2 = input.readByte();

                    //read in message body (if there is one)
                    if (body_len > 0){
                        raw_data = new byte[body_len];
                        input.readFully(raw_data, 0, body_len); // read the body
                    }

                    goodbye1 = input.readByte(); // read goodbye of incoming message
                    goodbye2 = input.readByte(); // read goodbye of incoming message
                    goodbye3 = input.readByte(); // read goodbye of incoming message
                } catch (IOException e) {
                    e.printStackTrace();
                    mConnectState = 0;
                    break;
                }

                //make sure footer is verified
                System.out.println("GOODBYE 1 IS " + goodbye1);
                System.out.println("GOODBYE 2 IS " + goodbye2);
                System.out.println("GOODBYE 3 IS " + goodbye3);
                if (goodbye1 != 0x03 || goodbye2 != 0x02 || goodbye3 != 0x01) {
                    Log.d(TAG, "Socket stream - footer broken, restarting socket");
                    break;
                }

                //then process the data
                if ((b1 == ack_id[0]) && (b2 == ack_id[1])){ //got ack response
                    System.out.println("ACK RECEIVED");
                    gotAck = true;
                } else if ((b1 == heart_beat_id[0]) && (b2 == heart_beat_id[1])) { //heart beat check if alive
                    //got heart beat, respond with heart beat
                    clientsocket.sendBytes(heart_beat_id, null);
                } else if ((b1 == eye_contact_info_id[0]) && (b2 == eye_contact_info_id[1])){ //we got a message with information to display
                    String message = Integer.toString(my_bb_to_int_be(raw_data));
                    final Intent intent = new Intent();
                    intent.putExtra(ClientSocket.EXTRAS_MESSAGE, message);
                    intent.setAction(ClientSocket.ACTION_RECEIVE_MESSAGE);
                    mContext.sendBroadcast(intent); //eventually, we won't need to use the activity context, as our service will have its own context to send from
                }else {
                    System.out.println("BAD SIGNAL, RECONNECT");
                    mConnectState = 0;
                    break;
                }
            }
            mConnectState = 0;
        }
    }
    static class SendThread implements Runnable {
        SendThread() {
        }
        @Override
        public void run() {
            //clear queue so we don't have a buildup of images
            queue.clear();
            while (true) {
                if (packets_in_buf > 5) { //if 5 packets in buffer (NOT QUEUE, BUF NETWORK BUFFER), restart socket
                    break;
                }
                byte[] data;
                try {
                    data = queue.take(); //block until there is something we can pull out to send
                } catch (InterruptedException e){
                    e.printStackTrace();
                    break;
                }
                try {
                    packets_in_buf++;
                    output.write(data);           // write the message
                    packets_in_buf--;
                } catch (java.io.IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
            mConnectState = 0;
        }
    }
}