import java.util.*;

/**
 * Provides referral-path discovery logic using graph traversal algorithms
 * such as Dijkstra's to find routes to a desired internship company.
 */
public class ReferralPathFinder {

    /**
     * Builds a path finder that operates on the supplied student graph.
     *
     * @param graph weighted graph that models student relationships
     */
    public ReferralPathFinder(StudentGraph graph) {
        // Constructor
    }

    /**
     * Searches for the best referral path from a starting student to a company.
     *
     * @param start         student initiating the referral search
     * @param targetCompany company for which a referral chain is needed
     * @return ordered list of students that comprise the referral path; empty if none exists
     */
    public List<UniversityStudent> findReferralPath(UniversityStudent start, String targetCompany) {
        // Method signature only
        return new ArrayList<>();
    }
}
