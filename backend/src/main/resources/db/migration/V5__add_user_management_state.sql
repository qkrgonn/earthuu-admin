alter table users add column revision integer not null default 0;
alter table user_restrictions add column revision integer not null default 0;

create table user_status_actions (
    id uuid primary key,
    user_id uuid not null,
    actor_id uuid not null,
    action varchar(20) not null,
    previous_status varchar(20) not null,
    new_status varchar(20) not null,
    reason text not null,
    created_at timestamp with time zone not null,
    constraint user_status_actions_user_fk foreign key (user_id) references users (id),
    constraint user_status_actions_actor_fk foreign key (actor_id) references users (id),
    constraint user_status_actions_action_check check (action in ('SUSPENDED', 'RESTORED')),
    constraint user_status_actions_previous_status_check check (previous_status in ('ACTIVE', 'SUSPENDED', 'WITHDRAWN')),
    constraint user_status_actions_new_status_check check (new_status in ('ACTIVE', 'SUSPENDED', 'WITHDRAWN'))
);

create index users_status_role_created_idx on users (status, role, created_at desc);
create index user_status_actions_user_created_idx on user_status_actions (user_id, created_at desc);
