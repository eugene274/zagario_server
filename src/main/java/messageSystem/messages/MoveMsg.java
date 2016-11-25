package messageSystem.messages;

import main.ApplicationContext;
import messageSystem.Abonent;
import messageSystem.Message;
import messageSystem.MessageSystem;
import model.Player;
import network.ClientConnectionServer;

/**
 * Created by ivan on 25.11.16.
 */
public class MoveMsg extends Message {
    private Player player;

    public MoveMsg(Player player) {
        super(null, ApplicationContext.instance()
                .get(MessageSystem.class)
                .getService(ClientConnectionServer.class)
                .getAddress());
    }

    @Override
    public void exec(Abonent abonent) {
        Message.log.info("Command move recieved for " + player);
    }
}
