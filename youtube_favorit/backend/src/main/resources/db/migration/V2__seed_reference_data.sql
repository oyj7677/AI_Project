insert into platforms (platform_code, display_name, support_mode, is_enabled, created_at, updated_at)
values
    ('youtube', 'YouTube', 'auto', true, now(), now()),
    ('instagram', 'Instagram', 'limited', true, now(), now()),
    ('x', 'X', 'disabled', false, now(), now()),
    ('chzzk', 'CHZZK', 'disabled', false, now(), now()),
    ('soop', 'SOOP', 'disabled', false, now(), now())
on conflict (platform_code) do nothing;

insert into categories (category_code, display_name, sort_order, is_enabled, created_at, updated_at)
values
    ('game', '게임', 10, true, now(), now()),
    ('talk', '토크', 20, true, now(), now()),
    ('daily', '일상', 30, true, now(), now()),
    ('music', '음악', 40, true, now(), now())
on conflict (category_code) do nothing;

