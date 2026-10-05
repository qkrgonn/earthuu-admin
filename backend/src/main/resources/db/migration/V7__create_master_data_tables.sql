create table cities (
    code varchar(30) primary key,
    official_name varchar(100) not null,
    display_name_ko varchar(50) not null,
    display_name_en varchar(50),
    active boolean not null default true,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null
);

create table categories (
    id uuid primary key,
    code varchar(50) not null unique,
    name_ko varchar(80) not null,
    name_en varchar(80),
    active boolean not null default true,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null
);

create index universities_active_name_idx on universities (active, name_ko);
create index university_domains_university_active_idx on university_domains (university_id, active);
create index cities_active_name_idx on cities (active, display_name_ko);
create index categories_active_name_idx on categories (active, name_ko);
