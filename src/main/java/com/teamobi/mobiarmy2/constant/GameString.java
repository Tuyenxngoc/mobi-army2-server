package com.teamobi.mobiarmy2.constant;

import com.teamobi.mobiarmy2.util.Utils;

/**
 * @author tuyen
 */
public class GameString {
    public static final String MAINTENANCE_MODE = "Server đang bảo trì. Vui lòng quay lại sau khi hoàn tất";
    public static final String RANKING_NOT_LOADED = "Chưa tải xong bảng xếp hạng";
    public static final String NO_RANKING = "Chưa có hạng";

    public static final String INVITE_OFFLINE = "Bạn đó đã offline rồi!";
    public static final String INVITE_ALREADY_IN_GAME = "Bạn đó đã vào bàn khác chơi rồi!";
    public static final String INVITE_DISABLED = "Bạn đó đã tắt chức năng mời bạn!";

    public static final String MISSION_NOT_FOUND = "Không tồn tại nhiệm vụ này!";
    public static final String MISSION_NOT_COMPLETED = "Nhiệm vụ chưa hoàn thành!";
    public static final String MISSION_COMPLETED = "Nhiệm vụ đã hoàn thành!";

    public static final String FRIEND_ADD_INVALID_NAME = "Tên bạn bè cần thêm có chứa kí tự đặc biệt!";
    public static final String FRIEND_ADD_MISSING_NAME = "Vui lòng nhập tên bạn bè cần thêm!";
    public static final String INVALID_ACCOUNT_PASSWORD = "Tài khoản hoặc mật khẩu không cho phép kí tự đặc biệt!";
    public static final String REGISTRATION_REQUIRED = "Vui lòng truy cập trang chủ để đăng ký";
    public static final String PASSWORD_INVALID_CHARACTER = "Mật khẩu không được chứa kí tự đặc biệt!";
    public static final String PASSWORD_INCORRECT_OLD = "Mật khẩu cũ không chính xác!";
    public static final String PASSWORD_CHANGE_SUCCESS = "Đổi mật khẩu thành công!";
    public static final String ACCOUNT_INACTIVE = "Tài khoản của bạn chưa được kích hoạt";
    public static final String LOGIN_FAILED = "Thông tin tài khoản hoặc mật khẩu không chính xác";
    public static final String ACCOUNT_OTHER_LOGIN = "Có người khác đăng nhập vào tài khoản của bạn!";
    public static final String LOGIN_ANOTHER_DEVICE = "Bạn đang đăng nhập ở máy khác. Hãy thử đăng nhập lại";
    public static final String ACCOUNT_LOCKED = "Tài khoản này đang bị khóa. Liên hệ tổng đài hỗ trợ khách hàng để biết thêm thông tin";

    public static final String MATERIAL_PURCHASE_LIMIT = "Bạn đã mua hết số lượng cho phép";
    public static final String CHEST_LOCKED_NO_SELL = "Rương đồ của bạn đang khóa không thể bán vật phẩm";
    public static final String ITEM_CRAFT_FAILURE = "Chế đồ thất bại";
    public static final String ITEM_CRAFT_SUCCESS = "Chế đồ thành công";
    public static final String COMBINE_ERROR = "Không thể kết hợp";
    public static final String COMBINE_FAILURE = "Chúc bạn may mắn lần sau";
    public static final String GEM_COMBINE_REQUEST = "Bạn có muốn gắn ngọc vào trang bị?";
    public static final String GEM_COMBINE_SUCCESS = "Chúc mừng bạn đã kết hợp thành công";
    public static final String GEM_COMBINE_NO_SLOT = "Trang bị đã chọn không còn đủ chỗ";
    public static final String INSUFFICIENT_FUNDS = "Bạn không có đủ tiền!";
    public static final String PURCHASE_SUCCESS = "Giao dịch thành công. Xin cảm ơn";
    public static final String CHEST_NO_SPACE = "Không đủ chỗ trong rương!";
    public static final String CHEST_MAXIMUM_REACHED = "Số lượng đã quá mức tối đa!";
    public static final String EQUIP_SELL_ERROR_IN_USE = "Không thể bán trang bị đang sử dụng!";
    public static final String EQUIP_SELL_ERROR_REMOVE_GEMS = "Vui lòng tháo hết ngọc trước khi bán trang bị";
    public static final String GEM_REMOVAL_SUCCESS = "Tháo lấy ngọc thành công";
    public static final String EXTEND_SUCCESS = "Gia hạn thành công!";

    public static final String ITEM_X2_XP_USAGE_REQUEST = "Bạn có muốn sử dụng item này không? Hiệu lực 1 ngày!";
    public static final String ITEM_X2_XP_USAGE_SUCCESS = "Sử dụng thành công. Bạn có 1 ngày x2 kinh nghiệm!";

    public static final String USE_BANH_TRUNG_REQUEST = "Bạn có muốn dùng bánh trưng không?";
    public static final String USE_BANH_TRUNG_SUCCESS = "Dùng bánh trưng thành công";
    public static final String USE_BANH_TET_REQUEST = "Bạn có muốn dùng bánh tét không?";
    public static final String USE_BANH_TET_SUCCESS = "Dùng bánh tét thành công";
    public static final String EXCHANGE_BANH_TET_TO_SILVER_EQUIP_1 = "Bạn có muốn đổi 50 bánh tét để lấy một bộ trang bị bạc cấp 1 không?";
    public static final String EXCHANGE_BANH_TET_TO_SILVER_EQUIP_2 = "Bạn có muốn đổi 100 bánh tét để lấy một bộ trang bị bạc cấp 2 không?";
    public static final String EXCHANGE_BANH_TET_TO_SILVER_EQUIP_3 = "Bạn có muốn đổi 150 bánh tét để lấy một bộ trang bị bạc cấp 3 không?";
    public static final String EXCHANGE_BANH_TRUNG_TO_GOLD_EQUIP_1 = "Bạn có muốn đổi 50 bánh trưng để lấy một bộ trang bị vàng cấp 1 không?";
    public static final String EXCHANGE_BANH_TRUNG_TO_GOLD_EQUIP_2 = "Bạn có muốn đổi 100 bánh trưng để lấy một bộ trang bị vàng cấp 2 không?";
    public static final String EXCHANGE_BANH_TRUNG_TO_GOLD_EQUIP_3 = "Bạn có muốn đổi 150 bánh trưng để lấy một bộ trang bị vàng cấp 3 không?";

    public static final String AREA_NOT_FOUND = "Không tìm thấy khu vực yêu cầu!";
    public static final String AREA_JOIN_IN_PROGRESS = "Không thể vào do còn đang chơi";
    public static final String AREA_INCORRECT_PASSWORD = "Mật khẩu không chính xác!";
    public static final String AREA_INSUFFICIENT_FUNDS = "Không đủ tiền cược!";
    public static final String AREA_FULL = "Khu vực đã đầy!";
    public static final String KICKED_BY_HOST = "Bạn bị đuổi bởi chủ phòng!";
    public static final String TEAM_MUST_BE_SAME_FACTION = "Đội phải cùng phe!";
    public static final String TEAM_NOT_READY = "Mọi người chưa sẵn sàng!";
    public static final String MAP_SELECTION_ERROR = "Không thể chọn map";
    public static final String MATCH_NOT_COUNTED = "Ván chơi không tính vì thời gian quá ngắn hoặc bạn có hành vi tiêu cực!";
    public static final String TEAM_SIZE_MISMATCH = "Số lượng 2 bên chưa ngang nhau";
    public static final String ESCAPED_GAME = "Đã bỏ chạy";
    public static final String ITEM_UNAUTHORIZED = "Bạn không thể dùng item này";
    public static final String PLAYER_ELIMINATED = "Bạn đã bị loại khỏi vòng chơi";

    public static final String NO_CLAN_MEMBERSHIP = "Bạn chưa gia nhập biệt đội nào!";
    public static final String CLAN_NOT_FOUND = "Biệt đội không tồn tại!";
    public static final String CLAN_LEVEL_INSUFFICIENT = "Biệt đội bạn chưa đủ cấp độ!";
    public static final String CLAN_NOT_AUTHORIZED = "Chỉ đội trưởng mới có thể mua!";
    public static final String CLAN_NOT_ENOUGH_XU = "Biệt đội bạn không có đủ xu!";
    public static final String CLAN_NOT_ENOUGH_LUONG = "Biệt đội bạn không có đủ lượng!";
    public static final String CONTRIBUTION_SUCCESS = "Góp thành công";

    public static final String GIFT_CODE_INVALID = "Mã quà tặng không tồn tại hoặc đã sử dụng!";
    public static final String GIFT_CODE_LIMIT_REACHED = "Số lần sử dụng đã hết!";
    public static final String GIFT_CODE_ALREADY_USED = "Tài khoản đã sử dụng mã quà tặng này";
    public static final String GIFT_CODE_SUCCESS = "Sử dụng thành công";
    public static final String SPIN_WAIT_TIME = "Quay từ từ thôi bạn ơi!";

    public static String createInviteMessage(String username) {
        return String.format("%s mời bạn chơi?", username);
    }

    public static String createMissionCompleteMessage(String reward) {
        return String.format("Chúc mừng bạn nhận được phần thưởng %s!", reward);
    }

    public static String createMessageFromSender(String sender, String content) {
        return String.format("Tin nhắn từ %s: %s.", sender, content);
    }

    public static String createDailyRewardMessage(byte quantity, String name) {
        return String.format("Hôm nay bạn được tặng %dx item %s. Chúc bạn chơi game vui vẻ", quantity, name);
    }

    public static String createDailyTopRewardMessage(int quantity) {
        return String.format("Chúc mừng bạn nhận được phần thưởng top là %s xu", Utils.getStringNumber(quantity));
    }

    public static String createGemUpgradeSuccessMessage(int quantity, String name) {
        return String.format("Chúc mừng, bạn đã kết hợp thành công %d viên %s", quantity, name);
    }

    public static String createGemFusionRequestMessage(int pt) {
        return String.format("Bạn có muốn kết hợp 5 viên ngọc này thành 1 viên ngọc cấp cao hơn? Tỉ lệ thành công là %d%%", pt);
    }

    public static String createGemSellRequestMessage(int quantity, int total) {
        return String.format("Bạn có muốn bán %d vật phẩm này với giá %d xu không?", quantity, total);
    }

    public static String createGemRemovalRequestMessage(int xu) {
        return String.format("Bạn có muốn tháo hết ngọc trong trang bị này ra? Chi phí là %d xu", xu);
    }

    public static String createEquipmentSellRequestMessage(int quantity, int xu) {
        return String.format("Bạn có muốn bán %d trang bị này với giá %d xu không?", quantity, xu);
    }

    public static String createEquipmentRenewalRequestMessage(int xu) {
        return String.format("Bạn có muốn gia hạn trang bị này với giá %d xu?", xu);
    }

    public static String createJoinAreaErrorMessage(int seconds) {
        return String.format("Còn %d giây nữa mới được vào khu vực", seconds);
    }

    public static String createBettingRangeErrorMessage(int min, int max) {
        return String.format("Chỉ có thể đặt cược từ %d xu đến %d xu!", min, max);
    }

    public static String createGameStartErrorMessageUserNotReady(String username) {
        return String.format("Còn %s chưa sẵn sàng!", username);
    }

    public static String createGameStartErrorMessageInsufficientFunds(String username) {
        return String.format("%s không đủ tiền đặt cược!", username);
    }

    public static String createGameStartErrorMessageInvalidSlot(String username, int slot) {
        return String.format("%s lỗi item slot %d xin chọn lại!", username, slot);
    }

    public static String createMapSelectionErrorMessage(String srt) {
        return String.format("Chỉ có thể chọn map %s", srt);
    }

    public static String createMapSelectionErrorMessageMultipleChoices() {
        return "Chỉ có thể chọn map %s hoặc map %s!";
    }

    public static String createWaitClickMessage(long second) {
        return String.format("Vui lòng chờ sau %s giây", second);
    }

    public static String createOpeningGiftMessage(String name) {
        return String.format("%s vẫn còn đang mở quà", name);
    }

    public static String createGiftOpeningSummaryMessage(int freeOpened, int totalFreeGifts, int costPerExtraGift) {
        return String.format("Bạn được mở %d trong %d hộp quà miễn phí. Trả thêm %s xu để mở thêm 1 hộp.", freeOpened, totalFreeGifts, Utils.formatThousands(costPerExtraGift));
    }

    public static String createClanContributionMinXuMessage(int xu) {
        return String.format("Mức góp ít nhất là %s xu", Utils.getStringNumber(xu));
    }

    public static String createTopBonusMessage(String username, String bonus) {
        return String.format("%s +%s xu mỗi ngày", username, bonus);
    }

    public static String createGiftCodeExpiryMessage(String expiryDate) {
        return "Mã quà tặng đã hết hạn lúc " + expiryDate;
    }

    public static String createGiftCodeRewardMessage(String code, String reward) {
        return String.format("Sử dụng mã quà tặng %s thành công, nhận được %s", code, reward);
    }

    public static String createGiftCodeRewardMessageWithQuantity(String code, int quantity, String reward) {
        return String.format("Sử dụng mã quà tặng %s thành công, nhận được %s %s", code, Utils.getStringNumber(quantity), reward);
    }

    public static String createMaterialPurchaseLimitMessage(int quantity) {
        return String.format("Bạn chỉ có thể mua thêm %d nguyên liệu", quantity);
    }

    public static String createLoginCooldownMessage(long seconds) {
        return String.format("Vui lòng đăng nhập lại sau %d giây", seconds);
    }
}
