package com.teamobi.mobiarmy2.network.handler;

import com.teamobi.mobiarmy2.bootstrap.ApplicationContext;
import com.teamobi.mobiarmy2.common.config.ServerConfig;
import com.teamobi.mobiarmy2.common.constant.Cmd;
import com.teamobi.mobiarmy2.common.constant.GameString;
import com.teamobi.mobiarmy2.common.util.Utils;
import com.teamobi.mobiarmy2.dao.UserDAO;
import com.teamobi.mobiarmy2.dto.FriendDTO;
import com.teamobi.mobiarmy2.entity.User;
import com.teamobi.mobiarmy2.network.Message;
import com.teamobi.mobiarmy2.network.Session;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class FriendMessageHandler extends BaseMessageHandler {
    private final UserDAO userDAO;

    public FriendMessageHandler(Session session, UserDAO userDAO) {
        super(session);
        this.userDAO = userDAO;
    }

    public void handleViewFriendList() {
        User user = session.getUser();
        try {
            Message ms = new Message(Cmd.FRIENDLIST);
            DataOutputStream ds = ms.writer();
            if (!user.getFriends().isEmpty()) {
                List<FriendDTO> friends = userDAO.getFriendsList(user.getUserId(), user.getFriends());
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
        } catch (IOException ignored) {
        }
    }

    public void handleAddFriend(Message ms) {
        int maxFriends = ApplicationContext.getInstance().getBean(ServerConfig.class).getMaxFriends();
        Set<Integer> friends = session.getUser().getFriends();
        try {
            int id = ms.reader().readInt();

            // Kiểm tra số lượng bạn bè đã đạt giới hạn
            if (friends.size() >= maxFriends) {
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

    private void sendAddFriendMessage(int status) {
        try {
            Message ms = new Message(Cmd.ADD_FRIEND_RESULT);
            DataOutputStream ds = ms.writer();
            ds.writeByte(status);
            ds.flush();
            sendMessage(ms);
        } catch (IOException ignored) {
        }
    }

    public void handleRemoveFriend(Message ms) {
        User user = session.getUser();
        try {
            Integer id = ms.reader().readInt();
            user.getFriends().remove(id);
            sendDeleteFriendMessage(0);
        } catch (IOException e) {
            sendDeleteFriendMessage(1);
        }
    }

    private void sendDeleteFriendMessage(int status) {
        try {
            Message ms = new Message(Cmd.DELETE_FRIEND_RESULT);
            DataOutputStream ds = ms.writer();
            ds.writeByte(status);
            ds.flush();
            sendMessage(ms);
        } catch (IOException ignored) {
        }
    }

    public void handleFindPlayer(Message ms) {
        try {
            String username = ms.reader().readUTF().trim();
            if (username.isEmpty()) {
                sendMessageLoginFail(GameString.FRIEND_ADD_MISSING_NAME);
                return;
            }
            if (Utils.isAlphanumeric(username)) {
                sendMessageLoginFail(GameString.FRIEND_ADD_INVALID_NAME);
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
        } catch (IOException ignored) {
        }
    }
}
