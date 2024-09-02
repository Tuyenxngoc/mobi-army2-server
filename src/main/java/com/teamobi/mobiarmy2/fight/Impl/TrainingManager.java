package com.teamobi.mobiarmy2.fight.Impl;

import com.teamobi.mobiarmy2.fight.ITrainingManager;
import com.teamobi.mobiarmy2.model.User;

public class TrainingManager implements ITrainingManager {

    private final User trainingUser;

    public TrainingManager(User trainingUser) {
        this.trainingUser = trainingUser;
    }

    @Override
    public void startTraining() {

    }

    @Override
    public void stopTraining() {

    }
}
