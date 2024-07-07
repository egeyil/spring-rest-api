package com.yildirim.springrestapi.features.user;

import jakarta.persistence.PostUpdate;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class UserAuditListener {
    private final ApplicationEventPublisher publisher;

    public UserAuditListener(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @PostUpdate
    public void postUpdate(User user) {
        publisher.publishEvent(new UserEvents.UpdatedEvent(user));
    }
}
