package minigame.core.server;

import minigame.core.net.Connection;
import minigame.core.net.InitPacket;
import minigame.core.net.Packet;
import minigame.core.net.StepPacket;
import minigame.core.players.Player;
import minigame.ui.FXChessUI;

import java.io.IOException;
import java.net.Socket;

/**
 * 远程服务器的镜像
 */
public final class GhostServer extends RemoteServer {
    private int playerId = 0;

    public GhostServer(String ip, int port) throws IOException{
        Socket socket = new Socket(ip, port);
        this.socket = socket;
        connection = new Connection(socket);
        online = true;
        startListen();
    }

    @Override
    public void onJoin(Player player) {
        this.player = player;
        if (playerId != 0) {
            player.setId(playerId);
        }
    }

    @Override
    public void packetReceive(Packet packet) {
        if (packet instanceof StepPacket) {
            StepPacket stepPacket = (StepPacket) packet;
            chess.set(stepPacket.x, stepPacket.y, turn);
            turn = player.getId();
            if (isFinished()){
                showGGMsg();
                gg=true;
            }
        }else if (packet instanceof InitPacket) {
            InitPacket initPacket = (InitPacket) packet;
            if (player == null) {
                playerId = initPacket.yourId;
            } else {
                player.setId(initPacket.yourId);
            }
            chess = initPacket.chess;
            FXChessUI.instance.setChess(chess);
        }
    }
}