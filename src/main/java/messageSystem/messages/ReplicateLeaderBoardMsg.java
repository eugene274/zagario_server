package messageSystem.messages;

import main.ApplicationContext;
import messageSystem.Abonent;
import messageSystem.Address;
import messageSystem.Message;
import messageSystem.MessageSystem;
import network.ClientConnectionServer;
import replication.LeaderBoardReplicator;

/**
 * Created by eugene on 11/21/16.
 */
public class ReplicateLeaderBoardMsg extends Message {
	public ReplicateLeaderBoardMsg(Address from) {
		super(from, ApplicationContext.instance().get(MessageSystem.class).getService(ClientConnectionServer.class).getAddress());
	}

	@Override
	public void exec(Abonent abonent) {
		ApplicationContext.instance().get(LeaderBoardReplicator.class).replicate();
	}
}
