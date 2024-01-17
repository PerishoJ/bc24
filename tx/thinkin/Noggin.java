package tx.thinkin;

import tx.thinkin.idears.*;
import battlecode.common.RobotController;

import java.util.LinkedList;
import java.util.List;

public class Noggin {

    public Noggin(BigPicture layOfTheLand, RobotController i) {
        this();
        this.layOfTheLand = layOfTheLand;
        I = i;
    }

    BigPicture layOfTheLand;
    RobotController I;

    private final List<BrightIdea> ideas = new LinkedList<>();

    private Noggin(){
        ideas.add(new GetOnGet());
        ideas.add(new MeanderAround());
        ideas.add(new RememberTheAlamo());
        ideas.add(new RaiseHell());
        ideas.add(new SweetTea());
        ideas.add(new FlagThief());
        ideas.add(new Trapper());
        //ideas.add(new TestBugNav());
    }

    public BrightIdea ponder (BigPicture layOfTheLand){
        BrightIdea best = null;
        int bestScore = 0;
        // score each idea and find the good one
        for (BrightIdea idea : ideas){
            if(best == null){
                best = idea;
                bestScore = idea.howAboutThat(layOfTheLand,I);
            } else {
                try {
                    int score = idea.howAboutThat(layOfTheLand, I);
                    if (score > bestScore) {
                        best = idea;
                        bestScore = score;
                    }
                }catch(Exception e){

                }
            }
        }
        return best;
    }
}
