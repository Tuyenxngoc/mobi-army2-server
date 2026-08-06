package com.teamobi.mobiarmy2.network;

import com.teamobi.mobiarmy2.constant.Cmd;
import com.teamobi.mobiarmy2.entity.User;
import io.netty.channel.Channel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;
import java.util.concurrent.*;

@Slf4j
public class Session {
    private static final ExecutorService VIRTUAL_EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();
    private static final Message POISON_PILL = new PoisonMessage();
    private static final Set<Byte> WHITE_LIST_CMDS = Set.of(
            Cmd.GET_KEY,
            Cmd.LOGIN,
            Cmd.REGISTER_2,
            Cmd.SET_PROVIDER,
            Cmd.VERSION_CODE,
            Cmd.GET_STRING);

    @Getter
    private final long sessionId;
    private final Channel channel;

    @Getter
    private final String ipAddress;

    @Setter
    @Getter
    private String platform;

    @Setter
    @Getter
    private String version;

    @Setter
    @Getter
    private byte provider = -1;

    @Setter
    @Getter
    private String agent;

    @Getter
    @Setter
    private User user;

    private MessageRouter messageRouter;
    private final BlockingQueue<Message> messageQueue = new LinkedBlockingQueue<>();
    private volatile boolean running = true;

    Session(long sessionId, Channel channel) {
        this.sessionId = sessionId;
        this.channel = channel;
        this.ipAddress = channel.remoteAddress().toString();
    }

    /**
     * Gắn router rồi bắt đầu vòng xử lý message.
     * <p>
     * Tách khỏi constructor vì các handler trong router cần chính Session này,
     * nên router chỉ dựng được sau khi Session tồn tại. Chỉ
     * {@link SessionFactory} gọi, đúng một lần, ngay sau khi tạo Session và
     * trước khi có message nào được enqueue.
     */
    void attachRouter(MessageRouter messageRouter) {
        this.messageRouter = messageRouter;
        VIRTUAL_EXECUTOR.submit(this::processLoop);
    }

    void sendMessage(Message msg) {
        if (msg == null || channel == null || !channel.isActive()) {
            return;
        }

        if (log.isDebugEnabled()) {
            log.debug("Server sends ms {} to client {}", Cmd.getCmdNameByValue(msg.getCommand()), sessionId);
        }

        channel.writeAndFlush(msg);
    }

    private void processLoop() {
        try {
            while (running) {
                Message msg = messageQueue.take();

                // Check for poison pill
                if (msg instanceof PoisonMessage) {
                    break;
                }

                messageRouter.onMessage(msg);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            // Cleanup on exit
            if (isUserLoggedIn()) {
                messageRouter.getAuthMessageHandler().handleUserLogoutCleanup();
            }
        }
    }

    public void enqueueMessage(Message msg) {
        if (!running || !isActive() || !isAuthorized(msg)) {
            return;
        }

        if (!messageQueue.offer(msg)) {
            log.warn("Failed to enqueue message for session {}, queue might be full", sessionId);
        }
    }

    public boolean isActive() {
        return channel != null && channel.isActive();
    }

    private boolean isAuthorized(Message msg) {
        if (WHITE_LIST_CMDS.contains(msg.getCommand())) {
            return true;
        }
        return isUserLoggedIn();
    }

    private boolean isUserLoggedIn() {
        return user != null && user.isLogged();
    }

    public void closeChannel() {
        if (isActive()) {
            channel.close();
        }
    }

    public void cleanup() {
        if (!running) {
            return;
        }
        running = false;

        messageQueue.clear();
        messageQueue.offer(POISON_PILL);
    }

    public static void shutdownExecutor() {
        VIRTUAL_EXECUTOR.shutdown();
        try {
            if (!VIRTUAL_EXECUTOR.awaitTermination(10, TimeUnit.SECONDS)) {
                log.warn("Executor did not terminate in time, forcing shutdown...");
                VIRTUAL_EXECUTOR.shutdownNow();
            }
        } catch (InterruptedException e) {
            log.error("Interrupted while waiting for executor shutdown", e);
            VIRTUAL_EXECUTOR.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
