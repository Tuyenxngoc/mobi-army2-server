package com.teamobi.mobiarmy2.constant;

import com.teamobi.mobiarmy2.util.Utils;

import java.time.LocalDateTime;

/**
 * @author tuyen
 */
public class GameString {

    public static String getNotFinishedLoadingRanking() {
        return "Chưa tải xong bảng xếp hạng";
    }

    public static String leave1() {
        return "Đã bỏ chạy -%dxp";
    }

    public static String leave2() {
        return "Đã bỏ chạy";
    }

    public static String inviteError1() {
        return "Bạn đó đã offline rồi!";
    }

    public static String inviteError2() {
        return "Bạn đó đã vào bàn khác chơi rồi!";
    }

    public static String inviteMessage(String username) {
        return String.format("%s mời bạn chơi?", username);
    }

    public static String missionError1() {
        return "Không tồn tại nhiệm vụ này!";
    }

    public static String missionError2() {
        return "Nhiệm vụ chưa hoàn thành!";
    }

    public static String missionError3() {
        return "Nhiệm vụ đã hoàn thành!";
    }

    public static String missionComplete(String reward) {
        return String.format("Chúc mừng bạn nhận được phần thưởng %s!", reward);
    }

    public static String phucHoiSuccess() {
        return "Phục hồi điểm nâng cấp thành công!";
    }

    public static String exchangeGift() {
        return "Bạn muốn sử dụng vật phẩm sự kiện!";
    }

    public static String x2XPSuccess() {
        return "Sử dụng thành công. Bạn có 1 ngày x2 kinh nghiệm!";
    }

    public static String x6XPSuccess() {
        return "Sử dụng thành công. Bạn có 1 ngày x6 kinh nghiệm!";
    }

    public static String x0XPSuccess() {
        return "Sử dụng thành công. Bạn sẽ không nhận mọi kinh nghiệm trong 1 ngày!";
    }

    public static String x0XPHuy() {
        return "Sử dụng thành công. Bạn đã hủy không nhận kinh nghiệm!";
    }

    public static String x2XPRequest() {
        return "Bạn có muốn sử dụng item này không? Hiệu lực 1 ngày!";
    }

    public static String x6XPRequest() {
        return "Bạn có muốn sử dụng item này không? Hiệu lực 1 ngày!";
    }

    public static String x0XPRequest() {
        return "Bạn có muốn không nhận kinh nghiệm không? Hiệu lực 1 ngày!";
    }

    public static String phucHoiDiemString() {
        return "Bạn có muốn sử dụng item này không? Sẽ xóa hết điểm đã nâng!";
    }

    public static String mssTGString(String sender, String content) {
        return String.format("Tin nhắn từ %s: %s.", sender, content);
    }

    public static String dailyReward(byte quantity, String name) {
        return String.format("Hôm nay bạn được tặng %dx item %s. Chúc bạn chơi game vui vẻ", quantity, name);
    }

    public static String dailyTopReward(int quantity) {
        return String.format("Chúc mừng bạn nhận được phần thưởng top là %s xu", Utils.getStringNumber(quantity));
    }

    public static String loginErr1() {
        return "Bạn đang đăng nhập ở máy khác. Hãy thử đăng nhập lại";
    }

    public static String userLoginMany() {
        return "Có người khác đăng nhập vào tài khoản của bạn!";
    }

    public static String loginPassFail() {
        return "Thông tin tài khoản hoặc mật khẩu không chính xác";
    }

    public static String hopNgocFail() {
        return "Chế ngọc thất bại, hao phí %d %s!";
    }

    public static String hopNgocSucess() {
        return "Chế ngọc thành công hao phí %d %s thu được %d %s!";
    }

    public static String cheDoSuccess() {
        return "Chế đồ thành công";
    }

    public static String cheDoFail() {
        return "Chế đồ thất bại";
    }

    public static String hopNgocError() {
        return "Lỗi kết hợp";
    }

    public static String hopNgocCantDo() {
        return "Không thể kết hợp";
    }

    public static String hopNgocRequest() {
        return "Bạn có muốn gắn ngọc vào trang bị?";
    }

    public static String hopNgocNC() {
        return "Bạn có muốn hợp thành ngọc cấp cao hơn? Tỉ lệ thành công là %s";
    }

    public static String hopNgocSell() {
        return "Bạn có muốn bán %d vật phẩm với giá %d xu?";
    }

    public static String hopNgocNoSlot() {
        return "Trang bị đã chọn không còn đủ chỗ";
    }

    public static String xuNotEnought() {
        return "Bạn không có đủ tiền!";
    }

    public static String buySuccess() {
        return "Giao dịch thành công. Xin cảm ơn.";
    }

    public static String addFrienvError1() {
        return "Tên bạn bè cần thêm có chứa kí tự đặc biệt!";
    }

    public static String addFrienvError2() {
        return "Vui lòng nhập tên bạn bè cần thêm!";
    }

    public static String changPassError1() {
        return "Mật khẩu không được chứa kí tự đặc biệt!";
    }

    public static String changPassError2() {
        return "Mật khẩu cũ không chính xác!";
    }

    public static String changPassSuccess() {
        return "Đổi mật khẩu thành công!";
    }

    public static String ruongNoSlot() {
        return "Không đủ chỗ trong rương!";
    }

    public static String ruongMaxSlot() {
        return "Số lượng đã quá mức tối đa!";
    }

    public static String thaoNgocRequest(int xu) {
        return String.format("Bạn có muốn tháo hết ngọc trong trang bị này ra? Chi phí là %d xu", xu);
    }

    public static String sellTBRequest(int quantity, int xu) {
        return String.format("Bạn có muốn bán %d trang bị này với giá %d xu không?", quantity, xu);
    }

    public static String sellTBError1() {
        return "Không thể bán trang bị đang sử dụng!";
    }

    public static String sellTBError2() {
        return "Vui lòng tháo hết ngọc trước khi bán trang bị.";
    }

    public static String thaoNgocError1() {
        return "Không thể tháo ngọc trang bị đang sử dụng!";
    }

    public static String thaoNgocError2() {
        return "Bạn không có đủ tiền. Bạn cần %d xu để thực hiện thao tác này!";
    }

    public static String thaoNgocSuccess() {
        return "Tháo lấy ngọc thành công.";
    }

    public static String giaHanRequest(int xu) {
        return String.format("Bạn có muốn gia hạn trang bị này với giá %d xu?", xu);
    }

    public static String giaHanSucess() {
        return "Gia hạn thành công!";
    }

    public static String findKVError1() {
        return "Không tìm được khu vực!";
    }

    public static String joinKVError0() {
        return "Khu vực đã bắt đầu!";
    }

    public static String joinKVError1() {
        return "Mật khẩu không chính xác!";
    }

    public static String joinKVError2() {
        return "Không đủ tiền cược!";
    }

    public static String joinKVError3() {
        return "Khu vực đã đầy!";
    }

    public static String kickString() {
        return "Bạn bị đuổi bởi chủ phòng!";
    }

    public static String datCuocError1(int min, int max) {
        return String.format("Chỉ có thể đặt cược từ %d xu đến %d xu!", min, max);
    }

    public static String startGameError1() {
        return "Mọi người chưa sẵn sàng!";
    }

    public static String startGameError2() {
        return "Còn %s chưa sẵn sàng!";
    }

    public static String startGameError3() {
        return "%s lỗi tiền đặt cược!";
    }

    public static String startGameError4() {
        return "%s lỗi item slot %d xin chọn lại!";
    }

    public static String startGameError5() {
        return "Số lượng 2 bên chưa ngang nhau";
    }

    public static String selectMapError1_1() {
        return "Chỉ có thể chọn map %s!";
    }

    public static String selectMapError1_2() {
        return "Chỉ có thể chọn map %s hoặc map %s!";
    }

    public static String selectMapError1_3() {
        return "Lỗi chọn map!";
    }

    public static String Wait_click() {
        return "Vui lòng chờ sau: %s";
    }

    public static String reg_Error1() {
        return "Tài khoản hoặc mật khẩu không cho phép kí tự đặc biệt!";
    }

    public static String reg_Error2() {
        return "Tài khoản phải có độ dài từ 5 - 16 kí tự!";
    }

    public static String reg_Error3() {
        return "Mật khẩu phải có độ dài từ 1 - 40 kí tự!";
    }

    public static String reg_Error4() {
        return "Tài khoản đã tồn tại. Vui lòng chọn một tài khoản khác!";
    }

    public static String reg_Error5() {
        return "Đăng kí tài khoản thành công!";
    }

    public static String reg_Error6() {
        return "Vui lòng truy cập trang chủ để đăng ký";
    }

    public static String notCompletedMatch() {
        return "Ván chơi không tính vì thời gian quá ngắn hoặc bạn có hành vi tiêu cực!";
    }

    public static String openingGift(String name) {
        return String.format("%s vẫn còn đang mở quà", name);
    }

    public static String notClan() {
        return "Bạn không có biệt đội!";
    }

    public static String clanNull() {
        return "Biệt đội không tồn tại!";
    }

    public static String clanLevelNotEnought() {
        return "Biệt đội bạn chưa đủ cấp độ!";
    }

    public static String clanrightNotEnought() {
        return "Chỉ đội trưởng mới có thể mua!";
    }

    public static String clanXuNotEnought() {
        return "Biệt đội bạn không có đủ xu!";
    }

    public static String clanLuongNotEnought() {
        return "Biệt đội bạn không có đủ lượng!";
    }

    public static String unauthorized_Item() {
        return "Bạn không thể sử dụng item này";
    }

    public static String giftFightWin() {
        return "Chúc mừng bạn đành chiến thắng phần quà của bạn là %s";
    }

    public static String LHFinish() {
        return "Hoàn thành trận đấu phần quà của bạn là %s";
    }

    public static String LHSuccess() {
        return "Vượt thành công trận đấu %d phần quà của bạn là %s";
    }

    public static String LHfailde() {
        return "Trận đấu thất bại hãy tiếp tục cố gắng";
    }

    public static String dapDoRequest_1() {
        return "Bạn có muốn nâng cấp trang bị lên +%d (%d";
    }

    public static String dapDoRequest_2() {
        return "Bạn đang không dùng bảo hiểm, có muốn nâng cấp trang bị lên +%d (%d";
    }

    public static String dapDoSuccess() {
        return "Chúc mừng bạn đã đập đồ lên cấp %s (+ %s %s)!";
    }

    public static String dapDoMax() {
        return "Chúc mừng bạn đã đập lên cấp tối đa %d, +%d%s và %d%s tất cả chỉ số!";
    }

    public static String dapDoDatMuc() {
        return "Chúc mừng bạn đã đập đồ lên cấp %d, %d%s và +%d tất cả chỉ số!";
    }

    public static String dapDoFail() {
        return "Đập đồ thất bại!";
    }

    public static String loginLock() {
        return "Tài khoản này đang bị khóa. Liên hệ tổng đài hỗ trợ khách hàng để biết thêm thông tin.";
    }

    public static String loginActive() {
        return "Tài khoản của bạn chưa được kích hoạt";
    }

    public static String gopClanMinXu(int xu) {
        return String.format("Mức góp ít nhất là %s xu", Utils.getStringNumber(xu));
    }

    public static String gopClanThanhCong() {
        return "Góp thành công";
    }

    public static String notRanking() {
        return "Chưa có hạng";
    }

    public static String topBonus(String username, String bonus) {
        return String.format("%s +%s xu mỗi ngày", username, bonus);
    }

    public static String giftCodeError1() {
        return "Mã quà tặng không tồn tại hoặc đã sử dụng!";
    }

    public static String giftCodeError2() {
        return "Số lần sử dụng đã hết!";
    }

    public static String giftCodeError3(LocalDateTime expiryDate) {
        return "Mã quà tặng đã hết hạn lúc " + expiryDate.toString();
    }

    public static String giftCodeError4() {
        return "Tài khoản đã sử dụng mã quà tặng này";
    }

    public static String giftCodeSuccess() {
        return "Sử dụng thành công";
    }

    public static String giftCodeReward(String code, String reward) {
        return String.format("Sử dụng mã quà tặng %s thành công, nhận được %s", code, reward);
    }

    public static String giftCodeReward(String code, int quantity, String reward) {
        return String.format("Sử dụng mã quà tặng %s thành công, nhận được %s %s", code, Utils.getStringNumber(quantity), reward);
    }

    public static String materialLimit() {
        return "Bạn đã mua hết số lượng cho phép.";
    }

    public static String materialLimit1(int quantity) {
        return String.format("Bạn chỉ có thể mua thêm %d nguyên liệu.", quantity);
    }
}
