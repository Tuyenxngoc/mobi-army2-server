package com.teamobi.mobiarmy2.constant;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * @author tuyen
 */
public class Cmd {
    public static final byte GET_BIG_EQUIP_HD = -120;
    public static final byte DELETE_ACC = -106;
    public static final byte MINI_MAP_REQUEST = -105;
    public static final byte BG_REQUEST = -104;
    public static final byte CHANGE_REQUEST = -103;
    public static final byte IN_APP_PURCHASE = -102;
    public static final byte MORE_GAME = -100;
    public static final byte CHARGE_MONEY_3 = -94;
    public static final byte CHECK_MAKE_HOLE = -92;
    public static final byte EMPTY_ROOM = -28;
    public static final byte NEW_ROOMLIST = -28;
    public static final byte GET_AGENT_PROVIDER = -26;
    public static final byte GET_MORE_DAY = -25;
    public static final byte CUP = -24;
    public static final byte MISSISON = -23;
    public static final byte CLAN_MONEY = -21;
    public static final byte CHANGE_ROOM_NAME = -19;
    public static final byte FOMULA = -18;
    public static final byte GET_LUCKYGIFT = -17;
    public static final byte DATA_IMAGE = -15;
    public static final byte BANGTHANHTICH = -14;
    public static final byte ITEM_NUM = -10;
    public static final byte CURR_EQUIP_DBKEY = -7;
    public static final byte DISCONECT = -4;
    public static final byte VIP_EQUIP = -2;
    public static final byte OPEN_UI_CONFIRM = -107;
    public static final byte REG3 = -93;
    public static final byte REG_NICK_FREE = -101;
    public static final byte REQUEST_PLAYER_DATA_ID = -108;
    public static final byte SHOP_BIETDOI = -12;
    public static final byte SHOP_LINHTINH = -3;
    public static final byte SIGN_OUT = -4;
    public static final byte TEST_2 = -5;
    public static final byte TRAINING_MAP = -6;
    public static final byte UPDATE_CLANMONEY = -22;
    public static final byte GET_KEY = -27;
    public static final byte CHAT_TO = 5;
    public static final byte ADD_FRIEND = 32;
    public static final byte BUY_AVATAR = 43;
    public static final byte ADMIN_COMMAND = 47;
    public static final byte CHANGE_TEAM = 71;
    public static final byte BUY_ITEM = 72;
    public static final byte CHANGE_MODE = 73;
    public static final byte BUY_GUN = 74;
    public static final byte CHANGE_PASS = 81;
    public static final byte CHAT_TO_BOARD = 9;
    public static final byte CHECK_CROSS = 79;
    public static final byte CHECK_FALL = 80;
    public static final byte CHOOSE_GUN = 69;
    public static final byte CHOOSE_ITEM = 68;
    public static final byte CHOOSE_MAP = 70;
    public static final byte DELETE_FRIEND = 33;
    public static final byte DIE = 60;
    public static final byte FIND_PLAYER = 78;
    public static final byte FIRE_ARMY = 22;
    public static final byte ADD_POINT = 98;
    public static final byte GETSTRING = Byte.MAX_VALUE;
    public static final byte GET_ITEM_SLOT = 94;
    public static final byte JOIN_ANY_BOARD = 28;
    public static final byte JOIN_BOARD = 8;
    public static final byte KICK = 11;
    public static final byte LEAVE_BOARD = 15;
    public static final byte LOAD_CARD = 77;
    public static final byte LOGIN = 1;
    public static final byte LOGOUT = 2;
    public static final byte MAP_SELECT = 75;
    public static final byte MOVE_ARMY = 21;
    public static final byte NEXT_TURN = 63;
    public static final byte PING = 42;
    public static final byte READY = 16;
    public static final byte REQUEST_AVATAR = 38;
    public static final byte REQUEST_AVATARLIST = 39;
    public static final byte REQUEST_DETAIL = 34;
    public static final byte REQUEST_FRIENDLIST = 29;
    public static final byte REQUEST_REGISTER = 35;
    public static final byte REQUEST_RICHEST = 31;
    public static final byte REQUEST_ROOMLIST = 6;
    public static final byte REQUEST_SERVICE = 85;
    public static final byte REQUEST_STRONGEST = 30;
    public static final byte RULET = 110;
    public static final byte SEARCH = 36;
    public static final byte SET_BOARD_NAME = 54;
    public static final byte SET_MAX_PLAYER = 56;
    public static final byte SET_MONEY = 19;
    public static final byte SET_PASS = 18;
    public static final byte SET_PROVIDER = 58;
    public static final byte SHOOT_RESULT = 23;
    public static final byte SKIP = 49;
    public static final byte START_ARMY = 20;
    public static final byte TRAINING = 83;
    public static final byte TRAININGSHOOT = 84;
    public static final byte UPDATE_USERDATA = 41;
    public static final byte UPDATE_XY = 53;
    public static final byte USER_DATA = 40;
    public static final byte USE_ITEM = 26;
    public static final byte VERSION = 48;
    public static final byte WIND = 25;
    public static final byte ZING_CONNECT = 87;
    public static final byte ADD_FRIEND_RESULT = 32;
    public static final byte ADMIN_COMMAND_RESPONSE = 47;
    public static final byte ANGRY = 113;
    public static final byte ANTI_HACK_MESS = 64;
    public static final byte AUTO_BOARD = 76;
    public static final byte BIT = 96;
    public static final byte BOARD_LIST = 7;
    public static final byte BONUS_MONEY = 52;
    public static final byte BUY_AVATAR_SUCCESS = 43;
    public static final byte BUY_EQUIP = 104;
    public static final byte CAPTURE = 95;
    public static final byte CHANGE_EQUIP = 102;
    public static final byte CHARACTOR_INFO = 99;
    public static final byte CHARGE_MONEY_2 = 122;
    public static final byte CHAT_FROM_BOARD = 9;
    public static final byte CHAT_TEAM = 123;
    public static final byte CLAN_ICON = 115;
    public static final byte CLAN_INFO = 117;
    public static final byte CLAN_MEMBER = 118;
    public static final byte DELETE_FRIEND_RESULT = 33;
    public static final byte DRAW = 25;
    public static final byte END_INVISIBLE = 80;
    public static final byte EYE_SMOKE = 106;
    public static final byte FINISH = 51;
    public static final byte FIRE_TRAINING = 84;
    public static final byte FLY = 93;
    public static final byte FREEZE = 107;
    public static final byte FRIENDLIST = 29;
    public static final byte GAME_RESULT = 37;
    public static final byte GET_BIG_IMAGE = 120;
    public static final byte GET_BOSS = 89;
    public static final byte GET_FILEPACK = 90;
    public static final byte GHOST_BIT = 124;
    public static final byte GIFT = 119;
    public static final byte IMBUE = 17;
    public static final byte INVENTORY = 101;
    public static final byte INVENTORY_UPDATE = 27;
    public static final byte ITEM_SLOT = 112;
    public static final byte LOGIN_FAIL = 4;
    public static final byte LOGIN_SUCESS = 3;
    public static final byte LUCKY = 100;
    public static final byte MATERIAL = 125;
    public static final byte MATERIAL_ICON = 126;
    public static final byte NEXT_TURN_2 = 24;
    public static final byte NHAN_SMS_DIALOG = 63;
    public static final byte OPEN_LINK = 86;
    public static final byte ORBIT = 82;
    public static final byte PLAYER_DETAIL = 34;
    public static final byte POISON = 108;
    public static final byte RANDOM_ITEM = 59;
    public static final byte REGISTER_2 = 121;
    public static final byte RICHEST_LIST = 31;
    public static final byte ROOM_CAPTION = 88;
    public static final byte ROOM_LIST = 6;
    public static final byte SERVER_INFO = 46;
    public static final byte SERVER_MESSAGE = 45;
    public static final byte SET_MONEY_ERROR = 10;
    public static final byte SHOP_EQUIP = 103;
    public static final byte SKIP_2 = 64;
    public static final byte SOMEONE_JOINBOARD = 12;
    public static final byte SOMEONE_LEAVEBOARD = 14;
    public static final byte SOMEONE_READY = 16;
    public static final byte STOP_GAME = 50;
    public static final byte SUB_FILEPACK_1 = 0;
    public static final byte SUB_FILEPACK_2 = 1;
    public static final byte SUB_FILEPACK_3 = 2;
    public static final byte SUB_FILEPACK_4 = 3;
    public static final byte SUB_FILEPACK_5 = 4;
    public static final byte TEST = 111;
    public static final byte TIME_BOMB = 109;
    public static final byte TOP_CLAN = 116;
    public static final byte UNDESTROYTILE = 92;
    public static final byte UPDATE_EXP = 97;
    public static final byte UPDATE_HP = 51;
    public static final byte UPDATE_MONEY = 105;
    public static final byte VAMPIRE = 59;
    public static final byte VERSION_CODE = 114;

    private static final Map<Byte, String> cmdMap = new HashMap<>();

    static {
        Field[] fields = Cmd.class.getDeclaredFields();

        for (Field field : fields) {
            try {
                if (field.getType() == byte.class) {
                    byte value = field.getByte(null);
                    String name = String.format("%s (%d)", field.getName(), value);
                    cmdMap.put(value, name);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public static String getCmdNameByValue(byte value) {
        return cmdMap.getOrDefault(value, String.valueOf(value));
    }

}