package com.discphy;

import com.discphy.application.alimtalk.AlimtalkSendQueryService;
import com.discphy.core.alimtalk.AlimtalkSendRepository;
import com.discphy.core.alimtalk.AlimtalkSendStatus;
import com.discphy.core.alimtalk.AlimtalkTemplate;
import com.discphy.notification.application.alimtalk.AlimtalkNotificationDispatcher;
import com.discphy.notification.application.alimtalk.AlimtalkSendEvent;
import com.discphy.notification.core.NotificationDispatcher;
import com.discphy.notification.core.NotificationMessage;
import com.discphy.notification.core.NotificationRegistry;
import com.discphy.notification.core.NotificationSendMode;
import com.discphy.notification.core.NotificationSendType;
import com.discphy.notification.core.NotificationTemplateKey;
import com.discphy.notification.core.PhoneNumber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = ApiApplication.class)
@AutoConfigureMockMvc
@RecordApplicationEvents
class NotificationFlowTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    NotificationRegistry registry;

    NotificationDispatcher dispatcher;

    @Autowired
    AlimtalkNotificationDispatcher alimtalk;

    @Autowired
    AlimtalkSendRepository repository;

    @Autowired
    AlimtalkSendQueryService queries;

    @Autowired
    PlatformTransactionManager transactions;

    @BeforeEach
    void selectDispatcher() {
        dispatcher = registry.getDispatcher(NotificationSendType.User.WELCOME.channel());
    }

    private NotificationMessage message(String phone, String name) {
        return new NotificationMessage(new PhoneNumber(phone), Map.of("name", name));
    }

    private Long id(String key) {
        return repository.findByNamespaceAndKey("USER", key).orElseThrow().getId();
    }

    @Test
    void userCreationSendsAfterCommit(ApplicationEvents events) throws Exception {
        String body = mvc.perform(post("/users").contentType("application/json")
                        .content("{\"name\":\"홍길동\",\"phoneNumber\":\"01012345678\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        long userId = new com.fasterxml.jackson.databind.ObjectMapper().readTree(body).get("id").asLong();
        var result = queries.get(id("welcome:" + userId));
        assertThat(result.status()).isEqualTo(AlimtalkSendStatus.SENT);
        assertThat(result.items().getFirst().content()).isEqualTo("홍길동님, 가입을 환영합니다!");
        assertThat(result.items().getFirst().providerId()).startsWith("demo-");
        assertThat(events.stream(AlimtalkSendEvent.class))
                .containsExactly(new AlimtalkSendEvent(result.id(), NotificationSendMode.IMMEDIATE));
    }

    @Test
    void duplicateGroupDoesNotSendAgain(ApplicationEvents events) {
        String key = "duplicate";
        dispatcher.send(NotificationSendType.User.WELCOME, key, List.of(message("01012345678", "첫 요청")));
        var before = queries.get(id(key));
        dispatcher.send(NotificationSendType.User.WELCOME, key, List.of(message("01012345678", "다른 내용")));
        assertThat(queries.get(id(key))).isEqualTo(before);
        assertThat(events.stream(AlimtalkSendEvent.class))
                .containsExactly(new AlimtalkSendEvent(before.id(), NotificationSendMode.IMMEDIATE));
    }

    @Test
    void batchPublishesEventButWaitsUntilExplicitDispatch(ApplicationEvents events) {
        String key = "batch";
        dispatcher.send(NotificationSendType.User.WELCOME_BATCH, key, List.of(message("01012345678", "배치")));
        assertThat(queries.get(id(key)).status()).isEqualTo(AlimtalkSendStatus.PENDING);
        assertThat(events.stream(AlimtalkSendEvent.class))
                .containsExactly(new AlimtalkSendEvent(id(key), NotificationSendMode.BATCH));

        alimtalk.dispatch(id(key));
        assertThat(queries.get(id(key)).status()).isEqualTo(AlimtalkSendStatus.SENT);
    }

    @Test
    void providerFailureIsPersistedPerItem() {
        String key = "partial-failure";
        dispatcher.send(NotificationSendType.User.WELCOME, key,
                List.of(message("01012345678", "성공"), message("01012340000", "실패")));
        var result = queries.get(id(key));
        assertThat(result.status()).isEqualTo(AlimtalkSendStatus.FAILED);
        assertThat(result.items()).filteredOn(i -> i.status() == AlimtalkSendStatus.SENT).hasSize(1);
        assertThat(result.items()).filteredOn(i -> i.status() == AlimtalkSendStatus.FAILED)
                .singleElement().satisfies(i -> assertThat(i.error()).contains("데모 공급자 발송 실패"));
    }

    @Test
    void rollbackDoesNotLeaveNotification() {
        new TransactionTemplate(transactions).executeWithoutResult(tx -> {
            dispatcher.send(NotificationSendType.User.WELCOME, "rollback", List.of(message("01012345678", "취소")));
            tx.setRollbackOnly();
        });
        assertThat(repository.findByNamespaceAndKey("USER", "rollback")).isEmpty();
    }

    @Test
    void missingVariableRejectsBeforePersistence() {
        assertThatThrownBy(() -> dispatcher.send(NotificationSendType.User.WELCOME, "missing",
                List.of(new NotificationMessage(new PhoneNumber("01012345678"), Map.of()))))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("name");
        assertThat(repository.findByNamespaceAndKey("USER", "missing")).isEmpty();
    }

    @Test
    void templateRenderingDoesNotMutateTemplateOrInterpretReplacementCharacters() {
        var template = new AlimtalkTemplate(NotificationTemplateKey.USER_WELCOME, "#{name}님");
        assertThat(template.render(Map.of("name", "$1"))).isEqualTo("$1님");
        assertThat(template.render(Map.of("name", "다음"))).isEqualTo("다음님");
    }
}
