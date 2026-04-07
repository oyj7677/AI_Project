alter table youtube_videos
    add column if not exists content_type varchar(32) not null default 'video';

update youtube_videos
set content_type = 'short'
where lower(title) like '%#shorts%'
   or lower(title) like '%shorts%';

create index if not exists ix_youtube_videos_content_type_published
    on youtube_videos (content_type, published_at desc);
