package minigame;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import minigame.core.Game;
import minigame.core.Util;
import minigame.core.ai.AI;
import minigame.core.ai.NoobAI;
import minigame.core.ai.NormalAI;
import minigame.core.players.AIPlayer;
import minigame.core.players.LocalPlayer;
import minigame.core.players.Player;
import minigame.core.server.GhostServer;
import minigame.core.server.LocalServer;
import minigame.core.server.MainServer;
import minigame.core.server.Server;
import minigame.ui.FXChessUI;
import minigame.ui.Gui;
import minigame.ui.MusicPlayer;

import javax.swing.*;
import java.awt.*;
import java.awt.peer.DesktopPeer;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

public final class App extends Application {
    public static App instance;
    /**
     * 一个哈希表,相当于组件库,方便取用
     */
    public static final HashMap<String, EventTarget> map=new HashMap<>();

    private int count=0;
    /**
     * 通过Id获取组件
     */
    public static Node getNodeById(String id){
        return (Node) map.get(id);
    }
    public static MenuItem getMenuById(String id){
        return (MenuItem) map.get(id);
    }
    public static URL getUrl(String path){
        return App.class.getClassLoader().getResource(path);
    }

    public static Label l;//邀请码文本

    /**
     * 状态标签，在游戏界面显示
     */
    private static final Label state=new Label();
    public static void setState(String s){
        if (Game.thePlayer.getId()==0){
            state.setText(s);
        }else {
            state.setText(s+"\n你是"+Game.IdMap[Game.thePlayer.getId()]); //判断棋子颜色
        }
    }

    public final Scene rootScene;
    public final Scene gameScene;
    private Stage stage;

    public App() throws IOException {
        instance=this;

        Parent root= FXMLLoader.load(getUrl("res/fxml/test.fxml"));
        //注册id
        //在加载完所有fxml后调用
        regNodes(root);
        state.setFont(Font.font(17));
        rootScene =new Scene(root);
        gameScene=new Scene(createGamePane());
        registerHandles();
        look();
    }
    @Override
    public void start(Stage primaryStage) throws IOException {
        stage=primaryStage;
        primaryStage.getIcons().add(new Image(getUrl("res/img/icon.png").openStream()));
        primaryStage.setTitle("MiniGame");
        primaryStage.setScene(rootScene);
        primaryStage.setResizable(false);
        primaryStage.show();
        primaryStage.addEventHandler(WindowEvent.WINDOW_HIDDEN,event -> System.exit(0));
    }
    private Parent createGamePane() {
        VBox vBox=new VBox();
        vBox.setPrefSize(550,600);
        vBox.setAlignment(Pos.CENTER);
        HBox box=new HBox();
        box.setAlignment(Pos.CENTER);
        box.setPrefSize(281,45);
        box.setSpacing(20);
        box.setLayoutX(134);
        box.setLayoutY(14);
        box.getChildren().add(state);

        Button b1=new Button("提示");
        box.getChildren().add(b1);
        map.put("button$tip",b1);

        Button b2=new Button("退出");
        box.getChildren().add(b2);
        map.put("button$exit",b2);

        l=new Label("");
        l.setFont(Font.font(17));
        box.getChildren().add(l);
        box.getChildren().add(MainServer.invite);

        vBox.getChildren().add(box);
        vBox.getChildren().add(new FXChessUI());
        return vBox;
    }

    /**
     * 递归找按钮
     */
    private void regNodes(Parent parent){
        for (Node node:parent.getChildrenUnmodifiable()) {
            if (node instanceof Parent) {
                if (node instanceof MenuBar){
                    MenuBar m = (MenuBar) node;
                    for (Menu menu:m.getMenus()){
                        for (MenuItem item:menu.getItems()){
                            if (item.getId()!=null){
                                map.put(item.getId(),item);
                            }
                        }
                    }
                }else {
                    regNodes(((Parent) node));
                }
            }
            if (node.getId() != null) {
                map.put(node.getId(), node);
            }
        }
    }

    private void registerHandles(){
        getNodeById("button$ai").setOnMouseClicked(event -> {
            Server server=new LocalServer(Game.size);
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
            setMode("game");
            if (Game.thePlayer.getId()==1){
                state.setText("轮到您下了");
            }
        });
        getNodeById("button$local").setOnMouseClicked(event -> {
            Server server=new LocalServer(Game.size);
            Game.setServer(server);
            FXChessUI.instance.setChess(server.getChess());
            Game.thePlayer.join(server);
            new LocalPlayer().join(server);
            setMode("game");
        });
        getNodeById("button$create").setOnMouseClicked(event -> {
            MainServer server=new MainServer(Game.size);
            Game.setServer(server);
            FXChessUI.instance.setChess(server.getChess());
            Game.thePlayer.join(server);
            setMode("game");
            l.setText("您的邀请码:");
        });
        getNodeById("button$join").setOnMouseClicked(event -> {
            String get= Gui.input("请输入邀请码：");
            if (get==null||get.equals("")) return;
            try {
                Object[] result=Util.unZipAddress(get);
                GhostServer ghost=new GhostServer(((String) result[0]), ((Integer) result[1]));
                Game.setServer(ghost);
                Game.thePlayer.join(ghost);
                System.out.println("join");
                setMode("game");
            }catch (IllegalArgumentException e){
                Gui.info("无效的邀请码！");
            }catch (IOException ioE){
                Gui.info("无法连接至服务器");
            }
        });
        getNodeById("button$tip").setOnMouseClicked(event -> {
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
        });
        getNodeById("button$exit").setOnMouseClicked(event -> {
            setMode("welcome");
            Game.exit();
        });  //返回主页面
        getNodeById("button$radio").setOnMouseClicked(event -> {
            RadioButton radioButton= (RadioButton) getNodeById("button$radio");
            if (radioButton.isSelected()){
                MusicPlayer.playBackground();
            }else {
                MusicPlayer.stopBgm();
            }
        });
        getMenuById("menu$resize").setOnAction(event -> {
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
        });
        getMenuById("menu$github").setOnAction(event -> {
//            try {
//                System.out.println(Desktop.getDesktop().getClass().getDeclaredField("peer").getType().getName());
//                Util.browse("https://github.com/BobbyWch/MiniGame");
//            } catch (Exception e) {
//                e.printStackTrace();
                Gui.info("无法自动打开浏览器！\nGithub地址：https://github.com/BobbyWch/MiniGame");
//            }
        });
    }
    public void setMode(String mode){
        switch (mode){
            case "game":
                stage.setScene(gameScene);
                break;
            case "welcome":
                stage.setScene(rootScene);
                state.setText(null);
                break;
            default:
                System.out.println("未知的mode？");
        }
    }

    /**
     * 添加阴影效果
     */
    private void look(){
        Node ai= getNodeById("button$ai");
        Node create= getNodeById("button$create");
        Node join=getNodeById("button$join");
        Node local= getNodeById("button$local");

        EffectListener l=new EffectListener(ai);
        //当鼠标进入按钮时添加阴影特效
        //当鼠标离开按钮时移除阴影效果
        ai.addEventHandler(MouseEvent.MOUSE_ENTERED, l);
        ai.addEventHandler(MouseEvent.MOUSE_EXITED, l);
        l=new EffectListener(create);
        create.addEventHandler(MouseEvent.MOUSE_ENTERED, l);
        create.addEventHandler(MouseEvent.MOUSE_EXITED, l);
        l=new EffectListener(join);
        join.addEventHandler(MouseEvent.MOUSE_ENTERED, l);
        join.addEventHandler(MouseEvent.MOUSE_EXITED, l);
        l=new EffectListener(local);
        local.addEventHandler(MouseEvent.MOUSE_ENTERED, l);
        local.addEventHandler(MouseEvent.MOUSE_EXITED, l);
    }
    private static final class EffectListener implements EventHandler<MouseEvent>{
        private static final DropShadow shadow=new DropShadow();
        private final Node node;

        public EffectListener(Node node) {
            this.node = node;
        }

        @Override
        public void handle(MouseEvent event) {
            if (event.getEventType()==MouseEvent.MOUSE_ENTERED){
                node.setEffect(shadow);
                node.setScaleX(1.3);
                node.setScaleY(1.3);
            }else if (event.getEventType()==MouseEvent.MOUSE_EXITED){
                node.setEffect(null);
                node.setScaleX(1);
                node.setScaleY(1);
            }
        }
    }
    public static void main(String[] args) {
        launch(args);
    }
}
