import java.util.*;

/**
 * Concrete representation of a student at the university that will
 * participate in roommate matching, pod formation, and referral searches.
 * The implementation encapsulates all behavior specific to a Longhorn
 * Network student such as matching logic and graph attributes.
 */
public class UniversityStudent extends Student {
    private UniversityStudent roommate;
    private final Set<UniversityStudent> friends = new LinkedHashSet<>();
    private final Map<UniversityStudent, List<String>> chatHistory = new HashMap<>();
    private final Map<String, Integer> preferenceRanks = new HashMap<>();
    private final Object friendLock = new Object();
    private final Object chatLock = new Object();

    /**
     * Creates a new student instance with the supplied profile data.
     *
     * @param name                 student's name
     * @param age                  student's age
     * @param gender               student's gender
     * @param year                 academic year
     * @param major                academic major
     * @param gpa                  grade point average
     * @param roommatePreferences  ordered roommate preferences (may be empty)
     * @param previousInternships  internship history (may be empty)
     */
    public UniversityStudent(String name,
                             int age,
                             String gender,
                             int year,
                             String major,
                             double gpa,
                             List<String> roommatePreferences,
                             List<String> previousInternships) {
        this.name = Objects.requireNonNull(name, "name");
        this.age = age;
        this.gender = Objects.requireNonNullElse(gender, "");
        this.year = year;
        this.major = Objects.requireNonNullElse(major, "");
        this.gpa = gpa;
        this.roommatePreferences = sanitizeList(roommatePreferences, false);
        this.previousInternships = sanitizeList(previousInternships, false);
        for (int i = 0; i < this.roommatePreferences.size(); i++) {
            preferenceRanks.put(this.roommatePreferences.get(i).toLowerCase(Locale.ROOT), i);
        }
    }

    private static List<String> sanitizeList(List<String> source, boolean keepNoneLiteral) {
        List<String> cleaned = new ArrayList<>();
        if (source != null) {
            for (String entry : source) {
                if (entry == null) {
                    continue;
                }
                String trimmed = entry.trim();
                if (trimmed.isEmpty()) {
                    continue;
                }
                if (!keepNoneLiteral && trimmed.equalsIgnoreCase("None")) {
                    continue;
                }
                cleaned.add(trimmed);
            }
        }
        return Collections.unmodifiableList(cleaned);
    }

    /**
     * Returns the student's name.
     *
     * @return student name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the student's age.
     *
     * @return age in years
     */
    public int getAge() {
        return age;
    }

    /**
     * Returns the student's gender.
     *
     * @return gender string
     */
    public String getGender() {
        return gender;
    }

    /**
     * Returns the student's academic year.
     *
     * @return academic year
     */
    public int getYear() {
        return year;
    }

    /**
     * Returns the student's major.
     *
     * @return major name
     */
    public String getMajor() {
        return major;
    }

    /**
     * Returns the student's GPA.
     *
     * @return GPA
     */
    public double getGpa() {
        return gpa;
    }

    /**
     * Returns an immutable view of the roommate preference list.
     *
     * @return roommate preferences
     */
    public List<String> getRoommatePreferences() {
        return roommatePreferences;
    }

    /**
     * Returns an immutable view of the previous internships list.
     *
     * @return previous internships
     */
    public List<String> getPreviousInternships() {
        return previousInternships;
    }

    /**
     * Returns the student's current roommate, if any.
     *
     * @return roommate or {@code null}
     */
    public synchronized UniversityStudent getRoommate() {
        return roommate;
    }

    /**
     * Assigns a roommate to this student.
     *
     * @param roommate roommate to assign
     */
    public synchronized void setRoommate(UniversityStudent roommate) {
        this.roommate = roommate;
    }

    /**
     * Removes the current roommate assignment.
     */
    public synchronized void clearRoommate() {
        this.roommate = null;
    }

    /**
     * Returns an immutable snapshot of the student's friends.
     *
     * @return friends set
     */
    public Set<UniversityStudent> getFriends() {
        synchronized (friendLock) {
            return Collections.unmodifiableSet(new LinkedHashSet<>(friends));
        }
    }

    /**
     * Establishes a friendship between this student and another.
     *
     * @param other student to befriend
     */
    public void establishFriendship(UniversityStudent other) {
        if (other == null || other == this) {
            return;
        }
        UniversityStudent first = this.name.compareTo(other.name) <= 0 ? this : other;
        UniversityStudent second = first == this ? other : this;
        synchronized (first.friendLock) {
            synchronized (second.friendLock) {
                this.friends.add(other);
                other.friends.add(this);
            }
        }
    }

    /**
     * Stores a chat message exchanged with another student.
     *
     * @param other   other conversation participant
     * @param message text of the message
     */
    public void addChatMessage(UniversityStudent other, String message) {
        if (other == null || message == null) {
            return;
        }
        synchronized (chatLock) {
            chatHistory.computeIfAbsent(other, key -> new ArrayList<>()).add(message);
        }
    }

    /**
     * Returns chat history with a specific student.
     *
     * @param other other participant
     * @return immutable view of the chat transcript (empty list if none)
     */
    public List<String> getChatHistoryWith(UniversityStudent other) {
        if (other == null) {
            return Collections.emptyList();
        }
        synchronized (chatLock) {
            List<String> history = chatHistory.get(other);
            return history == null ? Collections.emptyList() : Collections.unmodifiableList(new ArrayList<>(history));
        }
    }

    /**
     * Indicates whether this student has interned at the specified company.
     *
     * @param company company name to check
     * @return {@code true} if found in the internship list
     */
    public boolean hasInternedAt(String company) {
        if (company == null || company.isEmpty()) {
            return false;
        }
        for (String internship : previousInternships) {
            if (internship.equalsIgnoreCase(company)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the preference rank (zero-indexed) for a potential roommate.
     *
     * @param candidateName candidate's name
     * @return rank or {@code Integer.MAX_VALUE} if not listed
     */
    public int getPreferenceRank(String candidateName) {
        if (candidateName == null) {
            return Integer.MAX_VALUE;
        }
        return preferenceRanks.getOrDefault(candidateName.toLowerCase(Locale.ROOT), Integer.MAX_VALUE);
    }

    /**
     * Calculates the connection strength between two students.
     *
     * @param other another student whose compatibility is being measured
     * @return a non-negative score that reflects how strong the connection is
     */
    @Override
    public int calculateConnectionStrength(Student other) {
        if (!(other instanceof UniversityStudent)) {
            return 0;
        }
        UniversityStudent target = (UniversityStudent) other;
        int strength = 0;
        if (getRoommate() != null && getRoommate().equals(target)) {
            strength += 4;
        }

        Set<String> thisInternships = new HashSet<>();
        for (String internship : previousInternships) {
            thisInternships.add(internship.toLowerCase(Locale.ROOT));
        }
        Set<String> otherInternships = new HashSet<>();
        for (String internship : target.previousInternships) {
            otherInternships.add(internship.toLowerCase(Locale.ROOT));
        }
        thisInternships.retainAll(otherInternships);
        strength += 3 * thisInternships.size();

        if (!major.isEmpty() && major.equalsIgnoreCase(target.major)) {
            strength += 2;
        }
        if (age == target.age && age != 0) {
            strength += 1;
        }
        return strength;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof UniversityStudent)) {
            return false;
        }
        UniversityStudent that = (UniversityStudent) o;
        return name.equalsIgnoreCase(that.name);
    }

    @Override
    public int hashCode() {
        return name.toLowerCase(Locale.ROOT).hashCode();
    }

    @Override
    public String toString() {
        return "UniversityStudent{" +
                "name='" + name + '\'' +
                ", age=" + age +
                ", gender='" + gender + '\'' +
                ", year=" + year +
                ", major='" + major + '\'' +
                ", GPA=" + gpa +
                ", roommatePreferences=" + roommatePreferences +
                ", previousInternships=" + previousInternships +
                '}';
    }
}
