package com.lyricfind.dpgscript.annotations;

/**
 * BaseProcess.setup() parameter that defines a unique variable in the execution of a Process.
 *
 * Normally more than one instance of a certain Process cannot be run simultaneously. Imagine the set of @Unique
 * parameters in a Process' controller act as the members of a UNIQUE KEY in SQL:
 *
 * Any number of the given processes can now run, as long as they all have unique combinations of @Unique parameters.
 */
public @interface Unique {
}
