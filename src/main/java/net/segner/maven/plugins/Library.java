package net.segner.maven.plugins;

import org.apache.commons.lang3.StringUtils;

/**
 * @author aaronsegner
 */
public class Library {

    private String prefix;

    public Library() {
    }

    public Library(String prefix) {
        setPrefix(prefix);
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public boolean isMatch(String jarName) {
        jarName = jarName.toLowerCase();
        String lowerPrefix = prefix.toLowerCase();
        return StringUtils.startsWith(jarName, lowerPrefix);
    }
}

