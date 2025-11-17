import java.util.*;

/**
 * Base abstraction for every Longhorn Network participant.
 * Concrete subclasses provide the specific attributes and logic
 * required to compare, match, and connect students.
 */
public abstract class Student {
    protected String name;
    protected int age;
    protected String gender;
    protected int year;
    protected String major;
    protected double gpa;
    protected List<String> roommatePreferences;
    protected List<String> previousInternships;

    /**
     * Calculates the connection strength between this student and another student.
     *
     * @param other another student whose compatibility is being measured
     * @return a non-negative score that reflects how strong the connection is
     */
    public abstract int calculateConnectionStrength(Student other);
}
