package minigame.ui;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import minigame.App;
import minigame.core.Chess;
import minigame.core.Game;

import java.io.IOException;

public final class FXChessUI extends Canvas{
    public static FXChessUI instance;
    public static final int SIZE=500;//像素大小
    private static Image purpleImg;
    private static Image blueImg;

    private Chess chess;
    public FXChessUI(){
        super(SIZE,SIZE);
        instance=this;
        addEventHandler(MouseEvent.MOUSE_MOVED,event -> {
            int newX=toBlockX(((int) event.getX()));
            int newY=toBlockY(((int) event.getY()));
            if (newX==lastX&&newY==lastY) return;//没有变化就return
            lastX=newX>=chess.size||newX<0?-1:newX;
            lastY=newY>=chess.size||newY<0?-1:newY;
            repaint();
        });
        addEventHandler(MouseEvent.MOUSE_PRESSED,event -> {
            int newX=toBlockX(((int) event.getX()));
            int newY=toBlockY(((int) event.getY()));
            pressX=newX>=chess.size||newX<0?-1:newX;
            pressY=newY>=chess.size||newY<0?-1:newY;
            repaint();
        });
        addEventHandler(MouseEvent.MOUSE_RELEASED,event -> {
            if (toBlockX(((int) event.getX()))==pressX&&toBlockY(((int) event.getY()))==pressY&&pressX!=-1&&pressY!=-1){
                Game.thePlayer.step(pressX,pressY);
            }
            pressX=-1;
            pressY=-1;
            repaint();
        });
    }
    private void reloadImage() {
        try {
            purpleImg = new Image(App.getUrl("res/img/purple.png").openStream(),blockSize-10,blockSize-10,false,true);
            blueImg = new Image(App.getUrl("res/img/blue.png").openStream(),blockSize-10,blockSize-10,false,true);
        } catch (IOException e) {
            e.printStackTrace();
            Gui.info("无法加载程序图像，请重新安装程序");
        }
    }

    public void setChess(Chess chess){
        this.chess=chess;
        lastX=lastY=pressY=pressX=-1;
        blockSize= (int) (getWidth()/chess.size);
        chess.setUI(this);
        reloadImage();
        repaint();
    }
    /**
     * 像素点到格点坐标的转换
     * 已测试
     */
    public int toBlockX(int x){
        return x/blockSize;
    }
    public int toBlockY(int y){
        return y/blockSize;
    }
    /**
     * 渲染相关数据
     */
    private int blockSize;//一格的边长

    /**
     * 上一次鼠标的格点位置
     */
    private int lastX=-1;
    private int lastY=-1;
    private int pressX=-1;
    private int pressY=-1;

    private static final Color highLight= Color.rgb(54, 227, 241, 0.4);
    private static final Color click=Color.rgb(15, 21, 21, 0.7);
    private static final Color lastStep=Color.rgb(238, 226, 46);

    public void repaint(){
        GraphicsContext g=getGraphicsContext2D();
        g.clearRect(0,0,SIZE,SIZE);
        //缓存,因为要频繁调用
        int blockSize=this.blockSize;
        Chess chess=this.chess;
        g.setFill(Color.BLACK);
        int SIZE=FXChessUI.SIZE;
        for (int i=0,a;i<chess.size+1;i++){
            //本质就是一个drawLine，对GraphicsContext无语了
            a=i*blockSize>=SIZE?SIZE-1:i*blockSize;
            g.fillRect(0,a,SIZE-2,1);
            g.fillRect(a,0,1,SIZE-2);
        }
        byte[][] data=chess.getData();
        //高亮最后一步
        if (chess.lastX!=-1){
            g.setFill(lastStep);
            g.fillRect(chess.lastX*blockSize+1,chess.lastY*blockSize+1,blockSize-1,blockSize-1);
            g.setFill(Color.BLACK);
        }
        //渲染棋子
        int current,j;
        SIZE=chess.size;
        for (int i=0;i<SIZE;i++){
            for (j=0;j<SIZE;j++){
                current=data[i][j];
                if (current!=0){
                    if (current==1) g.drawImage(purpleImg, i*blockSize+5,j*blockSize+5);
                    else if (current==2) g.drawImage(blueImg, i*blockSize+5,j*blockSize+5);
                }
            }
        }

        if (lastX==-1||lastY==-1||!chess.isValid(lastX,lastY,Game.thePlayer.getId())) return;
        g.setFill(highLight);
        g.fillRect(lastX*blockSize,lastY*blockSize,blockSize,blockSize);
        if (pressX==-1||pressY==-1) return;
        g.setFill(click);
        g.fillRect(pressX*blockSize,pressY*blockSize,blockSize,blockSize);
    }
}
