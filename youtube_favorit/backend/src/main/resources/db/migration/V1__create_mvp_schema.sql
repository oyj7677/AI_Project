create table if not exists platforms (
    platform_code varchar(32) primary key,
    display_name varchar(64) not null,
    support_mode varchar(16) not null,
    is_enabled boolean not null default false,
    created_at timestamptz not null,
    updated_at timestamptz not null
);

create table if not exists influencers (
    id uuid primary key,
    slug varchar(120) not null unique,
    display_name varchar(120) not null,
    normalized_name varchar(120) not null,
    bio text,
    profile_image_url text,
    status varchar(16) not null,
    is_featured boolean not null default false,
    default_timezone varchar(64),
    latest_activity_at timestamptz,
    current_live_platform varchar(32),
    is_live_now boolean not null default false,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    deleted_at timestamptz,
    constraint fk_influencers_current_live_platform
        foreign key (current_live_platform) references platforms(platform_code)
);

create index if not exists ix_influencers_status_featured
    on influencers (status, is_featured desc, updated_at desc);
create index if not exists ix_influencers_live_latest
    on influencers (is_live_now desc, latest_activity_at desc);
create index if not exists ix_influencers_normalized_name
    on influencers (normalized_name);

create table if not exists categories (
    category_code varchar(64) primary key,
    display_name varchar(120) not null,
    sort_order integer not null default 0,
    is_enabled boolean not null default true,
    created_at timestamptz not null,
    updated_at timestamptz not null
);

create table if not exists influencer_categories (
    influencer_id uuid not null,
    category_code varchar(64) not null,
    created_at timestamptz not null,
    primary key (influencer_id, category_code),
    constraint fk_influencer_categories_influencer
        foreign key (influencer_id) references influencers(id),
    constraint fk_influencer_categories_category
        foreign key (category_code) references categories(category_code)
);

create index if not exists ix_influencer_categories_category
    on influencer_categories (category_code, influencer_id);

create table if not exists channels (
    id uuid primary key,
    influencer_id uuid not null,
    platform_code varchar(32) not null,
    external_channel_id varchar(255) not null,
    handle varchar(255),
    channel_url text not null,
    display_label varchar(255),
    is_official boolean not null default false,
    is_primary boolean not null default false,
    status varchar(16) not null,
    verified_at timestamptz,
    last_seen_at timestamptz,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    deleted_at timestamptz,
    constraint fk_channels_influencer
        foreign key (influencer_id) references influencers(id),
    constraint fk_channels_platform
        foreign key (platform_code) references platforms(platform_code),
    constraint ux_channels_platform_external
        unique (platform_code, external_channel_id)
);

create index if not exists ix_channels_influencer_platform
    on channels (influencer_id, platform_code, is_primary desc);
create index if not exists ix_channels_platform_handle
    on channels (platform_code, handle);
create unique index if not exists ux_channels_primary_per_platform
    on channels (influencer_id, platform_code)
    where is_primary = true and deleted_at is null;

create table if not exists schedule_items (
    id uuid primary key,
    influencer_id uuid not null,
    channel_id uuid,
    platform_code varchar(32),
    source_type varchar(16) not null,
    status varchar(16) not null,
    title varchar(255) not null,
    note text,
    scheduled_at timestamptz not null,
    ends_at timestamptz,
    source_reference text,
    created_by_operator varchar(120),
    updated_by_operator varchar(120),
    created_at timestamptz not null,
    updated_at timestamptz not null,
    constraint fk_schedule_items_influencer
        foreign key (influencer_id) references influencers(id),
    constraint fk_schedule_items_channel
        foreign key (channel_id) references channels(id),
    constraint fk_schedule_items_platform
        foreign key (platform_code) references platforms(platform_code)
);

create index if not exists ix_schedule_items_influencer_time
    on schedule_items (influencer_id, scheduled_at desc);
create index if not exists ix_schedule_items_today
    on schedule_items (status, scheduled_at asc);

create table if not exists live_status_cache (
    channel_id uuid primary key,
    influencer_id uuid not null,
    platform_code varchar(32) not null,
    is_live boolean not null,
    live_title text,
    watch_url text,
    viewer_count integer,
    started_at timestamptz,
    snapshot_at timestamptz not null,
    stale_at timestamptz,
    freshness_status varchar(16) not null,
    last_successful_sync_at timestamptz,
    last_attempted_sync_at timestamptz,
    error_code varchar(120),
    error_message text,
    updated_at timestamptz not null,
    constraint fk_live_status_cache_channel
        foreign key (channel_id) references channels(id),
    constraint fk_live_status_cache_influencer
        foreign key (influencer_id) references influencers(id),
    constraint fk_live_status_cache_platform
        foreign key (platform_code) references platforms(platform_code)
);

create index if not exists ix_live_status_cache_live
    on live_status_cache (is_live desc, snapshot_at desc);
create index if not exists ix_live_status_cache_influencer
    on live_status_cache (influencer_id, snapshot_at desc);

create table if not exists activity_items (
    id uuid primary key,
    influencer_id uuid not null,
    channel_id uuid,
    platform_code varchar(32) not null,
    source_type varchar(16) not null,
    content_type varchar(32) not null,
    title varchar(255) not null,
    summary text,
    thumbnail_url text,
    published_at timestamptz not null,
    external_url text not null,
    freshness_status varchar(16) not null,
    stale_at timestamptz,
    source_reference text,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    constraint fk_activity_items_influencer
        foreign key (influencer_id) references influencers(id),
    constraint fk_activity_items_channel
        foreign key (channel_id) references channels(id),
    constraint fk_activity_items_platform
        foreign key (platform_code) references platforms(platform_code)
);

create index if not exists ix_activity_items_influencer_published
    on activity_items (influencer_id, published_at desc);
create index if not exists ix_activity_items_global_published
    on activity_items (published_at desc);

