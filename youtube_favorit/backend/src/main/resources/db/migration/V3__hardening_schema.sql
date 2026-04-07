create table if not exists raw_source_records (
    id uuid primary key,
    platform_code varchar(32) not null,
    resource_scope varchar(32) not null,
    external_object_id varchar(255) not null,
    channel_id uuid,
    influencer_id uuid,
    http_status integer,
    payload_checksum varchar(128) not null,
    payload_json jsonb not null,
    fetched_at timestamptz not null,
    normalized_at timestamptz,
    request_trace_id varchar(255),
    created_at timestamptz not null,
    constraint fk_raw_source_platform
        foreign key (platform_code) references platforms(platform_code),
    constraint fk_raw_source_channel
        foreign key (channel_id) references channels(id),
    constraint fk_raw_source_influencer
        foreign key (influencer_id) references influencers(id)
);

create index if not exists ix_raw_source_lookup
    on raw_source_records (platform_code, resource_scope, external_object_id, fetched_at desc);
create index if not exists ix_raw_source_channel
    on raw_source_records (channel_id, fetched_at desc);
create index if not exists ix_raw_source_influencer
    on raw_source_records (influencer_id, fetched_at desc);
create index if not exists ix_raw_source_normalized
    on raw_source_records (normalized_at);

alter table live_status_cache
    add column if not exists source_record_id uuid,
    add constraint fk_live_status_cache_source_record
        foreign key (source_record_id) references raw_source_records(id);

create index if not exists ix_live_status_cache_platform
    on live_status_cache (platform_code, is_live desc, snapshot_at desc);
create index if not exists ix_live_status_cache_stale
    on live_status_cache (freshness_status, stale_at);

alter table activity_items
    add column if not exists source_activity_id varchar(255),
    add column if not exists is_pinned boolean not null default false,
    add column if not exists source_record_id uuid,
    add column if not exists last_successful_sync_at timestamptz,
    add constraint fk_activity_items_source_record
        foreign key (source_record_id) references raw_source_records(id);

create index if not exists ix_activity_items_home
    on activity_items (published_at desc);
create index if not exists ix_activity_items_platform_time
    on activity_items (platform_code, published_at desc);
create index if not exists ix_activity_items_pinned_time
    on activity_items (is_pinned desc, published_at desc);
create unique index if not exists ux_activity_items_source
    on activity_items (platform_code, source_activity_id)
    where source_activity_id is not null;

create table if not exists influencer_serving_state (
    influencer_id uuid primary key,
    is_live_now boolean not null default false,
    live_platform_code varchar(32),
    live_started_at timestamptz,
    latest_activity_at timestamptz,
    latest_schedule_at timestamptz,
    supported_platforms_cache jsonb not null default '[]'::jsonb,
    featured_rank integer,
    home_visibility boolean not null default true,
    detail_visibility boolean not null default true,
    last_projection_refresh_at timestamptz not null,
    constraint fk_serving_state_influencer
        foreign key (influencer_id) references influencers(id),
    constraint fk_serving_state_live_platform
        foreign key (live_platform_code) references platforms(platform_code)
);

create index if not exists ix_serving_state_home
    on influencer_serving_state (home_visibility, is_live_now desc, featured_rank asc, latest_activity_at desc);
create index if not exists ix_serving_state_live
    on influencer_serving_state (is_live_now desc, live_started_at desc);

create table if not exists platform_sync_metadata (
    id uuid primary key,
    platform_code varchar(32) not null,
    resource_scope varchar(32) not null,
    channel_id uuid,
    influencer_id uuid,
    sync_key varchar(255) not null,
    last_attempted_at timestamptz,
    last_succeeded_at timestamptz,
    last_status varchar(16) not null,
    last_error_code varchar(120),
    last_error_message text,
    consecutive_failures integer not null default 0,
    next_scheduled_at timestamptz,
    backoff_until timestamptz,
    cursor_token text,
    etag text,
    source_quota_bucket varchar(120),
    created_at timestamptz not null,
    updated_at timestamptz not null,
    constraint fk_sync_metadata_platform
        foreign key (platform_code) references platforms(platform_code),
    constraint fk_sync_metadata_channel
        foreign key (channel_id) references channels(id),
    constraint fk_sync_metadata_influencer
        foreign key (influencer_id) references influencers(id)
);

create unique index if not exists ux_sync_metadata_sync_key
    on platform_sync_metadata (platform_code, resource_scope, sync_key);
create index if not exists ix_sync_metadata_schedule
    on platform_sync_metadata (next_scheduled_at asc, backoff_until asc);
create index if not exists ix_sync_metadata_failures
    on platform_sync_metadata (last_status, consecutive_failures desc);
create index if not exists ix_sync_metadata_channel_scope
    on platform_sync_metadata (channel_id, resource_scope);

create table if not exists influencer_operator_metadata (
    influencer_id uuid primary key,
    override_display_name varchar(120),
    override_bio text,
    override_profile_image_url text,
    override_home_visibility boolean,
    override_detail_visibility boolean,
    note text,
    updated_by_operator varchar(120),
    created_at timestamptz not null,
    updated_at timestamptz not null,
    constraint fk_influencer_operator_metadata_influencer
        foreign key (influencer_id) references influencers(id)
);

create table if not exists channel_operator_metadata (
    channel_id uuid primary key,
    override_handle varchar(255),
    override_channel_url text,
    override_is_official boolean,
    override_is_primary boolean,
    note text,
    updated_by_operator varchar(120),
    created_at timestamptz not null,
    updated_at timestamptz not null,
    constraint fk_channel_operator_metadata_channel
        foreign key (channel_id) references channels(id)
);

