package com.teamobi.mobiarmy2.fight.boss;

import com.teamobi.mobiarmy2.fight.Boss;
import com.teamobi.mobiarmy2.fight.FightManager;
import com.teamobi.mobiarmy2.fight.Player;
import com.teamobi.mobiarmy2.server.ServerManager;
import com.teamobi.mobiarmy2.util.Utils;

import java.io.IOException;

public class UFOFire extends Boss {

    private boolean acll;
    private Player pl;
    private byte turnDie;

    public UFOFire(FightManager fightMNG, byte idGun, String name, byte location, int HPMax, short X, short Y, Player pl, byte tdie) throws IOException {
        super(fightMNG, idGun, name, location, HPMax, X, Y);
        super.theLuc = 0;
        super.width = 51;
        super.height = 46;
        this.fly = true;
        this.pl = pl;
        this.turnDie = tdie;
        this.XPExist = 0;
        this.team = pl.index % 2 == 0;
    }

    @Override
    public void turnAction() {
        try {
            if (this.turnDie == 0) {
                this.HP = 0;
                this.isUpdateHP = true;
                this.isDie = true;
                if (fightManager.checkWin()) {
                    fightManager.nextTurn();
                }
                return;
            }
            short ys = this.Y, xs = this.X;
            while (this.acll && ys < this.fightManager.mapMNG.Height + 200 && !this.fightManager.mapMNG.isCollision(xs, ys)) {
                if (ys > this.fightManager.mapMNG.Height) {
                    this.acll = false;
                }
                ys++;
            }
            if (!this.acll) {
                this.acll = true;
                Player pl2 = null;
                int i = this.team ? 1 : 0;
                int lent = 2;
                for (; i < ServerManager.maxPlayers; i += lent) {
                    Player pl3 = this.fightManager.players[i];
                    if (pl3 == null || pl3.isDie) {
                        continue;
                    }
                    pl2 = pl3;
                }
                if (pl2 != null) {
                    this.Y = (short) (pl2.Y - Utils.nextInt(150, 500));
                    this.X = pl2.X;
                    this.fightManager.flyChangeLocation(super.index);
                }
            } else {
                this.acll = false;
                this.turnDie--;
                this.fightManager.newShoot(this.index, (byte) 42, (short) 270, (byte) 20, (byte) 0, (byte) 1);
                return;
            }
            if (!fightManager.checkWin()) {
                fightManager.nextTurn();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
