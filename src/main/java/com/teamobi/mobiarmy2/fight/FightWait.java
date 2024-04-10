package com.teamobi.mobiarmy2.fight;

import com.teamobi.mobiarmy2.constant.GameString;
import com.teamobi.mobiarmy2.model.User;
import com.teamobi.mobiarmy2.service.IFightService;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FightWait {

    private FightManager fightManager;
    private final IFightService fightService;

    public FightWait(IFightService fightService) {
        this.fightService = fightService;
    }

    public void startGame(User user){
        fightService.sendMessageToUser(user, GameString.Wait_click());
    }

}
