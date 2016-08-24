package com.cxz.androidserver;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by chen.
 */
public class SimpleHttpServer {

    private final ExecutorService threadPool;
    private boolean isEnable = false;
    private WebConfiguration webConfig;
    private ServerSocket socket;

    public SimpleHttpServer(WebConfiguration webConfig){
        this.webConfig = webConfig;
        threadPool = Executors.newCachedThreadPool();
    }

    /**
     * 启动Server(异步)
     */
    public void startAsync(){
        isEnable = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                doProcAsync();
            }
        }).start();
    }

    /**
     * 停止Server(异步)
     */
    public void stopServer() throws IOException {
        if (!isEnable)
            return;
        isEnable = false;
        socket.close();
        socket = null;
    }

    private void doProcAsync() {

        try {
            InetSocketAddress socketAddr = new InetSocketAddress(webConfig.getPort());
            socket = new ServerSocket();
            socket.bind(socketAddr);
            while (isEnable){
                final Socket remotePeer = socket.accept();
                threadPool.submit(new Runnable() {
                    @Override
                    public void run() {
                        Log.i("cxz","a remote peer accept..." + remotePeer.getRemoteSocketAddress().toString());
                        onAcceptRemotePeer(remotePeer);
                    }
                });
            }
        } catch (IOException e) {
            Log.e("cxz",e.toString());
        }
    }

    private void onAcceptRemotePeer(Socket remotePeer) {
        try {
            //remotePeer.getOutputStream().write("configuration,connected successful...".getBytes());
            HttpContext httpContext = new HttpContext();
            httpContext.setUnderlySocket(remotePeer);
            InputStream is = remotePeer.getInputStream();
            String headerLine = null;
            while ((headerLine = StreamToolKit.readLine(is))!=null){
                if (headerLine.equals("\r\n")){
                    break;
                }
                String [] pair = headerLine.split(": ");
                httpContext.addRequestHeader(pair[0],pair[1]);
                Log.i("cxz","===>"+headerLine);
            }
        } catch (IOException e) {
            Log.e("cxz",e.toString());
        }
    }

}
