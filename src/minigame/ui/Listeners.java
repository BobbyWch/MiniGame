package minigame.ui;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.RadioButton;
import javafx.scene.input.MouseEvent;
import minigame.App;
import minigame.core.Game;
import minigame.core.Util;
import minigame.core.ai.NormalAI;
import minigame.core.players.AIPlayer;
import minigame.core.players.LocalPlayer;
import minigame.core.server.GhostServer;
import minigame.core.server.LocalServer;
import minigame.core.server.MainServer;
import minigame.core.server.Server;

import java.io.IOException;

import static minigame.App.l;

/**
 * 监听器的名称随意
 */
@SuppressWarnings("Convert2Lambda")
public final class Listeners {
    @IsListener(id = "button$ai")
    public static final EventHandler<MouseEvent> ai = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
            Server server = new LocalServer(Game.size);
//            String get=Gui.input("简单or困难? (选择前者输入1，选择后者输入2)");
//            if(get.equals("1"))
//                new AIPlayer(new NoobAI()).join(server);
//            else if (get.equals("2"))
//                new AIPlayer(new NormalAI()).join(server);
//            else {
//                Gui.info("请输入1或2!");
//                server=null;
//            }
            Game.setServer(server);
            FXChessUI.instance.setChess(server.getChess());
            Game.thePlayer.join(server);
            new AIPlayer(new NormalAI()).join(server);
            App.instance.setMode("game");
            if (Game.thePlayer.getId() == 1) {
                App.setState("轮到您下了");
            }
        }
    };
    @IsListener(id = "button$local")
    public static final EventHandler<MouseEvent> local = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
            Server server = new LocalServer(Game.size);
            Game.setServer(server);
            FXChessUI.instance.setChess(server.getChess());
            Game.thePlayer.join(server);
            new LocalPlayer().join(server);
            App.instance.setMode("game");
        }
    };
    @IsListener(id = "button$create")
    public static final EventHandler<MouseEvent> create = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
            MainServer server=new MainServer(Game.size);
            Game.setServer(server);
            FXChessUI.instance.setChess(server.getChess());
            Game.thePlayer.join(server);
            App.instance.setMode("game");
            l.setText("您的邀请码:");
        }
    };
    @IsListener(id = "button$join")
    public static final EventHandler<MouseEvent> join = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
            String get= Gui.input("请输入邀请码：");
            if (get==null||get.equals("")) return;
            try {
                Object[] result= Util.unZipAddress(get);
                GhostServer ghost=new GhostServer(((String) result[0]), ((Integer) result[1]));
                Game.setServer(ghost);
                Game.thePlayer.join(ghost);
                App.instance.setMode("game");
            }catch (IllegalArgumentException e){
                Gui.info("无效的邀请码！");
            }catch (IOException ioE){
                Gui.info("无法连接至服务器");
            }
        }
    };
    @IsListener(id = "button$tip")
    public static final EventHandler<MouseEvent> tip = new EventHandler<MouseEvent>() {
        private int count=0;
        @Override
        public void handle(MouseEvent event) {
            NormalAI normalAI=new NormalAI();
            if (count<5) {
                int[] pos=normalAI.nextStep(Game.getServer().getChess(), Game.thePlayer.getId());
                if (pos!=null){
                    int p1=pos[0]+1;
                    int p2=-pos[1]-1;
                    //真实坐标(左上角0,0)
                    Gui.info("AI建议你下"+p1+", "+ p2);
                }
            }
            else Gui.info("您的试用次数已结束");
            count+=1;
        }
    };
    @IsListener(id = "button$exit")
    public static final EventHandler<MouseEvent> exit = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
            App.instance.setMode("welcome");
            Game.exit();
        }
    };
    @IsListener(id = "button$radio")
    public static final EventHandler<MouseEvent> radio = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
            RadioButton radioButton= (RadioButton) event.getTarget();
            if (radioButton.isSelected()){
                MusicPlayer.playBackground();
            }else {
                MusicPlayer.stopBgm();
            }
        }
    };
    @IsListener(id = "menu$resize",type = "menu")//type为menu时，监听器必须为EventHandler<ActionEvent>
    public static final EventHandler<ActionEvent> resize = new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
            String input=Gui.input("请输入棋盘大小：（偶数）（4-20）\n当前棋盘大小："+Game.size);
            if (input==null) return;
            int size;
            try {
                size=Integer.parseInt(input);
            }catch (NumberFormatException e){
                Gui.info("请输入一个数字！");
                return;
            }
            if (size%2==1){
                Gui.info("请输入一个偶数！");
                return;
            }
            if (size<4||size>20){
                Gui.info("请输入一个4-20间的数");
                return;
            }
            Game.size=size;
            Gui.info("设置成功！");
        }
    };
    @IsListener(id = "menu$github",type = "menu")
    public static final EventHandler<ActionEvent> github = new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
//            try {
//                System.out.println(Desktop.getDesktop().getClass().getDeclaredField("peer").getType().getName());
//                Util.browse("https://github.com/BobbyWch/MiniGame");
//            } catch (Exception e) {
//                e.printStackTrace();
            Gui.info("无法自动打开浏览器！\nGithub地址：https://github.com/BobbyWch/MiniGame");
//            }
        }
    };
}
