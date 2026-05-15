package com.teamobi.mobiarmy2.fight;

import com.teamobi.mobiarmy2.constant.Cmd;
import com.teamobi.mobiarmy2.entity.User;
import com.teamobi.mobiarmy2.network.Message;
import com.teamobi.mobiarmy2.util.RandomUtil;
import lombok.Getter;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

public class TrainingManager implements IFightBase {
    private final User trainingUser;
    private final Player[] players;
    @Getter
    private final byte mapId;
    @Getter
    private byte windX;
    @Getter
    private byte windY;
    private final BulletManager bulletManager;
    @Getter
    private final FightMapManager fightMapManager;

    public TrainingManager(User trainingUser, byte mapId) {
        this.trainingUser = trainingUser;
        this.mapId = mapId;
        this.players = new Player[2];
        this.fightMapManager = new FightMapManager();
        this.bulletManager = new BulletManager(this, fightMapManager);
    }

    @Override
    public void startGame() {
        // Tải dữ liệu bản đồ
        fightMapManager.loadMapId(mapId);

        // Tải dữ liệu vị trí
        List<short[]> randomPositions = fightMapManager.getRandomPlayerPositions(8);

        short[] playerPosition = randomPositions.get(0);
        short[] enemyPosition = randomPositions.get(1);

        short gunId = trainingUser.getGunId();
        byte characterId = trainingUser.getActiveCharacterId();
        players[0] = new Player(this, 0, playerPosition[0], playerPosition[1], 70, 1000, gunId, characterId, true); // người chơi luyện tập
        players[1] = new Player(this, 1, enemyPosition[0], enemyPosition[1], 1000, 1000, gunId, characterId, false);

        try {
            Message ms = new Message(Cmd.START_ARMY);
            DataOutputStream ds = ms.writer();
            short[] equips = trainingUser.getEquips();
            for (short i : equips) {
                ds.writeShort(i);
            }
            ds.writeByte(mapId);
            ds.writeByte(30);
            ds.writeShort(0);
            for (Player player : players) {
                ds.writeShort(player.getX());
                ds.writeShort(player.getY());
                ds.writeShort(player.getMaxHp());
            }
            for (byte i = 0; i < 6; i++) {
                ds.writeShort(-1);
            }
            ds.flush();
            trainingUser.sendMessage(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Player[] getPlayers() {
        return players;
    }

    @Override
    public int getTotalPlayers() {
        return 2;
    }

    private void updateWind() {
        if (RandomUtil.nextInt(0, 100) > 25) {
            int[] range = getWindRange(trainingUser.getActiveCharacterId());

            windX = (byte) RandomUtil.nextInt(-range[0], range[0]);
            windY = (byte) RandomUtil.nextInt(-range[1], range[1]);
        }

        sendWindUpdate();
    }

    /**
     * Lấy phạm vi gió dựa trên ID nhân vật của người chơi.
     *
     * @param characterId ID nhân vật của người chơi.
     * @return mảng chứa phạm vi gió theo trục X và Y.
     */
    private int[] getWindRange(byte characterId) {
        if (characterId == 9) {
            return new int[]{60, 25};
        }
        return new int[]{70, 70};
    }

    private void sendWindUpdate() {
        try {
            Message ms = new Message(Cmd.WIND);
            DataOutputStream ds = ms.writer();
            ds.writeByte(windX);
            ds.writeByte(windY);
            ds.flush();
            trainingUser.sendMessage(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void doNextTurn() {
        for (Player player : players) {
            // Lưu lại vị trí ban đầu
            int preY = player.getY();

            // Cập nhật vị trí y
            player.updateYPosition();

            // Gửi thông báo nếu vị trí thay đổi
            if (preY != player.getY()) {
                sendMessageUpdateXY(player.getIndex());
            }
        }

        // Đặt lại giá trị của người chơi trong lượt mới như thể lực, ..., vv
        Player player = players[0];
        player.resetValueInNewTurn();

        // Random gió
        updateWind();

        // Gửi thông báo lượt chơi tiếp theo
        sendNextTurnMessage();
    }

    private void sendNextTurnMessage() {
        try {
            Message ms = new Message(Cmd.NEXT_TURN);
            DataOutputStream ds = ms.writer();
            ds.writeByte(0);
            ds.flush();
            trainingUser.sendMessage(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Player getPlayerTurn() {
        return players[0]; // Luôn trả về người chơi luyện tập
    }

    public void stopTraining() {
        bulletManager.resetBullets();
    }

    @Override
    public void handlePlayerShoot(int userId, byte bullId, short x, short y, short angle, byte force, byte force2, byte numShoot) {
        Player player = getPlayerTurn();

        // Cập nhật vị trí người chơi
        player.updateXY(x, y);

        // Lưu tọa độ ban đầu của người chơi trước khi bắn
        int xS = player.getX();
        int yS = player.getY();

        // Tạo đạn và cập nhật quỹ đạo
        bulletManager.addShoot(player, bullId, angle, force, force2, numShoot);
        bulletManager.updateBullets();

        // Gủi ms bắn đạn
        sendFireArmyPacket(bullId, xS, yS, angle, force2, numShoot, player);

        // Xóa các đạn đã bắn
        bulletManager.resetBullets();

        // Chuyển lượt chơi tiếp theo
        doNextTurn();
    }

    private void sendFireArmyPacket(byte bullId, int xS, int yS, short angle, byte force2, byte numShoot,
                                    Player player) {
        List<Bullet> bullets = bulletManager.getBullets();
        byte typeShoot = bulletManager.getTypeShoot();
        try {
            Message ms = new Message(Cmd.FIRE_ARMY);
            DataOutputStream ds = ms.writer();
            ds.writeByte(typeShoot);
            ds.writeByte(player.isUsePow() ? 1 : 0);
            ds.writeByte(player.getIndex());
            ds.writeByte(bullId);
            ds.writeShort(xS);
            ds.writeShort(yS);
            ds.writeShort(angle);
            if (bullId == 17 || bullId == 19) {
                ds.writeByte(force2);
            }
            if (bullId == 14 || bullId == 40) {
                ds.writeByte(0);// angle
                ds.writeByte(0);// force
            }
            if (bullId == 44 || bullId == 45 || bullId == 47) {
                ds.writeByte(0);// angle
            }
            ds.writeByte(numShoot);
            ds.writeByte(bullets.size());
            for (Bullet bullet : bullets) {
                List<Point> trajectory = bullet.getTrajectory();
                int size = trajectory.size();
                ds.writeShort(size);// Ghi độ dài quỹ đạo

                if (typeShoot == 0) {// Ghi tọa độ theo dạng delta (chênh lệch)
                    for (int i = 0; i < size; i++) {
                        Point point = bullet.getTrajectory().get(i);

                        if (i == 0) {
                            // Điểm đầu tiên: ghi tọa độ tuyệt đối
                            ds.writeShort(point.getX());
                            ds.writeShort(point.getY());
                        } else {
                            if ((i == size - 1) && bullId == 49) {// Điểm cuối của laser Magenta
                                ds.writeShort(point.getX());
                                ds.writeShort(point.getY());
                                ds.writeByte(bullet.getDXLaser());
                                ds.writeByte(bullet.getDYLaser());
                            } else {
                                Point prevPoint = bullet.getTrajectory().get(i - 1);
                                ds.writeByte((byte) (point.getX() - prevPoint.getX()));
                                ds.writeByte((byte) (point.getY() - prevPoint.getY()));
                            }
                        }
                    }
                } else if (typeShoot == 1) { // Ghi tọa độ tuyệt đối cho mỗi điểm
                    for (Point point : bullet.getTrajectory()) {
                        ds.writeShort(point.getX());
                        ds.writeShort(point.getY());
                    }
                }

                if (bullId == 48) {
                    ds.writeByte(bullet.getHitPoints().size());
                    for (Point hit : bullet.getHitPoints()) {
                        ds.writeShort(hit.getX());
                        ds.writeShort(hit.getY());
                    }
                }
            }

            // Ghi thông tin nếu đạn siêu cao
            byte superType = bulletManager.getSuperType();
            ds.writeByte(superType);
            if (superType == 1 || superType == 2) {
                ds.writeShort(bulletManager.getSuperX());
                ds.writeShort(bulletManager.getSuperY());
            }
            ds.flush();
            trainingUser.sendMessage(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendMessageUpdateXY(int index) {
        try {
            Player player = players[index];
            Message ms = new Message(Cmd.MOVE_ARMY);
            DataOutputStream ds = ms.writer();
            ds.writeByte(index);
            ds.writeShort(player.getX());
            ds.writeShort(player.getY());
            ds.flush();
            trainingUser.sendMessage(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
