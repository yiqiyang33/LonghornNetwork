import java.util.concurrent.Semaphore;

/**
 * Runnable that models processing of a friend request event between two students.
 */
public class FriendRequestThread implements Runnable {

    private static final Semaphore FRIEND_REQUEST_SEMAPHORE = new Semaphore(1);

    private final UniversityStudent sender;
    private final UniversityStudent receiver;

    /**
     * Creates a friend request action initiated by one student toward another.
     *
     * @param sender   student issuing the friend request
     * @param receiver intended recipient of the request
     */
    public FriendRequestThread(UniversityStudent sender, UniversityStudent receiver) {
        this.sender = sender;
        this.receiver = receiver;
    }

    /**
     * Executes the simulated friend request logic when scheduled.
     */
    @Override
    public void run() {
        if (sender == null || receiver == null) {
            return;
        }
        boolean acquired = false;
        try {
            FRIEND_REQUEST_SEMAPHORE.acquire();
            acquired = true;
            sender.establishFriendship(receiver);
            System.out.println("FriendRequestThread: " + sender.getName()
                    + " sent a friend request to " + receiver.getName());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            if (acquired) {
                FRIEND_REQUEST_SEMAPHORE.release();
            }
        }
    }
}
