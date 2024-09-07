package com.teamobi.mobiarmy2.fight;

import com.teamobi.mobiarmy2.model.User;

/**
 * @author tuyen
 */
public interface ITrainingManager {

    void startTraining();

    void stopTraining();

    void addShoot(User user, byte bullId, short x, short y, short angle, byte force, byte force2, byte numShoot);

    byte getMapId();

}
