package replication;

import main.MasterServer;
import org.junit.Test;

/**
 * Created by eugene on 11/25/16.
 */
public class FakeReplicaTest {
  @Test
  public void fullState() throws Exception{
    MasterServer.main(new String[]{"test", "src/test/resources/fake_replica_config.ini"});
  }
}
