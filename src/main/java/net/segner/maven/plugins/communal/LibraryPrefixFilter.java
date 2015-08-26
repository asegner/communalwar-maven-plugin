package net.segner.maven.plugins.communal;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

public class LibraryPrefixFilter implements LibraryFilter {
    private String prefix;

    public LibraryPrefixFilter(String prefix) {
        Validate.notBlank(prefix);
        this.prefix = prefix;
    }

    public LibraryPrefixFilter() {
    }

    public void set(String prefix) {
        setPrefix(prefix);
    }

    public final String getPrefix() {
        return prefix;
    }

    public final void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public final boolean isMatch(String jarName) {
        Validate.notBlank(jarName);
        jarName = jarName.toLowerCase();
        String lowerPrefix = prefix.toLowerCase();
        return StringUtils.startsWith(jarName, lowerPrefix);
    }
}

