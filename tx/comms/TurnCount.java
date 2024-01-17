package tx.comms;

import battlecode.common.GameConstants;

public class TurnCount {
    int turnCount = 0;

    public boolean isSetupRound(){
        return turnCount < GameConstants.SETUP_ROUNDS;
    }
    public void inc(){
        turnCount++;
    }

    public int get(){
        return turnCount;
    }
}
