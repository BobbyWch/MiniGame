package minigame.core.ai;

import minigame.core.Chess;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.Random;

public class NormalAI extends AILib{
    private static final Random rand=new Random();
    @Override
    public int[] nextStep(Chess chess, int id) {
        LinkedList<int[]> valids=getValidPoses(chess,id);
        if (valids.size()==0) return null;
        valids.sort(new Comparator<int[]>() {
            @Override
            public int compare(int[] o1, int[] o2) {
                return getScore(chess,o2[0],o2[1],id)-getScore(chess,o1[0],o1[1],id);
            }
        });
        return valids.get(rand.nextInt(Math.min(3, valids.size())));
    }
    private int getScore(Chess chess,int x,int y,int id){
        int num=0,dy;
        for (int dx=-1;dx<2;dx++){
            for (dy = -1; dy<2; dy++){
                if (dy==0&&dx==0) continue;
                num+=chess.find(x,y,dx,dy,id);
            }
        }
        return num;
    }
}
