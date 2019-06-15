var status;
var quests;

function start() {
    status = -1;
    quests = cm.getPlayer().getSkippableQuests();
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode != 1) {
        cm.dispose();
        return;
    }
    status++;

    if (status == 0) {
        var str = '';
        var count = 0;
        for (var i = 0; i < quests.size(); i++) {
            var quest = quests.get(i);
            str += "#b\r\n#L" + i + "# " + quest.getName() + "#l";
            count++;
        }
        if (count <= 0) {
            cm.sendOk("You don't have any skippable quest.");
            cm.dispose();
        } else {
            str = 'You have ' + count + ' skippable quests: ' + str;
            cm.sendSimple(str);
        }
    } else if (status == 1) {
        var quest = quests.get(selection);
        var npcid = quest.getNpcRequirement(true);
        quest.forceComplete(cm.getPlayer(), npcid);
        cm.getPlayer().dropMessage(5, 'You have completed quest ' + quests.get(selection).getName() + '.');
        cm.dispose();
    } else {
        cm.dispose();
    }
}
