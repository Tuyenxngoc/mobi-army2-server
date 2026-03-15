package com.teamobi.mobiarmy2.fight.boss;

import com.teamobi.mobiarmy2.fight.Boss;
import com.teamobi.mobiarmy2.fight.FightManager;
import com.teamobi.mobiarmy2.fight.Player;
import com.teamobi.mobiarmy2.util.RandomUtil;
import com.teamobi.mobiarmy2.util.Utils;
import lombok.Getter;

public class VenomousSpider extends Boss {
    @Getter
    private Player targetPlayer;
    private byte actionCountdown;

    public VenomousSpider(FightManager fightManager, short x, short y, int maxHp) {
        super(fightManager, (byte) 22, "Venomous Spider", x, y, (short) 45, (short) 48, maxHp, 4);
        super.isFlying = true;
    }

    @Override
    public boolean shouldCollide() {
        return !isDead;
    }

    @Override
    public void turnAction() {
        short preX = x;

        //Lấy ra ngẫu nhiên người chơi chưa bị kéo
        Player player = null;
        if (actionCountdown == 0) {
            player = fightManager.getRandomPlayer(p -> p.getY() - 150 > y);
        }

        if (actionCountdown == 0 && player != null) {
            actionCountdown = 3;
            targetPlayer = player;

            //Di chuyển đến vị trí x của người chơi
            x = player.getX();
            fightManager.sendPlayerFlyPosition(index);

            //Gửi message capture
            fightManager.sendCapture(index, player.getIndex());

            //Thả tơ nhện
            fightManager.createShoot(this, (byte) 8, (short) 270, (byte) 10, (byte) 0, (byte) 1, false);

            //Di chuyển về vị trí cũ
            x = preX;
            fightManager.sendPlayerFlyPosition(index);

            //Chuyển lượt tiếp theo
            fightManager.doNextTurn();
        } else {
            if (actionCountdown > 0) {
                actionCountdown--;
            }

            player = fightManager.findClosestPlayer(x, y);
            if (player != null) {
                if (!player.isPoisoned()) {
                    //Di chuyển tới vị trí x của người chơi
                    x = player.getX();
                    fightManager.sendPlayerFlyPosition(index);

                    //Gửi ms thả độc
                    fightManager.sendBulletHit(index, player.getIndex());
                    player.setPoisoned(true);

                    //Di chuyển về vị trí ban đầu
                    x = preX;
                    fightManager.sendPlayerFlyPosition(index);

                    //Chuyển lượt tiếp theo
                    fightManager.doNextTurn();
                } else {
                    x = (short) RandomUtil.nextInt(50, fightManager.getFightMapManager().getWidth() - 50);
                    fightManager.sendPlayerFlyPosition(index);
                    fightManager.createShoot(this, (byte) 47, (short) Utils.getArgXY(x, y, player.getX(), player.getY()), (byte) 10, (byte) 0, (byte) 1);
                }
            } else {
                //Chuyển lượt tiếp theo
                fightManager.doNextTurn();
            }
        }
    }
}
