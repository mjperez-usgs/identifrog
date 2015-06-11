package gov.usgs.identifrog.interfaces;

/**
 * Implementing this interface allows other code to query if the object implementing this interface is currently in use somewhere else, for example, in the database, and should not allow edits.
 * @author mjperez
 *
 */
public interface InUseFlag {

	public boolean isInUse();
}
