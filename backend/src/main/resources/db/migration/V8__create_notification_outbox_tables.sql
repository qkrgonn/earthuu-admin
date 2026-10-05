create table notification_settings (
    user_id uuid primary key,
    push_enabled boolean not null default true,
    chat_enabled boolean not null default true,
    updated_at timestamp with time zone not null,
    constraint notification_settings_user_fk foreign key (user_id) references users (id) on delete cascade
);

create table notifications (
    id uuid primary key,
    user_id uuid not null,
    type varchar(50) not null,
    target_type varchar(30),
    target_id uuid,
    title varchar(160) not null,
    body text not null,
    dedupe_key varchar(255) not null unique,
    read_at timestamp with time zone,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null,
    constraint notifications_user_fk foreign key (user_id) references users (id) on delete cascade
);

create table outbox (
    id uuid primary key,
    topic varchar(100) not null,
    aggregate_id uuid,
    payload jsonb not null,
    dedupe_key varchar(255) not null unique,
    status varchar(20) not null,
    attempts integer not null default 0,
    next_attempt_at timestamp with time zone not null,
    last_error text,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null,
    constraint outbox_status_check check (status in ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED'))
);

create index notifications_user_read_created_idx on notifications (user_id, read_at, created_at desc);
create index outbox_delivery_idx on outbox (status, next_attempt_at, created_at);
