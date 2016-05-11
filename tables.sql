CREATE TABLE Stopword (
stopwordId int PRIMARY KEY,
stopwordText varchar(40),
tfidf int,
counts int,
freq int
);

CREATE TABLE Abstract (
abstractId int PRIMARY KEY,
text varchar(40),
wordcount int,
source int,
stopwordId int
);
