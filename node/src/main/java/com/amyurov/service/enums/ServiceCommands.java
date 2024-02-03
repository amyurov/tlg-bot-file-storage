package com.amyurov.service.enums;

public enum ServiceCommands {
    HELP("/help"),
    REGISTRATION("/registration"),
    CANCEL("/cancel"),
    START("/start");
    private final String value;

    ServiceCommands(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

    public static ServiceCommands fromValue(String value) {
        for (ServiceCommands command : ServiceCommands.values()) {
            if (command.value.equals(value)) {
                return command;
            }
        }
        return null;
    }
}
