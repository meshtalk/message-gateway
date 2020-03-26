begin transaction;

create schema if not exists meshtalk_mg;

create table if not exists meshtalk_mg.messages
(
    pk            serial primary key not null,
    uuid_id       VARCHAR(36)        not null,
    uuid_chat     VARCHAR(36)        not null,
    uuid_sender   VARCHAR(36)        not null,
    uuid_receiver VARCHAR(36)        not null,
    date          TIME               not null,
    content       VARCHAR            not null
);

comment on table meshtalk_mg.messages is 'MeshTalk Messages';

create unique index if not exists mappings_pk_uindex
    on meshtalk_mg.messages (pk);

create unique index if not exists mappings_uuid_chat_uindex
    on meshtalk_mg.messages (uuid_chat);

commit;