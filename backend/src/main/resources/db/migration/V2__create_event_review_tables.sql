create table universities (
    id uuid primary key,
    name_ko varchar(150) not null,
    name_en varchar(150),
    active boolean not null default true,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null
);

create table profiles (
    user_id uuid primary key,
    name varchar(80) not null,
    university_id uuid,
    nationality_code varchar(3),
    primary_language varchar(20),
    secondary_language varchar(20),
    gender varchar(20),
    ui_locale varchar(5),
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null,
    constraint profiles_user_fk foreign key (user_id) references users (id) on delete cascade,
    constraint profiles_university_fk foreign key (university_id) references universities (id)
);

create table events (
    id uuid primary key,
    host_id uuid not null,
    current_version_id uuid,
    published_version_id uuid,
    moderation_status varchar(30) not null,
    lifecycle_status varchar(30) not null,
    revision integer not null default 0,
    published_at timestamp with time zone,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null,
    constraint events_host_fk foreign key (host_id) references users (id),
    constraint events_moderation_status_check check (moderation_status in ('PENDING', 'REVIEWING', 'APPROVED', 'REJECTED')),
    constraint events_lifecycle_status_check check (lifecycle_status in ('NOT_OPEN', 'OPEN', 'ENDED', 'CANCELLED', 'SUSPENDED'))
);

create table event_versions (
    id uuid primary key,
    event_id uuid not null,
    version_number integer not null,
    submission_state varchar(20) not null,
    title varchar(160) not null,
    thumbnail_asset_id uuid,
    category_id uuid,
    city_code varchar(30),
    venue_name varchar(200),
    address varchar(300),
    latitude numeric(10, 7),
    longitude numeric(10, 7),
    place_provider varchar(30),
    provider_place_id varchar(255),
    starts_at timestamp with time zone not null,
    ends_at timestamp with time zone not null,
    timezone varchar(64) not null,
    recruitment_start timestamp with time zone,
    recruitment_end timestamp with time zone,
    description_ko text,
    description_en text,
    submitted_at timestamp with time zone,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null,
    constraint event_versions_event_fk foreign key (event_id) references events (id) on delete cascade,
    constraint event_versions_event_number_uk unique (event_id, version_number)
);

alter table events
    add constraint events_current_version_fk foreign key (current_version_id) references event_versions (id);

alter table events
    add constraint events_published_version_fk foreign key (published_version_id) references event_versions (id);

create table participations (
    id uuid primary key,
    event_id uuid not null,
    user_id uuid not null,
    quota_group varchar(20),
    nationality_snapshot varchar(3),
    status varchar(20) not null,
    attendance varchar(20),
    confirmed_at timestamp with time zone,
    cancelled_at timestamp with time zone,
    cancellation_source varchar(30),
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null,
    constraint participations_event_fk foreign key (event_id) references events (id) on delete cascade,
    constraint participations_user_fk foreign key (user_id) references users (id),
    constraint participations_event_user_uk unique (event_id, user_id)
);

create table moderation_actions (
    id uuid primary key,
    event_id uuid not null,
    version_id uuid not null,
    actor_id uuid not null,
    action varchar(30) not null,
    reason text,
    created_at timestamp with time zone not null,
    constraint moderation_actions_event_fk foreign key (event_id) references events (id) on delete cascade,
    constraint moderation_actions_version_fk foreign key (version_id) references event_versions (id),
    constraint moderation_actions_actor_fk foreign key (actor_id) references users (id)
);

create index events_review_queue_idx on events (moderation_status, created_at);
create index events_host_idx on events (host_id);
create index event_versions_submitted_idx on event_versions (submitted_at);
create index participations_event_idx on participations (event_id);
create index moderation_actions_event_created_idx on moderation_actions (event_id, created_at);
