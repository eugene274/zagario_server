package leaderboard.dao;

import accountserver.dao.DAO;
import accountserver.dao.exceptions.DaoException;
import accountserver.database.DbHibernate;
import leaderboard.model.Score;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.StringJoiner;

/**
 * Created by eugene on 12/16/16.
 */

public class ScoreDAO implements DAO<Score> {
    private final int gsId;

    private Connection connection;
    private ScoreListExecutor scoreListExecutor = new ScoreListExecutor();

    public ScoreDAO(int gsId) throws DaoException {
        this.gsId = gsId;

        // grab connection
        DbHibernate.newSession().doWork(c -> {
            connection = c;
        });

        initSchema();
    }

    private void initSchema() throws DaoException {
        try {
            connection.createStatement().execute("CREATE TABLE IF NOT EXISTS LEADERBOARD (" +
                    "GS_ID INT NOT NULL," +
                    "PLAYER_ID INT NOT NULL," +
                    "SCORE INT NOT NULL" +
                    "PRIMARY KEY (GS_ID, PLAYER_ID)" +
                    ");");
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    @Override
    public Long insert(Score in) throws DaoException {
        try {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO LEADERBOARD (GS_ID, PLAYER_ID, SCORE)" +
                    "VALUES (?, ?, ?)" +
                    ";");
            statement.setInt(1, gsId);
            statement.setInt(2, in.getPlayerId());
            statement.setInt(3, in.getScore());
            statement.execute();
            return -1L;
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    @Override
    public Score getById(Long id) throws DaoException {
        List<Score> scores = getWhere("PLAYER_ID = " + id);
        if (scores.size() > 1){
            throw new DaoException("More than one result for id");
        }
        return (scores.size() == 1)? scores.get(0) : null;
    }

    @Override
    public List<Score> getWhere(String... conditions) throws DaoException {
        StringJoiner conditionJoiner = new StringJoiner(" AND ");
        for (String condition : conditions){
            conditionJoiner.add(condition);
        }
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM LEADERBOARD WHERE GS_ID = ? AND " + conditionJoiner.toString());
            statement.setInt(1, gsId);
            ResultSet resultSet = statement.executeQuery();
            return scoreListExecutor.execute(resultSet);
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    @Override
    public List<Score> getAll() throws DaoException {
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM LEADERBOARD WHERE GS_ID = ?;");
            statement.setInt(1, gsId);
            ResultSet resultSet = statement.executeQuery();
            return scoreListExecutor.execute(resultSet);
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    @Override
    public void remove(Score in) throws DaoException {

    }

    @Override
    public void remove(Long id) throws DaoException {

    }
}
