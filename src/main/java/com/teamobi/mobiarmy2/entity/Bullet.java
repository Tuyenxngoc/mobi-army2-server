package com.teamobi.mobiarmy2.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Bullet {
    private List<BulletPoint> trajectory;//Quỹ đạo bay của đạn từ súng đến mục tiêu

    private byte dXLaser;//Vector hướng X của tia laser
    private byte dYLaser;//Vector hướng Y của tia laser

    private List<BulletPoint> hitPoints;//Các điểm va chạm/nổ

}
