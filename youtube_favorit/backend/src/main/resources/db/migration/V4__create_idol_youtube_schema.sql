create table if not exists idol_groups (
    id uuid primary key,
    slug varchar(120) not null unique,
    display_name varchar(120) not null,
    normalized_name varchar(120) not null,
    description text,
    cover_image_url text,
    status varchar(16) not null,
    is_featured boolean not null default false,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    deleted_at timestamptz
);

create index if not exists ix_idol_groups_status_featured
    on idol_groups (status, is_featured desc, updated_at desc);
create index if not exists ix_idol_groups_normalized_name
    on idol_groups (normalized_name);

create table if not exists idol_members (
    id uuid primary key,
    group_id uuid not null,
    slug varchar(120) not null unique,
    display_name varchar(120) not null,
    normalized_name varchar(120) not null,
    profile_image_url text,
    sort_order integer not null default 0,
    status varchar(16) not null,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    deleted_at timestamptz,
    constraint fk_idol_members_group
        foreign key (group_id) references idol_groups(id)
);

create index if not exists ix_idol_members_group_sort
    on idol_members (group_id, sort_order asc, updated_at desc);
create index if not exists ix_idol_members_normalized_name
    on idol_members (normalized_name);

create table if not exists youtube_channels (
    id uuid primary key,
    platform_code varchar(32) not null,
    external_channel_id varchar(255) not null,
    handle varchar(255),
    channel_url text not null,
    display_label varchar(255),
    channel_type varchar(32) not null,
    owner_type varchar(16) not null,
    owner_group_id uuid,
    owner_member_id uuid,
    uploads_playlist_id varchar(255),
    is_official boolean not null default false,
    is_primary boolean not null default false,
    status varchar(16) not null,
    verified_at timestamptz,
    last_seen_at timestamptz,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    deleted_at timestamptz,
    constraint fk_youtube_channels_platform
        foreign key (platform_code) references platforms(platform_code),
    constraint fk_youtube_channels_owner_group
        foreign key (owner_group_id) references idol_groups(id),
    constraint fk_youtube_channels_owner_member
        foreign key (owner_member_id) references idol_members(id),
    constraint ux_youtube_channels_platform_external
        unique (platform_code, external_channel_id),
    constraint chk_youtube_channels_channel_type
        check (channel_type in ('GROUP_OFFICIAL','MEMBER_PERSONAL','SUB_UNIT','LABEL')),
    constraint chk_youtube_channels_owner_type
        check (owner_type in ('GROUP','MEMBER')),
    constraint chk_youtube_channels_status
        check (status in ('active','inactive','hidden')),
    constraint chk_youtube_channels_owner_match
        check (
            (owner_type = 'GROUP' and owner_group_id is not null and owner_member_id is null) or
            (owner_type = 'MEMBER' and owner_member_id is not null and owner_group_id is null)
        )
);

create index if not exists ix_youtube_channels_owner_group
    on youtube_channels (owner_group_id, channel_type, is_primary desc);
create index if not exists ix_youtube_channels_owner_member
    on youtube_channels (owner_member_id, channel_type, is_primary desc);
create index if not exists ix_youtube_channels_handle
    on youtube_channels (handle);

create table if not exists youtube_videos (
    id uuid primary key,
    channel_id uuid not null,
    external_video_id varchar(255),
    title varchar(255) not null,
    description text,
    thumbnail_url text,
    published_at timestamptz not null,
    video_url text not null,
    source_type varchar(32) not null,
    freshness_status varchar(16) not null,
    stale_at timestamptz,
    is_pinned boolean not null default false,
    last_successful_sync_at timestamptz,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    deleted_at timestamptz,
    constraint fk_youtube_videos_channel
        foreign key (channel_id) references youtube_channels(id),
    constraint ux_youtube_videos_external_video_id
        unique (external_video_id),
    constraint chk_youtube_videos_source_type
        check (source_type in ('youtube_imported','manual')),
    constraint chk_youtube_videos_freshness
        check (freshness_status in ('fresh','stale','manual','unknown'))
);

create index if not exists ix_youtube_videos_channel_published
    on youtube_videos (channel_id, published_at desc);
create index if not exists ix_youtube_videos_published
    on youtube_videos (published_at desc);

create table if not exists group_operator_metadata (
    group_id uuid primary key,
    override_description text,
    override_cover_image_url text,
    override_status varchar(16),
    note text,
    updated_by_operator varchar(120),
    created_at timestamptz not null,
    updated_at timestamptz not null,
    constraint fk_group_operator_metadata_group
        foreign key (group_id) references idol_groups(id)
);

create table if not exists member_operator_metadata (
    member_id uuid primary key,
    override_profile_image_url text,
    override_sort_order integer,
    override_status varchar(16),
    note text,
    updated_by_operator varchar(120),
    created_at timestamptz not null,
    updated_at timestamptz not null,
    constraint fk_member_operator_metadata_member
        foreign key (member_id) references idol_members(id)
);

create table if not exists youtube_channel_operator_metadata (
    channel_id uuid primary key,
    override_handle varchar(255),
    override_channel_url text,
    override_display_label varchar(255),
    override_channel_type varchar(32),
    override_is_official boolean,
    override_is_primary boolean,
    note text,
    updated_by_operator varchar(120),
    created_at timestamptz not null,
    updated_at timestamptz not null,
    constraint fk_youtube_channel_operator_metadata_channel
        foreign key (channel_id) references youtube_channels(id)
);

create table if not exists youtube_video_operator_metadata (
    video_id uuid primary key,
    override_title varchar(255),
    override_description text,
    override_thumbnail_url text,
    override_published_at timestamptz,
    override_video_url text,
    override_is_pinned boolean,
    note text,
    updated_by_operator varchar(120),
    created_at timestamptz not null,
    updated_at timestamptz not null,
    constraint fk_youtube_video_operator_metadata_video
        foreign key (video_id) references youtube_videos(id)
);

create table if not exists youtube_sync_metadata (
    id uuid primary key,
    channel_id uuid not null,
    resource_scope varchar(32) not null,
    sync_key varchar(255) not null,
    last_attempted_at timestamptz,
    last_succeeded_at timestamptz,
    last_status varchar(16) not null,
    last_error_code varchar(120),
    last_error_message text,
    consecutive_failures integer not null default 0,
    next_scheduled_at timestamptz,
    backoff_until timestamptz,
    etag text,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    constraint fk_youtube_sync_metadata_channel
        foreign key (channel_id) references youtube_channels(id),
    constraint ux_youtube_sync_metadata_channel_scope
        unique (channel_id, resource_scope),
    constraint chk_youtube_sync_metadata_scope
        check (resource_scope in ('channel_profile','channel_videos')),
    constraint chk_youtube_sync_metadata_status
        check (last_status in ('idle','success','failed','partial'))
);

create index if not exists ix_youtube_sync_metadata_due
    on youtube_sync_metadata (resource_scope, next_scheduled_at asc);

create table if not exists youtube_raw_source_records (
    id uuid primary key,
    channel_id uuid,
    resource_scope varchar(32) not null,
    external_object_id varchar(255) not null,
    http_status integer,
    request_trace_id varchar(120),
    payload_json jsonb not null,
    fetched_at timestamptz not null,
    normalized_at timestamptz,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    constraint fk_youtube_raw_source_records_channel
        foreign key (channel_id) references youtube_channels(id)
);

create index if not exists ix_youtube_raw_source_records_lookup
    on youtube_raw_source_records (resource_scope, external_object_id, fetched_at desc);

create table if not exists group_serving_state (
    group_id uuid primary key,
    official_channel_count integer not null default 0,
    member_count integer not null default 0,
    member_personal_channel_count integer not null default 0,
    latest_video_at timestamptz,
    latest_video_thumbnail_url text,
    home_visibility boolean not null default true,
    detail_visibility boolean not null default true,
    last_projection_refresh_at timestamptz not null,
    constraint fk_group_serving_state_group
        foreign key (group_id) references idol_groups(id)
);

create table if not exists member_serving_state (
    member_id uuid primary key,
    personal_channel_count integer not null default 0,
    latest_video_at timestamptz,
    detail_visibility boolean not null default true,
    last_projection_refresh_at timestamptz not null,
    constraint fk_member_serving_state_member
        foreign key (member_id) references idol_members(id)
);
