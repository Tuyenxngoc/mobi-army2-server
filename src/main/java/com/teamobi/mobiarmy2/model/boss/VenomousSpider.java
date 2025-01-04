package com.teamobi.mobiarmy2.model.boss;

import com.teamobi.mobiarmy2.fight.IFightManager;
import com.teamobi.mobiarmy2.model.Boss;
import com.teamobi.mobiarmy2.model.Player;
import com.teamobi.mobiarmy2.util.Utils;
import lombok.Getter;

/**
 * @author tuyen
 */
public class VenomousSpider extends Boss {

    @Getter
    private Player targetPlayer;
    private byte actionCountdown;

    public VenomousSpider(IFightManager fightManager, byte index, short x, short y, short maxHp) {
        super(fightManager, index, (byte) 22, "Venomous Spider", x, y, (short) 45, (short) 48, maxHp, 4);
        super.isFlying = true;
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
            fightManager.capture(index, player.getIndex());

            //Thả tơ nhện
            fightManager.newShoot(index, (byte) 8, (short) 270, (byte) 10, (byte) 0, (byte) 1, false);

            //Di chuyển về vị trí cũ
            x = preX;
            fightManager.sendPlayerFlyPosition(index);

            //Chuyển lượt tiếp theo
            skipTurn();
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
                    if (!fightManager.checkWin()) {
                        fightManager.nextTurn();
                    }
                } else {
                    x = (short) Utils.nextInt(50, fightManager.getMapManger().getWidth() - 50);
                    fightManager.sendPlayerFlyPosition(index);
                    fightManager.newShoot(index, (byte) 47, (short) Utils.getArgXY(x, y, player.getX(), player.getY()), (byte) 10, (byte) 0, (byte) 1, true);
                }
            } else {
                //Chuyển lượt tiếp theo
                if (!fightManager.checkWin()) {
                    fightManager.nextTurn();
                }
            }
        }
    }
}
