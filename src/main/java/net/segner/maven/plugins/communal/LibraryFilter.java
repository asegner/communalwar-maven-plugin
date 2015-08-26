package net.segner.maven.plugins.communal;

/**
 * @author aaronsegner
 */
public interface LibraryFilter {

    String getPrefix();

    boolean isMatch(String libraryName);
}
