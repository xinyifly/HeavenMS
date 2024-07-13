/*
    This file is part of the HeavenMS MapleStory Server, commands OdinMS-based
    Copyleft (L) 2016 - 2018 RonanLana

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation version 3 as published by
    the Free Software Foundation. You may not use, modify or distribute
    this program under any other version of the GNU Affero General Public
    License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

/*
   @Author: Arthur L - Refactored command content into modules
*/
package client.command.commands.gm0;

import client.MapleCharacter;
import client.command.Command;
import client.MapleClient;
import client.SkillFactory;
import tools.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ABuffCommand extends Command {
    {
        setDescription("");
    }

    @Override
    public void execute(MapleClient c, String[] params) {
        MapleCharacter player = c.getPlayer();

        PreparedStatement ps = null;
        ResultSet rs = null;
        Connection con = null;
        try {
            con = DatabaseConnection.getConnection();
            ps = con.prepareStatement("SELECT `skills`.`skillid`, `skills`.`skilllevel`, `characters`.`level` FROM (SELECT DISTINCT `skillid`, MAX(`skilllevel`) `skilllevel` FROM `skills` WHERE `characterid` IN (SELECT `id` FROM `characters` WHERE `accountid` = ? AND `id` <> ?) AND `skillid` IN (SELECT `skillid` FROM `accountbuffs`) GROUP BY `skillid`) `sub` LEFT JOIN `skills` ON `sub`.`skillid` = `skills`.`skillid` AND `sub`.`skilllevel` = `skills`.`skilllevel` LEFT JOIN `characters` ON `skills`.`characterid` = `characters`.`id` WHERE `characterid` IN (SELECT `id` FROM `characters` WHERE `accountid` = ? AND `id` <> ?)");
            ps.setInt(1, player.getAccountID());
            ps.setInt(2, player.getId());
            ps.setInt(3, player.getAccountID());
            ps.setInt(4, player.getId());
            rs = ps.executeQuery();

            while (rs.next()) {
                int skillLevel = rs.getInt("skilllevel") * Math.min(rs.getInt("level"), 200) / 200;
                if (skillLevel > 0) {
                    SkillFactory.getSkill(rs.getInt("skillid")).getEffect(skillLevel).applyTo(player);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (ps != null && !ps.isClosed()) {
                    ps.close();
                }
                if (rs != null && !rs.isClosed()) {
                    rs.close();
                }
                if (con != null && !con.isClosed()) {
                    con.close();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }
}
