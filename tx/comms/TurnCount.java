package tx.comms;

public class TurnCount {
    int turnCount = 0;
    public void inc(){
        turnCount++;
    }

    public int get(){
        return turnCount;
    }
}
