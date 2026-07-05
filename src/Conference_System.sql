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
    reservation_comment varchar(500),
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
	participant_id bigint primary key,
    reservation_id bigint not null,
    participant_staff_id bigint not null,
    sign_in_process varchar(50) default '未签到',
    sign_in_time datetime null,
    sign_in_permission varchar(50) default 'SELF',
    foreign key (reservation_id) references reservation (reservation_id),
    foreign key (participant_staff_id) references admin_staff (staff_id)
);


insert into department(dept_name) values ('系统管理部门'), ('会议室管理部门'), ('教导处');

insert into admin_staff (staff_no, staff_name, dept_id, staff_password, gender, position, phone, access_level) values
('A001', '系统管理员', 1, '123456', '男', '系统管理员', '15310009013', 'SYS_Admin'), 
('R001', '会议室管理员', 2, '123456', '女', '会议室管理员', '19910019011', 'ROOM_Admin'),
('T001', '张三', 3, '123456', '男', '老师', '13810099001', 'Staff'),
('T002', '李四', 3, '123456', '女', '老师', '13810099002', 'Staff'),
('T003', '王五', 3, '123456', '男', '老师', '13810099003', 'Staff'),
('T004', '赵六', 3, '123456', '女', '老师', '13810099004', 'Staff'),
('T005', '钱七', 3, '123456', '男', '老师', '13810099005', 'Staff'),
('T006', '孙八', 3, '123456', '女', '老师', '13810099006', 'Staff'),
('T007', '周九', 3, '123456', '男', '老师', '13810099007', 'Staff'),
('T008', '吴十', 3, '123456', '女', '老师', '13810099008', 'Staff'),
('T009', '郑一', 3, '123456', '男', '老师', '13810099009', 'Staff'),
('T010', '王二', 3, '123456', '女', '老师', '13810099010', 'Staff'),
('T011', '冯三', 3, '123456', '男', '老师', '13810099011', 'Staff'),
('T012', '陈四', 3, '123456', '女', '老师', '13810099012', 'Staff'),
('T013', '褚五', 3, '123456', '男', '老师', '13810099013', 'Staff'),
('T014', '卫六', 3, '123456', '女', '老师', '13810099014', 'Staff'),
('T015', '蒋七', 3, '123456', '男', '老师', '13810099015', 'Staff'),
('T016', '沈八', 3, '123456', '女', '老师', '13810099016', 'Staff'),
('T017', '韩九', 3, '123456', '男', '老师', '13810099017', 'Staff'),
('T018', '杨十', 3, '123456', '女', '老师', '13810099018', 'Staff'),
('T019', '朱一', 3, '123456', '男', '老师', '13810099019', 'Staff'),
('T020', '秦二', 3, '123456', '女', '老师', '13810099020', 'Staff'),
('T021', '尤三', 3, '123456', '男', '老师', '13810099021', 'Staff'),
('T022', '许四', 3, '123456', '女', '老师', '13810099022', 'Staff'),
('T023', '何五', 3, '123456', '男', '老师', '13810099023', 'Staff'),
('T024', '吕六', 3, '123456', '女', '老师', '13810099024', 'Staff'),
('T025', '施七', 3, '123456', '男', '老师', '13810099025', 'Staff'),
('T026', '张八', 3, '123456', '女', '老师', '13810099026', 'Staff'),
('T027', '孔九', 3, '123456', '男', '老师', '13810099027', 'Staff'),
('T028', '曹十', 3, '123456', '女', '老师', '13810099028', 'Staff'),
('T029', '严一', 3, '123456', '男', '老师', '13810099029', 'Staff'),
('T030', '华二', 3, '123456', '女', '老师', '13810099030', 'Staff'),
('T031', '金三', 3, '123456', '男', '老师', '13810099031', 'Staff'),
('T032', '魏四', 3, '123456', '女', '老师', '13810099032', 'Staff');


insert into meeting_room (room_code, room_name, location, capacity, has_projector, has_audio) values
('R101', '行政会议室1', '行政楼2楼', 30, 1, 0),
('R102', '行政会议室2', '行政楼2楼', 30, 1, 0),
('R201', '图书馆会议室1', '图书馆5楼', 50, 1, 1);
