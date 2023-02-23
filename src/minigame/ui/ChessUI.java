package minigame.ui;

import minigame.core.Chess;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

/**
 * 渲染一个Chess
 */
public class ChessUI extends JPanel implements ComponentListener, MouseMotionListener {
    private Chess chess;
    public ChessUI(Chess chess){
        super();
        this.chess=chess;
        chess.setUI(this);
        addComponentListener(this);
        addMouseMotionListener(this);
    }
    public void setChess(Chess chess){
        this.chess=chess;
        chess.setUI(this);
    }

    /**
     * 像素点到格点坐标的转换
     * 已测试
     */
    public int toBlockX(int x){
        return (x-rX)/blockSize;
    }
    public int toBlockY(int y){
        return (y-rY)/blockSize;
    }

    /**
     * 渲染相关数据
     */
    private int rX;
    private int rY;
    private int rSize;//边长
    private int blockSize;//一格的边长
    /**
     * 在大小改变时重新计算位置
     */
    @Override
    public void componentResized(ComponentEvent e) {
        int min= Math.min(getWidth(), getHeight());
        rSize=min-100;
        rX=(getWidth()-rSize)/2;
        rY=(getHeight()-rSize)/2;
        blockSize=rSize/chess.size;
    }

    private static final Color highLight=new Color(54, 227, 241, 77);
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        //缓存,因为要频繁调用
        int blockSize=this.blockSize;
        //TODO 在最后会多画一点
        for (int i=0;i<chess.size+1;i++){
            g.drawLine(rX+i*blockSize,rY,rX+i*blockSize,rY+rSize);
            g.drawLine(rX,rY+i*blockSize,rX+rSize,rY+i*blockSize);
        }
        byte[][] data=chess.getData();
        //渲染棋子
        for (int i=0;i<chess.size;i++){
            for (int j=0;j<chess.size;j++){
                g.drawString(String.valueOf(data[i][j]),rX+i*blockSize,rY+j*blockSize);
            }
        }
        g.setColor(highLight);
        if (lastX!=-1){
            g.fillRect(rX+lastX*blockSize,rY+lastY*blockSize,blockSize,blockSize);
        }
    }

    /**
     * 上一次鼠标的格点位置
     */
    private int lastX=-1;
    private int lastY=-1;
    /**
     * 在鼠标移动时高亮格子
     */
    @Override
    public void mouseMoved(MouseEvent e) {
        int newX=toBlockX(e.getX());
        int newY=toBlockY(e.getY());
        if (newX==lastX&&newY==lastY) return;//没有变化就return
        lastX=newX>=chess.size||newX<0?-1:newX;
        lastY=newY>=chess.size||newY<0?-1:newY;
        repaint();
    }

    @Override
    public void componentMoved(ComponentEvent e) {

    }

    @Override
    public void componentShown(ComponentEvent e) {

    }

    @Override
    public void componentHidden(ComponentEvent e) {

    }
    @Override
    public void mouseDragged(MouseEvent e) {

    }
}