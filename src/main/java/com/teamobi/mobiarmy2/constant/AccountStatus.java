package com.teamobi.mobiarmy2.constant;

public enum AccountStatus {
    PENDING_VERIFICATION, // vừa đăng ký
    ACTIVE,               // dùng bình thường
    LOCKED,               // khóa tạm
    BANNED,               // khóa vĩnh viễn
    DISABLED              // admin vô hiệu hóa
}
