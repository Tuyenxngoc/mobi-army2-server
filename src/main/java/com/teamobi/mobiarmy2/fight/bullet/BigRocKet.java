package com.teamobi.mobiarmy2.fight.bullet;

import com.teamobi.mobiarmy2.fight.Bullet;
import com.teamobi.mobiarmy2.fight.BulletManager;
import com.teamobi.mobiarmy2.fight.Player;

public class BigRocKet extends Bullet {
    public static final int MIN_Y = -614;
    private final short targetX;

    public BigRocKet(BulletManager bulletManager, int damage, Player player) {
        super(bulletManager, (byte) 37, damage, player, player.getX() - 20, player.getY() - 120, 0, 0, 0, 0);
        bulletManager.setTypeShoot((byte) 1);//Ghi tọa độ tuyệt đối
        this.targetX = bulletManager.getFightManager().getRandomPlayer().getX();
    }

    @Override
    public void update() {
        if (frame < 60) {
            // bay lên
            if (frame == 0) {
                vy = -30;
            } else {
                int mod = (frame - 1) % 5;
                if (mod == 0) {
                    vy = -25;
                } else {
                    vy = -24;
                }
            }

            // nếu đạt max thì giữ y tới frame 60
            if (y <= MIN_Y) {
                y = MIN_Y;
                vy = 0;
            }
        } else {
            // rơi xuống với vận tốc cố định
            vy = 25;
            x = targetX;
        }
        super.update();
    }
}