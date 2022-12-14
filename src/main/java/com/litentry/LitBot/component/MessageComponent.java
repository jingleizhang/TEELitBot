package com.litentry.LitBot.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class MessageComponent {

    private final MessageSource messageSource;
    private MessageSourceAccessor messageSourceAccessor;

    @Autowired
    public MessageComponent(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @PostConstruct
    private void initialize() {
        this.messageSourceAccessor = new MessageSourceAccessor(this.messageSource);
    }

    public String get(String key, String... args) {
        return this.messageSourceAccessor.getMessage(key, args);
    }
}
