/*
 This file is part of the OdinMS Maple Story Server
 Copyright (C) 2008 ~ 2010 Patrick Huy <patrick.huy@frz.cc>
 Matthias Butz <matze@odinms.de>
 Jan Christian Meyer <vimes@odinms.de>

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License version 3
 as published by the Free Software Foundation. You may not use, modify
 or distribute this program under any other version of the
 GNU Affero General Public License.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package server.life;

import config.YamlConfig;
import constants.inventory.ItemConstants;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import provider.MapleData;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;
import server.MapleItemInformationProvider;
import tools.DatabaseConnection;
import tools.Pair;
import tools.Randomizer;

public class MapleMonsterInformationProvider {
    // Author : LightPepsi

    private static final MapleMonsterInformationProvider instance = new MapleMonsterInformationProvider();

    public static MapleMonsterInformationProvider getInstance() {
        return instance;
    }

    private final Map<Integer, List<MonsterDropEntry>> drops = new HashMap<>();
    private final List<MonsterGlobalDropEntry> globaldrops = new ArrayList<>();
    private final Map<Integer, List<MonsterGlobalDropEntry>> continentdrops = new HashMap<>();

    private final Map<Integer, List<Integer>> dropsChancePool = new HashMap<>();    // thanks to ronan
    private final Set<Integer> hasNoMultiEquipDrops = new HashSet<>();
    private final Map<Integer, List<MonsterDropEntry>> extraMultiEquipDrops = new HashMap<>();

    private final Map<Pair<Integer, Integer>, Integer> mobAttackAnimationTime = new HashMap<>();
    private final Map<MobSkill, Integer> mobSkillAnimationTime = new HashMap<>();

    private final Map<Integer, Pair<Integer, Integer>> mobAttackInfo = new HashMap<>();

    private final Map<Integer, Boolean> mobBossCache = new HashMap<>();
    private final Map<Integer, String> mobNameCache = new HashMap<>();

    protected MapleMonsterInformationProvider() {
        retrieveGlobal();
    }

    public final List<MonsterGlobalDropEntry> getRelevantGlobalDrops(int mapid) {
        int continentid = mapid / 100000000;

        List<MonsterGlobalDropEntry> contiItems = continentdrops.get(continentid);
        if (contiItems == null) {   // continent separated global drops found thanks to marcuswoon
            contiItems = new ArrayList<>();

            for (MonsterGlobalDropEntry e : globaldrops) {
                if (e.continentid < 0 || e.continentid == continentid) {
                    contiItems.add(e);
                }
            }

            continentdrops.put(continentid, contiItems);
        }

        return contiItems;
    }

    private void retrieveGlobal() {
        PreparedStatement ps = null;
        ResultSet rs = null;
        Connection con = null;

        try {
            con = DatabaseConnection.getConnection();
            ps = con.prepareStatement("SELECT * FROM drop_data_global WHERE chance > 0");
            rs = ps.executeQuery();

            while (rs.next()) {
                globaldrops.add(
                        new MonsterGlobalDropEntry(
                                rs.getInt("itemid"),
                                rs.getInt("chance"),
                                rs.getByte("continent"),
                                rs.getInt("minimum_quantity"),
                                rs.getInt("maximum_quantity"),
                                rs.getShort("questid")));
            }

            rs.close();
            ps.close();
            con.close();
        } catch (SQLException e) {
            System.err.println("Error retrieving drop" + e);
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
            } catch (SQLException ignore) {
                ignore.printStackTrace();
            }
        }
    }

    public List<MonsterDropEntry> retrieveEffectiveDrop(final int monsterId) {
        // this reads the drop entries searching for multi-equip, properly processing them

        List<MonsterDropEntry> list = retrieveDrop(monsterId);
        if (hasNoMultiEquipDrops.contains(monsterId) || !YamlConfig.config.server.USE_MULTIPLE_SAME_EQUIP_DROP) {
            return list;
        }

        List<MonsterDropEntry> multiDrops = extraMultiEquipDrops.get(monsterId), extra = new LinkedList<>();
        if (multiDrops == null) {
            multiDrops = new LinkedList<>();

            for (MonsterDropEntry mde : list) {
                if (ItemConstants.isEquipment(mde.itemId) && mde.Maximum > 1) {
                    multiDrops.add(mde);

                    int rnd = Randomizer.rand(mde.Minimum, mde.Maximum);
                    for (int i = 0; i < rnd - 1; i++) {
                        extra.add(mde);   // this passes copies of the equips' MDE with min/max quantity > 1, but idc on equips they are unused anyways
                    }
                }
            }

            if (!multiDrops.isEmpty()) {
                extraMultiEquipDrops.put(monsterId, multiDrops);
            } else {
                hasNoMultiEquipDrops.add(monsterId);
            }
        } else {
            for (MonsterDropEntry mde : multiDrops) {
                int rnd = Randomizer.rand(mde.Minimum, mde.Maximum);
                for (int i = 0; i < rnd - 1; i++) {
                    extra.add(mde);
                }
            }
        }

        List<MonsterDropEntry> ret = new LinkedList<>(list);
        ret.addAll(extra);

        return ret;
    }

    public final List<MonsterDropEntry> retrieveDrop(final int monsterId) {
        if (drops.containsKey(monsterId)) {
            return drops.get(monsterId);
        }
        final List<MonsterDropEntry> ret = new LinkedList<>();
        
        PreparedStatement ps = null;
        ResultSet rs = null;
        Connection con = null;
        try {
            con = DatabaseConnection.getConnection();
            ps = con.prepareStatement("SELECT itemid, chance, minimum_quantity, maximum_quantity, questid FROM drop_data WHERE dropperid = ?");
            ps.setInt(1, monsterId);
            rs = ps.executeQuery();

            while (rs.next()) {
                ret.add(new MonsterDropEntry(rs.getInt("itemid"), rs.getInt("chance"), rs.getInt("minimum_quantity"), rs.getInt("maximum_quantity"), rs.getShort("questid")));
            }

            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return ret;
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
            } catch (SQLException ignore) {
                ignore.printStackTrace();
                return ret;
            }
        }

        // BOSS NX
        if (isBossFinal(monsterId)) {
            int monsterLevel = MapleLifeFactory.getMonster(monsterId).getLevel();
            int cardChance = Math.min(Math.max(monsterLevel, 20), 100) * 10000;
            ret.add(new MonsterDropEntry(4031865, cardChance, 1, 2, (short) 0)); // 100 NX
            ret.add(new MonsterDropEntry(4031866, cardChance, 1, 2, (short) 0)); // 250 NX
            if (monsterLevel >= 100) {
                int leafChance = Math.min(Math.max(monsterLevel - 100, 6), 50) * 20000;
                ret.add(new MonsterDropEntry(4001126, leafChance, 1, 1, (short) 0)); // Maple Leaf
            }
        }

        drops.put(monsterId, ret);
        return ret;
    }

    public final List<Integer> retrieveDropPool(final int monsterId) {  // ignores Quest and Party Quest items
        if (dropsChancePool.containsKey(monsterId)) {
            return dropsChancePool.get(monsterId);
        }

        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();

        List<MonsterDropEntry> dropList = retrieveDrop(monsterId);
        List<Integer> ret = new ArrayList<>();

        int accProp = 0;
        for (MonsterDropEntry mde : dropList) {
            if (!ii.isQuestItem(mde.itemId) && !ii.isPartyQuestItem(mde.itemId)) {
                accProp += mde.chance;
            }

            ret.add(accProp);
        }

        if (accProp == 0) {
            ret.clear();    // don't accept mobs dropping no relevant items
        }
        dropsChancePool.put(monsterId, ret);
        return ret;
    }

    public final void setMobAttackAnimationTime(int monsterId, int attackPos, int animationTime) {
        mobAttackAnimationTime.put(new Pair<>(monsterId, attackPos), animationTime);
    }

    public final Integer getMobAttackAnimationTime(int monsterId, int attackPos) {
        Integer time = mobAttackAnimationTime.get(new Pair<>(monsterId, attackPos));
        return time == null ? 0 : time;
    }

    public final void setMobSkillAnimationTime(MobSkill skill, int animationTime) {
        mobSkillAnimationTime.put(skill, animationTime);
    }

    public final Integer getMobSkillAnimationTime(MobSkill skill) {
        Integer time = mobSkillAnimationTime.get(skill);
        return time == null ? 0 : time;
    }

    public final void setMobAttackInfo(int monsterId, int attackPos, int mpCon, int coolTime) {
        mobAttackInfo.put((monsterId << 3) + attackPos, new Pair<>(mpCon, coolTime));
    }

    public final Pair<Integer, Integer> getMobAttackInfo(int monsterId, int attackPos) {
        if (attackPos < 0 || attackPos > 7) {
            return null;
        }
        return mobAttackInfo.get((monsterId << 3) + attackPos);
    }

    public static ArrayList<Pair<Integer, String>> getMobsIDsFromName(String search) {
        MapleDataProvider dataProvider = MapleDataProviderFactory.getDataProvider(new File("wz/String.wz"));
        ArrayList<Pair<Integer, String>> retMobs = new ArrayList<Pair<Integer, String>>();
        MapleData data = dataProvider.getData("Mob.img");
        List<Pair<Integer, String>> mobPairList = new LinkedList<Pair<Integer, String>>();
        for (MapleData mobIdData : data.getChildren()) {
            int mobIdFromData = Integer.parseInt(mobIdData.getName());
            String mobNameFromData = MapleDataTool.getString(mobIdData.getChildByPath("name"), "NO-NAME");
            mobPairList.add(new Pair<Integer, String>(mobIdFromData, mobNameFromData));
        }
        for (Pair<Integer, String> mobPair : mobPairList) {
            if (mobPair.getRight().toLowerCase().contains(search.toLowerCase())) {
                retMobs.add(mobPair);
            }
        }
        return retMobs;
    }

    public boolean isBoss(int id) {
        Boolean boss = mobBossCache.get(id);
        if (boss == null) {
            try {
                boss = MapleLifeFactory.getMonster(id).isBoss();
            } catch (NullPointerException npe) {
                boss = false;
            } catch (Exception e) {   //nonexistant mob
                boss = false;

                e.printStackTrace();
                System.err.println("Nonexistant mob id " + id);
            }

            mobBossCache.put(id, boss);
        }

        return boss;
    }

    public boolean isBossFinal(int id) {
        List<Integer> summons = new ArrayList<>(Arrays.asList(new Integer[]{
            6300004 /*Pachu*/, 6400004 /*Opachu*/,
            8500003 /*High Darkstar*/, 8500004 /*Low Darkstar*/,
            8510100 /*Bloody Boom*/,
            8810019 /*Red Wyvern*/, 8810020 /*Blue Wyvern*/, 8810021 /*Dark Wyvern*/,
            8810022 /*Green Cornian*/, 8810023 /*Dark Cornian*/
        }));
        // Zakum's Arms
        for (int summon=8800003; summon<=8800010; summon++) {
            summons.add(summon);
        }
        Boolean isSummon = summons.contains(id);

        List<Integer> specials = new ArrayList<>(Arrays.asList(new Integer[]{
            9400120 /*Male Boss*/, 9400121 /*Female Boss*/, 9400122 /*Male Boss*/,
            9400549 /*Headless Horseman*/, 9400571 /*Headless Horseman*/, 9400575 /*Bigfoot*/
        }));
        Boolean isEvent = (id >= 9000000 && !specials.contains(id));

        return isBoss(id) && !isSummon && !isEvent;
    }

    public String getMobNameFromId(int id) {
        String mobName = mobNameCache.get(id);
        if (mobName == null) {
            MapleDataProvider dataProvider = MapleDataProviderFactory.getDataProvider(new File("wz/String.wz"));
            MapleData mobData = dataProvider.getData("Mob.img");
            
            mobName = MapleDataTool.getString(mobData.getChildByPath(id + "/name"), "");
            mobNameCache.put(id, mobName);
        }

        return mobName;
    }

    public final void clearDrops() {
        drops.clear();
        hasNoMultiEquipDrops.clear();
        extraMultiEquipDrops.clear();
        dropsChancePool.clear();
        globaldrops.clear();
        continentdrops.clear();
        retrieveGlobal();
    }
}
