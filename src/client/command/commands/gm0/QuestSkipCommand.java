package client.command.commands.gm0;

import client.MapleClient;
import client.command.Command;

public class QuestSkipCommand extends Command {
    {
        setDescription("");
    }

    @Override
    public void execute(MapleClient client, String[] params) {
        client.getAbstractPlayerInteraction().openNpc(9200000, "QuestSkip");
    }
}
