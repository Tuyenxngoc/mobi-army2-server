package com.teamobi.mobiarmy2.model;

import com.teamobi.mobiarmy2.constant.CommonConstant;
import com.teamobi.mobiarmy2.constant.UserState;
import com.teamobi.mobiarmy2.fight.FightWait;
import com.teamobi.mobiarmy2.network.ISession;
import com.teamobi.mobiarmy2.network.Impl.Message;
import com.teamobi.mobiarmy2.service.IUserService;
import com.teamobi.mobiarmy2.service.Impl.UserService;
import com.teamobi.mobiarmy2.util.Until;
import lombok.Getter;
import lombok.Setter;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author tuyen
 */
@Getter
@Setter
public class User {

    public static NVData.EquipmentEntry[][] nvEquipDefault;
    public ISession session;
    public UserState state;
    public int id;
    public byte nhanVat;
    public String username;
    public String password;
    public short clanId;
    public int xu;
    public int luong;
    public int danhVong;
    public boolean isLogged;
    public boolean isLock;
    public boolean isActive;
    public byte nvUsed;
    public int pointEvent;
    public LocalDateTime xpX2Time;
    public boolean[] nvStt;
    public int[] lever;
    public byte[] leverPercent;
    public int[] xp;
    public int[] point;
    public int[][] pointAdd;
    public byte[] items;
    public int[][] NvData;
    public int[] friends;
    public int[] mission;
    public byte[] missionLevel;
    public ruongDoTBEntry[][] nvEquip;

    public List<ruongDoItemEntry> ruongDoItem;
    public List<ruongDoTBEntry> ruongDoTB;
    private FightWait fightWait;

    private final IUserService userService;

    public User() {
        this.userService = new UserService(this);
    }

    public User(ISession session) {
        this();
        this.session = session;
    }

    public boolean isNotWaiting() {
        return !state.equals(UserState.WAITING);
    }

    public void sendMessage(Message ms) {
        session.sendMessage(ms);
    }

    public void sendServerMessage(String ss) {
        userService.sendServerMessage(ss);
    }


    public void logout() {
        isLogged = false;
    }

    public int getLever(byte nv) {
        return lever[nv];
    }

    public void updateXu(int xuUp) {
        if (xuUp == 0) {
            return;
        }
        long sum = xuUp + xu;
        if (sum > CommonConstant.MAX_XU) {
            xu = CommonConstant.MAX_XU;
        } else if (sum < CommonConstant.MIN_XU) {
            xu = CommonConstant.MIN_XU;
        } else {
            xu += xuUp;
        }
        userService.sendUpdateMoney();
    }

    public void updateLuong(int luongUp) {
        if (luongUp == 0) {
            return;
        }
        long sum = luongUp + luong;
        if (sum > CommonConstant.MAX_LUONG) {
            luong = CommonConstant.MAX_LUONG;
        } else if (sum < CommonConstant.MIN_LUONG) {
            luong = CommonConstant.MIN_LUONG;
        } else {
            luong += luongUp;
        }
        userService.sendUpdateMoney();
    }

    public void updateDanhVong(int danhVongUp) {
        if (danhVongUp == 0) {
            return;
        }
        long sum = danhVongUp + danhVong;
        if (sum > CommonConstant.MAX_DANH_VONG) {
            danhVong = CommonConstant.MAX_DANH_VONG;
        } else if (sum < CommonConstant.MIN_DANH_VONG) {
            danhVong = CommonConstant.MIN_DANH_VONG;
        } else {
            danhVong += danhVongUp;
        }
        userService.sendUpdateDanhVong(danhVongUp);
    }

    public synchronized void updateRuong(ruongDoTBEntry tbUpdate, ruongDoTBEntry addTB, int removeTB, ArrayList<ruongDoItemEntry> addItem, ArrayList<ruongDoItemEntry> removeItem) throws IOException {
        Message ms;
        DataOutputStream ds;
        if (addTB != null) {
            int bestLocation = -1;
            for (int i = 0; i < this.ruongDoTB.size(); i++) {
                ruongDoTBEntry rdtbE = this.ruongDoTB.get(i);
                if (rdtbE == null) {
                    bestLocation = i;
                    break;
                }
            }
            addTB.dayBuy = new Date();
            addTB.isUse = false;
            if (addTB.invAdd == null) {
                addTB.invAdd = new short[addTB.entry.invAdd.length];
                for (int j = 0; j < addTB.entry.invAdd.length; j++) {
                    addTB.invAdd[j] = addTB.entry.invAdd[j];
                }
            }
            if (addTB.percentAdd == null) {
                addTB.percentAdd = new short[addTB.entry.percenAdd.length];
                for (int j = 0; j < addTB.entry.percenAdd.length; j++) {
                    addTB.percentAdd[j] = addTB.entry.percenAdd[j];
                }
            }
            addTB.slotNull = 3;
            addTB.cap = addTB.entry.cap;
            addTB.slot = new int[3];
            for (int i = 0; i < 3; i++) {
                addTB.slot[i] = -1;
            }
            if (bestLocation == -1) {
                addTB.index = ruongDoTB.size();
                ruongDoTB.add(addTB);
            } else {
                addTB.index = bestLocation;
                ruongDoTB.set(bestLocation, addTB);
            }
            ms = new Message(104);
            ds = ms.writer();
            ds.writeByte(0);
            ds.writeInt(addTB.index | 0x10000);
            ds.writeByte(addTB.entry.idNV);
            ds.writeByte(addTB.entry.idEquipDat);
            ds.writeShort(addTB.entry.id);
            ds.writeUTF(addTB.entry.name);
            ds.writeByte(addTB.invAdd.length * 2);
            for (int i = 0; i < addTB.invAdd.length; i++) {
                ds.writeByte(addTB.invAdd[i]);
                ds.writeByte(addTB.percentAdd[i]);
            }
            ds.writeByte(addTB.entry.hanSD);
            ds.writeByte(addTB.entry.isSet ? 1 : 0);
            ds.writeByte(addTB.vipLevel);
            ds.flush();
            sendMessage(ms);
        }
        ByteArrayOutputStream bas = new ByteArrayOutputStream();
        DataOutputStream ds1 = new DataOutputStream(bas);
        int nUpdate = 0;
        if (tbUpdate != null) {
            nUpdate++;
            ds1.writeByte(2);
            ds1.writeInt(tbUpdate.index | 0x10000);
            ds1.writeByte(tbUpdate.invAdd.length * 2);
            for (int i = 0; i < tbUpdate.invAdd.length; i++) {
                ds1.writeByte(tbUpdate.invAdd[i]);
                ds1.writeByte(tbUpdate.percentAdd[i]);
            }
            ds1.writeByte(tbUpdate.slotNull);
            // Ngay het han
            int hanSD = tbUpdate.entry.hanSD - Until.getNumDay(tbUpdate.dayBuy, new Date());
            if (hanSD < 0) {
                hanSD = 0;
            }
            ds1.writeByte(hanSD);
        }
        if (addItem != null && addItem.size() > 0) {
            for (int i = 0; i < addItem.size(); i++) {
                ruongDoItemEntry spE = addItem.get(i);
                if (spE.numb > 100) {
                    ruongDoItemEntry spE2 = new ruongDoItemEntry();
                    spE2.entry = spE.entry;
                    spE2.numb = spE.numb - 100;
                    spE.numb = 100;
                    addItem.add(spE2);
                }
                if (spE.numb <= 0) {
                    continue;
                }
                nUpdate++;
                // Kiem tra trong ruong co=>tang so luong. ko co=> tao moi
                boolean isHave = false;
                for (ruongDoItemEntry spE1 : ruongDoItem) {
                    if (spE1.entry.id == spE.entry.id) {
                        isHave = true;
                        spE1.numb += spE.numb;
                        break;
                    }
                }
                // ko co=> Tao moi
                if (!isHave) {
                    ruongDoItem.add(spE);
                }
                ds1.writeByte(spE.numb > 1 ? 3 : 1);
                ds1.writeByte(spE.entry.id);
                if (spE.numb > 1) {
                    ds1.writeByte(spE.numb);
                }
                ds1.writeUTF(spE.entry.name);
                ds1.writeUTF(spE.entry.detail);
            }
        }
        if (removeItem != null && removeItem.size() > 0) {
            for (int k = 0; k < removeItem.size(); k++) {
                ruongDoItemEntry spE = removeItem.get(k);
                if (spE.numb > 100) {
                    ruongDoItemEntry spE2 = new ruongDoItemEntry();
                    spE2.entry = spE.entry;
                    spE2.numb = spE.numb - 100;
                    spE.numb = 100;
                    removeItem.add(spE2);
                }
                if (spE.numb <= 0) {
                    continue;
                }
                // Kiem tra trong ruong co=>giam so luong
                for (int i = 0; i < ruongDoItem.size(); i++) {
                    ruongDoItemEntry spE1 = ruongDoItem.get(i);
                    if (spE1.entry.id == spE.entry.id) {
                        if (spE1.numb < spE.numb) {
                            spE.numb = spE1.numb;
                        }
                        spE1.numb -= spE.numb;
                        if (spE1.numb == 0) {
                            ruongDoItem.remove(i);
                        }
                        nUpdate++;
                        ds1.writeByte(0);
                        ds1.writeInt(spE.entry.id);
                        ds1.writeByte(spE.numb);
                        break;
                    }
                }
            }
        }
        if (removeTB >= 0 && removeTB < ruongDoTB.size() && ruongDoTB.get(removeTB) != null) {
            nUpdate++;
            ruongDoTB.set(removeTB, null);
            ds1.writeByte(0);
            ds1.writeInt(removeTB | 0x10000);
            ds1.writeByte(1);
        }
        ds1.flush();
        bas.flush();
        if (nUpdate == 0) {
            return;
        }
        ms = new Message(27);
        ds = ms.writer();
        ds.writeByte(nUpdate);
        ds.write(bas.toByteArray());
        ds.flush();
        sendMessage(ms);
    }

}
