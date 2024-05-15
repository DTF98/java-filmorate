insert into MPA select * from (
select 1, 'G' union
select 2, 'PG' union
select 3, 'PG-13' union
select 4, 'R' union
select 5, 'NC-17'
) where not exists(select * from MPA);

insert into GENRES select * from (
select 1, 'Комедия' union
select 2, 'Драма' union
select 3, 'Мультфильм' union
select 4, 'Триллер' union
select 5, 'Документальный' union
select 6, 'Боевик'
) where not exists(select * from GENRES);

