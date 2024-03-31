package com.teamobi.mobiarmy2.model;

import com.teamobi.mobiarmy2.constant.UserState;
import com.teamobi.mobiarmy2.network.Impl.Message;
import com.teamobi.mobiarmy2.network.Impl.Session;

public class User {

    private final Session session;
    private UserState state;

    private long id;
    private String username;
    private String password;
    private short clanId;
    private int xu;
    private int luong;

    private boolean isLogged;

    public User() {
        this.session = null;
    }

    public User(Session session) {
        this.session = session;
    }

    public Session getSession() {
        return session;
    }

    public UserState getState() {
        return state;
    }

    public void setState(UserState state) {
        this.state = state;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isLogged() {
        return isLogged;
    }

    public void setLogged(boolean logged) {
        isLogged = logged;
    }

    public short getClanId() {
        return clanId;
    }

    public void setClanId(short clanId) {
        this.clanId = clanId;
    }

    public int getXu() {
        return xu;
    }

    public void setXu(int xu) {
        this.xu = xu;
    }

    public int getLuong() {
        return luong;
    }

    public void setLuong(int luong) {
        this.luong = luong;
    }

    public boolean isWaiting() {
        return state.equals(UserState.WAITING);
    }

    public void sendMessage(Message ms) {
        session.sendMessage(ms);
    }
}
