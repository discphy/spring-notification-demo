package com.discphy.core.alimtalk;

import com.discphy.notification.core.NotificationTemplateKey;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;

import java.util.Map;
import java.util.regex.Pattern;

@Entity
public class AlimtalkTemplate {

    @Id
    @Enumerated(EnumType.STRING)
    private NotificationTemplateKey templateKey;

    @Column(nullable = false, length = 4000)
    private String content;

    protected AlimtalkTemplate() {
    }

    public AlimtalkTemplate(NotificationTemplateKey key, String content) {
        this.templateKey = key;
        this.content = content;
    }

    public String render(Map<String, String> variables) {
        return Pattern.compile("#\\{([a-zA-Z0-9_]+)}").matcher(content).replaceAll(match -> {
            String value = variables.get(match.group(1));
            if (value == null) {
                throw new IllegalArgumentException("필수 변수 누락: " + match.group(1));
            }
            return java.util.regex.Matcher.quoteReplacement(value);
        });
    }
}
