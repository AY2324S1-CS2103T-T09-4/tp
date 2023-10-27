package seedu.address.model;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.node.ObjectNode;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.storage.JsonSerializableAddressBook;

/**
 * Manages all address books in the application.
 */
@JsonSerialize(using = AddressBookManager.AddressBookManagerSerializer.class)
@JsonDeserialize(using = AddressBookManager.AddressBookManagerDeserializer.class)
public class AddressBookManager implements ReadOnlyAddressBookManager {
    private HashMap<String, ReadOnlyAddressBook> addressBooks;
    private AddressBook currentAddressBook;
    private String currentCourseCode;

    /**
     * Creates an empty address book manager.
     */
    public AddressBookManager() {
        this.addressBooks = new HashMap<>();
        this.currentCourseCode = "Temp";
        setAddressBook(new AddressBook("Temp"));
    }

    /**
     * Creates an address book manager with the given read only addressBookManager.
     */
    public AddressBookManager(ReadOnlyAddressBookManager addressBookManager) {
        this(addressBookManager.getAddressBooks(), addressBookManager.getActiveCourseCode());
    }

    /**
     * Creates an address book manager with the given address books and active address book course code.
     */
    public AddressBookManager(HashMap<String, ReadOnlyAddressBook> addressBooks, String currentCourseCode) {
        this.addressBooks = addressBooks;
        this.currentCourseCode = currentCourseCode.toUpperCase();
        this.currentAddressBook = getActiveAddressBook();
    }

    public AddressBook getActiveAddressBook() {
        if (this.currentAddressBook != null) {
            return this.currentAddressBook;
        }

        return addressBooks.containsKey(currentCourseCode)
                ? (AddressBook) addressBooks.get(currentCourseCode)
                : new AddressBook(currentCourseCode);
    }

    public void setAddressBook(ReadOnlyAddressBook addressBook) {
        requireNonNull(addressBook);

        removeAddressBook(currentCourseCode);
        addAddressBook(addressBook);
        this.currentCourseCode = addressBook.getCourseCode();
        setActiveAddressBook(currentCourseCode);
    }

    public void setActiveAddressBook(String courseCode) {
        requireNonNull(courseCode);
        this.currentCourseCode = courseCode.toUpperCase();

        if (!this.addressBooks.containsKey(currentCourseCode)) {
            throw new IllegalArgumentException("Address book does not exist");
        }

        this.currentAddressBook = (AddressBook) this.addressBooks.get(currentCourseCode);
    }

    /**
     * Returns true if an address book with the given {@code courseCode} exists.
     */
    public boolean hasAddressBook(String courseCode) {
        requireNonNull(courseCode);
        return this.addressBooks.containsKey(courseCode.toUpperCase());
    }

    /**
     * Adds an address book to the address book manager.
     * {@code addressBook} must not already exist in the address book manager.
     */
    public void addAddressBook(ReadOnlyAddressBook addressBook) {
        requireAllNonNull(addressBook);
        this.addressBooks.put(addressBook.getCourseCode().toUpperCase(), addressBook);
    }

    /**
     * Deletes an address book from the address book manager.
     * {@code courseCode} must exist in the address book manager.
     */
    public void removeAddressBook(String courseCode) {
        requireNonNull(courseCode);
        this.addressBooks.remove(courseCode.toUpperCase());
    }

    @Override
    public HashMap<String, ReadOnlyAddressBook> getAddressBooks() {
        HashMap<String, ReadOnlyAddressBook> copiedAddressBooks = new HashMap<>();
        addressBooks.forEach((courseCode, addressBook) ->
                copiedAddressBooks.put(courseCode, new AddressBook(courseCode, currentAddressBook)));
        return copiedAddressBooks;
    }

    @Override
    public String getActiveCourseCode() {
        return currentCourseCode;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof AddressBookManager)) {
            return false;
        }

        AddressBookManager otherModelManager = (AddressBookManager) other;
        return addressBooks.equals(otherModelManager.addressBooks)
                && currentAddressBook.equals(otherModelManager.currentAddressBook)
                && currentCourseCode.equalsIgnoreCase(otherModelManager.currentCourseCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(addressBooks, currentAddressBook, currentCourseCode);
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append("Address books: ")
                .append(addressBooks)
                .append("\n")
                .append("Current address book: ")
                .append(currentAddressBook)
                .append("\n")
                .append("Current course code: ")
                .append(currentCourseCode)
                .toString();
    }

    /**
     * Serializes an {@code AddressBookManager} into JSON format.
     */
    public static class AddressBookManagerSerializer extends JsonSerializer<AddressBookManager> {
        @Override
        public void serialize(AddressBookManager addressBookManager, JsonGenerator jsonGenerator,
                SerializerProvider serializerProvider) throws IOException, JsonProcessingException {
            jsonGenerator.writeStartObject();
            jsonGenerator.writeStringField("currentCourseCode", addressBookManager.currentCourseCode);
            jsonGenerator.writeObjectFieldStart("addressBooks");

            for (HashMap.Entry<String, ReadOnlyAddressBook> entry : addressBookManager.addressBooks.entrySet()) {
                JsonSerializableAddressBook jsonSerializableAddressBook =
                        new JsonSerializableAddressBook(entry.getValue());

                jsonGenerator.writeObjectField(entry.getKey(), jsonSerializableAddressBook);
            }

            jsonGenerator.writeEndObject();
            jsonGenerator.writeEndObject();
        }
    }

    /**
     * Deserializes a JSON object into an {@code AddressBookManager}.
     */
    public static class AddressBookManagerDeserializer extends JsonDeserializer<AddressBookManager> {
        private ObjectMapper mapper = new ObjectMapper();

        @Override
        public AddressBookManager deserialize(JsonParser jsonParser,
                DeserializationContext deserializationContext) throws IOException {
            JsonNode node = jsonParser.getCodec().readTree(jsonParser);
            String currentCourseCode = node.get("currentCourseCode").asText();
            JsonNode addressBooksNode = node.get("addressBooks");
            HashMap<String, ReadOnlyAddressBook> addressBooks = new HashMap<>();

            if (addressBooksNode.isObject()) {
                addressBooksNode.fields().forEachRemaining(entry -> {
                    String courseCode = entry.getKey();
                    JsonNode addressBookNode = entry.getValue();

                    try {
                        addressBookNode = addCourseCodeToJsonNode(addressBookNode, courseCode);
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    }

                    JsonSerializableAddressBook addressBook = null;
                    try {
                        addressBook = mapper.treeToValue(addressBookNode, JsonSerializableAddressBook.class);
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }

                    if (addressBook != null) {
                        try {
                            addressBooks.put(courseCode, addressBook.toModelType());
                        } catch (IllegalValueException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }

            return new AddressBookManager(addressBooks, currentCourseCode);
        }

        private static JsonNode addCourseCodeToJsonNode(JsonNode json, String courseCode)
                throws IllegalArgumentException {
            if (json.isObject()) {
                ObjectNode objectNode = (ObjectNode) json;
                objectNode.put("courseCode", courseCode);
                return objectNode;
            }

            throw new IllegalArgumentException("Input JSON is not an object.");
        }
    }
}
