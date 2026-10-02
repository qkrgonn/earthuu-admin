create table users (
    id uuid primary key,
    status varchar(20) not null,
    role varchar(20) not null,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null,
    constraint users_status_check check (status in ('ACTIVE', 'SUSPENDED', 'WITHDRAWN')),
    constraint users_role_check check (role in ('USER', 'ADMIN'))
);

create table password_credentials (
    user_id uuid primary key,
    login_email varchar(320) not null unique,
    password_hash varchar(255) not null,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null,
    constraint password_credentials_user_fk foreign key (user_id) references users (id) on delete cascade
);

create table audit_logs (
    id uuid primary key,
    actor_id uuid,
    action varchar(100) not null,
    target_type varchar(50) not null,
    target_id uuid,
    metadata jsonb,
    created_at timestamp with time zone not null,
    constraint audit_logs_actor_fk foreign key (actor_id) references users (id)
);

create index audit_logs_actor_created_idx on audit_logs (actor_id, created_at desc);
create index audit_logs_target_idx on audit_logs (target_type, target_id);
