package minigame.core.players;

import minigame.core.ai.AI;
import minigame.ui.Gui;

/**
 * 电脑玩家
 */
public final class AIPlayer extends AbstractPlayer {
    private final AI ai;

    public AIPlayer(AI ai) {
        this.ai = ai;
    }

    @Override
    public void step(int x, int y) {
        int[] result = ai.nextStep(server.getChess(), id);
        if (result==null){
            return;
        }
        server.step(this, result[0], result[1]);
    }

    @Override
    public void setId(int id) {
        super.setId(id);
        if (id==1){
            step(0,0);
        }
    }
}