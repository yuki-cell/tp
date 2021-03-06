package seedu.bagel.logic.commands;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.bagel.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.bagel.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.bagel.logic.commands.CommandTestUtil.showFlashcardAtIndex;
import static seedu.bagel.testutil.TypicalFlashcards.getTypicalBagel;
import static seedu.bagel.testutil.TypicalIndexes.INDEX_FIRST_FLASHCARD;
import static seedu.bagel.testutil.TypicalIndexes.INDEX_SECOND_FLASHCARD;

import org.junit.jupiter.api.Test;

import seedu.bagel.commons.core.Messages;
import seedu.bagel.commons.core.index.Index;
import seedu.bagel.model.Model;
import seedu.bagel.model.ModelManager;
import seedu.bagel.model.UserPrefs;
import seedu.bagel.model.flashcard.Flashcard;

/**
 * Contains integration tests (interaction with the Model, UndoCommand and RedoCommand) and unit tests for
 * {@code DeleteCommand}.
 */
public class DeleteCommandTest {

    private Model model = new ModelManager(getTypicalBagel(), new UserPrefs());

    @Test
    public void execute_validIndexUnfilteredList_success() {
        Flashcard flashcardToDelete = model.getFilteredFlashcardList().get(INDEX_FIRST_FLASHCARD.getZeroBased());
        DeleteCommand deleteCommand = new DeleteCommand(INDEX_FIRST_FLASHCARD);

        String expectedMessage = String.format(DeleteCommand.MESSAGE_DELETE_FLASHCARD_SUCCESS, flashcardToDelete);

        ModelManager expectedModel = new ModelManager(model.getBagel(), new UserPrefs());
        expectedModel.deleteFlashcard(flashcardToDelete);

        assertCommandSuccess(deleteCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_invalidIndexUnfilteredList_throwsCommandException() {
        Index outOfBoundIndex = Index.fromOneBased(model.getFilteredFlashcardList().size() + 1);
        DeleteCommand deleteCommand = new DeleteCommand(outOfBoundIndex);

        assertCommandFailure(deleteCommand, model, Messages.MESSAGE_INVALID_FLASHCARD_DISPLAYED_INDEX);
    }

    @Test
    public void execute_validIndexFilteredList_success() {
        showFlashcardAtIndex(model, INDEX_FIRST_FLASHCARD);

        Flashcard flashcardToDelete = model.getFilteredFlashcardList().get(INDEX_FIRST_FLASHCARD.getZeroBased());
        DeleteCommand deleteCommand = new DeleteCommand(INDEX_FIRST_FLASHCARD);

        String expectedMessage = String.format(DeleteCommand.MESSAGE_DELETE_FLASHCARD_SUCCESS, flashcardToDelete);

        Model expectedModel = new ModelManager(model.getBagel(), new UserPrefs());
        expectedModel.deleteFlashcard(flashcardToDelete);
        showNoPerson(expectedModel);

        assertCommandSuccess(deleteCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_invalidIndexFilteredList_throwsCommandException() {
        showFlashcardAtIndex(model, INDEX_FIRST_FLASHCARD);

        Index outOfBoundIndex = INDEX_SECOND_FLASHCARD;
        // ensures that outOfBoundIndex is still in bounds of bagel (flashcards) list
        assertTrue(outOfBoundIndex.getZeroBased() < model.getBagel().getFlashcardList().size());

        DeleteCommand deleteCommand = new DeleteCommand(outOfBoundIndex);

        assertCommandFailure(deleteCommand, model, Messages.MESSAGE_INVALID_FLASHCARD_DISPLAYED_INDEX);
    }

    @Test
    public void equals() {
        DeleteCommand deleteFirstCommand = new DeleteCommand(INDEX_FIRST_FLASHCARD);
        DeleteCommand deleteSecondCommand = new DeleteCommand(INDEX_SECOND_FLASHCARD);

        // same object -> returns true
        assertTrue(deleteFirstCommand.equals(deleteFirstCommand));

        // same values -> returns true
        DeleteCommand deleteFirstCommandCopy = new DeleteCommand(INDEX_FIRST_FLASHCARD);
        assertTrue(deleteFirstCommand.equals(deleteFirstCommandCopy));

        // different types -> returns false
        assertFalse(deleteFirstCommand.equals(1));

        // null -> returns false
        assertFalse(deleteFirstCommand.equals(null));

        // different person -> returns false
        assertFalse(deleteFirstCommand.equals(deleteSecondCommand));
    }

    /**
     * Updates {@code model}'s filtered list to show no one.
     */
    private void showNoPerson(Model model) {
        model.updateFilteredFlashcardList(p -> false);

        assertTrue(model.getFilteredFlashcardList().isEmpty());
    }
}
