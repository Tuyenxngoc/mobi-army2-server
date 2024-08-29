package com.teamobi.mobiarmy2.fight.boss;

import com.teamobi.mobiarmy2.fight.Boss;
import com.teamobi.mobiarmy2.fight.FightManager;
import com.teamobi.mobiarmy2.fight.Player;
import com.teamobi.mobiarmy2.util.Utils;

import java.io.IOException;
import java.util.ArrayList;

public class SpiderPoisonous extends Boss {

    protected byte nturn;
    public Player target;

    public SpiderPoisonous(FightManager fightMNG, byte idGun, String name, byte location, int HPMax, short X, short Y) throws IOException {
        super(fightMNG, idGun, name, location, HPMax, X, Y);
        super.theLuc = 0;
        this.fly = true;
        super.width = 45;
        super.height = 48;
        this.XPExist = 350;
        this.nturn = 0;
    }

    @Override
    public void turnAction() {
        try {
            short xpre = this.x;
            ArrayList<Player> player = new ArrayList<>();
            for (int i = 0; i < 8; i++) {
                if (this.fightManager.players[i] != null && !this.fightManager.players[i].isDie && this.fightManager.players[i].y - 150 > this.y) {
                    player.add(this.fightManager.players[i]);
                }
            }
            //kéo
            if (player.size() > 0 && nturn == 0) {
                nturn = 3;
                Player pl = (player.get(Utils.nextInt(0, player.size() - 1)));
                this.x = pl.x;
                this.fightManager.flyChangeLocation(super.index);
                this.fightManager.capture(super.index, pl.index);
                fightManager.isNextTurn = false;
                this.target = pl;
                this.fightManager.newShoot(this.index, (byte) 8, (short) 270, (byte) 10, (byte) 0, (byte) 1, false);
                fightManager.isNextTurn = true;
                this.x = xpre;
                this.fightManager.flyChangeLocation(super.index);
                if (!fightManager.checkWin()) {
                    this.fightManager.nextTurn();
                }
            } else {
                if (nturn > 0) {
                    nturn--;
                }
                Player pl = fightManager.findClosestPlayerByX(x);
                if (!pl.isBiDoc) {
                    this.x = pl.x;
                    this.fightManager.flyChangeLocation(super.index);
                    this.fightManager.thadocBullet(super.index, pl.index);
                    this.fightManager.updateBiDoc(pl);
                    this.x = xpre;
                    this.fightManager.flyChangeLocation(super.index);
                    if (!fightManager.checkWin()) {
                        this.fightManager.nextTurn();
                    }
                } else {
                    this.x = (short) Utils.nextInt(50, fightManager.mapManager.width - 50);
                    this.fightManager.flyChangeLocation(super.index);
                    this.fightManager.newShoot(this.index, (byte) 47, (short) Utils.getArgXY(x, y, pl.x, pl.y), (byte) 10, (byte) 0, (byte) 1, false);
                }
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
