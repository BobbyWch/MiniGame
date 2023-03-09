package minigame.core.ai;

import minigame.core.Chess;

import java.util.LinkedList;

public abstract class AILib implements AI{
    public LinkedList<int[]> getValidPoses(Chess chess, int id){
        LinkedList<int[]> list=new LinkedList<>();
        int[] pos;
        int size=chess.size;
        for (int x=0,y;x<size;x++){
            for (y=0;y<size;y++) {
                if (chess.isValid(x, y, id)) {
                    pos=new int[2];
                    pos[0] = x;
                    pos[1] = y;
                    list.add(pos);
                }
            }
        }
        return list;
    }
}
