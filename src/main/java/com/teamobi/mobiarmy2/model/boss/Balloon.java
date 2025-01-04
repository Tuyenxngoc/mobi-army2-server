package com.teamobi.mobiarmy2.model.boss;

import com.teamobi.mobiarmy2.fight.IFightManager;
import com.teamobi.mobiarmy2.model.Boss;
import com.teamobi.mobiarmy2.model.Player;
import com.teamobi.mobiarmy2.util.Utils;
import lombok.Getter;

/**
 * @author tuyen
 */
public class Balloon extends Boss {

    @Getter
    private final Player[] bodyParts = new Player[5];
    private int currentTurn = -1;

    public Balloon(IFightManager fightManager, byte index, short x, short y) {
        super(fightManager, index, (byte) 17, "Balloon", x, y, (short) 0, (short) 0, (short) 1, 4);
        super.isFlying = true;
    }

    @Override
    public void turnAction() {
        // Thêm Balloon Eye nếu bodyParts[4] là null, Gun và Gun Big bị tiêu diệt
        if (bodyParts[4] == null && bodyParts[1].isDead() && bodyParts[2].isDead()) {
            BalloonEye newBoss = new BalloonEye(fightManager, (byte) fightManager.getTotalPlayers(), (short) (x + 55), (short) (y - 27), (short) 1000);
            bodyParts[4] = newBoss;
            fightManager.addBoss(newBoss);
            skipTurn();
            return;
        }

        // Đánh dấu bị tiêu diệt nếu Gun, Gun Big, Eye bị tiêu diệt
        if (bodyParts[4] != null) {
            if (bodyParts[1].isDead() && bodyParts[2].isDead() && bodyParts[4].isDead()) {
                if (!bodyParts[3].isDead()) {
                    bodyParts[3].die();
                }
                bodyParts[0].die();

                //Kết thúc trận đấu
                fightManager.checkWin();
                return;
            }
        }

        Player player = fightManager.findClosestPlayer(x, y);
        if (player == null) {
            skipTurn();
            return;
        }

        // Tính toán lượt chơi
        if (bodyParts[4] != null) {
            currentTurn = 4;
        } else {
            do {
                currentTurn = (currentTurn + 1) % bodyParts.length;
            } while (bodyParts[currentTurn] == null || bodyParts[currentTurn].isDead() || currentTurn == 3);
        }

        switch (currentTurn) {
            case 0 -> {//Balloon
                short toX = (short) Utils.nextInt(100, fightManager.getMapManger().getWidth() - 100);
                short toY = (short) Utils.nextInt(-150, 50);
                updateBodyPartsCoordinates(toX, toY);

                if (!bodyParts[1].isDead()) {
                    fightManager.newShoot(index, (byte) 44, (short) Utils.getArgXY(x, y, player.getX(), player.getY()), (byte) 10, (byte) 0, (byte) 1, true);
                } else if (!bodyParts[2].isDead()) {
                    fightManager.newShoot(index, (byte) 43, (short) 270, (byte) 20, (byte) 0, (byte) 1, true);
                }
            }

            //Gun
            case 1 -> {
                short toX = (short) Utils.nextInt(100, fightManager.getMapManger().getWidth() - 100);
                short toY = (short) Utils.nextInt(-100, 50);
                updateBodyPartsCoordinates(toX, toY);

                fightManager.newShoot(index, (byte) 44, (short) Utils.getArgXY(x, y, player.getX(), player.getY()), (byte) 10, (byte) 0, (byte) 1, true);
            }

            //Gun Big
            case 2 -> fightManager.newShoot(index, (byte) 43, (short) 270, (byte) 20, (byte) 0, (byte) 1, true);

            //Eye
            case 4 -> {
                short toX = (short) Utils.nextInt(100, fightManager.getMapManger().getWidth() - 100);
                short toY = (short) Utils.nextInt(-50, 50);
                updateBodyPartsCoordinates(toX, toY);

                fightManager.newShoot(index, (byte) 45, (short) Utils.getArgXY(x, y, player.getX(), player.getY()), (byte) 20, (byte) 0, (byte) 1, true);
            }
        }
    }

    private void updateBodyPartsCoordinates(short toX, short toY) {
        for (byte i = 0; i < bodyParts.length; i++) {
            Player boss = bodyParts[i];
            if (boss == null || boss.isDead()) {
                continue;
            }
            switch (i) {
                case 0 -> {
                    boss.setX(toX);
                    boss.setY(toY);
                }
                case 1 -> {
                    boss.setX((short) (toX + 51));
                    boss.setY((short) (toY + 19));
                }
                case 2 -> {
                    boss.setX((short) (toX - 5));
                    boss.setY((short) (toY + 30));
                }
                case 3 -> {
                    boss.setX((short) (toX - 67));
                    boss.setY((short) (toY - 6));
                }
                case 4 -> {
                    boss.setX((short) (toX + 57));
                    boss.setY((short) (toY - 27));
                }
            }
        }
        fightManager.sendPlayerFlyPosition(index);
    }
}
