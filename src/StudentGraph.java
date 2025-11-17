import java.util.*;

/**
 * Weighted, undirected graph that models connection strengths between students.
 */
public class StudentGraph {
    private final Map<UniversityStudent, List<Edge>> adjacencyList = new LinkedHashMap<>();
    private final Map<String, UniversityStudent> studentsByName = new HashMap<>();

    /**
     * Constructs a graph from the provided students using their calculated connection strengths.
     *
     * @param students students to include as graph nodes
     */
    public StudentGraph(List<UniversityStudent> students) {
        if (students == null) {
            return;
        }
        for (UniversityStudent student : students) {
            adjacencyList.put(student, new ArrayList<>());
            studentsByName.put(student.getName().toLowerCase(Locale.ROOT), student);
        }
        for (int i = 0; i < students.size(); i++) {
            for (int j = i + 1; j < students.size(); j++) {
                UniversityStudent a = students.get(i);
                UniversityStudent b = students.get(j);
                int weight = a.calculateConnectionStrength(b);
                if (weight > 0) {
                    addEdge(a, b, weight);
                }
            }
        }
    }

    /**
     * Adds a new undirected edge between two students.
     *
     * @param from   first student
     * @param to     second student
     * @param weight connection strength
     */
    public void addEdge(UniversityStudent from, UniversityStudent to, int weight) {
        if (from == null || to == null || weight <= 0) {
            return;
        }
        upsertEdge(from, to, weight);
        upsertEdge(to, from, weight);
        studentsByName.putIfAbsent(from.getName().toLowerCase(Locale.ROOT), from);
        studentsByName.putIfAbsent(to.getName().toLowerCase(Locale.ROOT), to);
    }

    private void upsertEdge(UniversityStudent from, UniversityStudent to, int weight) {
        List<Edge> edges = adjacencyList.computeIfAbsent(from, key -> new ArrayList<>());
        ListIterator<Edge> iterator = edges.listIterator();
        while (iterator.hasNext()) {
            Edge edge = iterator.next();
            if (edge.neighbor.equals(to)) {
                iterator.set(new Edge(to, weight));
                return;
            }
        }
        edges.add(new Edge(to, weight));
    }

    /**
     * Returns an immutable list of neighbors for the provided student.
     *
     * @param student student to inspect
     * @return neighbors list
     */
    public List<Edge> getNeighbors(UniversityStudent student) {
        List<Edge> neighbors = adjacencyList.get(student);
        if (neighbors == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(neighbors);
    }

    /**
     * Returns all registered students (graph nodes).
     *
     * @return list of students
     */
    public List<UniversityStudent> getAllNodes() {
        return new ArrayList<>(adjacencyList.keySet());
    }

    /**
     * Finds a student node by name.
     *
     * @param name student's name
     * @return matching student or {@code null}
     */
    public UniversityStudent getStudent(String name) {
        if (name == null) {
            return null;
        }
        return studentsByName.get(name.toLowerCase(Locale.ROOT));
    }

    /**
     * Displays the adjacency list representation for debugging or grading output.
     */
    public void displayGraph() {
        for (Map.Entry<UniversityStudent, List<Edge>> entry : adjacencyList.entrySet()) {
            StringBuilder builder = new StringBuilder();
            for (Edge edge : entry.getValue()) {
                if (builder.length() > 0) {
                    builder.append(", ");
                }
                builder.append(edge.neighbor.getName()).append("(").append(edge.weight).append(")");
            }
            System.out.println(entry.getKey().getName() + " -> [" + builder + "]");
        }
    }

    /**
     * Graph edge representation.
     */
    public static class Edge {
        public final UniversityStudent neighbor;
        public final int weight;

        public Edge(UniversityStudent neighbor, int weight) {
            this.neighbor = neighbor;
            this.weight = weight;
        }
    }
}
