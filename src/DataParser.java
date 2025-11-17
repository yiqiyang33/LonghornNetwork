import java.io.*;
import java.util.*;

/**
 * Utility class responsible for translating raw input files into
 * {@link UniversityStudent} objects that the rest of the system can use.
 */
public class DataParser {

    private static final List<String> REQUIRED_FIELDS = Arrays.asList(
            "Name",
            "Age",
            "Gender",
            "Year",
            "Major",
            "GPA",
            "RoommatePreferences",
            "PreviousInternships"
    );
    private static final Set<String> OPTIONAL_LIST_FIELDS = new HashSet<>(Arrays.asList(
            "RoommatePreferences",
            "PreviousInternships"
    ));

    /**
     * Reads the provided file and builds a list of {@link UniversityStudent} objects.
     *
     * @param filename path to the input data file
     * @return the collection of students extracted from the file, never {@code null}
     * @throws IOException if the file cannot be read or parsed
     */
    public static List<UniversityStudent> parseStudents(String filename) throws IOException {
        List<UniversityStudent> students = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            Map<String, String> currentFields = new LinkedHashMap<>();
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                if (line.equalsIgnoreCase("Student:")) {
                    if (!currentFields.isEmpty()) {
                        students.add(buildStudent(currentFields));
                        currentFields.clear();
                    }
                    continue;
                }

                int colonIndex = line.indexOf(':');
                if (colonIndex == -1) {
                    String expectedKey = deriveExpectedKey(line);
                    throw new IOException("Parsing error: Incorrect format in line: '" + line
                            + "'. Expected format '" + expectedKey + ": <value>'.");
                }

                String key = line.substring(0, colonIndex).trim();
                String value = line.substring(colonIndex + 1).trim();
                currentFields.put(key, value);
            }

            if (!currentFields.isEmpty()) {
                students.add(buildStudent(currentFields));
            }
        }
        return students;
    }

    private static UniversityStudent buildStudent(Map<String, String> fields) throws IOException {
        String studentIdentifier = normalizeName(fields.get("Name"));
        for (String required : REQUIRED_FIELDS) {
            if (!fields.containsKey(required)) {
                throw new IOException("Parsing error: Missing required field '" + required
                        + "' in student entry for " + (studentIdentifier == null ? "Unknown" : studentIdentifier) + ".");
            }
            if (OPTIONAL_LIST_FIELDS.contains(required)) {
                continue;
            }
            if (fields.get(required).trim().isEmpty()) {
                throw new IOException("Parsing error: Missing required field '" + required
                        + "' in student entry for " + (studentIdentifier == null ? "Unknown" : studentIdentifier) + ".");
            }
        }

        String name = Objects.requireNonNull(studentIdentifier, "Name cannot be null");
        int age = parseIntField(fields.get("Age"), "Age", name);
        int year = parseIntField(fields.get("Year"), "Year", name);
        double gpa = parseDoubleField(fields.get("GPA"), "GPA", name);
        String gender = fields.get("Gender").trim();
        String major = fields.get("Major").trim();
        List<String> roommates = parseList(fields.get("RoommatePreferences"));
        List<String> internships = parseList(fields.get("PreviousInternships"));

        return new UniversityStudent(name, age, gender, year, major, gpa, roommates, internships);
    }

    private static String normalizeName(String raw) throws IOException {
        if (raw == null) {
            return null;
        }
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) {
            throw new IOException("Parsing error: Missing required field 'Name' in student entry for Unknown.");
        }
        return trimmed;
    }

    private static int parseIntField(String rawValue, String field, String studentName) throws IOException {
        try {
            return Integer.parseInt(rawValue.trim());
        } catch (NumberFormatException ex) {
            throw new IOException("Number format error: Invalid number format for "
                    + formatFieldLabel(field) + ": '" + rawValue + "' in student entry for " + studentName + ".");
        }
    }

    private static double parseDoubleField(String rawValue, String field, String studentName) throws IOException {
        try {
            return Double.parseDouble(rawValue.trim());
        } catch (NumberFormatException ex) {
            throw new IOException("Number format error: Invalid number format for "
                    + formatFieldLabel(field) + ": '" + rawValue + "' in student entry for " + studentName + ".");
        }
    }

    private static String formatFieldLabel(String key) {
        return "GPA".equalsIgnoreCase(key) ? "GPA" : key.toLowerCase(Locale.ROOT);
    }

    private static List<String> parseList(String raw) {
        if (raw == null || raw.trim().isEmpty() || raw.trim().equalsIgnoreCase("None")) {
            return Collections.emptyList();
        }
        String[] parts = raw.split(",");
        List<String> values = new ArrayList<>();
        for (String part : parts) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty() && !trimmed.equalsIgnoreCase("None")) {
                values.add(trimmed);
            }
        }
        return values;
    }

    private static String deriveExpectedKey(String line) {
        if (line.isEmpty()) {
            return "Field";
        }
        int spaceIndex = line.indexOf(' ');
        if (spaceIndex == -1) {
            return line;
        }
        return line.substring(0, spaceIndex);
    }
}
