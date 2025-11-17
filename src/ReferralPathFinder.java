import java.util.*;

/**
 * Provides referral-path discovery logic using graph traversal algorithms
 * such as Dijkstra's to find routes to a desired internship company.
 */
public class ReferralPathFinder {

    private final StudentGraph graph;

    /**
     * Builds a path finder that operates on the supplied student graph.
     *
     * @param graph weighted graph that models student relationships
     */
    public ReferralPathFinder(StudentGraph graph) {
        this.graph = graph;
    }

    /**
     * Searches for the best referral path from a starting student to a company.
     *
     * @param start         student initiating the referral search
     * @param targetCompany company for which a referral chain is needed
     * @return ordered list of students that comprise the referral path; empty if none exists
     */
    public List<UniversityStudent> findReferralPath(UniversityStudent start, String targetCompany) {
        if (graph == null || start == null || targetCompany == null) {
            return Collections.emptyList();
        }
        String normalizedCompany = targetCompany.trim();
        if (normalizedCompany.isEmpty()) {
            return Collections.emptyList();
        }

        Map<UniversityStudent, Integer> distance = new HashMap<>();
        Map<UniversityStudent, UniversityStudent> previous = new HashMap<>();
        PriorityQueue<PathNode> queue = new PriorityQueue<>(Comparator.comparingInt(node -> node.distance));
        Set<UniversityStudent> visited = new HashSet<>();

        distance.put(start, 0);
        queue.offer(new PathNode(start, 0));

        while (!queue.isEmpty()) {
            PathNode node = queue.poll();
            UniversityStudent current = node.student;
            if (!visited.add(current)) {
                continue;
            }

            if (current.hasInternedAt(normalizedCompany)) {
                return reconstructPath(previous, current);
            }

            for (StudentGraph.Edge edge : graph.getNeighbors(current)) {
                UniversityStudent neighbor = edge.neighbor;
                int adjustedWeight = Math.max(1, 10 - edge.weight);
                int newDistance = distance.get(current) + adjustedWeight;
                if (newDistance < distance.getOrDefault(neighbor, Integer.MAX_VALUE)) {
                    distance.put(neighbor, newDistance);
                    previous.put(neighbor, current);
                    queue.offer(new PathNode(neighbor, newDistance));
                }
            }
        }

        return Collections.emptyList();
    }

    private List<UniversityStudent> reconstructPath(Map<UniversityStudent, UniversityStudent> previous,
                                                    UniversityStudent target) {
        LinkedList<UniversityStudent> path = new LinkedList<>();
        UniversityStudent current = target;
        while (current != null) {
            path.addFirst(current);
            current = previous.get(current);
        }
        return path;
    }

    private static class PathNode {
        private final UniversityStudent student;
        private final int distance;

        private PathNode(UniversityStudent student, int distance) {
            this.student = student;
            this.distance = distance;
        }
    }
}
