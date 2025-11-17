import java.util.*;

/**
 * Implements the Gale-Shapley stable matching algorithm to pair students
 * with compatible roommates based on their ranked preferences.
 */
public class GaleShapley {

    /**
     * Executes the roommate assignment process for the provided students.
     *
     * @param students pool of students participating in pairing
     */
    public static void assignRoommates(List<UniversityStudent> students) {
        if (students == null || students.isEmpty()) {
            return;
        }
        Map<String, UniversityStudent> nameDirectory = new HashMap<>();
        Map<UniversityStudent, Integer> nextProposalIndex = new HashMap<>();
        Queue<UniversityStudent> freeStudents = new ArrayDeque<>();

        for (UniversityStudent student : students) {
            student.clearRoommate();
            nameDirectory.put(student.getName().toLowerCase(Locale.ROOT), student);
            nextProposalIndex.put(student, 0);
            if (!student.getRoommatePreferences().isEmpty()) {
                freeStudents.offer(student);
            }
        }

        while (!freeStudents.isEmpty()) {
            UniversityStudent proposer = freeStudents.poll();
            if (proposer.getRoommate() != null) {
                continue;
            }
            List<String> preferences = proposer.getRoommatePreferences();
            boolean paired = false;

            while (nextProposalIndex.get(proposer) < preferences.size()) {
                int prefIndex = nextProposalIndex.get(proposer);
                String candidateName = preferences.get(prefIndex);
                nextProposalIndex.put(proposer, prefIndex + 1);
                UniversityStudent candidate = nameDirectory.get(candidateName.toLowerCase(Locale.ROOT));
                if (candidate == null || candidate.getRoommatePreferences().isEmpty()) {
                    continue;
                }

                int candidateRank = candidate.getPreferenceRank(proposer.getName());
                if (candidateRank == Integer.MAX_VALUE) {
                    continue;
                }

                UniversityStudent currentRoommate = candidate.getRoommate();
                if (currentRoommate == null) {
                    pair(proposer, candidate);
                    paired = true;
                    break;
                }

                int currentRank = candidate.getPreferenceRank(currentRoommate.getName());
                if (candidateRank < currentRank) {
                    currentRoommate.clearRoommate();
                    if (!currentRoommate.getRoommatePreferences().isEmpty()) {
                        freeStudents.offer(currentRoommate);
                    }
                    pair(proposer, candidate);
                    paired = true;
                    break;
                }
            }
        }
    }

    private static void pair(UniversityStudent a, UniversityStudent b) {
        a.setRoommate(b);
        b.setRoommate(a);
    }
}
