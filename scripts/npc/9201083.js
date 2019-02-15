/**
 *9201083 - The Glimmer Man
 *@author Ronan
 */
 
var status;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    status++;
    if (mode != 1) {
        cm.dispose();
        return;
    }
    
    var arr = [
	[[1082223, "Stormcaster Gloves"], [4031824, "Stormcaster Gloves Forging Manual"]],
	[[1072344, "Facestompers"], [4031911, "Facestompers Forging Manual"]],
	[[1072427, "Red Christmas Sock"], [4000421, "Qualified Knitting Ball"]],
    ];

    if (status == 0) {
	var selStr = "Gimme a manual, I make the item in a second for you";
	for (var i = 0; i < arr.length; i++) {
	    selStr += "#b\r\n#L" + i + "# " + arr[i][0][1] + "#l";
	}
	cm.sendSimple(selStr);
	return;
    }
    if (status == 1) {
	if (!cm.haveItem(arr[selection][1][0])) {
	    cm.sendOk("You don't have a #b" + arr[selection][1][1] + "#k")
	    cm.dispose();
	    return;
	}
	if (!cm.canHold(arr[selection][0][0])) {
	    cm.sendOk("You got no free slot on your inventory.");
	    cm.dispose();
	    return;
	}
	cm.gainItem(arr[selection][1][0], -1);
	cm.gainItem(arr[selection][0][0], 1);
	cm.sendOk("A piece of art, isn't it?");
	cm.dispose();
    }
}
