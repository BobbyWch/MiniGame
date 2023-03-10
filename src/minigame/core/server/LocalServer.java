package minigame.core.server;

import minigame.App;
import minigame.core.Chess;
import minigame.core.Game;
import minigame.core.players.LocalPlayer;
import minigame.core.players.Player;

public final class LocalServer extends AbstractServer{
    private Player p1;
    private Player p2;

    /**
     * 是否两个都是本地玩家
     */
    private boolean playerOnly=false;

    public LocalServer(int size){
        chess=new Chess(size);
    }

    @Override
    public void onJoin(Player player) {
        if (p1==null){
            p1=player;
            if (player.getId()==0){
                //随机生成一个id
                //id决定棋子颜色和先后
                int id=1+(int) Math.round(Math.random());
                player.setId(id);
            }
        }else if (p2==null){
            p2=player;
            //第二名玩家强制分配id
            player.setId(3-p1.getId());
            playerOnly=p1 instanceof LocalPlayer&&p2 instanceof LocalPlayer;
        }
        if (playerOnly){
            Game.thePlayer=p1.getId()==1?p1:p2;
            App.setState("");
        }
    }

    @Override
    public void step(Player player,int x, int y) {
        if (gg) return;
        chess.set(x,y,player.getId());
        if (++turn==3) turn=1;
        if (playerOnly){
            Game.thePlayer=player==p1?p2:p1;
            App.setState("");
        }else {
            if (Game.thePlayer==player){
                App.setState("请稍候");
                if (player==p1){
                    p2.step(0,0);
                }else {
                    p1.step(0,0);
                }
            }else {
                App.setState("轮到您下了");
            }
        }
        if (isFinished()){
            showGGMsg();
            gg=true;
        }
    }
}
