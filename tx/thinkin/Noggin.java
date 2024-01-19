package tx.thinkin;

import tx.Cowboy;
import tx.thinkin.idears.*;

import java.util.LinkedList;
import java.util.List;


/**
 * Due to the simplicity of this class, maybe it should not be a class. This could be a method.
 */
public class Noggin {

    public Noggin(BigPicture layOfTheLand, Cowboy me) {
        this();
        this.layOfTheLand = layOfTheLand;
        yoursTruly = me;
    }

    BigPicture layOfTheLand;
    Cowboy yoursTruly;

    private final List<BrightIdea> ideas = new LinkedList<>();

    private Noggin(){
        ideas.add(new GetOnGet());
        ideas.add(new MeanderAround());
        ideas.add(new RememberTheAlamo());
        ideas.add(new RaiseHell());
        ideas.add(new SweetTea());
        ideas.add(new FlagThief());
        ideas.add(new Trapper());
//        ideas.add(new TestBugNav());
//        ideas.add(new TestAStarNav());
        ideas.add(new BlazinSomeTrails());
    }

    public BrightIdea ponder (BigPicture layOfTheLand){
        BrightIdea best = null;
        int bestScore = 0;
        // score each idea and find the good one
        for (BrightIdea idea : ideas){
            if(best == null){
                best = idea;
                bestScore = idea.howAboutThat(layOfTheLand, yoursTruly.me);
            } else {
                try {
                    int score = idea.howAboutThat(layOfTheLand, yoursTruly.me);
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
