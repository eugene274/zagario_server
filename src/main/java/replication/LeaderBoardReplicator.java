package replication;

import main.ApplicationContext;
import matchmaker.IMatchMaker;
import model.GameSession;
import network.ClientConnections;
import network.packets.PacketLeaderBoard;

import java.io.IOException;

/**
 * Created by eugene on 11/21/16.
 */
public class LeaderBoardReplicator implements Replicator {
	@Override
	public void replicate() {
        for (GameSession gameSession : ApplicationContext.instance().get(IMatchMaker.class).getActiveGameSessions()) {
            ApplicationContext.instance()
                    .get(ClientConnections.class)
                    .getConnections()
                    .stream()
                    .filter(connection -> gameSession.getPlayers().contains(connection.getKey()))
                    .forEach(connection -> {
                try {
                    new PacketLeaderBoard(gameSession.getLeaders()).write(connection.getValue());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
	}
}
