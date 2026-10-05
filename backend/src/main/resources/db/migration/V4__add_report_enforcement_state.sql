alter table events add column visibility_status varchar(20) not null default 'VISIBLE';
alter table events add column hidden_at timestamp with time zone;
alter table events add column hidden_by uuid;
alter table events add constraint events_visibility_status_check
    check (visibility_status in ('VISIBLE', 'HIDDEN'));
alter table events add constraint events_hidden_by_fk
    foreign key (hidden_by) references users (id);

create table user_restrictions (
    id uuid primary key,
    user_id uuid not null,
    source_report_id uuid not null unique,
    imposed_by uuid not null,
    reason text not null,
    status varchar(20) not null,
    starts_at timestamp with time zone not null,
    ends_at timestamp with time zone,
    revoked_at timestamp with time zone,
    revoked_by uuid,
    revoke_reason text,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null,
    constraint user_restrictions_user_fk foreign key (user_id) references users (id),
    constraint user_restrictions_report_fk foreign key (source_report_id) references reports (id),
    constraint user_restrictions_imposed_by_fk foreign key (imposed_by) references users (id),
    constraint user_restrictions_revoked_by_fk foreign key (revoked_by) references users (id),
    constraint user_restrictions_status_check check (status in ('ACTIVE', 'REVOKED', 'EXPIRED')),
    constraint user_restrictions_period_check check (ends_at is null or ends_at > starts_at)
);

create index events_visibility_idx on events (visibility_status, updated_at desc);
create index user_restrictions_user_status_idx on user_restrictions (user_id, status, starts_at desc);
create index user_restrictions_active_period_idx on user_restrictions (status, ends_at);
