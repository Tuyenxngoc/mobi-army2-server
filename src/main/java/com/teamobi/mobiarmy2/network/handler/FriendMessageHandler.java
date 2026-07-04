package com.teamobi.mobiarmy2.network.handler;

import com.teamobi.mobiarmy2.constant.Cmd;
import com.teamobi.mobiarmy2.constant.GameConstants;
import com.teamobi.mobiarmy2.constant.GameString;
import com.teamobi.mobiarmy2.dao.UserDAO;
import com.teamobi.mobiarmy2.dto.FriendDTO;
import com.teamobi.mobiarmy2.entity.User;
import com.teamobi.mobiarmy2.network.Message;
import com.teamobi.mobiarmy2.network.Session;
import com.teamobi.mobiarmy2.server.ServerManager;
import com.teamobi.mobiarmy2.util.Utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class FriendMessageHandler extends BaseMessageHandler {
    private static final int PRICE_CHAT_SERVER = 10_000;

    private final UserDAO userDAO;
    private final ServerManager serverManager;

    public FriendMessageHandler(Session session, UserDAO userDAO, ServerManager serverManager) {
        super(session);
        this.userDAO = userDAO;
        this.serverManager = serverManager;
    }

    public void handleSendMessage(Message ms) throws IOException {
        DataInputStream dis = ms.reader();
        int userId = dis.readInt();
        String content = dis.readUTF().trim();
        if (content.isEmpty() || content.length() > 100) {
            return;
        }

        //Neu la admin -> bo qua
        if (userId == 1) {
            return;
        }

        //Neu la nguoi dua tin -> chat The gioi
        if (userId == 2) {
            if (us().getXu() < PRICE_CHAT_SERVER) {
                messageSender.sendServerMessage(us(), GameString.INSUFFICIENT_FUNDS);
                return;
            }
            us().updateXu(-PRICE_CHAT_SERVER);
            messageSender.sendServerInfo(us(), GameString.createMessageFromSender(us().getUsername(), content), true);
            return;
        }

        User receiver = serverManager.getUserByUserId(userId);
        if (receiver == null) {
            messageSender.sendServerMessage(us(), GameString.INVITE_OFFLINE);
            return;
        }
        messageSender.sendMessageToUser(false, us(), content, receiver);
    }

    public void handleViewFriendList() throws IOException {
        Message ms = new Message(Cmd.FRIENDLIST);
        DataOutputStream ds = ms.writer();
        if (!us().getFriends().isEmpty()) {
            List<FriendDTO> friends = userDAO.getFriendsList(us().getUserId(), us().getFriends());
            for (FriendDTO friend : friends) {
                ds.writeInt(friend.getUserId());
                ds.writeUTF(friend.getName());
                ds.writeInt(friend.getXu());
                ds.writeByte(friend.getActiveCharacterId());
                ds.writeShort(friend.getClanId());
                ds.writeByte(friend.getOnline());
                ds.writeByte(friend.getLevel());
                ds.writeByte(friend.getLevelPt());
                for (short i : friend.getData()) {
                    ds.writeShort(i);
                }
            }
        }
        ds.flush();
        sendMessage(ms);
    }

    public void handleAddFriend(Message ms) throws IOException {
        Set<Integer> friends = us().getFriends();
        try {
            int id = ms.reader().readInt();

            // Kiểm tra số lượng bạn bè đã đạt giới hạn
            if (friends.size() >= GameConstants.MAX_FRIENDS) {
                sendAddFriendMessage(2);
                return;
            }

            // Kiểm tra nếu bạn bè đã tồn tại
            if (!friends.add(id)) {
                sendAddFriendMessage(1);
                return;
            }

            sendAddFriendMessage(0);
        } catch (IOException e) {
            sendAddFriendMessage(1);
        }
    }

    private void sendAddFriendMessage(int status) throws IOException {
        Message ms = new Message(Cmd.ADD_FRIEND_RESULT);
        DataOutputStream ds = ms.writer();
        ds.writeByte(status);
        ds.flush();
        sendMessage(ms);
    }

    public void handleRemoveFriend(Message ms) throws IOException {
        try {
            Integer id = ms.reader().readInt();
            us().getFriends().remove(id);
            sendDeleteFriendMessage(0);
        } catch (IOException e) {
            sendDeleteFriendMessage(1);
        }
    }

    private void sendDeleteFriendMessage(int status) throws IOException {
        Message ms = new Message(Cmd.DELETE_FRIEND_RESULT);
        DataOutputStream ds = ms.writer();
        ds.writeByte(status);
        ds.flush();
        sendMessage(ms);
    }

    public void handleFindPlayer(Message ms) throws IOException {
        String username = ms.reader().readUTF().trim();
        if (username.isEmpty()) {
            messageSender.sendServerMessage(us(), GameString.FRIEND_ADD_MISSING_NAME);
            return;
        }
        if (Utils.isAlphanumeric(username)) {
            messageSender.sendServerMessage(us(), GameString.FRIEND_ADD_INVALID_NAME);
            return;
        }
        Optional<Integer> foundUserId = userDAO.findUserIdByUsername(username);
        ms = new Message(Cmd.SEARCH);
        DataOutputStream ds = ms.writer();
        if (foundUserId.isPresent()) {
            ds.writeInt(foundUserId.get());
            ds.writeUTF(username);
        }
        ds.flush();
        sendMessage(ms);
    }
}
