package com.teamobi.mobiarmy2.fight.boss;

import com.teamobi.mobiarmy2.fight.Boss;
import com.teamobi.mobiarmy2.fight.FightManager;
import com.teamobi.mobiarmy2.fight.Player;
import com.teamobi.mobiarmy2.server.ServerManager;
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
            short xpre = this.X;
            ArrayList<Player> player = new ArrayList<>();
            for (int i = 0; i < ServerManager.maxPlayers; i++) {
                if (this.fightManager.players[i] != null && !this.fightManager.players[i].isDie && this.fightManager.players[i].Y - 150 > this.Y) {
                    player.add(this.fightManager.players[i]);
                }
            }
            //kéo
            if (player.size() > 0 && nturn == 0) {
                nturn = 3;
                Player pl = (player.get(Utils.nextInt(0, player.size() - 1)));
                this.X = pl.X;
                this.fightManager.flyChangeLocation(super.index);
                this.fightManager.capture(super.index, pl.index);
                fightManager.isNextTurn = false;
                this.target = pl;
                this.fightManager.newShoot(this.index, (byte) 8, (short) 270, (byte) 10, (byte) 0, (byte) 1);
                fightManager.isNextTurn = true;
                this.X = xpre;
                this.fightManager.flyChangeLocation(super.index);
                if (!fightManager.checkWin()) {
                    this.fightManager.nextTurn();
                }
            } else {
                if (nturn > 0) {
                    nturn--;
                }
                Player pl = fightManager.getPlayerClosest(X, Y);
                if (!pl.isBiDoc) {
                    this.X = pl.X;
                    this.fightManager.flyChangeLocation(super.index);
                    this.fightManager.thadocBullet(super.index, pl.index);
                    this.fightManager.updateBiDoc(pl);
                    this.X = xpre;
                    this.fightManager.flyChangeLocation(super.index);
                    if (!fightManager.checkWin()) {
                        this.fightManager.nextTurn();
                    }
                } else {
                    this.X = (short) Utils.nextInt(50, fightManager.mapMNG.Width - 50);
                    this.fightManager.flyChangeLocation(super.index);
                    this.fightManager.newShoot(this.index, (byte) 47, (short) Utils.getArgXY(X, Y, pl.X, pl.Y), (byte) 10, (byte) 0, (byte) 1);
                }
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
