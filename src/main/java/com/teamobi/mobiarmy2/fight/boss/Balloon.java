package com.teamobi.mobiarmy2.fight.boss;

import com.teamobi.mobiarmy2.fight.Boss;
import com.teamobi.mobiarmy2.fight.FightManager;
import com.teamobi.mobiarmy2.fight.Player;
import com.teamobi.mobiarmy2.util.Utils;

import java.io.IOException;

public class Balloon extends Boss {

    private byte[] part;
    private int turns;

    public Balloon(FightManager fightMNG, byte idGun, String name, byte location, int HPMax, short X, short Y) throws IOException {
        super(fightMNG, idGun, name, location, HPMax, X, Y);
        super.theLuc = 40;
        super.width = 0;
        super.height = 0;
        this.fly = true;
        this.part = new byte[]{super.index, (byte) (super.index + 1), (byte) (super.index + 2), (byte) (super.index + 3), -1};
        this.turns = -1;
    }

    @Override
    public void turnAction() {
        try {
            Player pl = this.fightManager.findClosestPlayerByX(super.x);
            if (pl == null) {
                return;
            }
            boolean fd = true;
            //turn
            int turn = this.turns + 1;
            while (turn != this.turns) {
                if (turn == this.part.length) {
                    turn = 0;
                }
                Player boss = null;
                if (this.part[turn] != -1) {
                    boss = this.fightManager.players[this.part[turn]];
                }
                if (boss != null && !boss.isDie && turn != 1 && turn != 3) {
                    if (turn != 2 || !fightManager.players[part[3]].isDie) {
                        this.turns = turn;
                        break;
                    }
                }
                turn++;
            }
            for (int i = 0; i < 5; i++) {
                if (this.part[i] == -1) {
                    continue;
                }
                Player boss = this.fightManager.players[this.part[i]];
                if (boss != null && !boss.isDie && (i == 1 || i == 2 || i == 4)) {
                    fd = false;
                }
            }
            if (((this.fightManager.players[this.part[1]].isDie && this.fightManager.players[this.part[2]].isDie) || this.fightManager.players[this.part[3]].isDie) && this.part[4] == -1) {
                this.part[4] = (byte) this.fightManager.totalPlayers;
                this.fightManager.addBoss(new BalloonEye(this.fightManager, (byte) 21, "Balloon Eye", (byte) this.fightManager.totalPlayers, 1500 + (this.fightManager.getLevelTeam() * 10), (short) (super.x + 55), (short) (super.y - 27)));
            } else if (fd) {

                this.fightManager.players[part[0]].isDie = true;
                this.fightManager.players[part[0]].HP = 0;
                this.fightManager.players[part[0]].isUpdateHP = true;

                this.fightManager.players[part[3]].isDie = true;
                this.fightManager.players[part[3]].HP = 0;
                this.fightManager.players[part[3]].isUpdateHP = true;

                if (!this.fightManager.checkWin()) {
                    fightManager.nextTurn();
                    return;
                }
            }
            if (this.turns == 0) {
                if (!fd) {
                    short toX = (short) Utils.nextInt(100, this.fightManager.mapManager.width - 100);
                    short toY = (short) Utils.nextInt(-150, 50);
                    for (int i = 0; i < 5; i++) {
                        if (this.part[i] == -1) {
                            continue;
                        }
                        Player boss = (Boss) this.fightManager.players[this.part[i]];
                        if (boss == null || boss.isDie) {
                            continue;
                        }
                        switch (i) {
                            case 0:
                                boss.x = toX;
                                boss.y = toY;
                                break;
                            case 1:
                                boss.x = (short) (toX + 51);
                                boss.y = (short) (toY + 19);
                                break;
                            case 2:
                                boss.x = (short) (toX - 5);
                                boss.y = (short) (toY + 30);
                                break;
                            case 3:
                                boss.x = (short) (toX - 67);
                                boss.y = (short) (toY - 6);
                                break;
                            case 4:
                                boss.x = (short) (toX + 57);
                                boss.y = (short) (toY - 27);
                                break;
                        }
                    }
                    this.fightManager.flyChangeLocation(super.index);
                }
                if (!this.fightManager.players[this.part[1]].isDie) {
                    this.fightManager.newShoot(this.index, (byte) 44, (short) Utils.getArgXY(x, y, pl.x, pl.y), (byte) 10, (byte) 0, (byte) 1, false);
                    return;
                }
            } else if (this.turns == 2 && !this.fightManager.players[this.part[2]].isDie) {
                this.fightManager.newShoot(this.index, (byte) 43, (short) 270, (byte) 20, (byte) 0, (byte) 1, false);
                return;
            } else if (this.turns == 4 && !this.fightManager.players[this.part[4]].isDie) {
                this.fightManager.newShoot(this.index, (byte) 45, (short) Utils.getArgXY(x, y, pl.x, pl.y), (byte) 20, (byte) 0, (byte) 1, false);
                return;
            }
            if (!fightManager.checkWin()) {
                this.fightManager.nextTurn();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
