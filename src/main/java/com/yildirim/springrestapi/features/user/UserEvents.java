package com.yildirim.springrestapi.features.user;

public class UserEvents {

    public record EmailUpdatedEvent(User user) {
    }

    public record RegisteredEvent(User user) {
    }

    public record UsernameUpdatedEvent(User user) {
    }

    public record UpdatedEvent(User user) {
    }

    public record LoginFromNewDeviceEvent(User user, DeviceInfo deviceInfo) {
    }

    public record PasswordChangedEvent(User user) {
    }
}
