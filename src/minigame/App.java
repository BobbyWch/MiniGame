package minigame;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import minigame.core.Game;
import minigame.core.server.MainServer;
import minigame.ui.FXChessUI;
import minigame.ui.IsListener;
import minigame.ui.Listeners;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.HashMap;

public final class App extends Application {
    public static App instance;
    /**
     * 一个哈希表,相当于组件库,方便取用
     */
    public static final HashMap<String, EventTarget> map=new HashMap<>();
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
        AnchorPane root= FXMLLoader.load(getUrl("res/fxml/test.fxml"));
        //注册id
        //在加载完所有fxml后调用
        regNodes(root);
        root.setBackground(
                new Background(new BackgroundImage(new Image(getUrl("res/img/background.jpg").openStream(),root.getPrefWidth(), root.getPrefHeight(),false,true),
                        BackgroundRepeat.ROUND,BackgroundRepeat.ROUND,null,null)));
        state.setFont(Font.font(17));
        rootScene =new Scene(root);
        gameScene=new Scene(createGamePane());
        try {
            regListeners();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        look();
    }
    @Override
    public void start(Stage primaryStage) throws IOException {
        stage=primaryStage;
        primaryStage.getIcons().add(new Image(getUrl("res/img/icon.png").openStream()));
        primaryStage.setTitle("蓝紫棋");
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
                    for (Menu menu:((MenuBar) node).getMenus()){
                        for (MenuItem item:menu.getItems()){
                            if (item.getId()!=null) map.put(item.getId(),item);
                        }
                    }
                }else regNodes(((Parent) node));
            }
            if (node.getId() != null) map.put(node.getId(), node);
        }
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
    @SuppressWarnings("unchecked")
    private void regListeners() throws IllegalAccessException {
        Field[] fields= Listeners.class.getFields();
        IsListener annotation;
        for (Field f:fields){
            annotation=f.getAnnotation(IsListener.class);
            if (annotation==null) continue;
            switch (annotation.type()){
                case "enter":
                    getNodeById(annotation.id()).addEventHandler(MouseEvent.MOUSE_ENTERED, (EventHandler<MouseEvent>) f.get(null));
                    break;
                case "click":
                    getNodeById(annotation.id()).addEventHandler(MouseEvent.MOUSE_CLICKED, (EventHandler<MouseEvent>) f.get(null));
                    break;
                case "exit":
                    getNodeById(annotation.id()).addEventHandler(MouseEvent.MOUSE_EXITED, (EventHandler<MouseEvent>) f.get(null));
                    break;
                case "menu":
                    getMenuById(annotation.id()).setOnAction((EventHandler<ActionEvent>) f.get(null));
                    break;
                default:
                    throw new IllegalArgumentException("未知的annotation type？ "+annotation.type());
            }
        }
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
