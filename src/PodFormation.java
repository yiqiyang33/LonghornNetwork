import java.util.*;

/**
 * Coordinates the grouping of students into pods based on connection strength
 * or other criteria derived from the {@link StudentGraph}.
 */
public class PodFormation {

    private final StudentGraph graph;
    private List<List<UniversityStudent>> latestPods = new ArrayList<>();

    /**
     * Creates a pod formation helper for the provided graph of students.
     *
     * @param graph weighted graph describing student relationships
     */
    public PodFormation(StudentGraph graph) {
        this.graph = graph;
    }

    /**
     * Forms pods of the requested size using the graph's structure.
     *
     * @param podSize target number of students per pod
     */
    public void formPods(int podSize) {
        if (graph == null || podSize <= 0) {
            return;
        }
        Set<UniversityStudent> visited = new LinkedHashSet<>();
        List<List<UniversityStudent>> pods = new ArrayList<>();
        for (UniversityStudent student : graph.getAllNodes()) {
            if (visited.contains(student)) {
                continue;
            }
            pods.add(buildPod(student, podSize, visited));
        }
        latestPods = pods;
        printPods(pods);
    }

    /**
     * Returns an immutable view of the most recently calculated pods.
     *
     * @return pods list
     */
    public List<List<UniversityStudent>> getLatestPods() {
        List<List<UniversityStudent>> snapshot = new ArrayList<>();
        for (List<UniversityStudent> pod : latestPods) {
            snapshot.add(Collections.unmodifiableList(new ArrayList<>(pod)));
        }
        return Collections.unmodifiableList(snapshot);
    }

    private List<UniversityStudent> buildPod(UniversityStudent start,
                                             int podSize,
                                             Set<UniversityStudent> visited) {
        List<UniversityStudent> pod = new ArrayList<>();
        Comparator<StudentGraph.Edge> comparator = Comparator
                .comparingInt((StudentGraph.Edge edge) -> edge.weight)
                .reversed()
                .thenComparing(edge -> edge.neighbor.getName());
        PriorityQueue<StudentGraph.Edge> queue = new PriorityQueue<>(comparator);

        visited.add(start);
        pod.add(start);
        for (StudentGraph.Edge edge : graph.getNeighbors(start)) {
            if (!visited.contains(edge.neighbor)) {
                queue.offer(edge);
            }
        }

        while (pod.size() < podSize && !queue.isEmpty()) {
            StudentGraph.Edge edge = queue.poll();
            UniversityStudent neighbor = edge.neighbor;
            if (visited.contains(neighbor)) {
                continue;
            }
            visited.add(neighbor);
            pod.add(neighbor);
            for (StudentGraph.Edge next : graph.getNeighbors(neighbor)) {
                if (!visited.contains(next.neighbor)) {
                    queue.offer(next);
                }
            }
        }

        return pod;
    }

    private void printPods(List<List<UniversityStudent>> pods) {
        System.out.println("Pod Assignments:");
        for (int i = 0; i < pods.size(); i++) {
            List<UniversityStudent> pod = pods.get(i);
            StringBuilder builder = new StringBuilder();
            for (UniversityStudent student : pod) {
                if (builder.length() > 0) {
                    builder.append(", ");
                }
                builder.append(student.getName());
            }
            System.out.println("  Pod " + i + ": " + builder);
        }
    }
}
