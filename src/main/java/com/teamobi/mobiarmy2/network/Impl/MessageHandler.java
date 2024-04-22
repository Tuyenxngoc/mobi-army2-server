package com.teamobi.mobiarmy2.network.Impl;

import com.teamobi.mobiarmy2.constant.Cmd;
import com.teamobi.mobiarmy2.network.IMessageHandler;
import com.teamobi.mobiarmy2.server.ServerManager;
import com.teamobi.mobiarmy2.service.IUserService;

/**
 * @author tuyen
 */
public class MessageHandler implements IMessageHandler {

    private final IUserService userService;

    public MessageHandler(IUserService userService) {
        this.userService = userService;
    }

    @Override
    public void onMessage(Message ms) {
        try {
            switch (ms.getCommand()) {
                case Cmd.GET_KEY -> userService.handleHandshakeMessage();

                case Cmd.GET_MORE_DAY -> userService.giaHanDo(ms);

                case Cmd.MISSISON -> userService.nhiemVuView(ms);

                case Cmd.CLAN_MONEY -> userService.gopClan(ms);

                case Cmd.FOMULA -> userService.hopTrangBi(ms);

                case Cmd.GET_LUCKYGIFT -> userService.moHopQua(ms);

                case Cmd.BANGTHANHTICH -> userService.bangXepHang(ms);

                case Cmd.SHOP_BIETDOI -> userService.clanShop(ms);

                case Cmd.TRAINING_MAP -> userService.luyenTap(ms);

                case Cmd.SIGN_OUT -> userService.dangXuat(ms);

                case Cmd.SHOP_LINHTINH -> userService.doDacBietShop(ms);

                case Cmd.VIP_EQUIP -> userService.macTrangBiVip(ms);

                case Cmd.LOGIN -> userService.handleLogin(ms);

                case Cmd.CHAT_TO -> userService.guiTinNhan(ms);

                case Cmd.ROOM_LIST -> userService.denKhuVuc(ms);

                case Cmd.BOARD_LIST -> userService.vaoPhong(ms);

                case Cmd.JOIN_BOARD -> userService.thamGiaKhuVuc(ms);

                case Cmd.CHAT_TO_BOARD -> userService.nhanTinn(ms);

                case Cmd.KICK -> userService.duoiNguoiCHoi(ms);

                case Cmd.LEAVE_BOARD -> userService.roiKhuVuc(ms);

                case Cmd.READY -> userService.SanSang(ms);

                case Cmd.IMBUE -> userService.hopNgoc(ms);

                case Cmd.SET_PASS -> userService.datMatKhau(ms);

                case Cmd.SET_MONEY -> userService.datCuoc(ms);

                case Cmd.START_ARMY -> userService.batDau(ms);

                case Cmd.MOVE_ARMY -> userService.diChuyen(ms);

                case Cmd.FIRE_ARMY -> userService.Bann(ms);

                case Cmd.SHOOT_RESULT -> userService.ketQUaBan(ms);

                case Cmd.USE_ITEM -> userService.dungItem(ms);

                case Cmd.JOIN_ANY_BOARD -> userService.choiNgay(ms);

                case Cmd.FRIENDLIST -> userService.xembanBe(ms);

                case Cmd.ADD_FRIEND_RESULT -> userService.ketBan(ms);

                case Cmd.DELETE_FRIEND_RESULT -> userService.xoaBan(ms);

                case Cmd.PLAYER_DETAIL -> userService.xemThongTIn(ms);

                case Cmd.SEARCH -> userService.timNguoiChoi(ms);

                case Cmd.SKIP -> userService.boLuot(ms);

                case Cmd.UPDATE_XY -> userService.capNhatXY(ms);

                case Cmd.SET_BOARD_NAME -> userService.datTenKhuVUc(ms);

                case Cmd.SET_MAX_PLAYER -> userService.datSoNguoi(ms);

                case Cmd.SET_PROVIDER -> userService.getProvider(ms);

                case Cmd.CHOOSE_ITEM -> userService.mangItem(ms);

                case Cmd.CHOOSE_GUN -> userService.chonNhanVat(ms);

                case Cmd.CHANGE_TEAM -> userService.doiPhe(ms);

                case Cmd.BUY_ITEM -> userService.muaItem(ms);

                case Cmd.BUY_GUN -> userService.muaNhanVat(ms);

                case Cmd.MAP_SELECT -> userService.chonBanDo(ms);

                case Cmd.LOAD_CARD -> userService.napTheCao(ms);

                case Cmd.FIND_PLAYER -> userService.timBanChoi(ms);

                case Cmd.CHECK_CROSS -> userService.xoaDan(ms);

                case Cmd.CHANGE_PASS -> userService.doiMatKhau(ms);

                case Cmd.TRAINING -> userService.startLuyenTap(ms);

                case Cmd.GET_FILEPACK -> userService.getFilePack(ms);

                case Cmd.ADD_POINT -> userService.nangCap(ms);

                case Cmd.CHARACTOR_INFO -> userService.sendCharacterInfo();

                case Cmd.CHANGE_EQUIP -> userService.macTrangBi(ms);

                case Cmd.SHOP_EQUIP -> userService.shopTrangBi(ms);

                case Cmd.BUY_EQUIP -> userService.muaTrangBi(ms);

                case Cmd.RULET -> userService.quaySo(ms);

                case Cmd.VERSION_CODE -> userService.getVersionCode(ms);

                case Cmd.CLAN_ICON -> userService.clanIcon(ms);

                case Cmd.TOP_CLAN -> userService.getTopClan(ms);

                case Cmd.CLAN_INFO -> userService.getInfoClan(ms);

                case Cmd.CLAN_MEMBER -> userService.getClanMember(ms);

                case Cmd.GET_BIG_IMAGE -> userService.getBigImage(ms);

                case Cmd.REGISTER_2 -> userService.register(ms);

                case Cmd.CHARGE_MONEY_2 -> userService.napTien(ms);

                case Cmd.MATERIAL_ICON -> userService.getMaterialIconMessage(ms);

                default -> ServerManager.getInstance().logger().logWarning("Command " + ms.getCommand() + " is not supported");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
