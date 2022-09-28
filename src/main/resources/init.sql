create table `mission_record`
(
    id                varchar(64)  not null unique comment 'id',
    mission_code      varchar(64)  not null comment '任务',
    class_name        varchar(128) not null,
    properties        text         null,
    error_msg         text         null,
    status            int          not null,
    times             int          not null,
    next_execute_date datetime         not null,
    created           datetime         not null,
    updated           datetime         not null
)
    comment '补偿任务记录表';

