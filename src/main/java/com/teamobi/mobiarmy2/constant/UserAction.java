package com.teamobi.mobiarmy2.constant;

public enum UserAction {
    INSERT_GEM_INTO_EQUIPMENT("Ghép ngọc vào trang bị"),
    REMOVE_GEM_FROM_EQUIPMENT("Tháo ngọc khỏi trang bị"),
    UPGRADE_GEM("Nâng cấp ngọc"),
    SELL_GEM("Bán ngọc"),
    USE_SPECIAL_ITEM("Dùng vật phẩm đặc biệt"),
    COMBINE_SPECIAL_ITEM("Ghép vật phẩm đặc biệt"),
    SELL_EQUIPMENT("Bán trang bị");

    private final String name;

    UserAction(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
