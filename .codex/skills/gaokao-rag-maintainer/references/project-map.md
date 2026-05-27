# Project Map

## Backend

- `src/main/java/com/example/gaokao/client/DifyClient.java`: Dify REST and streaming integration, error parsing, citation extraction.
- `src/main/java/com/example/gaokao/service/impl/ChatServiceImpl.java`: chat validation, user profile prompt enrichment, message persistence, SSE events.
- `src/main/java/com/example/gaokao/controller/ChatController.java`: chat HTTP endpoints.
- `src/main/java/com/example/gaokao/common/GlobalExceptionHandler.java`: API error response mapping.
- `src/main/resources/db/migration`: Flyway migrations.
- `src/main/resources/application.yml`: local config and Dify properties.

## Frontend

- `src/views/ChatView.vue`: main chat UI, health status, profile form, streaming status, feedback, citations.
- `src/api/chat.js`: blocking and streaming chat API, SSE parser, timeout/cancel behavior.
- `src/store/chat.js`: local user, role, profile, conversation state.
- `src/views/AdminView.vue`: statistics and feedback dashboard.
- `src/views/KnowledgeView.vue`: knowledge metadata maintenance.
- `src/router/index.js`: route definitions and local role guard.

## Validation

- Backend: run `mvn test` with Java 17.
- Frontend: run `npm run build`.
- Do not start services unless explicitly asked by the user.
