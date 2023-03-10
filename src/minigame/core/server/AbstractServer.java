package minigame.core.server;

import minigame.App;
import minigame.core.Chess;
import minigame.core.Game;
import minigame.core.ai.AI;
import minigame.core.ai.NoobAI;
import minigame.core.players.Player;
import minigame.ui.Gui;

import java.util.Arrays;

public abstract class AbstractServer implements Server{
    protected Chess chess;
    /**
     * 轮到哪个玩家下，值为id
     */
    protected int turn=1;
    private final AI innerAI=new NoobAI();

    protected boolean gg=false;

    /**
     * 是否无棋可走
     */
    protected boolean isFinished(){
        return innerAI.nextStep(chess,turn)==null;
    }
    protected int getWinner(){
        byte[][] data= chess.getData();
        int[] result={0,0,0};
        for (int i=0,j;i<chess.size;i++){
            for (j=0;j<chess.size;j++){
                result[data[i][j]]++;
            }
        }
        System.out.println(Arrays.toString(result));
        //noinspection ConstantConditions
        return result[1]>result[2]?1:(result[1]==result[2]?0:2);
    }
    protected void showGGMsg(){
        int id=getWinner();
        if (id==0){
            Gui.info("游戏结束！平手");
        }else {
            Gui.info("游戏结束！"+ Game.IdMap[id]+"获胜");
        }
    }

    @Override
    public Chess getChess() {
        return chess;
    }

    @Override
    public boolean canStepAt(Player player, int x, int y) {
        return player.getId()==turn&&chess.isValid(x,y,turn);
    }
}
