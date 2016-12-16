package leaderboard;

import accountserver.dao.exceptions.DaoException;
import leaderboard.dao.ScoreDAO;
import leaderboard.model.Score;
import model.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Created by eugene on 12/16/16.
 */
public class LeaderBoard {
    private final int gsId;
    private final int initialScore;
    private ScoreDAO dao;

    public LeaderBoard(int gsId, int initialScore) {
        this.gsId = gsId;
        this.initialScore = initialScore;
        try {
            dao = new ScoreDAO(gsId);
        } catch (DaoException e) {
            // todo
            e.printStackTrace();
        }
    }

    public void registerPlayer(@NotNull Player player){
        try {
            dao.insert(new Score(player.getId(), initialScore));
        } catch (DaoException e) {
            // TODO
            e.printStackTrace();
        }
    }

    public void addPoints(@NotNull Player player, int points){

    }

    public Score[] getLeaders(int n){
        return null;
    }

    public Score[] getLeaders(){
        return null;
    }
}
