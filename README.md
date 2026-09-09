# Spring 알림 멀티 모듈 데모

Java 21 · Spring Boot 3.5.5 · Gradle 8.14.3 · Spring Data JPA · H2

Store 없이 Dispatcher가 알림톡 접수·발송을 조율하는 작은 예제입니다. 실제 외부 발송 대신 가짜 Client를 사용합니다.

## 실행

```bash
./gradlew :apps:api:bootRun
```

API와 알림 기능이 한 프로세스에서 실행됩니다. 기본 주소는 `http://localhost:8080`입니다.

알림 앱만 독립적으로 실행할 수도 있습니다.

```bash
./gradlew :apps:notification:bootRun
```

독립 알림 앱은 `http://localhost:8081`에서 실행됩니다. 각 앱은 별도의 메모리 H2 DB를 사용하며 재시작하면 데이터가 사라집니다. 현재 API의 Notifier는 로컬 호출 구현입니다. 두 앱 사이의 HTTP 호출 및 분리 DB 트랜잭션 처리는 구현하지 않았습니다.

## 사용자 생성 → 즉시 알림

```bash
curl -i http://localhost:8080/users \
  -H 'Content-Type: application/json' \
  -d '{"name":"홍길동","phoneNumber":"01012345678"}'

curl http://localhost:8080/alimtalk/1
```

새 DB에서는 첫 사용자와 첫 발송 그룹의 ID가 각각 1입니다. 사용자 ID와 그룹 ID는 서로 다른 식별자입니다.

조회 결과의 그룹 상태와 아이템 상태는 `SENT`, 메시지는 `홍길동님, 가입을 환영합니다!`가 됩니다. 이 데모의 `SENT`는 가짜 공급자 접수 성공을 의미하며 실제 전달 완료를 의미하지 않습니다.

## 배치 접수 → 수동 발송

```bash
curl -i http://localhost:8080/alimtalk \
  -H 'Content-Type: application/json' \
  -d '{"sendType":"WELCOME_BATCH","key":"batch:demo-1","messages":[{"phoneNumber":"01012345678","variables":{"name":"배치 사용자"}}]}'

curl -X POST http://localhost:8080/alimtalk/dispatch-pending
```

배치는 처음에는 `PENDING`으로 저장됩니다. 수동 실행 API는 배치를 포함한 모든 `PENDING` 그룹을 처리합니다. 스케줄러는 구현하지 않았습니다.

즉시 접수는 `sendType`을 `WELCOME`으로 지정합니다. `POST /alimtalk`는 202와 빈 응답을 반환하며, 접수 성공을 의미합니다. 기본 이벤트 리스너는 동기적으로 실행되므로 커밋 후 발송 시도까지 요청 스레드에서 처리합니다.

## 실패 확인

전화번호가 `0000`으로 끝나면 가짜 Client가 예외를 발생시킵니다.

```bash
curl -i http://localhost:8080/alimtalk \
  -H 'Content-Type: application/json' \
  -d '{"sendType":"WELCOME","key":"failure:demo-1","messages":[{"phoneNumber":"01012340000","variables":{"name":"실패 사용자"}}]}'
```

그룹과 아이템에 `FAILED` 상태가 저장되고 아이템 `error` 및 서버 오류 로그에서 원인을 확인할 수 있습니다. 일부 아이템만 실패해도 그룹은 `FAILED`이며, 성공한 아이템은 `SENT`로 유지됩니다. 실패한 그룹의 자동 재시도는 구현하지 않았습니다.

## 구조와 책임

```text
apps/
├── api/src/main/java/com/discphy/
│   ├── ApiApplication
│   ├── core/user/                       User Entity, UserRepository, UserNotification, UserNotifier
│   ├── application/user/                UserCommand.Create, UserCommandHandler, UserQueryService
│   ├── infrastructure/persistence/user/ Repository 구현
│   ├── infrastructure/notification/user/UserNotifierImpl
│   └── presentation/user/               HTTP 진입점
└── notification/src/main/java/com/discphy/
    ├── NotificationApplication
    ├── core/alimtalk/                    AlimtalkSend, AlimtalkSendItem, AlimtalkTemplate Entity
    ├── application/alimtalk/             AlimtalkComponent, CommandHandler, QueryService, 내부 Command 타입
    ├── infrastructure/persistence/alimtalk/
    └── notification/
        ├── NotificationConfiguration
        ├── core/                        Registry, SendType, Destination, Message, Dispatcher 인터페이스
        │   └── alimtalk/                AlimtalkNotificationClient 인터페이스
        ├── application/
        │   └── alimtalk/                Dispatcher, 접수 이벤트
        ├── infrastructure/alimtalk/     가짜 Client 구현
        └── presentation/                Controller, AFTER_COMMIT Listener, 데모 템플릿 초기화
```

- Entity는 core에 있습니다. 아이템은 그룹의 `@OneToMany`로 함께 저장하여 별도의 아이템 Repository·Handler를 생략했습니다.
- 각 CommandHandler는 내부 Command 타입을 받는 `handle` 메서드만 제공합니다.
- UserNotifierImpl은 core의 Registry에서 `getDispatcher(sendType.channel())`로 전략을 얻은 후 `send`를 호출합니다.
- Registry는 순수 Java 클래스로, 설정 클래스에서 Bean으로 등록합니다. `supports`로 채널 구현을 선택하고 해당 Dispatcher를 반환합니다. `send` 안에서는 채널 지원 여부를 다시 검사하지 않습니다.
- SendType이 채널·발송 모드·템플릿을 결정합니다. namespace는 `USER`로 고정합니다.
- Store는 없습니다. Dispatcher는 Component와 Client를 호출하며 새 그룹 접수 시 발송 모드와 무관하게 `AlimtalkSendEvent(groupId, sendMode)`를 발행합니다. 중복 접수는 이벤트를 재발행하지 않습니다.
- `sendMode`는 영속화하지 않고 이벤트에 담습니다. `AlimtalkSendListener`가 커밋 후 이벤트의 `sendMode`를 확인해 즉시 모드만 발송하고 배치 모드는 `PENDING`으로 유지합니다. 발행부는 수신부의 처리 방식을 결정하지 않습니다.
- `AlimtalkComponent`는 Handler·QueryService를 조합해 `render`, `register`, `claim`, `get`, `complete`를 제공합니다. `render`는 템플릿을 한 번 조회하고 수신자별 변수를 적용합니다.
- 템플릿 Entity를 변경하지 않고 완성된 메시지를 접수 시 저장합니다.
- infrastructure에서 application을 참조하지 않습니다. application을 호출하는 HTTP·이벤트 진입점은 presentation에 둡니다.
- 두 앱의 기본 패키지가 같으므로 스캔 범위를 명시하여 실행 설정 충돌을 방지합니다.

## 트랜잭션과 멱등성

```text
UserCommandHandler.handle(UserCommand.Create)
  → UserNotifierImpl.notify
  → NotificationRegistry.getDispatcher(sendType.channel())
  → AlimtalkNotificationDispatcher.send          [같은 저장 트랜잭션]
      → AlimtalkComponent.render                [템플릿 조회·변수 적용]
      → AlimtalkComponent.register → handle(Register)
      → 새 그룹이면 AlimtalkSendEvent 발행       [즉시·배치 공통]
  → COMMIT
  → AFTER_COMMIT AlimtalkSendListener
      → event.sendMode() 확인                    [모드 판별을 위한 DB 조회 없음]
      → 배치 모드이면 대기, 즉시 모드이면 아래 발송 실행
  → AlimtalkNotificationDispatcher.dispatch(groupId)
      → AlimtalkComponent.claim → handle(Claim)   [새 트랜잭션, 잠금으로 선점]
      → AlimtalkComponent.get                    [새 읽기 트랜잭션]
      → Client.send                              [DB 트랜잭션 밖]
      → AlimtalkComponent.complete → handle(Complete) [새 트랜잭션]
```

`(namespace, key)` DB 유니크 제약으로 그룹 중복을 방지합니다. 이미 존재하는 그룹에 같은 키를 요청하면 최초 접수를 유지합니다. 같은 namespace의 서로 다른 사건은 `welcome:123`, `batch:123`처럼 다른 key를 사용해야 합니다. 동시 최초 접수의 유니크 충돌은 호출자에게 오류로 전달하며 성공 응답으로 변환하지 않습니다.

이 예제는 전송의 exactly-once를 보장하지 않습니다. 처리 중 프로세스가 종료되면 `PROCESSING`이 남을 수 있으며, 공급자 응답 유실·자동 복구·outbox는 구현하지 않았습니다. 미처리 `PENDING`은 수동 실행 API로 처리할 수 있습니다. 결과 저장 자체가 실패하면 예외를 전파하며 `PROCESSING` 상태가 남습니다.

Spring의 트랜잭션 이벤트 동작과 커밋 후 별도 트랜잭션이 필요한 이유는 [공식 이벤트 문서](https://docs.spring.io/spring-framework/reference/data-access/transaction/event.html), [TransactionalEventListener API](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/transaction/event/TransactionalEventListener.html)를 참고하세요.

## 검증

```bash
./gradlew test bootJar
```

사용자 생성 후 이벤트·발송, 중복 접수 시 이벤트 재발행 방지, 배치 이벤트 발행·대기·발송, 부분 실패 저장, 롤백 시 접수 취소, 필수 변수 누락, 템플릿 불변성 및 독립 알림 앱 시작을 테스트합니다. 이벤트 검증에는 [Spring의 ApplicationEvents 지원](https://docs.spring.io/spring-framework/reference/testing/testcontext-framework/application-events.html)을 사용합니다.

실행 JAR:

```bash
java -jar apps/api/build/libs/api-0.0.1-SNAPSHOT.jar
java -jar apps/notification/build/libs/notification-0.0.1-SNAPSHOT.jar
```

## 코드 스타일

Java는 4칸 들여쓰기, 120자 줄 길이, 선언별 어노테이션 줄바꿈, 명시적 import를 사용합니다. IntelliJ에서 프로젝트의 `.editorconfig`를 적용하고 코드 정렬(`⌥⌘L`)을 실행하면 됩니다. 포맷터 설정은 `config/intellij-code-style.xml`에도 보관했습니다. [IntelliJ 공식 포맷터 문서](https://www.jetbrains.com/help/idea/command-line-formatter.html)
