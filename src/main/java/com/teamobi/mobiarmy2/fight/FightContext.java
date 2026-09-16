package com.teamobi.mobiarmy2.fight;

import com.teamobi.mobiarmy2.network.MessageSender;
import com.teamobi.mobiarmy2.server.SessionRegistry;
import com.teamobi.mobiarmy2.service.ClanService;

/**
 * Các dependency tầm server mà một {@link FightWait} cần.
 *
 * <p>{@link com.teamobi.mobiarmy2.server.RoomManager} và
 * {@link com.teamobi.mobiarmy2.entity.Room} không dùng tới chúng, chỉ chuyển
 * tiếp xuống FightWait — gom lại thành một tham số để hai lớp đó không phải
 * thêm ba tham số chỉ để đi qua.
 */
public record FightContext(
        MessageSender messageSender,
        ClanService clanService,
        SessionRegistry sessionRegistry) {
}
