package minigame.core.server;

import com.sun.javafx.tk.Toolkit;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.input.DataFormat;
import javafx.scene.text.Font;
import javafx.util.Pair;
import minigame.App;
import minigame.core.Chess;
import minigame.core.Util;
import minigame.core.net.Connection;
import minigame.core.net.InitPacket;
import minigame.core.net.Packet;
import minigame.core.net.StepPacket;
import minigame.core.players.Player;
import minigame.ui.Gui;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;

/**
 * 真正的服务器
 */
public final class MainServer extends RemoteServer{
    public static int port;
    public static String ip;
    public static Label invite=new Label();
    ServerSocket serverS;
    public MainServer(int size) {
        chess=new Chess(size);
        new Thread(new Runnable() {
            /**
             * 监听玩家加入
             */
            @Override
            public void run() {
                try {
                    serverS = new ServerSocket(0);
                    port = serverS.getLocalPort();
                    ip = InetAddress.getLocalHost().getHostAddress();
                } catch (UnknownHostException e) {
                    Gui.info("无法获取本机IP");
                    return;
                }catch (IOException e) {
                    Gui.info("无法启动服务器");
                    return;
                }
                Platform.runLater(()->Toolkit.getToolkit().getSystemClipboard().putContent(new Pair<>(DataFormat.PLAIN_TEXT,Util.zipAddress(ip,port))));
                Gui.info("创建服务器成功！\n邀请码："+Util.zipAddress(ip,port)+"\n已复制到剪切板");
                Platform.runLater(()->{
                    invite.setText(Util.zipAddress(ip, port));
                    invite.setFont(Font.font(17));
                });
                System.out.println("socket start at " + serverS.getLocalPort());
                while (socket == null) {
                    if (serverS==null) return;
                    try {
                        socket = serverS.accept();
                    } catch (IOException e) {
                        socket = null;
                        if (e.getMessage().equals("socket closed")){
                            return;
                        }else {
                            e.printStackTrace();
                        }
                    }
                }
                try {
                    connection = new Connection(socket);
                    connection.writePacket(new InitPacket(chess,3-player.getId()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    serverS.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                serverS = null;
                startListen();
                online=true;
                Gui.info("游戏开始！");
                checkState();
            }
        }).start();
        App.setState("等待玩家加入...");
    }

    @Override
    public void onJoin(Player player) {
        this.player=player;
        if (player.getId()==0){
            //随机生成一个id
            //id决定棋子颜色和先后
            int id=1+(int) Math.round(Math.random());
            player.setId(id);
        }
    }

    @Override
    public void close() {
        super.close();
        if (serverS!=null){
            try {
                serverS.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            serverS=null;
        }
    }

    @Override
    public void packetReceive(Packet packet) {
        if (packet instanceof StepPacket){
            StepPacket stepPacket = (StepPacket) packet;
            chess.set(stepPacket.x, stepPacket.y, turn);
            turn=player.getId();
            if (isFinished()){
                showGGMsg();
                gg=true;
            }
        }
    }
}
