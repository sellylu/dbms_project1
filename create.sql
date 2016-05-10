create table tweets(
	twid int primary key,
	tweet varchar(30),
	utcDate varchar(30),
	city varchar(30),
	userId int
);



import tweets.sql;

create table user1 (
	userId int primary key,
	name varchar(30),
	userLocation varchar(30)
);

CREATE INDEX uindex on user1(userId);

import user1.sql;