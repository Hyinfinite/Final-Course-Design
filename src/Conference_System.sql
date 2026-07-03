drop database if exists conference_system;
create database conference_system;
use conference_system;

create table department (
	dept_id bigint primary key auto_increment,
    dept_name varchar(100) not null
);
    
create table admin_staff (
	staff_id bigint primary key auto_increment,
    staff_no varchar(100) not null,
    staff_name varchar(100) not null,
    dept_id bigint,
    staff_password varchar(100) not null,
    gender varchar(20),
    position varchar(100),
    phone varchar(100),
    access_level varchar(50) not null
);

create table meeting_room (
	room_id bigint primary key auto_increment,
    room_code varchar(100),
    room_name varchar(100),
    location varchar(100),
    capacity int not null,
    has_projector tinyint(1) default 0,
    has_audio tinyint(1) default 0
);

create table reservation (
	reservation_id bigint primary key auto_increment,
    reservation_no varchar(50) not null,
    meeting_topic varchar(300) not null,
    apply_dept_id bigint not null,
    applicant_staff_id bigint not null,
    reservation_room_id bigint not null,
    start_time datetime not null,
    end_time datetime not null,
    participant_count int not null,
    meeting_desc varchar(1000),
    reservation_process varchar(50) not null default '待确认',
    created_at datetime default current_timestamp,
    foreign key (apply_dept_id) references department (dept_id),
    foreign key (applicant_staff_id) references admin_staff (staff_id),
    foreign key (reservation_room_id) references meeting_room (room_id)
);

create table confirmation_log (
	confirm_id bigint primary key auto_increment,
    reservation_id bigint not null,
    confirmer_staff_id bigint not null,
    confirm_process varchar(50) not null,
    confirm_comment varchar(500),
    confirm_time datetime default current_timestamp,
    foreign key (reservation_id) references reservation (reservation_id),
    foreign key (confirmer_staff_id) references admin_staff (staff_id)
);

create table participant (
	participant_id bigint primary key auto_increment,
    reservation_id bigint not null,
    participant_staff_id bigint not null,
    sign_in_process varchar(50) default '未签到',
    sign_in_time datetime null,
    foreign key (reservation_id) references reservation (reservation_id),
    foreign key (participant_staff_id) references admin_staff (staff_id)
);


insert into department(dept_name) values ('系统管理部门'), ('会议室管理部门'), ('教导处');

insert into admin_staff (staff_no, staff_name, dept_id, staff_password, gender, position, phone, access_level) values
('A001', '系统管理员', 1, '123456', '男', '系统管理员', '15310009013', 'SYS_Admin'), 
('M001', '会议室管理员', 2, '123456', '女', '会议室管理员', '19910019011', 'ROOM_Admin'),
('T001', '张三', 3, '123456', '男', '老师', '13810099001', 'Staff'),
('T002', '李四', 3, '123456', '男', '老师', '13810099002', 'Staff'),
('T003', '王五', 3, '123456', '男', '老师', '13810099003', 'Staff'),
('T004', '周六', 3, '123456', '男', '老师', '13810099004', 'Staff');


insert into meeting_room (room_code, room_name, location, capacity, has_projector, has_audio) values
('R101', '行政会议室1', '行政楼2楼', 30, 1, 0),
('R102', '行政会议室2', '行政楼2楼', 30, 1, 0),
('R201', '图书馆会议室1', '图书馆5楼', 50, 1, 1);
