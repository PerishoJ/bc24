package tx.thinkin;

import tx.thinkin.idears.*;
import battlecode.common.RobotController;

import java.util.LinkedList;
import java.util.List;

public class Noggin {


    private List<BrightIdea> ideas = new LinkedList<>();

    public Noggin(){
        ideas.add(new GetOnGet());
        ideas.add(new MeanderAround());
        ideas.add(new RaiseHell());
        ideas.add(new SweetTea());
    }

    public BrightIdea ponder (BigPicture layOfTheLand , RobotController I){
        BrightIdea best = null;
        int bestScore = 0;
        // score each idea and find the good one
        for (BrightIdea idea : ideas){
            if(best == null){
                best = idea;
                bestScore = idea.howAboutThat(layOfTheLand,I);
            } else {
                int score = idea.howAboutThat(layOfTheLand,I);
                if(score>bestScore){
                    best = idea;
                    bestScore = score;
                }
            }
        }
        return best;
    }
}
