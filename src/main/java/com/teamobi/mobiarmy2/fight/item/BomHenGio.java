package com.teamobi.mobiarmy2.fight.item;

import com.teamobi.mobiarmy2.fight.Bullet;

public class BomHenGio {
    public int id;
    public int X;
    public int Y;
    public int count;
    public Bullet bull;

    public BomHenGio(int id, Bullet bull, int count) {
        this.id = id;
        this.X = bull.getX();
        this.Y = bull.getY();
        this.count = count;
        this.bull = bull;
    }

}