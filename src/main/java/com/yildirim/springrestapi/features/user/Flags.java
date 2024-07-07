package com.yildirim.springrestapi.features.user;

import java.util.EnumSet;

public class Flags {
    public static <T extends Enum<T>> boolean isFlagSet(int flags, Enum<T> candidateFlag) {
        return (flags & (1 << candidateFlag.ordinal())) != 0;
    }

    public static <T extends Enum<T>> boolean isFlagSet(EnumSet<T> flags, T candidateFlag) {
        return flags.contains(candidateFlag);
    }

    public static <T extends Enum<T>> int setFlag(int flags, Enum<T> flag) {
        return flags | (1 << flag.ordinal());
    }

    public static <T extends Enum<T>> EnumSet<T> setFlag(EnumSet<T> flags, T flag) {
        flags.add(flag);
        return flags;
    }

    public static <T extends Enum<T>> int unsetFlag(int flags, Enum<T> flag) {
        return flags & ~(1 << flag.ordinal());
    }

    public static <T extends Enum<T>> EnumSet<T> unsetFlag(EnumSet<T> flags, T flag) {
        flags.remove(flag);
        return flags;
    }

    public enum UserFlag {
        EMAIL_VERIFIED,
        EMAIL_UPDATED,
        DISABLED,
        DELETED,
        PASSWORD_RESET,
        USERNAME_UPDATED,
    }
}
