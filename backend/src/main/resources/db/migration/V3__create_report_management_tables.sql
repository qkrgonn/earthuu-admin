create table reports (
    id uuid primary key,
    reporter_id uuid not null,
    target_type varchar(20) not null,
    target_id uuid not null,
    reason_code varchar(50) not null,
    description text not null,
    evidence text,
    status varchar(20) not null,
    reviewer_id uuid,
    review_started_at timestamp with time zone,
    resolution varchar(40),
    resolution_note text,
    resolved_by uuid,
    resolved_at timestamp with time zone,
    revision integer not null default 0,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null,
    constraint reports_reporter_fk foreign key (reporter_id) references users (id),
    constraint reports_reviewer_fk foreign key (reviewer_id) references users (id),
    constraint reports_resolved_by_fk foreign key (resolved_by) references users (id),
    constraint reports_target_type_check check (target_type in ('EVENT', 'HOST')),
    constraint reports_status_check check (status in ('RECEIVED', 'INVESTIGATING', 'RESOLVED')),
    constraint reports_resolution_check check (resolution is null or resolution in (
        'NO_ACTION', 'GUIDANCE', 'WARNING', 'CONTENT_HIDDEN',
        'EVENT_SUSPENDED', 'EVENT_DISCARDED', 'HOST_RESTRICTED', 'HOST_SUSPENDED'
    ))
);

create table event_dispositions (
    id uuid primary key,
    report_id uuid not null,
    event_id uuid,
    target_type varchar(20) not null,
    target_id uuid not null,
    actor_id uuid not null,
    action varchar(40) not null,
    reason text not null,
    created_at timestamp with time zone not null,
    constraint event_dispositions_report_fk foreign key (report_id) references reports (id) on delete cascade,
    constraint event_dispositions_event_fk foreign key (event_id) references events (id) on delete set null,
    constraint event_dispositions_actor_fk foreign key (actor_id) references users (id),
    constraint event_dispositions_target_type_check check (target_type in ('EVENT', 'HOST')),
    constraint event_dispositions_action_check check (action in (
        'NO_ACTION', 'GUIDANCE', 'WARNING', 'CONTENT_HIDDEN',
        'EVENT_SUSPENDED', 'EVENT_DISCARDED', 'HOST_RESTRICTED', 'HOST_SUSPENDED'
    ))
);

create table disposition_recipients (
    id uuid primary key,
    disposition_id uuid not null,
    recipient_id uuid,
    channel varchar(20) not null,
    delivery_status varchar(20) not null,
    delivered_at timestamp with time zone,
    created_at timestamp with time zone not null,
    constraint disposition_recipients_disposition_fk foreign key (disposition_id) references event_dispositions (id) on delete cascade,
    constraint disposition_recipients_user_fk foreign key (recipient_id) references users (id) on delete set null,
    constraint disposition_recipients_channel_check check (channel in ('IN_APP', 'EMAIL', 'PUSH')),
    constraint disposition_recipients_status_check check (delivery_status in ('PENDING', 'SENT', 'FAILED'))
);

create index reports_status_created_idx on reports (status, created_at desc);
create index reports_target_idx on reports (target_type, target_id);
create index reports_resolved_by_idx on reports (resolved_by, resolved_at desc);
create index event_dispositions_report_idx on event_dispositions (report_id, created_at desc);
create index event_dispositions_target_idx on event_dispositions (target_type, target_id, created_at desc);
create index disposition_recipients_delivery_idx on disposition_recipients (delivery_status, created_at);
