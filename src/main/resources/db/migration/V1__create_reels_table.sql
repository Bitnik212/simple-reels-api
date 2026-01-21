create table public.reels
(
    id         serial,
    reel_id    varchar(255)                        not null constraint reels_reel_id_unique unique,
    video_url  text,
    metadata   jsonb                               not null,
    created_at timestamp default CURRENT_TIMESTAMP not null
);
