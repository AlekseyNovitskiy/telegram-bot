-- liquibase formatted sql

-- changeset anovitskiy:1
CREATE TABLE notification_task (
    id     BIGSERIAL,
    id_chat BIGSERIAL,
    text   TEXT,
    data   TIMESTAMP
)