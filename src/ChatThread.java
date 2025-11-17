import java.util.concurrent.Semaphore;

/**
 * Runnable that simulates an asynchronous chat exchange between students.
 */
public class ChatThread implements Runnable {

    private static final Semaphore CHAT_SEMAPHORE = new Semaphore(1);

    private final UniversityStudent sender;
    private final UniversityStudent receiver;
    private final String message;

    /**
     * Creates a chat task representing a message sent from one student to another.
     *
     * @param sender   student initiating the conversation
     * @param receiver student receiving the message
     * @param message  textual content to transmit
     */
    public ChatThread(UniversityStudent sender, UniversityStudent receiver, String message) {
        this.sender = sender;
        this.receiver = receiver;
        this.message = message == null ? "" : message;
    }

    /**
     * Performs the simulated message send when the thread executes.
     */
    @Override
    public void run() {
        if (sender == null || receiver == null) {
            return;
        }
        boolean acquired = false;
        try {
            CHAT_SEMAPHORE.acquire();
            acquired = true;
            String logMessage = sender.getName() + " -> " + receiver.getName() + ": " + message;
            sender.addChatMessage(receiver, "Sent: " + message);
            receiver.addChatMessage(sender, "Received: " + message);
            System.out.println("ChatThread: " + logMessage);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            if (acquired) {
                CHAT_SEMAPHORE.release();
            }
        }
    }
}
