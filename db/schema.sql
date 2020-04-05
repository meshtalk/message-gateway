begin transaction;

create schema if not exists meshtalk_mg;

create table if not exists meshtalk_mg.messages
(
    pk            serial primary key not null,
    uuid_id       VARCHAR(36)        not null,
    uuid_chat     VARCHAR(36)        not null,
    uuid_sender   VARCHAR(36)        not null,
    uuid_receiver VARCHAR(36)        not null,
    date          VARCHAR            not null,
    content       VARCHAR            not null
);

comment on table meshtalk_mg.messages is 'MeshTalk Messages';

create unique index if not exists messages_pk_uindex
    on meshtalk_mg.messages (pk);

create unique index if not exists messages_uuid_id_uindex
    on meshtalk_mg.messages (uuid_id);

create table if not exists meshtalk_mg.handshakes
(
    pk            serial primary key not null,
    uuid_id       VARCHAR(36)        not null,
    uuid_chat     VARCHAR(36)        not null,
    uuid_sender   VARCHAR(36)        not null,
    uuid_receiver VARCHAR(36)        not null,
    date          VARCHAR            not null,
    key           VARCHAR            not null,
    iv            VARCHAR            not null
);

comment on table meshtalk_mg.handshakes is 'MeshTalk Handshakes';

create unique index if not exists handshakes_pk_uindex
    on meshtalk_mg.handshakes (pk);

create unique index if not exists handshakes_uuid_id_uindex
    on meshtalk_mg.handshakes (uuid_id);

commit;