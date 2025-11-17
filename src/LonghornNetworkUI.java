import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Simple Swing UI that visualizes the core Longhorn Network components.
 */
public class LonghornNetworkUI {

    private JFrame frame;
    private JComboBox<String> testCaseSelector;
    private JTextField companyField;
    private JTextArea studentArea;
    private JTextArea graphArea;
    private JTextArea roommateArea;
    private JTextArea podArea;
    private JTextArea referralArea;
    private JTextArea interactionsArea;

    private List<UniversityStudent> currentStudents;
    private StudentGraph currentGraph;
    private ReferralPathFinder currentPathFinder;
    private List<List<UniversityStudent>> currentPods;

    /**
     * Launches the UI on the Swing event dispatch thread.
     */
    public static void launch() {
        SwingUtilities.invokeLater(() -> {
            LonghornNetworkUI ui = new LonghornNetworkUI();
            ui.initialize();
        });
    }

    private void initialize() {
        frame = new JFrame("Longhorn Network UI");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(1000, 800);
        frame.setLayout(new BorderLayout());

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controlPanel.add(new JLabel("Select Test Case:"));
        testCaseSelector = new JComboBox<>(new String[]{"Test Case 1", "Test Case 2", "Test Case 3"});
        testCaseSelector.addActionListener(e -> loadSelectedTestCase());
        controlPanel.add(testCaseSelector);

        controlPanel.add(new JLabel("Referral Company:"));
        companyField = new JTextField("DummyCompany", 15);
        companyField.addActionListener(e -> updateReferralArea());
        controlPanel.add(companyField);

        JButton refreshButton = new JButton("Refresh Referral Paths");
        refreshButton.addActionListener(e -> updateReferralArea());
        controlPanel.add(refreshButton);

        frame.add(controlPanel, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        studentArea = createTextArea();
        graphArea = createTextArea();
        roommateArea = createTextArea();
        podArea = createTextArea();
        referralArea = createTextArea();
        interactionsArea = createTextArea();

        tabs.add("Students", new JScrollPane(studentArea));
        tabs.add("Graph", new JScrollPane(graphArea));
        tabs.add("Roommates", new JScrollPane(roommateArea));
        tabs.add("Pods", new JScrollPane(podArea));
        tabs.add("Referral Paths", new JScrollPane(referralArea));
        tabs.add("Friends & Chats", new JScrollPane(interactionsArea));

        frame.add(tabs, BorderLayout.CENTER);
        frame.setVisible(true);

        loadSelectedTestCase();
    }

    private JTextArea createTextArea() {
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        return area;
    }

    private void loadSelectedTestCase() {
        int index = testCaseSelector.getSelectedIndex();
        List<UniversityStudent> students;
        switch (index) {
            case 0:
                students = Main.generateTestCase1();
                break;
            case 1:
                students = Main.generateTestCase2();
                break;
            case 2:
            default:
                students = Main.generateTestCase3();
                break;
        }
        currentStudents = students;

        GaleShapley.assignRoommates(currentStudents);
        currentGraph = new StudentGraph(currentStudents);
        PodFormation podFormation = new PodFormation(currentGraph);
        podFormation.formPods(Math.max(1, currentStudents.size() / 2));
        currentPods = podFormation.getLatestPods();
        currentPathFinder = new ReferralPathFinder(currentGraph);

        simulateInteractions(currentStudents);

        studentArea.setText(formatStudentData());
        graphArea.setText(formatGraphData());
        roommateArea.setText(formatRoommateAssignments());
        podArea.setText(formatPodAssignments());
        interactionsArea.setText(formatInteractions());
        updateReferralArea();
    }

    private void simulateInteractions(List<UniversityStudent> students) {
        if (students.size() < 2) {
            return;
        }
        ExecutorService executor = Executors.newFixedThreadPool(4);
        UniversityStudent s1 = students.get(0);
        UniversityStudent s2 = students.get(1);
        executor.submit(new FriendRequestThread(s1, s2));
        executor.submit(new ChatThread(s1, s2, "Hello from UI!"));
        executor.submit(new FriendRequestThread(s2, s1));
        executor.submit(new ChatThread(s2, s1, "Welcome to the Longhorn Network UI!"));
        executor.shutdown();
        try {
            executor.awaitTermination(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private String formatStudentData() {
        StringBuilder builder = new StringBuilder();
        for (UniversityStudent student : currentStudents) {
            builder.append(student).append("\n");
        }
        return builder.toString();
    }

    private String formatGraphData() {
        if (currentGraph == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (UniversityStudent student : currentGraph.getAllNodes()) {
            builder.append(student.getName()).append(" -> ");
            List<StudentGraph.Edge> edges = currentGraph.getNeighbors(student);
            if (edges.isEmpty()) {
                builder.append("[None]");
            } else {
                builder.append("[");
                for (int i = 0; i < edges.size(); i++) {
                    StudentGraph.Edge edge = edges.get(i);
                    builder.append(edge.neighbor.getName()).append("(").append(edge.weight).append(")");
                    if (i < edges.size() - 1) {
                        builder.append(", ");
                    }
                }
                builder.append("]");
            }
            builder.append("\n");
        }
        return builder.toString();
    }

    private String formatRoommateAssignments() {
        StringBuilder builder = new StringBuilder();
        for (UniversityStudent student : currentStudents) {
            UniversityStudent roommate = student.getRoommate();
            builder.append(student.getName()).append(" -> ");
            builder.append(roommate == null ? "Unpaired" : roommate.getName());
            builder.append("\n");
        }
        return builder.toString();
    }

    private String formatPodAssignments() {
        StringBuilder builder = new StringBuilder();
        if (currentPods == null) {
            return builder.toString();
        }
        for (int i = 0; i < currentPods.size(); i++) {
            List<UniversityStudent> pod = currentPods.get(i);
            builder.append("Pod ").append(i).append(": ");
            if (pod.isEmpty()) {
                builder.append("None");
            } else {
                for (int j = 0; j < pod.size(); j++) {
                    builder.append(pod.get(j).getName());
                    if (j < pod.size() - 1) {
                        builder.append(", ");
                    }
                }
            }
            builder.append("\n");
        }
        return builder.toString();
    }

    private String formatInteractions() {
        StringBuilder builder = new StringBuilder();
        for (UniversityStudent student : currentStudents) {
            builder.append(student.getName()).append("\n");
            builder.append("  Friends: ");
            if (student.getFriends().isEmpty()) {
                builder.append("None\n");
            } else {
                builder.append(formatFriendList(student)).append("\n");
            }
            if (student.getFriends().isEmpty()) {
                builder.append("  Chats: None\n");
            } else {
                for (UniversityStudent friend : student.getFriends()) {
                    List<String> history = student.getChatHistoryWith(friend);
                    builder.append("  Chat with ").append(friend.getName()).append(": ");
                    if (history.isEmpty()) {
                        builder.append("None");
                    } else {
                        builder.append(String.join(" | ", history));
                    }
                    builder.append("\n");
                }
            }
            builder.append("\n");
        }
        return builder.toString();
    }

    private String formatFriendList(UniversityStudent student) {
        StringBuilder builder = new StringBuilder();
        int count = 0;
        for (UniversityStudent friend : student.getFriends()) {
            if (count++ > 0) {
                builder.append(", ");
            }
            builder.append(friend.getName());
        }
        return builder.toString();
    }

    private void updateReferralArea() {
        if (currentPathFinder == null || currentStudents == null) {
            referralArea.setText("");
            return;
        }
        String company = companyField.getText().trim();
        if (company.isEmpty()) {
            company = "DummyCompany";
        }
        StringBuilder builder = new StringBuilder();
        for (UniversityStudent student : currentStudents) {
            List<UniversityStudent> path = currentPathFinder.findReferralPath(student, company);
            builder.append(student.getName()).append(": ");
            if (path.isEmpty()) {
                builder.append("No referral path found.");
            } else {
                builder.append(formatPath(path));
            }
            builder.append("\n");
        }
        referralArea.setText(builder.toString());
    }

    private String formatPath(List<UniversityStudent> path) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < path.size(); i++) {
            builder.append(path.get(i).getName());
            if (i < path.size() - 1) {
                builder.append(" -> ");
            }
        }
        return builder.toString();
    }
}
