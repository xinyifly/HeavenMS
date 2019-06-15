importPackage(Packages.server.quest)

var status;
var quests;

function start() {
    status = -1;
    quests = MapleQuest.getMatchedQuests('');
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
            var npcid = quest.getNpcRequirement(false);
            if (quest.canStart(cm.getPlayer(), npcid, false)) continue;
            if (!quest.canStart(cm.getPlayer(), npcid)) continue;

            str += "#b\r\n#L" + i + "# " + quest.getName() + "#l";
            count++;
        }
        if (count <= 0) {
            cm.sendOk("You don't have any missed quest.");
            cm.dispose();
        } else {
            str = 'You have ' + count + ' missed quests: ' + str;
            cm.sendSimple(str);
        }
    } else if (status == 1) {
        var cost = 100000;
        if (cm.getMeso() < cost) {
            cm.sendOk('You have to pay #b' + cost + '#k mesos to retrive each missing quest.');
            cm.dispose();
            return;
        }
        var quest = quests.get(selection);
        var npcid = quest.getNpcRequirement(false);
        quest.start(cm.getPlayer(), npcid);
        cm.gainMeso(-cost);
        cm.getPlayer().dropMessage(5, 'You have started quest ' + quests.get(selection).getName() + '.');
        cm.dispose();
    } else {
        cm.dispose();
    }
}
