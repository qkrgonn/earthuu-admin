create table university_domains (
    domain varchar(255) primary key,
    university_id uuid not null,
    verified_by uuid,
    verified_at timestamp with time zone,
    active boolean not null default true,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null,
    constraint university_domains_university_fk foreign key (university_id) references universities (id),
    constraint university_domains_verified_by_fk foreign key (verified_by) references users (id)
);

create table student_verifications (
    id uuid primary key,
    user_id uuid not null,
    school_email varchar(320) not null,
    university_id uuid not null,
    status varchar(30) not null,
    email_verified_at timestamp with time zone,
    domain_verified_by uuid,
    verified_at timestamp with time zone,
    reviewed_by uuid,
    reviewed_at timestamp with time zone,
    rejection_reason text,
    revision integer not null default 0,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null,
    constraint student_verifications_user_fk foreign key (user_id) references users (id),
    constraint student_verifications_university_fk foreign key (university_id) references universities (id),
    constraint student_verifications_domain_verifier_fk foreign key (domain_verified_by) references users (id),
    constraint student_verifications_reviewer_fk foreign key (reviewed_by) references users (id),
    constraint student_verifications_status_check check (status in ('PENDING', 'APPROVED', 'REJECTED', 'EXPIRED'))
);

create table verification_challenges (
    id uuid primary key,
    user_id uuid not null,
    purpose varchar(30) not null,
    email varchar(320) not null,
    code_hash varchar(255) not null,
    expires_at timestamp with time zone not null,
    attempts integer not null default 0,
    consumed_at timestamp with time zone,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null,
    constraint verification_challenges_user_fk foreign key (user_id) references users (id)
);

create index student_verifications_status_created_idx on student_verifications (status, created_at desc);
create index student_verifications_user_idx on student_verifications (user_id, created_at desc);
create index student_verifications_university_idx on student_verifications (university_id, created_at desc);
create index verification_challenges_user_created_idx on verification_challenges (user_id, created_at desc);
