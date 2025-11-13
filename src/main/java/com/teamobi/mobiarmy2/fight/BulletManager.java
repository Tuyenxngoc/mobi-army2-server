package com.teamobi.mobiarmy2.fight;

import com.teamobi.mobiarmy2.entity.Bullet;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BulletManager {

    private byte typeShoot;

    private List<Bullet> bullets;

    private byte typeSuper;//Loại đạn siêu cao
    private short xSuper;//Tọa độ X của đạn siêu cao
    private short ySuper;//Tọa độ Y của đạn siêu cao
}
