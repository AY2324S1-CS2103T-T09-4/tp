package seedu.address.model.predicate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.model.tag.Tag;

/**
 * Tests that a {@code Person}'s {@code Tag} matches the tag given.
 */
public class ContainsTagPredicate extends SerializablePredicate {
    private final Tag tag;


    /**
     * Constructor a {@code ContainsTagPredicate} with a tag.
     *
     * @param tag Tag to check for in a person.
     */
    public ContainsTagPredicate(Tag tag) {
        super(person -> person.getTags().stream().anyMatch(
                personTag -> tag.getTutorialGroup() == null
                        ? personTag.getCourseCode().equals(tag.getCourseCode())
                        : personTag.getTagName().equals(tag.getTagName())));
        this.tag = tag;
    }

    @JsonCreator
    public static ContainsTagPredicate create(@JsonProperty("tag") Tag tag) {
        return new ContainsTagPredicate(tag);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof ContainsTagPredicate)) {
            return false;
        }

        ContainsTagPredicate otherPredicate = (ContainsTagPredicate) other;
        return tag.equals(otherPredicate.tag);
    }

    @Override
    public int hashCode() {
        return tag.hashCode();
    }

    @Override
    public String toString() {
        return "Tag Filter: " + tag.getTagName();
    }
}
