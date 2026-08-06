package com.teamobi.mobiarmy2.server;

import lombok.Getter;
import lombok.Setter;

/**
 * Cờ trạng thái toàn server, đọc/ghi được từ nhiều luồng.
 * Tách khỏi {@link ServerManager} để handler không phải phụ thuộc vào cả
 * ServerManager chỉ để đọc một cờ — nếu không sẽ tạo vòng phụ thuộc
 * ServerManager -> SessionFactory -> handler -> ServerManager.
 */
@Getter
@Setter
public class ServerState {
    private volatile boolean maintenanceMode = false;
}
