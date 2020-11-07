package seedu.bagel.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.bagel.commons.core.Messages.MESSAGE_INVALID_FLASHCARD_DISPLAYED_INDEX;
import static seedu.bagel.commons.core.Messages.MESSAGE_UNKNOWN_COMMAND;
import static seedu.bagel.logic.commands.CommandTestUtil.DESC_DESC_AMY;
import static seedu.bagel.logic.commands.CommandTestUtil.TITLE_DESC_AMY;
import static seedu.bagel.testutil.Assert.assertThrows;
import static seedu.bagel.testutil.TypicalFlashcards.AMY;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.bagel.logic.commands.AddCommand;
import seedu.bagel.logic.commands.CommandResult;
import seedu.bagel.logic.commands.FlipCommand;
// import seedu.bagel.logic.commands.ListCommand;
import seedu.bagel.logic.commands.exceptions.CommandException;
import seedu.bagel.logic.parser.exceptions.ParseException;
import seedu.bagel.model.Model;
import seedu.bagel.model.ModelManager;
import seedu.bagel.model.ReadOnlyBagel;
import seedu.bagel.model.UserPrefs;
import seedu.bagel.model.flashcard.Flashcard;
import seedu.bagel.storage.JsonBagelStorage;
import seedu.bagel.storage.JsonUserPrefsStorage;
import seedu.bagel.storage.StorageManager;
import seedu.bagel.testutil.FlashcardBuilder;

public class LogicManagerTest {
    private static final IOException DUMMY_IO_EXCEPTION = new IOException("dummy exception");

    @TempDir
    public Path temporaryFolder;

    private Model model = new ModelManager();
    private Logic logic;

    @BeforeEach
    public void setUp() {
        JsonBagelStorage bagelStorage =
                new JsonBagelStorage(temporaryFolder.resolve("bagel.json"));
        JsonUserPrefsStorage userPrefsStorage = new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json"));
        StorageManager storage = new StorageManager(bagelStorage, userPrefsStorage);
        logic = new LogicManager(model, storage);
    }

    @Test
    public void execute_invalidCommandFormat_throwsParseException() {
        String invalidCommand = "uicfhmowqewca";
        assertParseException(invalidCommand, MESSAGE_UNKNOWN_COMMAND);
    }

    @Test
    public void execute_commandExecutionError_throwsCommandException() {
        String deleteCommand = "delete 9";
        assertCommandException(deleteCommand, MESSAGE_INVALID_FLASHCARD_DISPLAYED_INDEX);
    }

    @Test
    public void execute_validCommand_success() throws Exception {
        // String listCommand = ListCommand.COMMAND_WORD;
        // assertCommandSuccess(listCommand, ListCommand.MESSAGE_SUCCESS, model);
        String flipCommand = FlipCommand.COMMAND_WORD;
        assertCommandSuccess(flipCommand, FlipCommand.MESSAGE_SUCCESS, model);
    }

    @Test
    public void execute_storageThrowsIoException_throwsCommandException() {
        // Setup LogicManager with JsonAddressBookIoExceptionThrowingStub
        JsonBagelStorage bagelStorage =
                new JsonBagelIoExceptionThrowingStub(temporaryFolder.resolve("ioExceptionBagel.json"));
        JsonUserPrefsStorage userPrefsStorage =
                new JsonUserPrefsStorage(temporaryFolder.resolve("ioExceptionUserPrefs.json"));
        StorageManager storage = new StorageManager(bagelStorage, userPrefsStorage);
        logic = new LogicManager(model, storage);

        // Execute add command
        String addCommand = AddCommand.COMMAND_WORD + TITLE_DESC_AMY + DESC_DESC_AMY;
        Flashcard expectedFlashcard = new FlashcardBuilder(AMY).withTags().build();
        ModelManager expectedModel = new ModelManager();
        expectedModel.addFlashcard(expectedFlashcard);
        String expectedMessage = LogicManager.FILE_OPS_ERROR_MESSAGE + DUMMY_IO_EXCEPTION;
        assertCommandFailure(addCommand, CommandException.class, expectedMessage, expectedModel);
    }

    @Test
    public void getFilteredPersonList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> logic.getFilteredFlashcardList().remove(0));
    }

    /**
     * Executes the command and confirms that
     * - no exceptions are thrown <br>
     * - the feedback message is equal to {@code expectedMessage} <br>
     * - the internal model manager state is the same as that in {@code expectedModel} <br>
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandSuccess(String inputCommand, String expectedMessage,
            Model expectedModel) throws CommandException, ParseException {
        CommandResult result = logic.execute(inputCommand);
        assertEquals(expectedMessage, result.getFeedbackToUser());
        assertEquals(expectedModel, model);
    }

    /**
     * Executes the command, confirms that a ParseException is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertParseException(String inputCommand, String expectedMessage) {
        assertCommandFailure(inputCommand, ParseException.class, expectedMessage);
    }

    /**
     * Executes the command, confirms that a CommandException is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandException(String inputCommand, String expectedMessage) {
        assertCommandFailure(inputCommand, CommandException.class, expectedMessage);
    }

    /**
     * Executes the command, confirms that the exception is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandFailure(String inputCommand, Class<? extends Throwable> expectedException,
            String expectedMessage) {
        Model expectedModel = new ModelManager(model.getBagel(), new UserPrefs());
        assertCommandFailure(inputCommand, expectedException, expectedMessage, expectedModel);
    }

    /**
     * Executes the command and confirms that
     * - the {@code expectedException} is thrown <br>
     * - the resulting error message is equal to {@code expectedMessage} <br>
     * - the internal model manager state is the same as that in {@code expectedModel} <br>
     * @see #assertCommandSuccess(String, String, Model)
     */
    private void assertCommandFailure(String inputCommand, Class<? extends Throwable> expectedException,
            String expectedMessage, Model expectedModel) {
        assertThrows(expectedException, expectedMessage, () -> logic.execute(inputCommand));
        assertEquals(expectedModel, model);
    }

    /**
     * A stub class to throw an {@code IOException} when the save method is called.
     */
    private static class JsonBagelIoExceptionThrowingStub extends JsonBagelStorage {
        private JsonBagelIoExceptionThrowingStub(Path filePath) {
            super(filePath);
        }

        @Override
        public void saveBagel(ReadOnlyBagel bagel, Path filePath) throws IOException {
            throw DUMMY_IO_EXCEPTION;
        }
    }
}
