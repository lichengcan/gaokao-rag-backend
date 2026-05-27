---
name: gaokao-rag-maintainer
description: 'Maintain and extend this Gaokao RAG project. Use when working on the backend in gaokao-rag-backend or the paired frontend in D:\code\github\gaokao-rag-frontend, especially for Dify integration, streaming SSE answers, citations, Flyway migrations, chat history, feedback, health checks, admin dashboards, knowledge metadata, or Vue/Element Plus question-answer UI changes.'
---

# Gaokao RAG Maintainer

## Core Workflow

1. Treat this as a two-repo system:
   - Backend: `D:\code\github\gaokao-rag-backend`
   - Frontend: `D:\code\github\gaokao-rag-frontend`
2. Preserve the existing stack:
   - Spring Boot 3, MyBatis Plus, MySQL, Dify API, SSE.
   - Vue 3, Vite, Pinia, Element Plus.
3. Do not start or restart services unless the user explicitly asks.
4. Prefer incremental changes over replacing architecture.
5. Verify backend with Java 17:
   ```powershell
   $env:JAVA_HOME='C:\Program Files\Java\jdk-17.0.7'
   $env:Path="$env:JAVA_HOME\bin;$env:Path"
   mvn test
   ```
6. Verify frontend with:
   ```powershell
   npm run build
   ```
7. After frontend builds, restore tracked `dist` and Vite cache noise if changed.

## Backend Rules

- Keep Dify calls centralized in `DifyClient`.
- Keep chat orchestration in `ChatServiceImpl`.
- Use `SseEmitter` for streaming `/api/chat/stream`.
- Return meaningful Dify error messages and codes; avoid replacing everything with a generic unavailable message.
- Add database changes as Flyway migrations under `src/main/resources/db/migration`.
- Make migrations idempotent where practical because the local database may have been manually altered.
- Keep `application.yml` compatible with environment variables for Dify settings.
- Never hard-code a new real API key in code.

## Frontend Rules

- Keep the main user experience in `src/views/ChatView.vue`.
- Use `src/api/chat.js` for chat requests and SSE parsing.
- Update Pinia state directly when streaming chunks arrive so Vue reactivity is preserved.
- Keep UI in the current Element Plus operational style; avoid large marketing-style pages.
- Show visible status for slow or failed streaming requests.
- Keep citations under the AI answer and preserve history display.
- Use current local user state from `src/store/chat.js`; avoid reintroducing hard-coded `test-user-001` outside defaults.

## Common Pitfalls

- If answers only appear after refresh, check whether code updates a raw object instead of `store.messages[index]`.
- If `Unknown column` appears, check Flyway migration state and the actual MySQL table.
- If Dify returns `metadata_model_config is required`, inspect the Dify knowledge retrieval node configuration.
- If Vite fails with `crypto.getRandomValues`, use Node 18 or newer.
- If Vite cannot resolve a view import, check whether a view file was deleted during a partial rewrite.

## Extra Reference

Read `references/project-map.md` when you need a compact map of important files and responsibilities.
