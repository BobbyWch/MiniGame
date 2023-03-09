package minigame.core.server;

import com.sun.javafx.tk.Toolkit;
import javafx.application.Platform;
import minigame.App;
import minigame.core.net.Connection;
import minigame.core.net.Packet;
import minigame.core.net.StepPacket;
import minigame.core.players.Player;
import minigame.ui.Gui;

import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

public abstract class RemoteServer extends AbstractServer implements Runnable {
    protected Socket socket;
    protected Connection connection;
    /**
     * 是否连接正常
     */
    public boolean online = false;
    /**
     * 网络服务器都只需要储存一个player
     */
    protected Player player;

    public abstract void packetReceive(Packet packet);

    /**
     * 监听对方发送的数据包
     */
    @Override
    public void run() {
        Packet packet;
        while (!socket.isClosed()) {
            try {
                packet = connection.readPacket();
            }catch (EOFException eof){
                online=false;
                Gui.info("对方已离开");
                return;
            }catch (SocketException se){
                if (se.getMessage().equals("Socket closed")){
                    break;
                }else {
                    se.printStackTrace();
                    online=false;
                    continue;
                }
            } catch (IOException e) {
                e.printStackTrace();
                online = false;
                continue;
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                continue;
            }
            if (packet != null) {
                packetReceive(packet);
                checkState();
            }
        }
        online = false;
    }

    public void close() {
        if (connection == null) {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            connection.close();
        }
    }

    /**
     * 开始监听
     * 不调用的话无法接受数据！！！
     */
    protected void startListen() {
        new Thread(this).start();
    }

    @Override
    public boolean canStepAt(Player player, int x, int y) {
        if (!online){
            Gui.info("请等待玩家加入！或网络连接已断开");
            return false;
        }
        return super.canStepAt(player, x, y);
    }
    @Override
    public void step(Player player, int x, int y) {
        if (gg) return;
        chess.set(x,y,player.getId());
        turn=3-player.getId();
        connection.writePacket(new StepPacket(x,y));
        checkState();
        if (isFinished()){
            showGGMsg();
            gg=true;
        }
    }
    protected void checkState(){
        if (turn== player.getId()){
            if (Toolkit.getToolkit().isFxUserThread()){
                App.setState("轮到您下了");
            }else {
                Platform.runLater(()->App.setState("轮到您下了"));
            }
        }else {
            if (Toolkit.getToolkit().isFxUserThread()){
                App.setState("等待对方下棋");
            }else {
                Platform.runLater(()->App.setState("等待对方下棋"));
            }
        }
    }
}