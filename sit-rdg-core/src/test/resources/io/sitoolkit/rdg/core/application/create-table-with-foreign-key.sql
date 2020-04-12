create table tab_1 (
  col_1_1 int primary key
);

create table tab_2 (
  col_2_1 int,
  col_2_2 int,
  foreign key (col_2_1) references tab_1(col_1_1)
);

create table tab_3 (
  col_3_1 int,
  col_3_2 int,
  foreign key (col_3_1, col_3_2) references tab_2(col_2_1, col_2_2)
);


create table tab_4 (
  col_4_1 int
);


alter table tab_4 add constraint tab4_tab1 foreign key (col_4_1) references tab_1(col_1_1);

